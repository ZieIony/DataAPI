package tk.zielony.dataapi;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Marcin on 2016-04-29.
 */
public abstract class DataAPI<User> {

    private static ObjectMapper mapper = new ObjectMapper();

    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private Thread networkThread = new Thread() {
        @Override
        public void run() {
            setDaemon(true);
            while (true) {
                Runnable r;
                synchronized (taskQueue) {
                    try {
                        while (taskQueue.isEmpty())
                            taskQueue.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                    r = taskQueue.remove();
                    taskQueue.notify();
                }
                r.run();
            }
        }
    };

    protected User currentUser;

    public DataAPI() {
        networkThread.start();
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <Type> Type fromJson(String json, Class<Type> klass) {
        try {
            return mapper.readValue(json, klass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void addTask(Runnable runnable) {
        synchronized (taskQueue) {
            taskQueue.add(runnable);
            taskQueue.notify();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public <Type> void getAsync(final String endpoint, final Class<Type> dataClass, final OnCallFinishedListener<Type> listener) {
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onSuccess(getInternal(endpoint, dataClass));
                } catch (APIException e) {
                    listener.onError(e);
                }
            }
        });
    }

    public <Type> Type get(final String endpoint, Class<Type> dataClass) throws APIException {
        return getInternal(endpoint, dataClass);

    }

    protected abstract <Type2> Type2 getInternal(final String endpoint, Class<Type2> dataClass) throws APIException;

    public <Type> void putAsync(final String endpoint, final Type param, @Nullable final OnCallFinishedListener<Type> listener) {
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Type data = putInternal(endpoint, param);
                    if (listener != null)
                        listener.onSuccess(data);
                } catch (APIException e) {
                    if (listener != null)
                        listener.onError(e);
                }
            }
        });
    }

    public <Type> Type put(final String endpoint, final Type param) throws APIException {
        return putInternal(endpoint, param);
    }

    public abstract <Type> Type putInternal(final String endpoint, final Type param) throws APIException;

    public <Type> void deleteAsync(final String endpoint, @Nullable final OnCallFinishedListener<Type> listener) {
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteInternal(endpoint);
                    if (listener != null)
                        listener.onSuccess(null);
                } catch (APIException e) {
                    if (listener != null)
                        listener.onError(e);
                }
            }
        });
    }

    public void delete(final String endpoint) throws APIException {
        deleteInternal(endpoint);
    }

    protected abstract void deleteInternal(final String endpoint) throws APIException;

    public <Type, Type2> void postAsync(final String endpoint, final Type param, @Nullable final OnCallFinishedListener<Type2> listener) {
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Type2 data = postInternal(endpoint, param);
                    if (listener != null)
                        listener.onSuccess(data);
                } catch (APIException e) {
                    if (listener != null)
                        listener.onError(e);
                }
            }
        });
    }

    public <Type, Type2> Type2 post(final String endpoint, final Type param) throws APIException {
        return postInternal(endpoint, param);
    }

    protected abstract <Type, Type2> Type2 postInternal(final String endpoint, Type param) throws APIException;

    public void signupAsync(final User user) {
        addTask(new Runnable() {
            @Override
            public void run() {
                signupInternal(user);
            }
        });
    }

    public void signup(final User user) {
        signupInternal(user);
    }

    protected abstract void signupInternal(User user);

    public void loginAsync(final String email, final String pass) {
        addTask(new Runnable() {
            @Override
            public void run() {
                loginInternal(email, pass);
            }
        });
    }

    public void login(String email, String pass) {
        loginInternal(email, pass);
    }

    protected abstract void loginInternal(String email, String pass);

    public String saveBitmap(Bitmap bitmap) {
        return saveBitmapInternal(bitmap);
    }

    protected abstract String saveBitmapInternal(Bitmap bitmap);

    public Bitmap loadBitmap(String url) {
        return loadBitmapInternal(url);
    }

    protected abstract Bitmap loadBitmapInternal(String url);
}
