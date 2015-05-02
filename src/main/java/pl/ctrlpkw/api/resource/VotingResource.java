package pl.ctrlpkw.api.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Głosowania")
@Path("/votings")
@Produces(MediaType.APPLICATION_JSON)
@Component
@ClientVersionCheck
public class VotingResource {

    @Resource
    private VotingRepository votingRepository;

    @ApiOperation("Pobranie informacji o wszystkich głosowaniach")
    @GET
    public Iterable<pl.ctrlpkw.api.dto.Voting> readAll() {
        return StreamSupport
                .stream(votingRepository.findAll().spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    @ApiOperation("Pobranie informacji o głosowaniu w konkretnym dniu")
    @GET
    @Path("/{date}")
    public pl.ctrlpkw.api.dto.Voting readOne(@PathParam("date") String dateString) {
        return entityToDto.apply(votingRepository.findByDate(LocalDate.parse(dateString)));
    }

    private static Function<Voting, pl.ctrlpkw.api.dto.Voting> entityToDto = entity ->
            pl.ctrlpkw.api.dto.Voting.builder()
                    .date(entity.getDate())
                    .description(entity.getDescription())
                    .build();

}
