package pl.ctrlpkw.api.resource;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.Location;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;
import pl.ctrlpkw.model.read.WardRepository;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Obwody")
@Path("/votings/{date}/wards")
@Produces(MediaType.APPLICATION_JSON)
@Component
@ClientVersionCheck
public class WardsResource {

    @Resource
    private GeometryFactory geometryFactory;

    @Resource
    private WardRepository wardRepository;

    @Resource
    private VotingRepository votingRepository;

    @ApiOperation(value = "Pobranie obwodów, których lokale wyborcze są nabliższe podanej lokalizacji", response = pl.ctrlpkw.api.dto.Ward.class, responseContainer = "List")
    @GET
    public Iterable<pl.ctrlpkw.api.dto.Ward> readByClosestLocation(
            @PathParam("date") String votingDate,
            @QueryParam("latitude") @NotNull Double latidude,
            @QueryParam("longitude") @NotNull Double longitude,
            @QueryParam("radius") @DefaultValue("4000") double radius,
            @QueryParam("minCount") @DefaultValue("3") short minCount)
    {
        Voting voting = votingRepository.findByDate(LocalDate.parse(votingDate));
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latidude));

        return StreamSupport.stream(
                getAllWardsWithinRadiusAndTopUpWithClosestIfAtLeastMinCountNotFound(
                        voting, location, radius, minCount
                ).spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    private Iterable<Ward> getAllWardsWithinRadiusAndTopUpWithClosestIfAtLeastMinCountNotFound(
            Voting voting, Point location, double radius, short minCount
    ) {
        Collection<Ward> wardsWithinRadius = wardRepository.findWithinRadiusAndOrderByDistance(
                voting, location, radius
        );
        if (minCount <= wardsWithinRadius.size()) {
            return wardsWithinRadius;
        } else {
            List<Ward> closestMinCountWards = wardRepository.findOrderedByDistance(
                    voting, location, new PageRequest(0, minCount)
            ).getContent();
            if (closestMinCountWards.size() == 0) {
                throw new NotFoundException();
            }
            Point minCountThWardLocation =
                    Iterables.getLast(closestMinCountWards).getLocation();
            return wardRepository.findCloserThanAndOrderByDistance(voting, location, minCountThWardLocation);
        }
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
                    .protocolStatus(pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK)
                    .build();

}
