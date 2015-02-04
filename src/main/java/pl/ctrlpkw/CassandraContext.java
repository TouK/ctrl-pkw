package pl.ctrlpkw;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CassandraContext implements InitializingBean, DisposableBean {

    @Getter
    private Cluster cluster;

    @Getter
    private Session session;

    @Getter
    private MappingManager mappingManager;

    @Value("${cassandra.contactPoint}")
    private String contactPoint;

    @Override
    public void afterPropertiesSet() throws Exception {
        cluster = Cluster.builder().addContactPoint(contactPoint).build();
        session = cluster.connect();
        mappingManager = new MappingManager(session);
    }

    @Override
    public void destroy() throws Exception {
        session.close();
        cluster.close();
    }
}
