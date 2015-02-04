package pl.ctrlpkw.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;
import pl.ctrlpkw.api.resource.BallotsResource;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.joda.time.LocalDate;

import javax.ws.rs.core.Link;
import java.util.*;

@Getter
@Setter
@Builder
public class Ballot {

    @JsonIgnore
    private LocalDate votingDate;

    private Integer no;

    private String question;

    private List<String> options;

    @InjectLinks({
            @InjectLink(resource = BallotsResource.class, rel = "self", method = "readOne", style = InjectLink.Style.ABSOLUTE,
                    bindings = { @Binding(name = "date", value = "${instance.votingDate}") }
            )
    })
    private List<Link> links;


}
