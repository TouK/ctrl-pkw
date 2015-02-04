package pl.ctrlpkw.api.resource;

//import com.wordnik.swagger.annotations.Api;
//import com.wordnik.swagger.annotations.ApiOperation;
import pl.ctrlpkw.model.read.BallotsRepository;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Path("/votings/{date}/ballots")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class BallotsResource {

    @Resource
    private BallotsRepository ballotsRepository;

    @GET
    @Transactional
    public Collection<pl.ctrlpkw.api.dto.Ballot> readAll(@PathParam("date") String votingDate) {
        return StreamSupport.stream(ballotsRepository.findByDate(LocalDate.parse(votingDate)).spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{no}")
    @Transactional
    public pl.ctrlpkw.api.dto.Ballot readOne(@PathParam("date") String votingDate, @PathParam("no") Integer ballotNo) {
        return entityToDto.apply(ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo));
    }

    private static Function<pl.ctrlpkw.model.read.Ballot, pl.ctrlpkw.api.dto.Ballot> entityToDto = entity -> pl.ctrlpkw.api.dto.Ballot.builder()
            .votingDate(entity.getVoting().getDate())
            .no(entity.getNo())
            .question(entity.getQuestion())
            .options(
                    entity.getOptions()
                            .stream()
                            .map(option -> option.getDescription())
                            .collect(Collectors.toList())
            ).build();

}
