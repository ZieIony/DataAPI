package tk.zielony.dataapi;

public interface OnCallFinishedListener<Type> {
    void onSuccess();

    void onSuccess(Type data);

    void onError(Exception e);
}
