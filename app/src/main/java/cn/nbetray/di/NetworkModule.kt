package cn.nbetray.di

import cn.nbetray.data.api.ClashApi
import cn.nbetray.data.local.ApiConfig
import cn.nbetray.data.local.ApiConfigDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        apiConfigDataStore: ApiConfigDataStore
    ): Retrofit {
        // Create a dynamic interceptor for auth and base URL
        val authInterceptor = Interceptor { chain ->
            val config = runBlocking { apiConfigDataStore.currentConfig.first() }
            val originalRequest = chain.request()

            val newRequest = if (config.secret.isNotEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer ${config.secret}")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }

        val client = okHttpClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://127.0.0.1:9090/") // Default, will be overridden
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideClashApi(retrofit: Retrofit): ClashApi {
        return retrofit.create(ClashApi::class.java)
    }
}
