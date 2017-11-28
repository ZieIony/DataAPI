package tk.zielony.dataapi;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.http.HttpMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class DataAPI {

    protected static final String SAVED_RESPONSES_FILENAME = "cache";

    protected static ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private ScheduledExecutorService executor;
    private Handler handler = new Handler();
    private Configuration configuration;

    protected Map<String, Response> cache = new HashMap<>();
    private CompositeDisposable disposables = new CompositeDisposable();

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
            ObjectOutputStream responseOutputStream = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(), SAVED_RESPONSES_FILENAME)));
            for (Map.Entry<String, Response> entry : cache.entrySet())
                entry.getValue().write(responseOutputStream);
            responseOutputStream.close();
        } catch (IOException e) {
        }
    }

    public void loadCache(Context context) {
        try {
            ObjectInputStream responseInputStream = new ObjectInputStream(new FileInputStream(new File(context.getFilesDir(), SAVED_RESPONSES_FILENAME)));
            while (true) {
                String key = responseInputStream.readUTF();
                Response cacheEntry = new Response();
                cacheEntry.read(responseInputStream);
                cache.put(key, cacheEntry);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void runOnUIThread(Runnable runnable) {
        handler.post(runnable);
    }

    public <ResponseBodyType extends Serializable> Observable<Response<ResponseBodyType>> get(final String endpoint, final Class<ResponseBodyType> responseClass) {
        return reactiveRequest(new Request<>(endpoint, HttpMethod.GET, null, responseClass));
    }

    public <RequestBodyType, ResponseBodyType extends Serializable> Observable<Response<ResponseBodyType>> put(final String endpoint, final RequestBodyType requestBody, Class<ResponseBodyType> responseClass) {
        return reactiveRequest(new Request<>(endpoint, HttpMethod.PUT, requestBody, responseClass));
    }

    public <ResponseBodyType extends Serializable> Observable<Response<ResponseBodyType>> delete(final String endpoint, Class<ResponseBodyType> responseClass) {
        return reactiveRequest(new Request<>(endpoint, HttpMethod.DELETE, null, responseClass));
    }

    public <RequestBodyType, ResponseBodyType extends Serializable> Observable<Response<ResponseBodyType>> post(final String endpoint, final RequestBodyType requestBody, Class<ResponseBodyType> responseClass) {
        return reactiveRequest(new Request<>(endpoint, HttpMethod.POST, requestBody, responseClass));
    }

    protected <RequestBodyType, ResponseBodyType extends Serializable> Response<ResponseBodyType> executeRequest(Request<RequestBodyType, ResponseBodyType> request) {
        String key = request.getMethod() + request.getEndpoint();
        Response<ResponseBodyType> response = cache.get(key);
        if (response != null) {
            if (response.getTime() - System.currentTimeMillis() < configuration.getCacheTimeout())
                return response;
            if (configuration.getCacheStrategy() != CacheStrategy.DEMO)
                cache.remove(key);
        }
        response = executeRequestInternal(request);
        if (configuration.getCacheStrategy() != CacheStrategy.NONE && response.isSuccess() && !cache.containsKey(key))
            cache.put(key, response);
        return response;
    }

    private <RequestBodyType, ResponseBodyType extends Serializable> Observable<Response<ResponseBodyType>> reactiveRequest(Request<RequestBodyType, ResponseBodyType> request) {
        return Observable.create(new RequestObservable<>(this, request))
                .subscribeOn(Schedulers.from(executor))
                .retryWhen(new RetryObservable(configuration.getRetries()));
    }

    protected abstract <RequestBodyType, ResponseBodyType extends Serializable> Response<ResponseBodyType> executeRequestInternal(Request<RequestBodyType, ResponseBodyType> request);

    public void clearCache() {
        cache.clear();
    }

    public void clearCache(String endpoint, HttpMethod method) {
        cache.remove(endpoint + method);
    }

    public void cancelRequests() {
        disposables.clear();
    }
}
