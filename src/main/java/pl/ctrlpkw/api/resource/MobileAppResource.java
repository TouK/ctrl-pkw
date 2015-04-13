package pl.ctrlpkw.api.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

@Api("Aplikacja Mobilna")
@Path("/mobileApp")
@Component
public class MobileAppResource {

    @Value("${api.mobileApp.url.android}")
    private URI androidUri;

    @Value("${api.mobileApp.url.ios}")
    private URI iosUri;

    @ApiOperation(value = "Pobranie aktualnej wersji aplikacji mobilnej")
    @GET
    public Response get(@HeaderParam("User-Agent") String userAgent) {
        if (userAgent.toLowerCase().contains("android"))
            return Response.seeOther(androidUri).build();
        else if (userAgent.toLowerCase().contains("ipad") || userAgent.toLowerCase().contains("iphone"))
            return Response.seeOther(iosUri).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

}
