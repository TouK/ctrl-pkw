package pl.ctrlpkw.api.dto;

//import com.wordnik.swagger.annotations.ApiModel;
//import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@ApiModel
public class Ward {

    private String communityCode;
    private Integer no;
    private String address;
    private Location location;

}
