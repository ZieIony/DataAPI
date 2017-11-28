package tk.zielony.dataapi;

public class RequestException extends Throwable {
    private Request request;
    private int attempt;

    public RequestException(Throwable cause, Request request, int attempt) {
        super(cause);
        this.request = request;
        this.attempt = attempt;
    }

    public Request getRequest() {
        return request;
    }

    public int getAttempt() {
        return attempt;
    }
}
