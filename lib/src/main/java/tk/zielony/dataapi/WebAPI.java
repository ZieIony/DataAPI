package tk.zielony.dataapi;

import com.fasterxml.jackson.databind.SerializationFeature;

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
        this(apiUrl, new Configuration());
    }

    public WebAPI(String apiUrl, Configuration configuration) {
        super(configuration);

        this.apiUrl = apiUrl;

        init();
    }

    private void init() {
        SimpleClientHttpRequestFactory s = new SimpleClientHttpRequestFactory();
        s.setConnectTimeout(getConfiguration().getConnectTimeout());
        s.setReadTimeout(getConfiguration().getReadTimeout());
        restTemplate = new RestTemplate(s);
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        restTemplate.getMessageConverters().add(messageConverter);
    }

    public void setHeader(String header, String value) {
        headers.put(header, Collections.singletonList(value));
    }

    public String getUrl() {
        return apiUrl;
    }

    @Override
    protected <RequestBodyType> String executeRequestInternal(String endpoint, HttpMethod method, RequestBodyType requestBody) {
        String url = apiUrl + endpoint;
        HttpEntity<RequestBodyType> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, request, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            if (e instanceof ResourceAccessException && e.getCause() instanceof SocketTimeoutException)
                throw new TimeoutException();
            throw e;
        }
    }
}
