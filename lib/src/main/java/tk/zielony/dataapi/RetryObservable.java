package tk.zielony.dataapi;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

class RetryObservable implements Function<Observable<? extends Throwable>, Observable<?>> {

    private int maxRetries;

    RetryObservable(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> errors) throws Exception {
        return errors.flatMap(throwable -> {
            if (!(throwable instanceof RequestException))
                return Observable.error(throwable);

            RequestException requestException = (RequestException) throwable;
            if (requestException.getAttempt() > maxRetries)
                return Observable.error(throwable);

            return Observable.just(throwable);
        });
    }
}
