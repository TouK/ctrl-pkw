package pl.ctrlpkw.service;

import pl.ctrlpkw.model.write.Protocol;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class FirstProtocol implements ProtocolSelectorStrategy {

    @Override
    public Optional<Protocol> select(List<Protocol> wardProtocols) {

        if (wardProtocols.size() != 1) {
            System.out.println(wardProtocols);
            throw new IllegalArgumentException();
        }

        return wardProtocols.stream().findFirst();
    }
}
