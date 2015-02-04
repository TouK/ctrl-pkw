package pl.ctrlpkw.api.resource;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import pl.ctrlpkw.CassandraContext;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.read.Ballot;
import pl.ctrlpkw.model.read.BallotsRepository;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.ProtocolAccessor;
import org.joda.time.LocalDate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@Path("/votings/{date}/ballots/{ballotNo}/result")
@Produces(MediaType.APPLICATION_JSON)
@Component
public class ResultsResource {

    @Resource
    private BallotsRepository ballotsRepository;

    @Resource
    CassandraContext cassandraContext;

    @GET
    @Transactional
    @Cacheable("results")
    public BallotResult count(@PathParam("date") String votingDate, @PathParam("ballotNo") Integer ballotNo) {

        Ballot ballot = ballotsRepository.findByDateAndNo(LocalDate.parse(votingDate), ballotNo);

        ProtocolAccessor accessor = cassandraContext.getMappingManager().createAccessor(ProtocolAccessor.class);

        BallotResult results = StreamSupport.stream(accessor.findByVotingDateAndBallotNo().spliterator(), false)
                .reduce(identity, accumulator, combiner);
        results.setBallotNo(ballot.getNo());
        return results;
    }

    private BallotResult identity = BallotResult.builder()
            .votersEntitledCount(0l).ballotsGivenCount(0l).votesCastCount(0l).votesValidCount(0l)
            .votesCountPerOption(LongStream.rangeClosed(1, 10).boxed().map(i -> 0l).collect(Collectors.toList()))
            .build();

    private BiFunction<BallotResult, Protocol, BallotResult> accumulator = (BallotResult r1, Protocol r2) -> BallotResult.builder()
            .votersEntitledCount(r1.getVotersEntitledCount() + r2.getVotersEntitledCount())
            .ballotsGivenCount(r1.getBallotsGivenCount() + r2.getBallotsGivenCount())
            .votesCastCount(r1.getVotesCastCount() + r2.getVotesCastCount())
            .votesValidCount(r1.getVotesValidCount() + r2.getVotesValidCount())
            .votesCountPerOption(
                    Lists.newArrayList(
                            summingIterator(
                                    r1.getVotesCountPerOption().iterator(),
                                    r2.getVotesCountPerOption().iterator(),
                                    Long.valueOf(0l),
                                    (a, b) -> a + b
                            )
                    )
            )
            .build();

    private BinaryOperator<BallotResult> combiner = (BallotResult r1, BallotResult r2) -> BallotResult.builder()
            .votersEntitledCount(r1.getVotersEntitledCount() + r2.getVotersEntitledCount())
            .ballotsGivenCount(r1.getBallotsGivenCount() + r2.getBallotsGivenCount())
            .votesCastCount(r1.getVotesCastCount() + r2.getVotesCastCount())
            .votesValidCount(r1.getVotesValidCount() + r2.getVotesValidCount())
            .votesCountPerOption(
                    Lists.newArrayList(
                            summingIterator(
                                    r1.getVotesCountPerOption().iterator(),
                                    r2.getVotesCountPerOption().iterator(),
                                    Long.valueOf(0l),
                                    (a, b) -> a + b
                            )
                    )
            )
            .build();

    private <T> Iterator<T> summingIterator(Iterator<T> i1, Iterator<T> i2, T zero, BinaryOperator<T> addition) {
        return new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                if (!i1.hasNext() && !i2.hasNext())
                    return endOfData();
                T sum = i1.hasNext() ? i1.next() : zero;
                if (i2.hasNext()) sum = addition.apply(sum, i2.next());
                return sum;
            }
        };
    }


}
