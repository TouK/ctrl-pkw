package pl.ctrlpkw.service;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Protocol;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Component
@ConfigurationProperties(prefix = "protocol.selector")
public class QuorumConsensusResultsSelector implements ResultsSelector {

    @Getter
    @Setter
    private List<QuorumConfigurationEntry> config = Lists.newArrayList();

    @Override
    public Optional<BallotResult> apply(List<Protocol> wardProtocols) {

        Optional<BallotResult> approvedCoherentResult = retrieveResultIfAllApprovedProtocolsHasItTheSame(wardProtocols);
        if (approvedCoherentResult.isPresent()) return approvedCoherentResult;

        Optional<BallotResult> quorumSatisfiedResult = retrieveBestResultIfItSatisfiesQuorum(wardProtocols);
        return quorumSatisfiedResult;
    }

    private Optional<BallotResult> retrieveResultIfAllApprovedProtocolsHasItTheSame(Collection<Protocol> protocols) {
        return protocols.stream()
                .filter(Protocol::isApproved)
                .map(resultFromProtocol)
                .map(Optional::of)
                .reduce((r1, r2) -> (r1.equals(r2)) ? r1 : Optional.empty())
                .orElse(Optional.empty())
                ;
    }

    private Optional<BallotResult> retrieveBestResultIfItSatisfiesQuorum(List<Protocol> protocols) {
        return protocols.stream()
                .filter(Protocol::isNotDeprecated)
                .collect(groupingBy(resultFromProtocol, counting()))
                .entrySet().stream()
                .map(BestResultHolder::new)
                .reduce(BestResultHolder::chooseBetterAndSumProtocolsCount)
                .flatMap(holder -> holder.getResult())
                ;
    }

    private class BestResultHolder {

        private long allProtocolsCount;

        private Map.Entry<BallotResult, Long> result;

        public BestResultHolder(Map.Entry<BallotResult, Long> result) {
            this(result, result.getValue());
        }

        public BestResultHolder(Map.Entry<BallotResult, Long> result, long allProtocolsCount) {
            this.result = result;
            this.allProtocolsCount = allProtocolsCount;
        }

        Optional<BallotResult> getResult() {
            return Optional.ofNullable(isQuorumSatisfied() ? result.getKey() : null);
        }

        public boolean isQuorumSatisfied() {
            return config.stream()
                    .anyMatch(c -> c.isInRuleRange(allProtocolsCount, result.getValue().doubleValue() / allProtocolsCount));
        }

        public BestResultHolder chooseBetterAndSumProtocolsCount(BestResultHolder other) {
            return new BestResultHolder(
                    this.result.getValue() >= other.result.getValue() ? this.result : other.result,
                    this.allProtocolsCount + other.allProtocolsCount
            );
        }

    };

    @Getter
    @Setter
    public static class QuorumConfigurationEntry implements Serializable {

        private long size;

        private double quorum;

        public boolean isInRuleRange(long size, double quorum) {
            return this.size <= size && this.quorum <= quorum;
        }

    }
}
