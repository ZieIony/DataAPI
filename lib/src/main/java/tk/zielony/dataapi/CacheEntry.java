package tk.zielony.dataapi;

import org.springframework.http.HttpMethod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CacheEntry {
    HttpMethod method;
    String endpoint;
    long time;
    public String response;

    public CacheEntry(String endpoint, HttpMethod method, String response) {
        this.method = method;
        this.endpoint = endpoint;
        this.time = System.currentTimeMillis();
        this.response = response;
    }

    public CacheEntry() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheEntry that = (CacheEntry) o;

        if (time != that.time) return false;
        if (method != that.method) return false;
        return endpoint.equals(that.endpoint);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + endpoint.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(method.ordinal());
        outputStream.writeUTF(endpoint);
        outputStream.writeUTF(response);
    }

    public void read(DataInputStream inputStream) throws IOException {
        method = HttpMethod.values()[inputStream.readInt()];
        endpoint = inputStream.readUTF();
        response = inputStream.readUTF();
        time = Long.MAX_VALUE;
    }
}
