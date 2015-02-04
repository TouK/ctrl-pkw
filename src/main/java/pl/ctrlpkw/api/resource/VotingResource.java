package pl.ctrlpkw.api.resource;

import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Path("/votings")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class VotingResource {

    @Resource
    private VotingRepository votingRepository;

    @GET
    public Iterable<pl.ctrlpkw.api.dto.Voting> readAll() {
        return StreamSupport
                .stream(votingRepository.findAll().spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{date}")
    public pl.ctrlpkw.api.dto.Voting readOne(@PathParam("date") String dateString) {
        return entityToDto.apply(votingRepository.findByDate(LocalDate.parse(dateString)));
    }

    private static Function<Voting, pl.ctrlpkw.api.dto.Voting> entityToDto = entity -> pl.ctrlpkw.api.dto.Voting.builder().date(entity.getDate()).description(entity.getDescription()).build();

}
