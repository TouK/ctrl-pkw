package pl.ctrlpkw.service;

import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;


public class FirstProtocolResultsSelector implements ResultsSelector {

    @Override
    public WardResult apply(List<Protocol> wardProtocols) {
        return wardProtocols
                .stream()
                .findFirst()
                .map(protocol -> new WardResult(protocol.getWard(), Optional.of(resultFromProtocol.apply(protocol))))
                .get();
    }
}
