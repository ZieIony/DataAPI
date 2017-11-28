package tk.zielony.dataapi;

public class Configuration {
    private static final int DEFAULT_CORE_THREADS = 2;
    private static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    private static final int DEFAULT_RETRIES = 2;
    private static final int DEFAULT_READ_TIMEOUT = 5000;
    private static final long DEFAULT_CACHE_TIMEOUT = 1000 * 60;

    private int coreThreads = DEFAULT_CORE_THREADS;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int retries = DEFAULT_RETRIES;
    private boolean saveResponses = false;
    private CacheStrategy cacheStrategy = CacheStrategy.RECENT;
    private long cacheTimeout = DEFAULT_CACHE_TIMEOUT;

    public int getCoreThreads() {
        return coreThreads;
    }

    public void setCoreThreads(int coreThreads) {
        this.coreThreads = coreThreads;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isSaveResponsesEnabled() {
        return saveResponses;
    }

    public void setSaveResponsesEnabled(boolean saveResponses) {
        this.saveResponses = saveResponses;
    }

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public long getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(long cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }
}
