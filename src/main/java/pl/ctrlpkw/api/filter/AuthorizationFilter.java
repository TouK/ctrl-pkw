package pl.ctrlpkw.api.filter;

import com.stormpath.sdk.api.ApiAuthenticationResult;
import com.stormpath.sdk.application.Application;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@AuthorizationRequired
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest servletRequest;

    @Autowired
    private Application stormpathApplication;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ApiAuthenticationResult apiAuthenticationResult = stormpathApplication.authenticateApiRequest(servletRequest);
    }
}
