package com.project.threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private ImageView imageView;
    private ExecutorService executor;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.button);

        // Inicializa el Executor y el Handler
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // Configura el botón
        button.setOnClickListener(v -> {
            // Realiza la llamada a la API
            getDataFromUrl("https://api.myip.com");
            loadImage("https://randomfox.ca/images/122.jpg");
        });
    }

    private void getDataFromUrl(String urlString) {
        executor.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                    reader.close();
                } else {
                    Log.e("HTTP Error", "Response code: " + responseCode);
                }
            } catch (IOException e) {
                Log.e("Error", "Error en la conexión: " + e.getMessage(), e);
            }

            // Procesar el resultado
            String finalResult = result.toString();
            handler.post(() -> {
                // Mostrar el resultado en el TextView
                textView.setText(finalResult);
            });
        });
    }

    private void loadImage(String imageUrl) {
        executor.execute(() -> {
            try {
                InputStream in = new URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                // Actualiza la imagen en el hilo principal
                handler.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e("Image Load Error", e.getMessage(), e);
            }
        });
    }
}

