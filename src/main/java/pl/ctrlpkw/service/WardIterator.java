package pl.ctrlpkw.service;


import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.Ward;

import java.util.*;

public class WardIterator implements Iterator<Protocol>{

//    Set<Ward> wardHandled = new HashSet<>();

    private final Iterator<Protocol> iterator;
    private final ProtocolSelectorStrategy selector;

    private Optional<Protocol> next;
    private Optional<Protocol> lastReturned;
    private boolean isConsumed = true;
    private Optional<Protocol> nextInOriginal = Optional.empty();
    private Ward ward;

    public WardIterator(Iterator<Protocol> iterator, ProtocolSelectorStrategy selector) {
        this.iterator = iterator;
        this.selector = selector;
        init();
    }

    private void init() {

        if (!iterator.hasNext()) {
            return;
        }
        nextInOriginal = Optional.of(iterator.next());
        ward = nextInOriginal.get().getWard();
    }


    @Override
    public boolean hasNext() {
        if (isConsumed) {
            isConsumed = false;
            next = alwaysNext();
        }

        return next.isPresent();
    }

    @Override
    public Protocol next() {

        if (!isConsumed) {
            lastReturned = next;
            isConsumed = true;
            return next.get();
        }

        lastReturned = alwaysNext();

        return lastReturned.orElseThrow(() -> new NoSuchElementException());
    }


    private Optional<Protocol> alwaysNext() {

        return nextInOriginal.flatMap(protocol -> {

            List<Protocol> wardProtocols = collectWardProtocols(protocol);
            Optional<Protocol> selected = selector.select(wardProtocols);

            if (!selected.isPresent()) {
                selected = alwaysNext();
            }

            return selected;
        });

    }

    private List<Protocol> collectWardProtocols(Protocol protocol) {
        List<Protocol> wardProtocols = new ArrayList<>();
//        if (wardHandled.contains(ward)) {
//            System.out.println(ward);
////            throw new IllegalStateException();
//        }

        ward = protocol.getWard();

        do {
            wardProtocols.add(nextInOriginal.get());
            nextInOriginal = forward(iterator);
        } while (nextInOriginal.isPresent() && nextInOriginal.get().getWard().equals(ward));

        return wardProtocols;
    }

    private static Optional<Protocol> forward(Iterator<Protocol> iterator) {
        if (iterator.hasNext())
            return Optional.of(iterator.next());
        else
            return Optional.empty();
    }
}
