package pl.ctrlpkw.service;

import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;


public class UseFirstProtocolStrategy implements ResultsSelectorStrategy {

    @Override
    public Optional<BallotResult> apply(List<Protocol> wardProtocols) {
        return wardProtocols.stream().findFirst().map(ResultsSelectorStrategy.resultsFromProtocol);
    }
}
