package tk.zielony.dataapi.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import tk.zielony.dataapi.CacheStrategy;
import tk.zielony.dataapi.Configuration;
import tk.zielony.dataapi.Response;
import tk.zielony.dataapi.WebAPI;

public class MainActivity extends AppCompatActivity {
    WebAPI webAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration configuration = new Configuration();
        configuration.setCacheStrategy(CacheStrategy.NONE);
        webAPI = new WebAPI("https://jsonplaceholder.typicode.com", configuration);

        TextView tv = findViewById(R.id.textView);

        findViewById(R.id.button).setOnClickListener(view -> webAPI.get("/posts", String.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<String>>() {
                    @Override
                    public void onNext(Response<String> stringResponse) {
                        tv.setText(stringResponse.getData());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("test", "error");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("test", "complete");
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        webAPI.cancelRequests();
    }
}
