package pl.ctrlpkw.service

import pl.ctrlpkw.api.dto.BallotResult
import pl.ctrlpkw.model.write.Protocol
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class SuspiciousSelectorTest extends Specification {

    SuspiciousSelector selector = new SuspiciousSelector()

    def "should handle empty list"() {
        when:
            Optional<Protocol> selected = selector.apply([])
        then:
            !selected.isPresent()
    }

    def "should select any of coherent approved protocols"() {
        when:
            Optional<Protocol> selected = selector.apply(coherentProtocols)
        then:
            selected.isPresent()
        where:
            coherentProtocols                                              | _
            [approved(), approved()]                                       | _
            [approved(VOTES([1, 2, 3, 4])), approved(VOTES([1, 2, 3, 4]))] | _
    }


    @Unroll
    def "should not select any of non coherent protocols"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            !selected.isPresent()
        where:
            protocols                                                      | _
            [with(BALLOTS(1)), with(BALLOTS(1)), with(BALLOTS(2))]         | _
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
    def "should select unverified protocol if all are coferent, unverified and they are 3 min"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            selected.present == isSelected
            selected.map { p ->
                assert p.ballotsGivenCount == selectedBallotsCount
            }
        where:
            protocols                                                | isSelected | selectedBallotsCount
            [unverified(), unverified()]                             | false      | _
            [unverified(), unverified(), unverified()]               | true       | unverified().ballotsGivenCount
            [unverified(), unverified(), unverified(), unverified()] | true       | unverified().ballotsGivenCount

    }

    @Unroll
    def "should select best protocol if possible "() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            selected.present == isSelected
            selected.map { p ->
                assert p.ballotsGivenCount == selectedBallotsCount
            }
        where:
            protocols             | isSelected | selectedBallotsCount
            [any(), any()]        | false      | _
            [any(), any(), any()] | true       | any().ballotsGivenCount

    }

    @Unroll
    @Ignore
    def "should filter out any protocol without image"() {
        when:
            Optional<BallotResult> selected = selector.apply(protocols)
        then:
            selected.present == isSelected
        where:
            protocols                                                    | isSelected
            [approved()]                                                 | true
            [approved(NO_IMAGE)]                                         | false
            [approved(NO_IMAGE), approved(NO_IMAGE), approved(NO_IMAGE)] | false
    }

    private Protocol with(Function<Protocol, Void> firstModificator, Function<Protocol, Void>... modifier) {
        Protocol any = any()

        List<Function<Protocol, Void>> allModifiers = [firstModificator] + modifier.toList()
        allModifiers.each {
            it.apply(any)
        }
        return any
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


    private Protocol unverified() {
        return with(UNVERIFIED)
    }

    private Protocol any() {
        return Protocol.builder()
                .id(UUID.randomUUID())
                .ballotsGivenCount(1)
                .votersEntitledCount(2)
                .votesCastCount(3)
                .votesValidCount(4)
                .isVerified(false)
                .votesCountPerOption([1, 2, 3])
                .cloudinaryCloudName("image")
                .build()
    }

    private static Function<Protocol, Void> NO_IMAGE = { p -> p.cloudinaryCloudName = null }

    private static Function<Protocol, Void> APPROVED = { p ->
        p.isVerified = true
        p.approvals = ["a", "b"]
        p.deprecations = []
    }

    private static Function<Protocol, Void> DEPRECIATED = { p ->
        p.isVerified = true
        p.approvals = ["a", "b"]
        p.deprecations = ["a", "b"]
    }

    private static Function<Protocol, Void> UNVERIFIED = { p ->
        p.isVerified = false
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
