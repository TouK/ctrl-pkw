package pl.ctrlpkw.service

import pl.ctrlpkw.api.dto.BallotResult
import pl.ctrlpkw.model.write.Protocol
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class QuorumConsensusResultsSelectorTest extends Specification {

    @Shared
    QuorumConsensusResultsSelector selector = new QuorumConsensusResultsSelector()

    def setupSpec() {
        selector.setConfig([
                new QuorumConsensusResultsSelector.QuorumConfigurationEntry(
                        size: 5,
                        quorum: 0.80
                ),
                new QuorumConsensusResultsSelector.QuorumConfigurationEntry(
                        size: 4,
                        quorum: 0.75
                ),
                new QuorumConsensusResultsSelector.QuorumConfigurationEntry(
                        size: 3,
                        quorum: 0.66
                ),
                new QuorumConsensusResultsSelector.QuorumConfigurationEntry(
                        size: 2,
                        quorum: 1.00
                )
        ])
    }


    def "should handle empty list"() {
        when:
            Optional<BallotResult> selected = selector.apply([])
        then:
            !selected.isPresent()
    }

    def "should select any of coherent approved protocols"() {
        when:
            Optional<BallotResult> selected = selector.apply(coherentProtocols)
        then:
            selected.isPresent()
        where:
            coherentProtocols                                                     | _
            [approved()]                                                          | _
            [approved(), approved()]                                              | _
            [approved(VOTES([1, 2])), approved(VOTES([1, 2]))]                    | _
            [approved(BALLOTS(1)), approved(BALLOTS(1)), depreciated(BALLOTS(2))] | _
    }

    @Unroll
    def "should select if quorum is satisfied: #message"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            selected.isPresent() == isPresent
        where:
            protocols                                                                                                    | isPresent | message
            times(1, UNVERIFIED, VOTES([1, 2]))                                                                          | false     | "1 of 1 protocols is too less"
            [unverified(VOTES([1, 2])), unverified(VOTES([2, 1]))]                                                       | false     | "no same of 2 protocols"
            times(2, UNVERIFIED, VOTES([1, 2]))                                                                          | true      | "2 of 2 are same"
            [unverified(VOTES([1, 2]))] + times(2, UNVERIFIED, VOTES([2, 1]))                                            | true      | "2 same of 3 protocols"
            times(3, UNVERIFIED, VOTES([1, 2]))                                                                          | true      | "3 same of 3 protocols"
            [unverified(VOTES([2, 1])), unverified(VOTES([3, 4])), unverified(VOTES([5, 6]))]                            | false     | "no same of 3 protocols"
            times(2, UNVERIFIED, VOTES([1, 2])) + [unverified(VOTES([2, 1])), unverified(VOTES([3, 4]))]                 | false     | "2 same of 4 protocols"
            times(3, UNVERIFIED, VOTES([1, 2])) + [unverified(VOTES([2, 1]))]                                            | true      | "3 same of 4 protocols"
            times(4, UNVERIFIED, VOTES([1, 2]))                                                                          | true      | "4 same of 4 protocols"
            [unverified(VOTES([2, 1])), unverified(VOTES([3, 4])), unverified(VOTES([5, 6])), unverified(VOTES([5, 6]))] | false     | "no same of 4 protocols"
    }


    @Unroll
    def "should select none of non coherent protocols"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            !selected.isPresent()
        where:
            protocols                                                      | _
            [with(BALLOTS(1)), with(BALLOTS(2))]                           | _
            [approved(VOTERS_ENTITLED(1)), approved(VOTERS_ENTITLED(2))]   | _
            [approved(VOTES_CAST(1)), approved(VOTES_CAST(2))]             | _
            [approved(VOTES_VALID(1)), approved(VOTES_VALID(2))]           | _
            [approved(VOTES([1, 2, 3, 4])), approved(VOTES([4, 3, 2, 1]))] | _
            [approved(BALLOTS(1)), approved(BALLOTS(2))]                   | _

    }

    def "should select no one of depreciated protocols"() {
        given:
            Protocol p1 = depreciated()
            Protocol p2 = depreciated()
        when:
            Optional<BallotResult> selected = selector.apply([p1, p2])
        then:
            !selected.isPresent()
    }

    @Unroll
    def "should filter out any depraciateed protocol"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            selected.present == isSelected
        where:
            protocols                                     | isSelected
            [approved()]                                  | true
            [depreciated()]                               | false
            [approved(), approved(), approved()]          | true
            [depreciated(), depreciated(), depreciated()] | false
    }

    private Protocol with(Function<Protocol, Void> firstModificator, Function<Protocol, Void>... modifier) {
        Protocol any = any()

        List<Function<Protocol, Void>> allModifiers = [firstModificator] + modifier.toList()
        allModifiers.each {
            it.apply(any)
        }
        return any
    }

    private List<Protocol> times(int times, Function<Protocol, Void> firstModifier, Function<Protocol, Void>... modifiers) {
        List<Protocol> protocols = []
        while (times-- > 0) {
            protocols += with(firstModifier, modifiers)
        }
        return protocols
    }

    private Protocol noImage(Function<Protocol, Void>... p) {
        return with(NO_IMAGE, p)
    }

    private Protocol approved(Function<Protocol, Void>... p) {
        return with(APPROVED, p)
    }

    private Protocol depreciated(Function<Protocol, Void>... p) {
        return with(DEPRECIATED, p)
    }


    private Protocol unverified(Function<Protocol, Void>... p) {
        return with(UNVERIFIED, p)
    }

    private Protocol any() {
        return Protocol.builder()
                .id(UUID.randomUUID())
                .ballotsGivenCount(1)
                .votersEntitledCount(2)
                .votesCastCount(3)
                .votesValidCount(4)
                .verified(false)
                .votesCountPerOption([1, 2, 3])
                .cloudinaryCloudName("image")
                .build()
    }

    private static Function<Protocol, Void> NO_IMAGE = { p -> p.cloudinaryCloudName = null }

    private static Function<Protocol, Void> APPROVED = { p ->
        p.verified = true
        p.approvals = ["a", "b"]
        p.deprecations = []
    }

    private static Function<Protocol, Void> DEPRECIATED = { p ->
        p.verified = true
        p.approvals = ["a", "b"]
        p.deprecations = ["a", "b"]
    }

    private static Function<Protocol, Void> UNVERIFIED = { p ->
        p.verified = false
        p.approvals = []
        p.deprecations = []
    }

    private static Function<Protocol, Void> BALLOTS(Long ballots) {
        return { Protocol p ->
            p.ballotsGivenCount = ballots
        }
    }

    private static Function<Protocol, Void> VOTERS_ENTITLED(Long votersEntitled) {
        return { Protocol p ->
            p.votersEntitledCount = votersEntitled
        }
    }

    private static Function<Protocol, Void> VOTES_CAST(Long votesCastCount) {
        return { Protocol p ->
            p.votesCastCount = votesCastCount
        }
    }

    private static Function<Protocol, Void> VOTES_VALID(Long votesValidCount) {
        return { Protocol p ->
            p.votesValidCount = votesValidCount
        }
    }

    private static Function<Protocol, Void> VOTES(List<Long> votesCountPerOption) {
        return { Protocol p ->
            p.votesCountPerOption = votesCountPerOption
        }
    }


}
