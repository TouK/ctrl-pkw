package pl.ctrlpkw.service

import pl.ctrlpkw.api.dto.BallotResult
import pl.ctrlpkw.model.write.Protocol
import pl.ctrlpkw.model.write.Ward
import spock.lang.Specification

class VotesCountingServiceSpockTest extends Specification {

    VotesCountingService countingService = new VotesCountingService()

    def setup() {

    }

    def testsumVotes() {
        when:
            BallotResult result = countingService.sumVotes(protocols)
        then:
            result == expected
        where:
            [protocols, expected] << testData()


    }

    List testData() {
        return [[
                [new Protocol(
//                        ballotsGivenCount: 1,
//                        votersEntitledCount: 1,
//                        votesValidCount: 1,
//                        votesCastCount: 1,
                        votesCountPerOption: [1L] + [0L] * 9,
                        ward: new Ward(
                                no: 1L
                        )
                ),
                 new Protocol(
                         votesCountPerOption: [1L] + [0L] * 9,
                         ward: new Ward(
                                 no: 1L
                         )
                 ),
                 new Protocol(
                         votesCountPerOption: [1L] + [0L] * 9,
                         ward: new Ward(
                                 no: 2L
                         )
                 )
                ],
                new BallotResult(
                        ballotsGivenCount: 0L,
                        votersEntitledCount: 0L,
                        votesValidCount: 0L,
                        votesCastCount: 0L,
                        votesCountPerOption: [2L] + [0L] * 9
                )

        ]]
    }
}
