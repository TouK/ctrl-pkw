package pl.ctrlpkw.model.read;

import org.joda.time.LocalDate;
import org.springframework.data.repository.CrudRepository;

public interface VotingRepository extends CrudRepository<Voting, Long> {
    Voting findByDate(LocalDate date);
}
