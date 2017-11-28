package tk.zielony.dataapi;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Response<Type extends Serializable> {
    private String endpoint;
    private HttpMethod method;
    private long time;
    private Type data;
    private HttpStatus code;

    Response() {
    }

    public Response(HttpMethod method, String endpoint, HttpStatus code) {
        this.endpoint = endpoint;
        this.method = method;
        this.code = code;
    }

    public Response(HttpMethod method, String endpoint, Type data, HttpStatus code) {
        this.endpoint = endpoint;
        this.method = method;
        this.data = data;
        this.code = code;
    }

    public boolean isSuccess() {
        return code == HttpStatus.OK;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Type getData() {
        return data;
    }

    public HttpStatus getCode() {
        return code;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Response that = (Response) o;

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

    public void write(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeInt(method.ordinal());
        outputStream.writeUTF(endpoint);
        outputStream.writeObject(data);
        outputStream.writeLong(time);
        outputStream.writeInt(code.value());
    }

    public void read(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        method = HttpMethod.values()[inputStream.readInt()];
        endpoint = inputStream.readUTF();
        data = (Type) inputStream.readObject();
        time = inputStream.readLong();
        code = HttpStatus.valueOf(inputStream.readInt());
    }
}
