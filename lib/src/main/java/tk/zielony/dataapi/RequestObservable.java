package tk.zielony.dataapi;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

class RequestObservable<RequestBodyType, ResponseBodyType extends Serializable> implements ObservableOnSubscribe<Response<ResponseBodyType>> {
    private DataAPI api;
    private Request<RequestBodyType, ResponseBodyType> request;
    private int attempt = 0;

    RequestObservable(DataAPI api, Request<RequestBodyType, ResponseBodyType> request) {
        this.api = api;
        this.request = request;
    }

    @Override
    public void subscribe(ObservableEmitter<Response<ResponseBodyType>> e) throws Exception {
        try {
            Response<ResponseBodyType> response = api.executeRequest(request);
            if (response.getCode() != HttpStatus.OK) {
                e.onError(new RequestException(null, request, attempt));
            } else {
                e.onNext(response);
                e.onComplete();
            }
        } catch (Exception ex) {
            e.onError(new RequestException(ex, request, attempt));
        }
        attempt++;
    }
}
