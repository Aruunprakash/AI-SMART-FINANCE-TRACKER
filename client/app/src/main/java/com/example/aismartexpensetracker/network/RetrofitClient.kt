package com.example.aismartexpensetracker.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANT -- change this depending on how you're running the demo:
    //   - Android EMULATOR + server running on your laptop -> "http://10.0.2.2:8000/"
    //     (10.0.2.2 is the emulator's alias for your laptop's 127.0.0.1)
    //   - REAL PHONE on the same Wi-Fi as your laptop -> "http://<laptop-LAN-IP>:8000/"
    //     Find your LAN IP with `ipconfig` (Windows) or `ifconfig`/`ip addr` (Mac/Linux),
    //     e.g. "http://192.168.1.42:8000/". Phone and laptop must be on the same network,
    //     and the server must be started as `uvicorn app.main:app --host 0.0.0.0 --port 8000`
    //     (not the default 127.0.0.1) so it accepts connections from other devices.
    const val BASE_URL = "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
