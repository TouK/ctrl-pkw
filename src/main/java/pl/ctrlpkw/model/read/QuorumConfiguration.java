package pl.ctrlpkw.model.read;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table
public class QuorumConfiguration implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long fromSize;

    @Column(nullable = false)
    private Integer percent;

}
