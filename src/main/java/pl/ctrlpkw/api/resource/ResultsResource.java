package pl.ctrlpkw.api.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.CommunityBallotResult;
import pl.ctrlpkw.api.filter.ClientVersionCheck;
import pl.ctrlpkw.model.read.Ballot;
import pl.ctrlpkw.model.read.BallotsRepository;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.VotingRepository;
import pl.ctrlpkw.model.read.Ward;
import pl.ctrlpkw.model.read.WardRepository;
import pl.ctrlpkw.service.VotesCountingService;
import pl.ctrlpkw.service.WardStateProvider;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("Wyniki")
@Path("/votings/{date}/results")
@Produces(MediaType.APPLICATION_JSON)
@Component
@ClientVersionCheck
@Slf4j
public class ResultsResource {

    @Resource
    private BallotsRepository ballotsRepository;

    @Resource
    VotesCountingService votesCountingService;

    @ApiOperation("Pobranie wyników głosowania dla konkretnego dnia i konkretnej karty")
    @GET
    @Path("ballots/{ballotNo}")
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
    @Path("ballots/{ballotNo}")
    @Transactional
    @CachePut(value = "results", cacheManager = "redisCacheManager", key = "#votingDate + ':' + #ballotNo")
    public BallotResult count(@PathParam("date") String votingDate, @PathParam("ballotNo") Integer ballotNo) {

        Ballot ballot = ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo);
        if (ballot == null) {
            throw new NotFoundException();
        }

        return votesCountingService.sumVotes(ballot);
    }

    @Resource
    WardRepository wardRepository;

    @Resource
    VotingRepository votingRepository;

    @Resource
    WardStateProvider localResultsProvider;

    @ApiOperation("Pobranie wyników głosowania dla konkretnego dnia i konkretnej karty")
    @GET
    @Path("communities/{communityCode}/ballots/{ballotNo}")
    @Cacheable(value = "communityResults")
    @Transactional
    public CommunityBallotResult readPerCommunity(@PathParam("date") String votingDate, @PathParam("communityCode") String communityCode, @PathParam("ballotNo") Integer ballotNo) {
        Voting voting = votingRepository.findByDate(LocalDate.parse(votingDate));
        if (voting == null)
            throw new NotFoundException();

        Map<Integer, Object> resultsPerWards = StreamSupport.stream(wardRepository.findByVotingsAndCommunityCode(voting, communityCode).spliterator(), false)
                .collect(Collectors.toMap(
                        Ward::getWardNo,
                        ward -> Optional.<Object>ofNullable(localResultsProvider.read(voting.getDate(), ballotNo, communityCode, ward.getWardNo())).orElse("null")
                ));


        return CommunityBallotResult.builder()
                .resultsPerWards(resultsPerWards)
                .summarized(resultsPerWards.values().stream()
                        .filter(v -> v instanceof BallotResult)
                        .map(v -> (BallotResult)v)
                        .reduce(BallotResult::add).orElse(null))
                .build();
    }


}
