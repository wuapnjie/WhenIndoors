package com.xiaopo.flying.whenindoors.di

import android.content.Context
import com.xiaopo.flying.whenindoors.IndoorsApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author wupanjie
 */
@Module
class AppModule(private val application : IndoorsApplication) {

  @Provides @Singleton fun provideContext() : Context = application

}