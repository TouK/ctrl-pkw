package pl.ctrlpkw.api.dto;

//import com.wordnik.swagger.annotations.ApiModel;
//import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@ApiModel
public class Protocol {

    @NotNull
    private String communityCode;

    @NotNull
    private Integer wardNo;

    @Valid
    private Collection<BallotResult> ballotResults;


}
