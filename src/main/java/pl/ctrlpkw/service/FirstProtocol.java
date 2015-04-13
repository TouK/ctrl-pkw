package pl.ctrlpkw.service;

import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;


public class FirstProtocol implements ProtocolSelectorStrategy {

    @Override
    public Optional<Protocol> select(List<Protocol> wardProtocols) {
        return wardProtocols.stream().findFirst();
    }
}
