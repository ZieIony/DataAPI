package tk.zielony.dataapi;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

class RetryObservable implements Function<Observable<? extends Throwable>, Observable<?>> {

    int retries = 1;
    private int maxRetries;

    RetryObservable(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> errors) throws Exception {
        return errors.flatMap(throwable -> {
            if (retries > maxRetries)
                return Observable.error(throwable);

            retries++;
            return Observable.just(throwable);
        });
    }
}
