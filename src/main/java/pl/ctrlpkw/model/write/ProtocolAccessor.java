package pl.ctrlpkw.model.write;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface ProtocolAccessor {
    @Query("SELECT * FROM ctrl_pkw.protocol")
    public Result<Protocol> findByVotingDateAndBallotNo();
}
