package pl.ctrlpkw.service

import pl.ctrlpkw.model.write.Protocol
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class SuspiciousSelectorTest extends Specification {

    SuspiciousSelector selector = new SuspiciousSelector()

    @Unroll
    def "should filter out any protocol without image"() {
        when:
            Optional<Protocol> selected =  selector.select(protocols)
        then:
            selected.present == isSelected
        where:
            protocols                                                    | isSelected
            [approved()]                                                 | true
            [approved(NO_IMAGE)]                                         | false
            [approved(NO_IMAGE), approved(NO_IMAGE), approved(NO_IMAGE)] | false
    }


    def "should select any of coherent approved protocols"() {
        given:

            int ballotsGivenCount = 789
            Protocol p1 = approved()
            p1.ballotsGivenCount = ballotsGivenCount
            Protocol p2 = approved()
            p2.ballotsGivenCount = ballotsGivenCount

        when:
            Optional<Protocol> selected =  selector.select([p1, p2])
        then:
            selected.isPresent()
            selected.get().ballotsGivenCount == ballotsGivenCount


    }

    def "should select no one of depreciated protocols"() {
        given:
            Protocol p1 = depreciated()
            Protocol p2 = depreciated()

        when:
            Optional<Protocol> selected =  selector.select([p1, p2])
        then:
            !selected.isPresent()

    }

    def "should not select any of non coherent protocols"() {
        when:
            Optional<Protocol> selected =  selector.select(protocols)
        then:
            !selected.isPresent()
        where:
            protocols                                    | _
            [with(BALLOTS(1)), with(BALLOTS(2))]         | _
            [approved(BALLOTS(1)), approved(BALLOTS(2))] | _

    }

    @Unroll
    def "should select best protocol if possible"() {
        when:
            Optional<Protocol> selected =  selector.select(protocols)
        then:
            selected.present == isSelected
            selected.map{p ->
                assert p.ballotsGivenCount == selectedBallotsCount
            }
        where:
            protocols             | isSelected | selectedBallotsCount
            [noImage(), any()]    | false      | _
            [noImage(), any()]    | false      | _
            [any(), any(), any()] | true       | any().ballotsGivenCount


    }


    private Protocol with(Function<Protocol, Void> firstModificator, Function<Protocol, Void> ... modifier ) {
        Protocol any = any()

        List<Function<Protocol, Void>> allModifiers = [firstModificator] + modifier.toList()
        allModifiers.each {
            it.apply(any)
        }
        return any
    }

    private Protocol noImage(Function<Protocol, Void> ... p) {
        return with(NO_IMAGE, p)
    }

    private Protocol approved(Function<Protocol, Void> ... p ) {
        return with(APPROVED, p)
    }

    private Protocol depreciated(Function<Protocol, Void> ... p) {
        return with(DEPRECIATED, p)
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

    private static Function<Protocol, Void> NO_IMAGE = {p -> p.cloudinaryCloudName = null}
    private static Function<Protocol, Void> APPROVED = {p ->
        p.isVerified = true
        p.approvals = ["a", "b"]
        p.deprecations = []
    }

    private static Function<Protocol, Void> DEPRECIATED = {p ->
        p.isVerified = true
        p.approvals = ["a", "b"]
        p.deprecations = ["a", "b"]
    }

    private static Function<Protocol, Void> BALLOTS(Integer ballots) {
        return {Protocol p ->
            p.ballotsGivenCount = ballots
        }
    }


}
