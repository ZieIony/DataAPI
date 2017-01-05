package tk.zielony.dataapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.Collections;

public class WebAPI extends DataAPI {

    private String apiUrl;

    private HttpHeaders headers = new HttpHeaders();
    private RestTemplate restTemplate;

    public WebAPI(String apiUrl) {
        this.apiUrl = apiUrl;

        init();
    }

    public WebAPI(String apiUrl, int coreThreads, int connectTimeout, int readTimeout, int retries) {
        super(coreThreads, connectTimeout, readTimeout, retries);

        this.apiUrl = apiUrl;

        init();
    }

    private void init() {
        SimpleClientHttpRequestFactory s = new SimpleClientHttpRequestFactory();
        s.setConnectTimeout(getConnectTimeout());
        s.setReadTimeout(getReadTimeout());
        restTemplate = new RestTemplate(s);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public void setHeader(String header, String value) {
        headers.put(header, Collections.singletonList(value));
    }

    public String getUrl() {
        return apiUrl;
    }

    protected <RequestBodyType, ResponseBodyType> ResponseBodyType executeRequest(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        String url = apiUrl + endpoint;
        HttpEntity<RequestBodyType> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<ResponseBodyType> responseEntity = restTemplate.exchange(url, method, request, responseBodyClass);
            return responseEntity.getBody();
        } catch (Exception e) {
            if (e instanceof ResourceAccessException && e.getCause() instanceof SocketTimeoutException)
                throw new TimeoutException();
            throw e;
        }
    }
}
