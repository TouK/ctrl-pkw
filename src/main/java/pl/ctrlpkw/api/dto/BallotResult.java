package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallotResult implements Serializable {

    @NotNull
    private Long votersEntitledCount;

    @NotNull
    private Long ballotsGivenCount;

    @NotNull
    private Long votesCastCount;

    @NotNull
    private Long votesValidCount;

    private List<Long> votesCountPerOption;

}
