package tk.zielony.dataapi;

/**
 * Created by Marcin on 2016-06-22.
 */

public interface OnCallFinishedListener<Type> {
    void onSuccess(Type data);
    void onError(APIException e);
}
