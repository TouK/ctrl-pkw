package pl.ctrlpkw.api.resource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.Location;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.api.filter.ClientVersionFilter;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;
import pl.ctrlpkw.model.read.WardRepository;
import pl.ctrlpkw.service.GeolocationService;
import pl.ctrlpkw.service.WardStateProvider;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Api("Obwody")
@Path("/votings/{date}/wards")
@Produces(MediaType.APPLICATION_JSON)
@Component
@ClientVersionCheck
@Slf4j
public class WardsResource {

    @Resource
    private GeometryFactory geometryFactory;

    @Resource
    private WardRepository wardRepository;

    @Resource
    private GeolocationService geolocationService;

    @Resource
    private VotingRepository votingRepository;

    @Resource
    private WardStateProvider wardStateProvider;

    @Value("${wards.maxRadiusUpperLimit:25000}")
    private double maxRadiusUpperLimit;

    @Value("${wards.minCountLowerLimit:8}")
    private short minCountLowerLimit;

    @Value("${wards.missing.tolerance:0.002}")
    private double missingWardTolerance;

    @HeaderParam(ClientVersionFilter.CLIENT_VERSION_HEADER)
    private String clientVersionHeader;

    @ApiOperation(value = "Pobranie obwodów, których lokale wyborcze są nabliższe podanej lokalizacji", response = pl.ctrlpkw.api.dto.Ward.class, responseContainer = "List")
    @GET
    public Iterable<pl.ctrlpkw.api.dto.Ward> readByClosestLocation(
            @PathParam("date") String votingDate,
            @QueryParam("latitude") @NotNull Double latidude,
            @QueryParam("longitude") @NotNull Double longitude,
            @QueryParam("radius") @DefaultValue("4000") double radius,
            @QueryParam("minCount") @DefaultValue("8") short minCount,
            @QueryParam("maxCount") @DefaultValue("100") short maxCount)
    {
        Voting voting = votingRepository.findByDate(LocalDate.parse(votingDate));
        if (voting == null)
            throw new NotFoundException();
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latidude));

        if (minCount < minCountLowerLimit) minCount = minCountLowerLimit;
        if (radius > maxRadiusUpperLimit) radius = maxRadiusUpperLimit;

        List<pl.ctrlpkw.api.dto.Ward> wards = geolocationService.getAllWardsWithinRadiusAndTopUpWithClosestIfAtLeastMinCountNotFound(
                voting, location, radius, minCount, maxCount
        )
                .map(ward -> {
                    pl.ctrlpkw.api.dto.Ward.ProtocolStatus protocolStatus = retrieveProtocolStatus(voting, ward);
                    ComparableVersion clientVersion = new ComparableVersion(
                            Optional.ofNullable(clientVersionHeader).orElse("0.0.1")
                    );
                    ComparableVersion currentVersion = new ComparableVersion("1.89.0");
                    if (clientVersion.compareTo(currentVersion) < 0 && pl.ctrlpkw.api.dto.Ward.ProtocolStatus.VAGUE.equals(protocolStatus)) {
                        protocolStatus = pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK;
                    }
                    pl.ctrlpkw.api.dto.Ward wardDto = entityToDto.apply(ward);
                    wardDto.setProtocolStatus(
                            protocolStatus
                    );
                    return wardDto;
                })
                .collect(Collectors.toList());

        if (!location.equalsExact(geometryFactory.createPoint(new Coordinate(wards.get(0).getLocation().getLongitude(), wards.get(0).getLocation().getLatitude())), missingWardTolerance)) {
            wards.add(pl.ctrlpkw.api.dto.Ward.builder()
                            .communityCode(location.toText())
                            .no(0)
                            .label("Brakująca obwodowa komisja wyborcza")
                            .shortLabel("Brakująca komisja")
                            .address("W miejscu, gdzie jestem")
                            .location(Location.builder().latitude(location.getY()).longitude(location.getX()).build())
                            .protocolStatus(pl.ctrlpkw.api.dto.Ward.ProtocolStatus.CONFIRMED)
                            .build()
            );
        }

        return wards;
    }


    @ApiOperation("Pobranie konkretnego obwodu głosowania")
    @Path("{communityCode}/{wardNo}")
    @GET
    public pl.ctrlpkw.api.dto.Ward readOne(
            @PathParam("date") String votingDate,
            @PathParam("communityCode") String communityCode,
            @PathParam("wardNo") int wardNo
    ) {
        Voting voting = votingRepository.findByDate(LocalDate.parse(votingDate));
        if (voting == null)
            throw new NotFoundException();
        Ward ward = wardRepository.findByVotingsAndCommunityCodeAndWardNo(voting, communityCode, wardNo);
        if (ward == null)
            throw new NotFoundException();

        pl.ctrlpkw.api.dto.Ward wardDto = entityToDto.apply(ward);
        wardDto.setProtocolStatus(
                retrieveProtocolStatus(voting, ward)
        );
        return wardDto;
    }

    private pl.ctrlpkw.api.dto.Ward.ProtocolStatus retrieveProtocolStatus(Voting voting, Ward ward) {
        BallotResult result = wardStateProvider.read(voting.getDate(), 1, ward.getCommunityCode(), ward.getWardNo());
        if (result == null)
            return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK;
        else if (result.getVotesCountPerOption() == null)
            return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.VAGUE;
        else
            return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.CONFIRMED;

    }

    private static Function<Ward, pl.ctrlpkw.api.dto.Ward> entityToDto = entity ->
            pl.ctrlpkw.api.dto.Ward.builder()
                    .communityCode(entity.getCommunityCode())
                    .no(entity.getWardNo())
                    .address(entity.getWardAddress())
                    .location(entity.getLocation() != null ?
                                    Location.builder().latitude(entity.getLocation().getY()).longitude(entity.getLocation().getX()).build()
                                    : null
                    )
                    .label(entity.getLabel())
                    .shortLabel(entity.getShortLabel())
                    .protocolStatus(pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK)
                    .build();

}
