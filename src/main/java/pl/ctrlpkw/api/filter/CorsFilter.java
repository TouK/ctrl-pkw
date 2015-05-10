package pl.ctrlpkw.api.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        String allowedOrigin = Optional.ofNullable(requestContext.getHeaderString("origin"))
                .orElse(Optional.ofNullable(requestContext.getHeaderString("referer"))
                                .map(refererHeader -> URI.create(refererHeader).getHost())
                                .orElse("*")
                );
            String allowedHeaders = String.join(", ", requestContext.getHeaders().keySet()) + ", " + requestContext.getHeaderString("Access-Control-Request-Headers");

        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", allowedHeaders);
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
    }
}
