package pl.ctrlpkw.api.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.Ballot;
import pl.ctrlpkw.model.read.BallotOption;
import pl.ctrlpkw.model.read.BallotsRepository;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Karty")
@Path("/votings/{date}/ballots")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class BallotsResource {

    @Resource
    private BallotsRepository ballotsRepository;

    @ApiOperation(value = "Pobranie wszystkich kart dla podanej daty głosowania", response = Ballot.class)
    @GET
    @Transactional
    public Collection<Ballot> readAll(@PathParam("date") String votingDate) {
        return StreamSupport.stream(ballotsRepository.findByDate(LocalDate.parse(votingDate)).spliterator(), false)
                .map(entityToDto)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Pobranie konkretnej karty dla podanej daty głosowania", response = Ballot.class)
    @GET
    @Path("/{no}")
    @Transactional
    public Ballot readOne(@PathParam("date") String votingDate, @PathParam("no") Integer ballotNo) {
        return entityToDto.apply(ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo));
    }

    private static Function<pl.ctrlpkw.model.read.Ballot, Ballot> entityToDto = entity -> Ballot.builder()
            .votingDate(entity.getVoting().getDate())
            .no(entity.getNo())
            .question(entity.getQuestion())
            .options(
                    entity.getOptions()
                            .stream()
                            .map(BallotOption::getDescription)
                            .collect(Collectors.toList())
            ).build();

}
