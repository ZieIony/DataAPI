package tk.zielony.dataapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class ApiError {
    public enum Type {
        GENERIC, UNAUTHORIZED
    }

    private Exception exception;
    private Type type;

    ApiError(Exception exception) {
        this.exception = exception;
        if (exception instanceof HttpStatusCodeException && ((HttpStatusCodeException) exception).getStatusCode() == HttpStatus.UNAUTHORIZED) {
            type = Type.UNAUTHORIZED;
        } else {
            type = Type.GENERIC;
        }
    }

    public Exception getException() {
        return exception;
    }

    public Type getType() {
        return type;
    }
}
