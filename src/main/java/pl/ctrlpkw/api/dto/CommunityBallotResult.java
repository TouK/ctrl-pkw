package pl.ctrlpkw.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.Map;

@ApiModel
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommunityBallotResult {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Map resultsPerWards;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BallotResult summarized;

}
