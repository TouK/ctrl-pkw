package pl.ctrlpkw.model.read;

import com.vividsolutions.jts.geom.Point;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Collection;

@Entity
@Getter
@Setter
public class Ward {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany(targetEntity = Voting.class)
    private Collection<Voting> votings;

    private String communityCode;
    private Integer wardNo;
    private String wardAddress;
    private Integer votersCount;

    @Type(type="org.hibernate.spatial.GeometryType")
    private Point location;

    private String label;
    private String shortLabel;
}
