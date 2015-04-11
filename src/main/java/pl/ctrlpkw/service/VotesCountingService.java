package pl.ctrlpkw.service;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import pl.ctrlpkw.CassandraContext;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.read.Ballot;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.ProtocolAccessor;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

@Service
public class VotesCountingService {

    @Resource
    CassandraContext cassandraContext;

    @Resource
    ProtocolSelectorStrategy selector;

    public BallotResult sumVotes(Ballot ballot) {
        ProtocolAccessor accessor = cassandraContext.getMappingManager().createAccessor(ProtocolAccessor.class);

        return sumVotes(accessor.findByBallot(
                pl.ctrlpkw.model.write.Ballot.builder()
                        .votingDate(ballot.getVoting().getDate().toDate())
                        .no(ballot.getNo())
                        .build()));
    }

    public BallotResult sumVotes(Iterable<Protocol> protocols) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new WardIterator(protocols.iterator(), selector), 0), false)
                .reduce(identity, accumulator, combiner);
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
                                    0l,
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
                                    0l,
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
