package pl.ctrlpkw.model.read;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface VotingRepository extends CrudRepository<Voting, Long> {
    Voting findByDate(LocalDate date);

    @Query("select v from Voting v where v.date in :dates")
    Iterable<Voting> findByDate(@Param("dates")Iterable<LocalDate> dates);
}
