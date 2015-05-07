package pl.ctrlpkw.model.read;

import com.vividsolutions.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface WardRepository extends PagingAndSortingRepository<Ward, Long> {

    @Query("select w, distance_sphere(w.location, :point) from Ward w join w.votings v " +
            "where w.location is not null and v = :voting  and distance_sphere(w.location, :point) is not null " +
            "order by distance_sphere(w.location, :point)")
    Iterable<Object[]> findAllOrderedByDistanceReturnWithDistance(@Param("voting") Voting voting, @Param("point") Point point);

    @Query("select w from Ward w join w.votings v " +
            "where w.location is not null and v = :voting  and distance_sphere(w.location, :point) is not null " +
            "order by distance_sphere(w.location, :point)")
    Page<Ward> findOrderedByDistance(@Param("voting") Voting voting, @Param("point") Point point, Pageable pageable);

    @Query("select w, distance_sphere(w.location, :point) from Ward w join w.votings v " +
            "where w.location is not null and v = :voting  and distance_sphere(w.location, :point) is not null " +
            "order by distance_sphere(w.location, :point)")
    Page<Object[]> findOrderedByDistanceReturnWithDistance(@Param("voting") Voting voting, @Param("point") Point point, Pageable pageable);

    @Query("select w from Ward w join w.votings v " +
            "where w.location is not null and v = :voting and distance_sphere(w.location, :point) <= :radius " +
            "order by distance_sphere(w.location, :point)")
    Collection<Ward> findWithinRadiusAndOrderByDistance(@Param("voting") Voting voting, @Param("point") Point point, @Param("radius") double radius);

    @Query("select w from Ward w join w.votings v " +
            "where w.location is not null and v = :voting and distance_sphere(w.location, :point) <= :radius " +
            "order by distance_sphere(w.location, :point)")
    Page<Ward> findWithinRadiusAndOrderByDistance(@Param("voting") Voting voting, @Param("point") Point point, @Param("radius") double radius, Pageable pageable);

    @Query("select w from Ward w join w.votings v " +
            "where w.location is not null and v = :voting and distance_sphere(w.location, :point) <= distance_sphere(:distantPoint, :point) " +
            "order by distance_sphere(w.location, :point)")
    Collection<Ward> findCloserThanAndOrderByDistance(@Param("voting") Voting voting, @Param("point") Point point, @Param("distantPoint") Point distantPoint);

    Ward findByVotingsAndCommunityCodeAndWardNo(Voting voting, String communityCode, Integer wardNo);
}
