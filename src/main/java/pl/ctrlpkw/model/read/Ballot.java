package pl.ctrlpkw.model.read;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"voting_id", "no"}))
@Getter
@Setter
public class Ballot {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity = Voting.class, optional = false)
    private Voting voting;

    @Column(nullable = false)
    private Integer no;

    @Column(nullable = false)
    private String question;

    @OneToMany(targetEntity = BallotOption.class, mappedBy = "ballot")
    @OrderBy("no")
    private List<BallotOption> options;

}
