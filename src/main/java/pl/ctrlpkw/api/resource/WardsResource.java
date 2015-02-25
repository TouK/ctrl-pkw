package pl.ctrlpkw.api.resource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.Location;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;
import pl.ctrlpkw.model.read.WardRepository;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Obwody")
@Path("/votings/{date}/wards")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class WardsResource {

    @Resource
    private GeometryFactory geometryFactory;

    @Resource
    private WardRepository wardRepository;

    @Resource
    private VotingRepository votingRepository;

    @ApiOperation("Pobranie obwodów, których lokale wyborcze są nabliższe podanej lokalizacji")
    @GET
    public Iterable<pl.ctrlpkw.api.dto.Ward> readByClosestLocation(
            @PathParam("date") String votingDate,
            @QueryParam("latitude") @NotNull Double latidude,
            @QueryParam("longitude") @NotNull Double longitude,
            @QueryParam("count") @DefaultValue("3") short count)
    {
        Voting voting = votingRepository.findByDate(LocalDate.parse(votingDate));
        return StreamSupport
                .stream(
                        wardRepository.findOrderedByDistance(
                                voting,
                                geometryFactory.createPoint(new Coordinate(latidude, longitude)),
                                new PageRequest(0, count)
                        ).spliterator(),
                        false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    private static Function<Ward, pl.ctrlpkw.api.dto.Ward> entityToDto = entity ->
            pl.ctrlpkw.api.dto.Ward.builder()
                    .communityCode(entity.getCommunityCode())
                    .no(entity.getWardNo())
                    .address(entity.getWardAddress())
                    .location(entity.getLocation() != null ?
                            Location.builder().latitude(entity.getLocation().getX()).longitude(entity.getLocation().getY()).build()
                            : null
                    )
                    .build();

}
