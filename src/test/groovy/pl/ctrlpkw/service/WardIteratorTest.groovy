package pl.ctrlpkw.service

import pl.ctrlpkw.model.write.Protocol
import pl.ctrlpkw.model.write.Ward
import spock.lang.Specification
import spock.lang.Unroll

import static java.util.Optional.empty
import static java.util.Optional.of


class WardIteratorTest extends Specification {


    public static final Optional empty = empty()
    private static Optional<Protocol> any = of(fromWardNo(1))

    def "should valid has elements count"() {
        given:
            FirstProtocol selector = Mock(FirstProtocol)
            selector.select(_) >> any
            WardIterator sut = new WardIterator(protocols.iterator(), selector)
        when:
            int counter = 0;
            sut.forEachRemaining({counter++})
        then:

            counter == count
        where:
            protocols                                                    | count
            []                                                           | 0
            [fromWardNo(1), fromWardNo(1), fromWardNo(2)]                | 2
            [fromWardNo(1), fromWardNo(2), fromWardNo(2)]                | 2
            [fromWardNo(1), fromWardNo(1), fromWardNo(1)]                | 1
            [fromWardNo(1), fromWardNo(2), fromWardNo(3)]                | 3
            [fromWardNo(1), fromWardNo(2), fromWardNo(3), fromWardNo(3)] | 3

    }

    @Unroll
    def "test has valid elements count using custom selector"() {
        given:
            FirstProtocol selector = Mock(FirstProtocol)
            selector.select(_) >>> selected
            WardIterator sut = new WardIterator(protocols.iterator(), selector)
        when:
            int counter = 0;
            sut.forEachRemaining({counter++})
        then:
            counter == count
        where:
            protocols           | selected            | count
            []                  | []                  | 0
            fromWardNo(1, 2, 3) | [empty] * 3         | 0
            fromWardNo(1, 2, 3) | [any, any, empty]   | 2
            fromWardNo(1, 2, 3) | [empty, any, empty] | 1
            fromWardNo(1, 2, 3) | [empty, empty, any] | 1
            fromWardNo(1, 2, 3) | [any] * 3           | 3
    }

    @Unroll
    def "should return protocols"() {
        given:
            FirstProtocol selector = Mock(FirstProtocol)
            Protocol p1 = fromWardNo(1)
            Protocol p2 = fromWardNo(1)

            List<Protocol> protocols = [p1, p2]
            Iterator<Protocol> iterator = protocols.iterator()
            selector.select({
                it == protocols
            }) >> Optional.of(p1)

            WardIterator sut = new WardIterator(protocols.iterator(), selector)
        when:
            int counter = 0;
            sut.forEachRemaining({
                counter++
                assert it == iterator.next()
            })
        then:
            counter == 1

    }



    Iterable<Protocol> protocols() {
        return [
                fromWardNo(1),
                fromWardNo(1),
                fromWardNo(2),
        ]
    }

    private static Protocol fromWardNo(int i) {
        return Protocol.builder().ward(Ward.builder().no(i).build()).build()

    }

    private List<Protocol> fromWardNo(int ... i) {
        return i.collect({
            Protocol.builder().ward(Ward.builder().no(it).build()).build()
        })

    }

}
