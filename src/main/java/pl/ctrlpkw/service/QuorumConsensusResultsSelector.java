package pl.ctrlpkw.service;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Protocol;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "protocol.selector")
public class QuorumConsensusResultsSelector implements ResultsSelector, InitializingBean {

    @Getter
    @Setter
    private List<QuorumConfigurationEntry> config = Lists.newArrayList();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.getClass();
    }

    @Override
    public Optional<BallotResult> apply(List<Protocol> wardProtocols) {
        wardProtocols = getNonDepreciated(wardProtocols);
        List<Protocol> approvedProtocols = getApprovedProtocols(wardProtocols);

        if (areCoherent(approvedProtocols)) {
            return approvedProtocols.stream().findFirst().map(resultFromProtocol);
        }

        return quorumSatisfied(wardProtocols);
    }

    private List<Protocol> getNonDepreciated(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(p -> !p.getIsVerified() || isApproved(p))
                .collect(Collectors.toList());
    }

    private List<Protocol> getApprovedProtocols(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(QuorumConsensusResultsSelector::isApproved)
                .collect(Collectors.toList());
    }

    private boolean areCoherent(List<Protocol> verifiedProtocols) {
        return verifiedProtocols.stream()
                .findFirst()
                .map(first -> verifiedProtocols.stream()
                        .allMatch(protocol -> resultEquals(first, protocol))
                ).orElse(false);
    }

    private Optional<BallotResult> quorumSatisfied(List<Protocol> protocols) {
        Optional<List<Protocol>> biggestCoherentGroup = getBiggestCoherentGroup(protocols);

        return biggestCoherentGroup.flatMap(group -> {
            int protocolsSize = protocols.size();
            int coherentToAll = getPercentage(group.size(), protocolsSize);
            return group.stream()
                    .findFirst()
                    .filter(protocol -> config.stream()
                            .filter(c -> c.isInRuleRange(protocolsSize, coherentToAll))
                            .findFirst()
                            .isPresent())
                    .map(resultFromProtocol);
        });

    }

    private static boolean isApproved(Protocol protocol) {
        return protocol.getIsVerified()
                && !protocol.getApprovals().isEmpty()
                && protocol.getDeprecations().isEmpty();
    }

    private boolean resultEquals(Protocol p1, Protocol p2) {
        return resultFromProtocol.apply(p1)
                .equals(resultFromProtocol.apply(p2));
    }

    private Optional<List<Protocol>> getBiggestCoherentGroup(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .collect(Collectors.groupingBy(resultFromProtocol))
                .values().stream()
                .sorted((p1, p2) -> p2.size() - p1.size())
                .findFirst();


    }

    private int getPercentage(long subset, long all) {
        return (int) Math.round((double) subset / (double) all * 100.0);
    }

    @Getter
    @Setter
    public static class QuorumConfigurationEntry implements Serializable {

        private Long size;

        private Integer percent;

        public boolean isInRuleRange(int size, Integer percent) {
            return this.size <= size && this.percent <= percent;
        }

    }
}
