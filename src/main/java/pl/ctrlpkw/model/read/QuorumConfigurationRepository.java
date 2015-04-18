package pl.ctrlpkw.model.read;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuorumConfigurationRepository extends PagingAndSortingRepository<QuorumConfiguration, Long> {

    @Query("select q from QuorumConfiguration q order by q.fromSize desc")
    Iterable<QuorumConfiguration> allOrderByFromSizeDesc();

}
