package pl.ctrlpkw.service

import pl.ctrlpkw.model.write.Protocol
import spock.lang.Specification
import spock.lang.Unroll

class SuspiciousSelectorTest extends Specification {

    SuspiciousSelector selector = new SuspiciousSelector()

    def "should select one of coherent approved protocols"() {
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
        given:
            Protocol p1 = any()
            p1.ballotsGivenCount = 123
            Protocol p2 = any()
            p2.ballotsGivenCount = 321

        when:
            Optional<Protocol> selected =  selector.select([p1, p2])
        then:
            !selected.isPresent()

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
            [any(), any()]        | false      | _
            [any(), any(), any()] | true       | any().ballotsGivenCount


    }

    private Protocol any() {
        return any(1)
    }

    private Protocol verified() {
        Protocol anyProtocol = any(1)
        anyProtocol.isVerified = true
        return anyProtocol
    }

    private Protocol approved() {
        Protocol anyProtocol = any(1)
        anyProtocol.isVerified = true
        anyProtocol.approvals = ["a", "b"]
        return anyProtocol
    }

    private Protocol depreciated() {
        Protocol anyProtocol = any(1)
        anyProtocol.isVerified = true
        anyProtocol.approvals = ["a", "b"]
        anyProtocol.deprecations = ["c"]
        return anyProtocol
    }


    private Protocol any(int ballotsCount) {
        return Protocol.builder()
                .id(UUID.randomUUID())
                .ballotsGivenCount(ballotsCount)
                .votersEntitledCount(2)
                .votesCastCount(3)
                .votesValidCount(4)
                .isVerified(false)
                .votesCountPerOption([1, 2, 3])
                .build()

    }
}
