package pl.ctrlpkw.api.resource;

import com.datastax.driver.mapping.Mapper;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.CassandraContext;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Ballot;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.Ward;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Api("Protokoły")
@Path("/votings/{date}/protocols")
@Produces(MediaType.APPLICATION_JSON)
@Component
@Slf4j
public class ProtocolsResource {

    @Resource
    CassandraContext cassandraContext;

    @ApiOperation("Przesłanie informacji o wynikach głosowania w obwodzie dla wszystkich kart")
    @POST
    public void create(@Valid pl.ctrlpkw.api.dto.Protocol protocol) {
        for (BallotResult ballotResult : Optional.fromNullable(protocol.getBallotResults()).or(Sets.<BallotResult>newHashSet())) {
            Protocol localBallotResult = Protocol.builder()
                    .id(UUID.randomUUID())
                    .ballot(Ballot.builder().votingDate(LocalDate.parse("2010-06-20").toDate()).no(ballotResult.getBallotNo()).build())
                    .ward(Ward.builder().communityCode(protocol.getCommunityCode()).no(protocol.getWardNo()).build())
                    .votersEntitledCount(ballotResult.getVotersEntitledCount())
                    .ballotsGivenCount(ballotResult.getBallotsGivenCount())
                    .votesCastCount(ballotResult.getVotesCastCount())
                    .votesValidCount(ballotResult.getVotesValidCount())
                    .votesCountPerOption(
                            ballotResult.getVotesCountPerOption()
                    )
                    .build();
            Mapper<Protocol> mapper = cassandraContext.getMappingManager().mapper(Protocol.class);
            mapper.save(localBallotResult);
        }
    }

}
