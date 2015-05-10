package pl.ctrlpkw.api.dto;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BallotResultTest {

    @Test
    public void shouldAddResultsToEmptyResults() {
        BallotResult r1 = new BallotResult(0, 0l, 0l, 0l, 0l, Lists.<Long>newArrayList());
        BallotResult r2 = new BallotResult(1, 32l, 16l, 8l, 6l, Lists.<Long>newArrayList(1l, 2l, 3l));

        BallotResult res = r1.add(r2);

        assertArrayEquals(r2.getVotesCountPerOption().toArray(), res.getVotesCountPerOption().toArray());
        assertEquals(r2.getIncludedWardsCount(), res.getIncludedWardsCount());
        assertEquals(r2.getBallotsGivenCount(), res.getBallotsGivenCount());
        assertEquals(r2.getVotersEntitledCount(), res.getVotersEntitledCount());
        assertEquals(r2.getVotesCastCount(), res.getVotesCastCount());
        assertEquals(r2.getVotesValidCount(), res.getVotesValidCount());

        res = r2.add(r1);

        assertArrayEquals(r2.getVotesCountPerOption().toArray(), res.getVotesCountPerOption().toArray());
        assertEquals(r2.getIncludedWardsCount(), res.getIncludedWardsCount());
        assertEquals(r2.getBallotsGivenCount(), res.getBallotsGivenCount());
        assertEquals(r2.getVotersEntitledCount(), res.getVotersEntitledCount());
        assertEquals(r2.getVotesCastCount(), res.getVotesCastCount());
        assertEquals(r2.getVotesValidCount(), res.getVotesValidCount());

    }

}