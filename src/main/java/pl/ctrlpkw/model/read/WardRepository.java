package pl.ctrlpkw.model.read;

import com.vividsolutions.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface WardRepository extends PagingAndSortingRepository<Ward, Long> {

    @Query("select w from Ward w where w.location is not null and w.voting = :voting and distance(w.location, :point) is not null order by distance(w.location, :point)")
    Page<Ward> findOrderedByDistance(@Param("voting") Voting voting, @Param("point") Point point, Pageable pageable);

    Iterable<Ward> findByCommunityCode(String communityCode);

}
