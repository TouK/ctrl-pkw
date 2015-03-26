package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    private String communityCode;
    private Integer no;
    private String address;
    private Location location;
    private String label;

    public enum ProtocolStatus { LACK, VAGUE, CONFIRMED };
    @ApiModelProperty(dataType = "string", allowableValues = "LACK, VAGUE, CONFIRMED")
    private ProtocolStatus protocolStatus;

}
