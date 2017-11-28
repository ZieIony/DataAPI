package tk.zielony.dataapi;

import org.springframework.http.HttpMethod;

import java.io.Serializable;

class Request<RequestBodyType, ResponseBodyType extends Serializable> {
    private final String endpoint;
    private final HttpMethod method;
    private final RequestBodyType requestBody;
    private final Class<ResponseBodyType> responseBodyClass;

    Request(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        this.endpoint = endpoint;
        this.method = method;
        this.requestBody = requestBody;
        this.responseBodyClass = responseBodyClass;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public RequestBodyType getBody() {
        return requestBody;
    }

    public Class<ResponseBodyType> getResponseBodyClass() {
        return responseBodyClass;
    }

}
