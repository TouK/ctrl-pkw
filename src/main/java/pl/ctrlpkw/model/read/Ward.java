package pl.ctrlpkw.model.read;

import com.vividsolutions.jts.geom.Point;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class Ward {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity = Voting.class, optional = false)
    private Voting voting;

    private String communityCode;
    private Integer wardNo;
    private String wardAddress;

    @Type(type="org.hibernate.spatial.GeometryType")
    private Point location;

    private String label;

}
