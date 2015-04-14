package pl.ctrlpkw.service;

import pl.ctrlpkw.model.write.Protocol;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SuspiciousSelector implements ProtocolSelectorStrategy {
    @Override
    public Optional<Protocol> select(List<Protocol> wardProtocols) {

        wardProtocols = getProtocolsWtihImage(wardProtocols);

        List<Protocol> approvedProtocols = getApprovedProtocols(wardProtocols);

        if (areCoherent(approvedProtocols)) {
            return approvedProtocols.stream().findFirst();
        }

        if (areCoherent(wardProtocols) && wardProtocols.size() > 2) {
            return wardProtocols.stream().findFirst();
        }

        //TODO(cdr) pass quorum satisfied

        return Optional.empty();
    }

    private List<Protocol> getApprovedProtocols(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(SuspiciousSelector::isApproved)
                .collect(Collectors.toList());
    }

    private List<Protocol> getProtocolsWtihImage(List<Protocol> wardProtocols) {
        return wardProtocols.stream()
                .filter(p -> p.getCloudinaryCloudName() != null)
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
        return p1.getVotesValidCount() == p2.getVotesValidCount()
                && p1.getVotesCountPerOption().containsAll(p2.getVotesCountPerOption()) && p2.getVotesCountPerOption().containsAll(p1.getVotesCountPerOption())
                && p1.getBallotsGivenCount() == p2.getBallotsGivenCount()
                && p1.getVotersEntitledCount() == p2.getVotersEntitledCount()
                && p1.getVotesCastCount() == p2.getVotesCastCount();
    }
}
