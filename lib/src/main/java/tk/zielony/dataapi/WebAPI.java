package tk.zielony.dataapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class WebAPI extends DataAPI {

    private String apiUrl;

    private HttpHeaders headers = new HttpHeaders();

    public WebAPI(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public void setHeader(String header, String value) {
        headers.put(header, Collections.singletonList(value));
    }

    public String getUrl() {
        return apiUrl;
    }

    private <RequestBodyType, ResponseBodyType> ResponseBodyType executeRequest(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        String url = apiUrl + endpoint;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        HttpEntity<RequestBodyType> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<ResponseBodyType> responseEntity = restTemplate.exchange(url, method, request, responseBodyClass);
        return responseEntity.getBody();
    }

    protected <ResponseBodyType> ResponseBodyType getInternal(final String endpoint, Class<ResponseBodyType> responseBodyClass) {
        return executeRequest(endpoint, HttpMethod.GET, "", responseBodyClass);
    }

    @Override
    protected <RequestBodyType, ResponseBodyType> ResponseBodyType putInternal(String endpoint, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        return executeRequest(endpoint, HttpMethod.PUT, requestBody, responseBodyClass);
    }

    @Override
    protected <ResponseBodyType> ResponseBodyType deleteInternal(String endpoint, Class<ResponseBodyType> responseBodyClass) {
        return executeRequest(endpoint, HttpMethod.DELETE, "", responseBodyClass);
    }

    @Override
    protected <RequestBodyType, ResponseBodyType> ResponseBodyType postInternal(String endpoint, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        return executeRequest(endpoint, HttpMethod.POST, requestBody, responseBodyClass);
    }
}
