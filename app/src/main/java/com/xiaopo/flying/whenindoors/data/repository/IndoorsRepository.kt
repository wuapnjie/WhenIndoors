package com.xiaopo.flying.whenindoors.data.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import com.xiaopo.flying.whenindoors.data.remote.ImageUploader
import com.xiaopo.flying.whenindoors.data.remote.RemoteDataSource
import com.xiaopo.flying.whenindoors.kits.Result
import com.xiaopo.flying.whenindoors.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * @author wupanjie
 */
class IndoorsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val context: Context
) : Repository {

  val allDisposables: CompositeDisposable = CompositeDisposable()

  override fun getRoomList(limit: Int, offset: Int): LiveData<Result<RoomsData, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<RoomsData, Throwable>>()
    val disposable = remoteDataSource.fetchRoomList(limit, offset)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ rooms: RoomsData ->
          mutableLiveData.value = Result.of(rooms)
        }, { throwable ->
          mutableLiveData.value = Result.error(throwable)
        })
    allDisposables.add(disposable)

    return mutableLiveData
  }

  override fun getRoomInfo(roomId: String): LiveData<Result<Room, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<Room, Throwable>>()
    val disposable = remoteDataSource.fetchRoomInfo(roomId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ room: Room ->
          mutableLiveData.value = Result.of(room)
        }, { throwable ->
          mutableLiveData.value = Result.error(throwable)
        })

    allDisposables.add(disposable)

    return mutableLiveData
  }

  override fun clearFingerprints(roomId: String): LiveData<Result<ResponseTemplate, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<ResponseTemplate, Throwable>>()
    val disposable = remoteDataSource.clearFingerprints(roomId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          mutableLiveData.value = Result.of(it)
        }, {
          mutableLiveData.value = Result.error(it)
        })

    allDisposables.add(disposable)

    return mutableLiveData
  }

  override fun uploadWifi(roomId: String, wifiData: WifiData): LiveData<Result<ResponseTemplate, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<ResponseTemplate, Throwable>>()
    val disposable = remoteDataSource.uploadWifi(roomId, wifiData)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          mutableLiveData.value = Result.of(it)
        }, {
          mutableLiveData.value = Result.error(it)
        })

    allDisposables.add(disposable)

    return mutableLiveData
  }

  override fun uploadImage(uri: Uri):
      LiveData<Result<String, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<String, Throwable>>()
    val disposable = ImageUploader.upload(context, uri, remoteDataSource)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          mutableLiveData.value = Result.of(it.response.optString("key", ""))
        }, {
          mutableLiveData.value = Result.error(it)
        })

    allDisposables.add(disposable)

    return mutableLiveData;
  }

  override fun createRoom(room: Room): LiveData<Result<Room, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<Room, Throwable>>()
    val disposable = remoteDataSource.createRoom(room)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          mutableLiveData.value = Result.of(it)
        }, {
          mutableLiveData.value = Result.error(it)
        })

    allDisposables.add(disposable)

    return mutableLiveData
  }

  override fun fetchLocation(roomId: String, needComputePosition: NeedComputePosition): LiveData<Result<RoomPosition, Throwable>> {
    val mutableLiveData = MutableLiveData<Result<RoomPosition, Throwable>>()
    val disposable = remoteDataSource.fetchLocation(roomId, needComputePosition)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          mutableLiveData.value = Result.of(it)
        }, {
          mutableLiveData.value = Result.error(it)
        })

    allDisposables.add(disposable)

    return mutableLiveData
  }
}