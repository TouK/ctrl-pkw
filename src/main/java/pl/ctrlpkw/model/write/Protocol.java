package pl.ctrlpkw.model.write;


import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Table(keyspace = "ctrl_pkw", name="protocol")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Protocol {


    @ClusteringColumn
    private UUID id;

    @PartitionKey(1)
    @Frozen
    private Ballot ballot;

    @PartitionKey(0)
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

    @Column(name = "comment")
    private String comment;

    @Column(name = "is_verified")
    private boolean verified = false;

    @Column(name = "approvals")
    private Set<String> approvals;

    @Column(name = "deprecations")
    private Set<String> deprecations;

    @Column(name ="image_ids")
    private Set<UUID> imageIds;

    @Column(name = "creation_time")
    private Date creationTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "client_id")
    private String clientId;

    public boolean isSameWard(Protocol protocol) {
        return this.ward.equals(protocol.getWard());
    }

    public boolean isApproved() {
        return verified
                && !Collections.isEmpty(approvals)
                && Collections.isEmpty(deprecations);

    }

    public boolean isNotDeprecated() {
        return !verified
                || Collections.isEmpty(deprecations);

    }

}
