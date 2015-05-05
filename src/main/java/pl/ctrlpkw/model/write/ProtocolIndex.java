package pl.ctrlpkw.model.write;


import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

import java.util.UUID;

@Table(keyspace = "ctrl_pkw", name="protocol_index")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolIndex {

    @PartitionKey
    private UUID id;

    @Frozen
    private Ballot ballot;

    @Frozen
    private Ward ward;

}
