package pl.ctrlpkw.service;

import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.function.Function;

public interface ResultsSelector extends Function<List<Protocol>, WardResult> {

    public static final Function<Protocol, BallotResult > resultFromProtocol = protocol ->
            BallotResult.builder()
                    .votersEntitledCount(protocol.getVotersEntitledCount())
                    .ballotsGivenCount(protocol.getBallotsGivenCount())
                    .votesCastCount(protocol.getVotesCastCount())
                    .votesValidCount(protocol.getVotesValidCount())
                    .votesCountPerOption(protocol.getVotesCountPerOption())
                    .build();

}
