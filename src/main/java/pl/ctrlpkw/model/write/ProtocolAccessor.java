package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

import java.util.UUID;

@Accessor
public interface ProtocolAccessor {

    @Query("SELECT * FROM ctrl_pkw.protocol where is_verified = false limit :count")
    public Result<Protocol> findNotVerified(@Param("count") int count);

    @Query("SELECT * FROM ctrl_pkw.protocol where ballot = :ballot")
    public Result<Protocol> findByBallot(@Param("ballot") Ballot ballot);

    @Query("SELECT * FROM ctrl_pkw.protocol where id = :id limit 1")
    public Protocol findById(@Param("id") UUID id);

}
