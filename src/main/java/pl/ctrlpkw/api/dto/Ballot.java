package pl.ctrlpkw.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.joda.time.LocalDate;
import pl.ctrlpkw.api.resource.BallotsResource;
import pl.ctrlpkw.api.resource.ResultsResource;

import java.util.List;

@ApiModel
@Getter
@Setter
@Builder
public class Ballot {

    @JsonIgnore
    private LocalDate votingDate;

    private Integer no;

    private String question;

    private List<String> options;

    @ApiModelProperty(hidden = true)
    @InjectLinks({
            @InjectLink(resource = BallotsResource.class, rel = "self", method = "readOne", style = InjectLink.Style.ABSOLUTE,
                    bindings = { @Binding(name = "date", value = "${instance.votingDate}") }
            ),
            @InjectLink(resource = ResultsResource.class, rel = "result", method = "read", style = InjectLink.Style.ABSOLUTE,
                bindings = {
                        @Binding(name = "date", value = "${instance.votingDate}"),
                        @Binding(name = "ballotNo", value = "${instance.no}")
                }
    )
    })
    private List links;


}
