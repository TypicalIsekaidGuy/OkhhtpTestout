package com.example.okhhtptestout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.conscrypt.Conscrypt;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class MainActivity extends AppCompatActivity {
    String LOG = "ABOBA";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Provider conscrypt = Conscrypt.newProvider();

// Add as provider
        Security.insertProviderAt(conscrypt, 1);

// Init OkHttp
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient()
                .newBuilder()
                .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS));

// OkHttp 3.12.x
// ConnectionSpec.COMPATIBLE_TLS = TLS1.0
// ConnectionSpec.MODERN_TLS = TLS1.0 + TLS1.1 + TLS1.2 + TLS 1.3
// ConnectionSpec.RESTRICTED_TLS = TLS 1.2 + TLS 1.3

// OkHttp 3.13+
// ConnectionSpec.COMPATIBLE_TLS = TLS1.0 + TLS1.1 + TLS1.2 + TLS 1.3
// ConnectionSpec.MODERN_TLS = TLS1.2 + TLS 1.3
// ConnectionSpec.RESTRICTED_TLS = TLS 1.2 + TLS 1.3

        try {
            X509TrustManager tm = Conscrypt.getDefaultX509TrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLS", conscrypt);
            sslContext.init(null, new TrustManager[] { tm }, null);
            okHttpBuilder.sslSocketFactory(new InternalSSLSocketFactory(sslContext.getSocketFactory()), tm);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG,e.toString());
        }

// Build OkHttp
        OkHttpClient okHttpClient = okHttpBuilder.build();
        Request request = new Request.Builder()
                .url("https://pq.cloudflareresearch.com/") // You can try another TLS 1.3 capable HTTPS server
                .build();

        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.d(LOG, call.toString());
                        e.printStackTrace();
                        Log.d(LOG, e.toString());
                    }

                    @Override
                    public void onResponse(Call call,final Response response) throws IOException {
                        Log.d(LOG, "onResponse() tlsVersion=" + response.handshake().tlsVersion());
                        Log.d(LOG, "onResponse() cipherSuite=" + response.handshake().cipherSuite().toString());
                        // D/TestApp##: onResponse() tlsVersion=TLS_1_3
                        // D/TestApp##: onResponse() cipherSuite=TLS_AES_256_GCM_SHA384
                    }
                });
/*
        try {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(spec))
                    .build();
            Log.d("SSSS",client.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

    }
}