package pl.ctrlpkw.model.write;


import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.List;
import java.util.UUID;

@Table(keyspace = "ctrl_pkw", name="protocol")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Protocol {

    @PartitionKey
    private UUID id;

    @ClusteringColumn(0)
    @Frozen
    private Ballot ballot;

    @ClusteringColumn(1)
    @Frozen
    private Ward ward;

    @Column(name = "voters_entitled_count")
    private long votersEntitledCount;

    @Column(name = "ballots_given_count")
    private long ballotsGivenCount;

    @Column(name = "votes_cast_count")
    private long votesCastCount;

    @Column(name = "votes_valid_count")
    private long votesValidCount;

    @Column(name = "votes_count_per_option")
    private List<Long> votesCountPerOption;

    @Column(name = "cloudinary_cloud_name")
    private String cloudinaryCloudName;

}
