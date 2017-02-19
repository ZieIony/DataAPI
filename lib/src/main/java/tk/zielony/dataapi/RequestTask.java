package tk.zielony.dataapi;

import android.util.Log;

import org.springframework.http.HttpMethod;

public class RequestTask<RequestBodyType, ResponseBodyType> implements Runnable {
    private DataAPI dataAPI;
    private final HttpMethod method;
    private final String endpoint;
    private RequestBodyType requestBody;
    private Class<ResponseBodyType> responseBodyClass;
    private int retry = 0;
    private OnCallFinishedListener<ResponseBodyType> listener;
    private boolean cancelled = false;

    public RequestTask(DataAPI dataAPI, String endpoint, HttpMethod method, RequestBodyType requestBody, Class<ResponseBodyType> responseBodyClass, OnCallFinishedListener<ResponseBodyType> listener) {
        this.dataAPI = dataAPI;
        this.endpoint = endpoint;
        this.method = method;
        this.requestBody = requestBody;
        this.responseBodyClass = responseBodyClass;
        this.listener = listener;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (cancelled)
                return;
        }
        String key = method + ":" + endpoint;
        try {
            Log.i("request", key);
            if (responseBodyClass != Void.class) {
                ResponseBodyType data = dataAPI.executeRequest(endpoint, method, requestBody, responseBodyClass);
                Log.i("success", key + "\n" + DataAPI.toJson(data));
                synchronized (this) {
                    if (listener != null)
                        listener.onSuccess(data);
                }
            } else {
                dataAPI.executeRequest(endpoint, method, requestBody, Void.class);
                Log.i("success", key);
                synchronized (this) {
                    if (listener != null)
                        listener.onSuccess();
                }
            }
        } catch (TimeoutException te) {
            if (retry < dataAPI.getConfiguration().getRetries()) {
                Log.i("retrying", key);
                retry++;
                synchronized (this) {
                    if (listener != null)
                        listener.onRetry();
                    dataAPI.execute(this);
                }
            } else {
                Log.i("timeout", key);
                synchronized (this) {
                    if (listener != null)
                        listener.onTimeout();
                }
            }
        } catch (Exception e) {
            Log.i("error", key, e);
            synchronized (this) {
                if (listener != null)
                    listener.onError(new ApiError(e));
            }
        }
    }

    public void cancel() {
        synchronized (this) {
            cancelled = true;
            listener = null;
        }
    }
}
