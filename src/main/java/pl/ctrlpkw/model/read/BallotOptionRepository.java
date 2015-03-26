package pl.ctrlpkw.model.read;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface BallotOptionRepository extends PagingAndSortingRepository<BallotOption, Long> {

    @Query("select count(b) from BallotOption o join o.ballot b join b.voting v where v.date = :date and b.no = :no")
    long countByDateAndNo(@Param("date") LocalDate date, @Param("no") Integer no);
}
