package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.joda.time.LocalDate;
import pl.ctrlpkw.api.resource.BallotsResource;
import pl.ctrlpkw.api.resource.VotingResource;
import pl.ctrlpkw.api.resource.WardsResource;

import java.util.List;

@ApiModel
@Getter
@Setter
@Builder
public class Voting {

    private String description;

    private LocalDate date;

    @ApiModelProperty(hidden = true)
    @InjectLinks({
            @InjectLink(resource = VotingResource.class, rel = "self", method = "readOne", style = InjectLink.Style.ABSOLUTE),
            @InjectLink(resource = BallotsResource.class, rel = "ballots", method = "readAll", style = InjectLink.Style.ABSOLUTE),
            @InjectLink(resource = WardsResource.class, rel = "wards", method = "readAll", style = InjectLink.Style.ABSOLUTE)
    })
    private List links;

}
