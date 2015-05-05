package pl.ctrlpkw.api.filter;

import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.servlet.account.AccountResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@AuthorizationRequired
@Slf4j
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest servletRequest;

    @Autowired(required = false)
    Application stormpathApplication;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (stormpathApplication == null) {
            log.warn("Authorization is disabled. This should happen only while testing");
        } else if (!AccountResolver.INSTANCE.hasAccount(servletRequest)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
