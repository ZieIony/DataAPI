package tk.zielony.dataapi;

import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class DataAPI {

    private static final int DEFAULT_CORE_THREADS = 2;
    private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    private static final int DEFAULT_RETRIES = 3;
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private static ObjectMapper mapper = new ObjectMapper();

    private int coreThreads;
    private int connectTimeout;
    private int readTimeout;
    private int retries;

    private ScheduledExecutorService executor;

    private class Task<RequestBodyType, ResponseBodyType> implements Runnable {
        private final HttpMethod method;
        private final String endpoint;
        RequestBodyType requestBody;
        private Class<ResponseBodyType> responseBodyClass;
        private int retry = 0;
        private OnCallFinishedListener<ResponseBodyType> listener;

        public Task(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass, OnCallFinishedListener<ResponseBodyType> listener) {
            this.endpoint = endpoint;
            this.method = method;
            this.requestBody = requestBody;
            this.responseBodyClass = responseBodyClass;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                Log.i("request", method + ": " + endpoint);
                if (responseBodyClass != Void.class) {
                    ResponseBodyType data = executeRequest(endpoint, method, requestBody, responseBodyClass);
                    Log.i("success", method + ": " + endpoint + "\n" + toJson(data));
                    if (listener != null)
                        listener.onSuccess(data);
                } else {
                    executeRequest(endpoint, method, requestBody, Void.class);
                    Log.i("success", method + ": " + endpoint);
                    if (listener != null)
                        listener.onSuccess();
                }
            } catch (TimeoutException te) {
                if (retry < retries) {
                    Log.i("retrying", method + ": " + endpoint);
                    retry++;
                    if (listener != null)
                        listener.onRetry();
                    executor.execute(this);
                } else {
                    Log.i("timeout", method + ": " + endpoint);
                    if (listener != null)
                        listener.onTimeout();
                }
            } catch (Exception e) {
                Log.i("error", method + ": " + endpoint, e);
                if (listener != null)
                    listener.onError(e);
            }
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            Log.w("to json", e);
            return null;
        }
    }

    public static <RequestType> RequestType fromJson(String json, Class<RequestType> klass) {
        try {
            return mapper.readValue(json, klass);
        } catch (IOException e) {
            Log.w("to json", e);
            return null;
        }
    }

    public DataAPI() {
        this(DEFAULT_CORE_THREADS, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_RETRIES);
    }

    public DataAPI(int coreThreads, int connectTimeout, int readTimeout, int retries) {
        executor = Executors.newScheduledThreadPool(coreThreads);
        this.coreThreads = coreThreads;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retries = retries;
    }

    public int getCoreThreads() {
        return coreThreads;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getRetries() {
        return retries;
    }

    // GET

    public void get(final String endpoint) {
        get(endpoint, String.class, null);
    }

    public <ResponseType> void get(final String endpoint, final Class<ResponseType> responseClass) {
        get(endpoint, responseClass, null);
    }

    public void get(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        get(endpoint, String.class, listener);
    }

    public <ResponseType> void get(final String endpoint, final Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        executor.execute(new Task<Void, ResponseType>(endpoint, HttpMethod.GET, null, responseClass, listener));
    }

    // PUT

    public <RequestType> void put(final String endpoint, final RequestType requestBody) {
        put(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> void put(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        put(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> void put(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        put(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> void put(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        executor.execute(new Task<>(endpoint, HttpMethod.PUT, requestBody, responseClass, listener));
    }

    // DELETE

    public void delete(final String endpoint) {
        delete(endpoint, String.class, null);
    }

    public <ResponseType> void delete(final String endpoint, Class<ResponseType> responseClass) {
        delete(endpoint, responseClass, null);
    }

    public void delete(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        delete(endpoint, String.class, listener);
    }

    public <ResponseType> void delete(final String endpoint, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        executor.execute(new Task<>(endpoint, HttpMethod.DELETE, null, responseClass, listener));
    }

    // POST

    public <RequestType> void post(final String endpoint, final RequestType requestBody) {
        post(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> void post(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        post(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> void post(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        post(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> void post(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        executor.execute(new Task<>(endpoint, HttpMethod.POST, requestBody, responseClass, listener));
    }

    protected abstract <RequestBodyType, ResponseBodyType> ResponseBodyType executeRequest(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass);
}
