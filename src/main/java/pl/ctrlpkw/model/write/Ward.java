package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

@UDT(keyspace = "ctrl_pkw", name = "ward")
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Field(name = "community_code")
    private String communityCode;

    @Field(name = "ward_no")
    private int no;

}
