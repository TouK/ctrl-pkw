package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface ProtocolAccessor {
    @Query("SELECT * FROM ctrl_pkw.protocol where ballot = :ballot")
    public Result<Protocol> findByBallot(@Param("ballot") Ballot ballot);
}
