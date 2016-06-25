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

    protected <Type2> Type2 getInternal(final String endpoint, Class<Type2> dataClass) throws APIException {
        throw new RuntimeException("Not implemented");
    }

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

    public <Type> Type putInternal(final String endpoint, final Type param) throws APIException {
        throw new RuntimeException("Not implemented");
    }

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

    protected void deleteInternal(final String endpoint) throws APIException {
        throw new RuntimeException("Not implemented");
    }

    public <Type, Type2> void postAsync(final String endpoint, final Type param, final Class<Type2> dataClass, @Nullable final OnCallFinishedListener<Type2> listener) {
        addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Type2 data = postInternal(endpoint, param, dataClass);
                    if (listener != null)
                        listener.onSuccess(data);
                } catch (APIException e) {
                    if (listener != null)
                        listener.onError(e);
                }
            }
        });
    }

    public <Type, Type2> Type2 post(final String endpoint, final Type param, Class<Type2> dataClass) throws APIException {
        return postInternal(endpoint, param, dataClass);
    }

    protected <Type, Type2> Type2 postInternal(final String endpoint, Type param, Class<Type2> dataClass) throws APIException {
        throw new RuntimeException("Not implemented");
    }

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

    protected void signupInternal(User user) {
        throw new RuntimeException("Not implemented");
    }

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

    protected void loginInternal(String email, String pass) {
        throw new RuntimeException("Not implemented");
    }

    public String saveObject(Object object) {
        return saveObjectInternal(object);
    }

    protected String saveObjectInternal(Object object) {
        throw new RuntimeException("Not implemented");
    }

    public Object loadObject(String url) {
        return loadObjectInternal(url);
    }

    protected Object loadObjectInternal(String url) {
        throw new RuntimeException("Not implemented");
    }
}
