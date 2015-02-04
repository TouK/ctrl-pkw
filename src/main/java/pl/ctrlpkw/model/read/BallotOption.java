package pl.ctrlpkw.model.read;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"ballot_id", "no"}))
public class BallotOption implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity = Ballot.class, optional = false)
    private Ballot ballot;

    @Column(nullable = false)
    private Integer no;

    @Column(nullable = false)
    private String description;

}
