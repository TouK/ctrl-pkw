package pl.ctrlpkw.model.write;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.CassandraContext;

import javax.annotation.Resource;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProtocolAccessorFactory extends AbstractFactoryBean<ProtocolAccessor> {

    @Resource
    private CassandraContext cassandraContext;

    @Override
    public Class<?> getObjectType() {
        return ProtocolAccessor.class;
    }

    @Override
    protected ProtocolAccessor createInstance() throws Exception {
        return cassandraContext.getMappingManager().createAccessor(ProtocolAccessor.class);
    }
}
