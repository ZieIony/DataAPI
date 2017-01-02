package tk.zielony.dataapi;

import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public abstract class DataAPI {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <RequestType> RequestType fromJson(String json, Class<RequestType> klass) {
        try {
            return mapper.readValue(json, klass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void execute(final Runnable runnable) {
        new Thread() {
            @Override
            public void run() {
                runnable.run();
            }
        }.start();
    }

    // GET

    public void getAsync(final String endpoint) {
        getAsync(endpoint, String.class, null);
    }

    public <ResponseType> void getAsync(final String endpoint, final Class<ResponseType> responseClass) {
        getAsync(endpoint, responseClass, null);
    }

    public void getAsync(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        getAsync(endpoint, String.class, listener);
    }

    public <ResponseType> void getAsync(final String endpoint, final Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        execute(() -> {
            try {
                Log.i("request", "GET: " + endpoint);
                ResponseType data = getInternal(endpoint, responseClass);
                Log.i("success", "GET: " + endpoint + "\n" + toJson(data));
                if (listener != null)
                    listener.onSuccess(data);
            } catch (Exception e) {
                Log.i("error", "GET: " + endpoint, e);
                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public String get(final String endpoint) throws Exception {
        return get(endpoint, String.class);
    }

    public <ResponseType> ResponseType get(final String endpoint, Class<ResponseType> responseClass) throws Exception {
        return getInternal(endpoint, responseClass);
    }

    protected abstract <ResponseType> ResponseType getInternal(final String endpoint, Class<ResponseType> responseClass) throws Exception;

    // PUT

    public <RequestType> void putAsync(final String endpoint, final RequestType requestBody) {
        putAsync(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> void putAsync(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        putAsync(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> void putAsync(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        putAsync(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> void putAsync(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        execute(() -> {
            try {
                Log.i("request", "PUT: " + endpoint + "\n" + toJson(requestBody));
                putInternal(endpoint, requestBody, responseClass);
                Log.i("success", "PUT: " + endpoint);
                if (listener != null)
                    listener.onSuccess();
            } catch (Exception e) {
                Log.i("error", "PUT: " + endpoint, e);
                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public <RequestType> String put(final String endpoint, final RequestType requestBody) throws Exception {
        return put(endpoint, requestBody, String.class);
    }

    public <RequestType, ResponseType> ResponseType put(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) throws Exception {
        return putInternal(endpoint, requestBody, responseClass);
    }

    protected abstract <RequestType, ResponseType> ResponseType putInternal(String endpoint, RequestType requestBody, Class<ResponseType> responseClass) throws Exception;

    // DELETE

    public void deleteAsync(final String endpoint) {
        deleteAsync(endpoint, String.class, null);
    }

    public <ResponseType> void deleteAsync(final String endpoint, Class<ResponseType> responseClass) {
        deleteAsync(endpoint, responseClass, null);
    }

    public void deleteAsync(final String endpoint, @Nullable final OnCallFinishedListener<String> listener) {
        deleteAsync(endpoint, String.class, listener);
    }

    public <ResponseType> void deleteAsync(final String endpoint, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        execute(() -> {
            try {
                Log.i("request", "DELETE: " + endpoint);
                deleteInternal(endpoint, responseClass);
                Log.i("success", "DELETE: " + endpoint);
                if (listener != null)
                    listener.onSuccess(null);
            } catch (Exception e) {
                Log.i("error", "DELETE: " + endpoint, e);
                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public String delete(final String endpoint) throws Exception {
        return delete(endpoint, String.class);
    }

    public <ResponseType> ResponseType delete(final String endpoint, Class<ResponseType> responseClass) throws Exception {
        return deleteInternal(endpoint, responseClass);
    }

    protected abstract <ResponseType> ResponseType deleteInternal(String endpoint, Class<ResponseType> responseClass) throws Exception;

    // POST

    public <RequestType> void postAsync(final String endpoint, final RequestType requestBody) {
        postAsync(endpoint, requestBody, String.class, null);
    }

    public <RequestType, ResponseType> void postAsync(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) {
        postAsync(endpoint, requestBody, responseClass, null);
    }

    public <RequestType> void postAsync(final String endpoint, final RequestType requestBody, @Nullable final OnCallFinishedListener<String> listener) {
        postAsync(endpoint, requestBody, String.class, listener);
    }

    public <RequestType, ResponseType> void postAsync(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass, @Nullable final OnCallFinishedListener<ResponseType> listener) {
        execute(() -> {
            try {
                Log.i("request", "POST: " + endpoint + "\n" + toJson(requestBody));
                ResponseType data = postInternal(endpoint, requestBody, responseClass);
                Log.i("success", "POST: " + endpoint + "\n" + toJson(data));
                if (listener != null)
                    listener.onSuccess(data);
            } catch (Exception e) {
                Log.i("error", "POST: " + endpoint, e);
                if (listener != null)
                    listener.onError(e);
            }
        });
    }

    public <RequestType> String post(final String endpoint, final RequestType requestBody) throws Exception {
        return post(endpoint, requestBody, String.class);
    }

    public <RequestType, ResponseType> ResponseType post(final String endpoint, final RequestType requestBody, Class<ResponseType> responseClass) throws Exception {
        return postInternal(endpoint, requestBody, responseClass);
    }

    protected abstract <RequestType, ResponseType> ResponseType postInternal(final String endpoint, RequestType requestBody, Class<ResponseType> responseClass) throws Exception;
}
