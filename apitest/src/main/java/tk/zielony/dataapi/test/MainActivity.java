package tk.zielony.dataapi.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import tk.zielony.dataapi.CacheStrategy;
import tk.zielony.dataapi.Configuration;
import tk.zielony.dataapi.Response;
import tk.zielony.dataapi.WebAPI;

public class MainActivity extends AppCompatActivity {
    WebAPI webAPI;
    CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration configuration = new Configuration();
        configuration.setCacheStrategy(CacheStrategy.NONE);
        webAPI = new WebAPI("https://jsonplaceholder.typicode.com", configuration);

        findViewById(R.id.button).setOnClickListener(view -> disposables.add(webAPI.get("/posts", String.class).subscribeWith(new DisposableObserver<Response<String>>() {
            @Override
            public void onNext(Response<String> stringResponse) {
                Log.e("test", "next");
            }

            @Override
            public void onError(Throwable e) {
                Log.e("test", "error");
            }

            @Override
            public void onComplete() {
                Log.e("test", "complete");
            }
        })));
    }

    @Override
    protected void onStop() {
        super.onStop();
        webAPI.cancelRequests();
    }
}
