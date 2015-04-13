package pl.ctrlpkw.integrationtest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;
import pl.ctrlpkw.api.filter.ClientVersionFilter;

import java.io.IOException;

@Component
public class ClientVersionHeaderInterceptor implements ClientHttpRequestInterceptor {

    @Value("${api.client.requiredVersion}")
    private String clientVersion;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
        requestWrapper.getHeaders().add(ClientVersionFilter.CLIENT_VERSION_HEADER, clientVersion);
        return execution.execute(requestWrapper, body);
    }
}
