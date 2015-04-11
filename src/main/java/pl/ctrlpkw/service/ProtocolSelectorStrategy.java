package pl.ctrlpkw.service;

import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;

public interface ProtocolSelectorStrategy {
    Optional<Protocol> select(List<Protocol> wardProtocols);
}
