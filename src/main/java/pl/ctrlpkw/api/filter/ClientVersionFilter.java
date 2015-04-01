package pl.ctrlpkw.api.filter;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ctrlpkw.api.resource.MobileAppResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Provider
@ClientVersionCheck
public class ClientVersionFilter implements ContainerRequestFilter {

    public static final String CLIENT_VERSION_HEADER = "Ctrl-PKW-Client-Version";

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest servletRequest;

    @Autowired
    private ComparableVersion requiredClientVersion;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ComparableVersion version = new ComparableVersion(
                Optional.ofNullable(requestContext.getHeaderString(CLIENT_VERSION_HEADER)).orElse("0.0.1")
        );
        if (requiredClientVersion.compareTo(version) > 0) {
            URI redirectionUri = UriBuilder.fromPath(this.servletRequest.getContextPath() + servletRequest.getServletPath())
                    .scheme(servletRequest.getScheme())
                    .host(servletRequest.getServerName())
                    .port(servletRequest.getServerPort())
                    .path(MobileAppResource.class)
                    .build();
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .location(redirectionUri)
                            .entity("Client upgrade required. See Location header.")
                            .build()
            );
        }
    }
}
