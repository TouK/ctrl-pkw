package pl.ctrlpkw.api.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.joda.time.LocalDate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.model.read.Ballot;
import pl.ctrlpkw.model.read.BallotsRepository;
import pl.ctrlpkw.service.VotesCountingService;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;

@Api("Wyniki")
@Path("/votings/{date}/ballots/{ballotNo}/result")
@Produces(MediaType.APPLICATION_JSON)
@Component
@ClientVersionCheck
public class ResultsResource {

    @Resource
    private BallotsRepository ballotsRepository;

    @Resource
    VotesCountingService votesCountingService;

    @ApiOperation("Pobranie wyników głosowania dla konkretnego dnia i konkretnej karty")
    @GET
    @Transactional
    @Cacheable(value = "results", cacheManager = "redisCacheManager", key = "#votingDate + ':' + #ballotNo")
    public BallotResult read(@PathParam("date") String votingDate, @PathParam("ballotNo") Integer ballotNo) {

        Ballot ballot = ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo);
        if (ballot == null) {
            throw new NotFoundException();
        }
        throw new ServiceUnavailableException();
    }

    @ApiOperation("Pobranie wyników głosowania dla konkretnego dnia i konkretnej karty")
    @POST
    @Transactional
    @CachePut(value = "results", cacheManager = "redisCacheManager", key = "#votingDate + ':' + #ballotNo")
    public BallotResult count(@PathParam("date") String votingDate, @PathParam("ballotNo") Integer ballotNo) {

        Ballot ballot = ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo);
        if (ballot == null) {
            throw new NotFoundException();
        }

        return votesCountingService.sumVotes(ballot);
    }



}
