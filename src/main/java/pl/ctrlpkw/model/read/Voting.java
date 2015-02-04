package pl.ctrlpkw.model.read;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
public class Voting {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "voting_date", nullable = false, unique = true)
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate date;

    private String description;

    @OneToMany(targetEntity = Ballot.class, mappedBy = "voting")
    private Collection<Ballot> ballots;

}
