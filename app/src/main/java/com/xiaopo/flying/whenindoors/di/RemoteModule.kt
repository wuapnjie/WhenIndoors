package com.xiaopo.flying.whenindoors.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xiaopo.flying.whenindoors.data.remote.BASE_URL
import com.xiaopo.flying.whenindoors.data.remote.IndoorsAPI
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author wupanjie
 */
@Module
class RemoteModule {

  @Provides
  @Singleton
  fun provideGson(): Gson =
      GsonBuilder()
          .setLenient()
          .create()

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient =
      OkHttpClient.Builder()
          .build()

  @Provides
  @Singleton
  fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit =
      Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
          .addConverterFactory(GsonConverterFactory.create(gson))
          .client(okHttpClient)
          .build()

  @Provides
  @Singleton
  fun provideIndoorsApi(retrofit: Retrofit): IndoorsAPI =
      retrofit.create(IndoorsAPI::class.java)
}