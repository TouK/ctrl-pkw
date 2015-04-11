package pl.ctrlpkw.service;

import org.junit.Test;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Protocol;

import java.util.Arrays;

import static org.junit.Assert.*;

public class VotesCountingServiceTest {


    private VotesCountingService sut = new VotesCountingService();

    @Test
    public void testSumVotes() throws Exception {


        Protocol p1 = protocol(1, 1, 1, 1);
        Protocol p2 = protocol(2, 2, 2, 2);
        BallotResult ballotResult = sut.sumVotes(Arrays.asList(p1, p2));


    }

    private Protocol protocol(int ballotsGivenCount, int votersEntitledCount, int votesCastCount, int votesValidCount) {
        Protocol protocol = new Protocol();
        protocol.setBallotsGivenCount(ballotsGivenCount); // wydanych kart
        protocol.setVotersEntitledCount(votersEntitledCount); // uprawnieni
        protocol.setVotesCastCount(votesCastCount); // glosow oddanych
        protocol.setVotesValidCount(votesValidCount); // glosow waznych
        return protocol;
    }


    @Test
    public void testSumVotes1() throws Exception {

    }
}