package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import lombok.*;
import lombok.experimental.Builder;

import java.util.Date;

@UDT(keyspace = "ctrl_pkw", name = "ward")
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Field(name = "community_code")
    private String communityCode;

    @Field(name = "ward_no")
    private int no;

}
