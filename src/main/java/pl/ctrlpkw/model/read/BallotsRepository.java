package pl.ctrlpkw.model.read;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BallotsRepository extends CrudRepository<Ballot, Long> {

    @Query("select b from Ballot b join b.voting v where v.date = :date")
    Iterable<Ballot> findByDate(@Param("date") LocalDate date);


    @Query("select b from Ballot b join b.voting v where v.date = :date and b.no = :no")
    Ballot findByDateAndNo(@Param("date") LocalDate date, @Param("no") Integer no);

}
