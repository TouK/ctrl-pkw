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

import java.util.Date;

@UDT(keyspace = "ctrl_pkw", name = "ballot")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ballot {

    @Field(name = "voting_date")
    private Date votingDate;

    @Field(name = "ballot_no")
    private int no;

}
