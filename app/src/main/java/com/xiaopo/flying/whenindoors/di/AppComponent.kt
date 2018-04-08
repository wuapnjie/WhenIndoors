package com.xiaopo.flying.whenindoors.di

import com.xiaopo.flying.whenindoors.RoomViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * @author wupanjie
 */


@Singleton
@Component(modules = arrayOf(AppModule::class, RemoteModule::class))
interface AppComponent {

  fun inject(roomViewModel: RoomViewModel)

}