package pl.ctrlpkw;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.ObjectMapperProvider;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages("pl.ctrlpkw.api.resource");
        packages("pl.ctrlpkw.api.filter");
        packages("com.wordnik.swagger.jersey.listing");
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);

        //Declarative linking need the patched version of Jersey
        register(DeclarativeLinkingFeature.class);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
    }
}
