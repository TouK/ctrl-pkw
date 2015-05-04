package pl.ctrlpkw.service;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.api.dto.Ward;
import pl.ctrlpkw.model.read.Ballot;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.ProtocolAccessor;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class VotesCountingService {

    @Resource
    ResultsSelector resultsSelector;

    @Resource
    WardStateProvider wardStateProvider;

    @Resource

    private ProtocolAccessor protocolAccessor;

    public BallotResult sumVotes(Ballot ballot) {
        return sumVotes(
                pl.ctrlpkw.model.write.Ballot.builder()
                        .votingDate(ballot.getVoting().getDate().toDate())
                        .no(ballot.getNo())
                        .build()
        );
    }

    public BallotResult sumVotes(pl.ctrlpkw.model.write.Ballot ballot) {
        Iterable<Protocol> protocols = protocolAccessor.findByBallot(ballot);

        ExecutorService executorService = Executors.newFixedThreadPool(64);

        log.info("Votes summing started");
        BallotResult result = StreamUtils.aggregate(StreamSupport.stream(protocols.spliterator(), true), Protocol::isSameWard)
                .map(wardProtocols -> Pair.of(wardProtocols.get(0).getWard(), resultsSelector.apply(wardProtocols)))
                .map(localBallotResult -> {
                    executorService.execute(() -> {
                        wardStateProvider.save(
                                LocalDate.fromDateFields(ballot.getVotingDate()),
                                localBallotResult.getKey().getCommunityCode(),
                                localBallotResult.getKey().getNo(),
                                localBallotResult.getValue().isPresent() ?
                                        Ward.ProtocolStatus.CONFIRMED : Ward.ProtocolStatus.VAGUE

                        );
                    });
                    return localBallotResult.getValue();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(new BallotResult(0l, 0l, 0l, 0l, Lists.newArrayList()), BallotResult::add);

        log.info("Votes summing finished");
        try {
            executorService.shutdown();
            log.info("Waitng for cache tasks to finish...");
            if (executorService.awaitTermination(15, TimeUnit.MINUTES)) {
                log.warn("Finished.");
            } else {
                log.warn("Timed out.");
            }
        } catch (InterruptedException e) {
            log.warn("Timeout waiting for cache tasks");
        }

        return result;
    }

}
