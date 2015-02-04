package pl.ctrlpkw.api.dto;

import com.google.common.collect.Sets;
//import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallotResult {

    @NotNull
    private Integer ballotNo;

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
