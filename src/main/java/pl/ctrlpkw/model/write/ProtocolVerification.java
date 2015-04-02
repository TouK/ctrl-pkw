package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.UUID;

@Table(keyspace = "ctrl_pkw", name="protocol_verification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolVerification {

    @PartitionKey
    private UUID id;

    public enum Result { APPROVAL, DEPRECATION }

    Result result;

}
