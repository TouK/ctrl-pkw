package pl.ctrlpkw.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.joda.time.LocalDate;
import pl.ctrlpkw.api.resource.BallotsResource;
import pl.ctrlpkw.api.resource.ProtocolsResource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import java.util.List;
import java.util.UUID;

@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Protocol {

    @JsonIgnore
    private UUID id;

    @NotNull
    private LocalDate votingDate;

    @NotNull
    private Integer ballotNo;

    @NotNull
    private String communityCode;

    @NotNull
    private Integer wardNo;

    @Valid
    private BallotResult ballotResult;

    @ApiModelProperty(hidden = true)
    @InjectLinks({
            @InjectLink(resource = ProtocolsResource.class, rel = "self", method = "readOne", style = InjectLink.Style.ABSOLUTE),
            @InjectLink(resource = BallotsResource.class, rel = "ballot", method = "readOne", style = InjectLink.Style.ABSOLUTE,
                    bindings = { @Binding(name = "no", value = "${instance.ballotNo}"), @Binding(name = "date", value = "${instance.votingDate}") }
            ),
            @InjectLink(value="http://res.cloudinary.com/${resource.cloudinaryCloudName}/image/upload/{id}", rel = "img", style = InjectLink.Style.RELATIVE_PATH)

    })
    private List<Link> links;

}
