package tk.zielony.dataapi;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.http.HttpMethod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class DataAPI {

    protected static final String SAVED_RESPONSES_FILENAME = "cache";

    protected static ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private ScheduledExecutorService executor;
    private Configuration configuration;

    protected Map<String, CacheEntry> cache = new HashMap<>();
    protected DataOutputStream responseOutputStream;

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
            Log.w("from json", e);
            return null;
        }
    }

    public DataAPI() {
        this(new Configuration());
    }

    public DataAPI(Configuration configuration) {
        this.configuration = configuration;
        executor = Executors.newScheduledThreadPool(configuration.getCoreThreads());
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void saveCache(Context context) {
        try {
            responseOutputStream = new DataOutputStream(new FileOutputStream(new File(context.getFilesDir(), SAVED_RESPONSES_FILENAME)));
            for (Map.Entry<String, CacheEntry> entry : cache.entrySet())
                entry.getValue().write(responseOutputStream);
            responseOutputStream.close();
            responseOutputStream = null;
        } catch (IOException e) {
        }
    }

    public void loadCache(Context context) {
        try {
            DataInputStream responseInputStream = new DataInputStream(new FileInputStream(new File(context.getFilesDir(), SAVED_RESPONSES_FILENAME)));
            while (true) {
                String key = responseInputStream.readUTF();
                CacheEntry cacheEntry = new CacheEntry();
                cacheEntry.read(responseInputStream);
                cache.put(key, cacheEntry);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // GET

    public RequestTask<Void, String> get(final String endpoint) {
        return get(endpoint, String.class, null);
    }

    public <ResponseType> RequestTask<Void, ResponseType> get(final String endpoint, final Class<ResponseType> responseClass) {
        return get(endpoint, responseClass, null);
    }

    public RequestTask<Void, String> get(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        return get(endpoint, String.class, listener);
    }

    public <ResponseType> RequestTask<Void, ResponseType> get(final String endpoint, final Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        RequestTask<Void, ResponseType> task = new RequestTask<>(this, endpoint, HttpMethod.GET, null, responseClass, listener);
        executor.execute(task);
        return task;
    }

    // PUT

    public <RequestType> RequestTask<RequestType, String> put(final String endpoint, final RequestType requestBody) {
        return put(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> RequestTask<RequestType, ResponseType> put(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        return put(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> RequestTask<RequestType, String> put(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        return put(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> RequestTask<RequestType, ResponseType> put(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        RequestTask<RequestType, ResponseType> task = new RequestTask<>(this, endpoint, HttpMethod.PUT, requestBody, responseClass, listener);
        executor.execute(task);
        return task;
    }

    // DELETE

    public RequestTask<Object, String> delete(final String endpoint) {
        return delete(endpoint, String.class, null);
    }

    public <ResponseType> RequestTask<Object, ResponseType> delete(final String endpoint, Class<ResponseType> responseClass) {
        return delete(endpoint, responseClass, null);
    }

    public RequestTask<Object, String> delete(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        return delete(endpoint, String.class, listener);
    }

    public <ResponseType> RequestTask<Object, ResponseType> delete(final String endpoint, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        RequestTask<Object, ResponseType> task = new RequestTask<>(this, endpoint, HttpMethod.DELETE, null, responseClass, listener);
        executor.execute(task);
        return task;
    }

    // POST

    public <RequestType> RequestTask<RequestType, String> post(final String endpoint, final RequestType requestBody) {
        return post(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> RequestTask<RequestType, ResponseType> post(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        return post(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> RequestTask<RequestType, String> post(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        return post(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> RequestTask<RequestType, ResponseType> post(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        RequestTask<RequestType, ResponseType> task = new RequestTask<>(this, endpoint, HttpMethod.POST, requestBody, responseClass, listener);
        executor.execute(task);
        return task;
    }

    protected <RequestBodyType, ResponseBodyType> ResponseBodyType executeRequest(String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass) {
        String key = endpoint + method;
        CacheEntry cacheEntry = cache.get(key);
        if (cacheEntry != null) {
            if (cacheEntry.time - System.currentTimeMillis() < configuration.getCacheTimeout())
                return fromJson(cacheEntry.response, responseBodyClass);
            if (configuration.getCacheStrategy() != CacheStrategy.DEMO)
                cache.remove(key);
        }
        String response = executeRequestInternal(endpoint, method, requestBody);
        if (configuration.getCacheStrategy() != CacheStrategy.NONE && !cache.containsKey(key))
            cache.put(key, new CacheEntry(endpoint, method, response));
        return fromJson(response, responseBodyClass);
    }

    protected abstract <RequestBodyType> String executeRequestInternal(String endpoint, HttpMethod method, RequestBodyType requestBody);

    <RequestBodyType, ResponseBodyType> void execute(RequestTask<RequestBodyType, ResponseBodyType> task) {
        executor.execute(task);
    }

    public void clearCache() {
        cache.clear();
    }

    public void clearCache(String endpoint, HttpMethod method) {
        cache.remove(endpoint + method);
    }
}
