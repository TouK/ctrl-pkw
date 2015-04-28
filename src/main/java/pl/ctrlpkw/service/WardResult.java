package pl.ctrlpkw.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.ctrlpkw.api.dto.BallotResult;
import pl.ctrlpkw.model.write.Ward;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class WardResult {
    private Ward ward;
    private Optional<BallotResult> ballotResult;
}
