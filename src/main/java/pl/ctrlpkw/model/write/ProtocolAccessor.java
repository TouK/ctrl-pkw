package pl.ctrlpkw.model.write;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

import java.util.Set;
import java.util.UUID;

@Accessor
public interface ProtocolAccessor {

    @Query("SELECT * FROM ctrl_pkw.protocol where is_verified = false")
    public Result<Protocol> findNotVerified();

    @Query("SELECT * FROM ctrl_pkw.protocol where is_verified = false limit :count")
    public Result<Protocol> findNotVerified(@Param("count") int count);

    @Query("SELECT * FROM ctrl_pkw.protocol where ballot = :ballot")
    public Result<Protocol> findByBallot(@Param("ballot") Ballot ballot);

    @Query("SELECT * FROM ctrl_pkw.protocol_index where id = :id limit 1")
    public ProtocolIndex findById(@Param("id") UUID id);

    @Query("UPDATE ctrl_pkw.protocol SET approvals = approvals + :username, is_verified = true, update_time = dateof(now()) WHERE ward = :ward AND ballot = :ballot AND id = :id")
    public ResultSet addApproval(@Param("ward") Ward ward, @Param("ballot") Ballot ballot, @Param("id") UUID id, @Param("username") Set<String> username);

    @Query("UPDATE ctrl_pkw.protocol SET deprecations = deprecations + :username, is_verified = true, update_time = dateof(now()) WHERE ward = :ward AND ballot = :ballot AND id = :id")
    public ResultSet addDeprecation(@Param("ward") Ward ward, @Param("ballot") Ballot ballot, @Param("id") UUID id, @Param("username") Set<String> username);

    @Query("UPDATE ctrl_pkw.protocol SET is_verified = true, update_time = dateof(now()) WHERE ward = :ward AND ballot = :ballot AND id = :id")
    public ResultSet skipVerification(@Param("ward") Ward ward, @Param("ballot") Ballot ballot, @Param("id") UUID id);

    @Query("UPDATE ctrl_pkw.protocol SET image_ids = image_ids + :imageId, update_time = dateof(now()), cloudinary_cloud_name = :cloudinaryCloudName WHERE ward = :ward AND ballot = :ballot AND id = :id")
    public ResultSet addImageId(@Param("ward") Ward ward, @Param("ballot") Ballot ballot, @Param("id") UUID id, @Param("cloudinaryCloudName") String cloudinaryCloudName, @Param("imageId") Set<UUID> imageId);
}
