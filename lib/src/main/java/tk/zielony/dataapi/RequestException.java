package tk.zielony.dataapi;

public class RequestException extends RuntimeException {
    private Request request;

    public RequestException(Throwable cause, Request request) {
        super(cause);
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
