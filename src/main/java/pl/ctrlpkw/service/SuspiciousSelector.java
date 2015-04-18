package pl.ctrlpkw.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.read.QuorumConfiguration;
import pl.ctrlpkw.model.read.QuorumConfigurationRepository;
import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SuspiciousSelector implements ResultsSelectorStrategy {

    private QuorumConfigurationRepository quorumConfigurationRepository;
    private List<QuorumConfiguration> configurationEntries;

    @Autowired
    public SuspiciousSelector(QuorumConfigurationRepository quorumConfigurationRepository) {
        this.quorumConfigurationRepository = quorumConfigurationRepository;
        this.configurationEntries = Lists.newArrayList(quorumConfigurationRepository.allOrderByFromSizeDesc());
    }

    @Override
    public Optional<BallotResult> apply(List<Protocol> wardProtocols) {
        wardProtocols = getNonDepreciated(wardProtocols);

        if (wardProtocols.size() == 0 ) return Optional.empty();

        List<Protocol> approvedProtocols = getApprovedProtocols(wardProtocols);

        if (areCoherent(approvedProtocols)) {
            return approvedProtocols.stream().findFirst().map(resultsFromProtocol);
        }

        return quorumSatisfied(wardProtocols);
    }

    private Optional<BallotResult> quorumSatisfied(List<Protocol> wardProtocols) {
        List<Protocol> biggestCoherentGroup = getBiggestCoherentGroup(wardProtocols);
        Optional<QuorumConfiguration> first = configurationEntries.stream().filter(entry -> entry.getFromSize() <= wardProtocols.size()).findFirst();
        int percent = getPercentage(biggestCoherentGroup.size(), wardProtocols.size());

        if (first.filter(entry -> percent >= entry.getPercent()).isPresent()) {
            return biggestCoherentGroup.stream().findFirst().map(Protocol::toResult);
        }

        return Optional.empty();
    }

    private int getPercentage(long subset, long all) {
        return (int) Math.round((double) subset / (double) all * 100.0);
    }

    private List<Protocol> getBiggestCoherentGroup(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .collect(Collectors.groupingBy(Protocol::toResult))
                .values().stream()
                .sorted((p1, p2) -> p2.size() - p1.size())
                .collect(Collectors.toList())
                .get(0);
    }

    private List<Protocol> getApprovedProtocols(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(SuspiciousSelector::isApproved)
                .collect(Collectors.toList());
    }

    private List<Protocol> getNonDepreciated(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(p -> !p.getIsVerified() || isApproved(p))
                .collect(Collectors.toList());
    }

    private static boolean isApproved(Protocol protocol) {
        return protocol.getIsVerified()
                && protocol.getApprovals().size() > 0
                && protocol.getDeprecations().size() == 0;
    }

    private boolean areCoherent(List<Protocol> verifiedProtocols) {
        if (verifiedProtocols.size() == 0) {
            return false;
        }

        return verifiedProtocols.stream().allMatch(protocol -> {
            Protocol firstProcotol = verifiedProtocols.stream().findFirst().get();
            return resultEqauls(firstProcotol, protocol);
        });
    }

    private boolean resultEqauls(Protocol p1, Protocol p2) {
        return p1.toResult().equals(p2.toResult());
    }

}
