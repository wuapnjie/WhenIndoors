package com.xiaopo.flying.whenindoors.data.remote

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore.Images.ImageColumns
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.UploadManager
import io.reactivex.Observable


/**
 * @author wupanjie
 */
object ImageUploader {

  private val uploadManager: UploadManager

  init {
    val config = Configuration.Builder().build()
    uploadManager = UploadManager(config)
  }


  fun upload(context: Context, uri: Uri, remoteDataSource: RemoteDataSource): Observable<ResponseInfo> {
    val data = getRealFilePath(context, uri)
    return Observable.create { emitter ->
      remoteDataSource.fetchUploadToken()
          .subscribe { token ->
            emitter.onNext(uploadManager.syncPut(data,null,token,null))
            emitter.onComplete()
          }
    }
  }

  private fun getRealFilePath(context: Context, uri: Uri?): String? {
    if (null == uri) return null
    val scheme = uri.scheme
    var data: String? = null
    if (scheme == null)
      data = uri.path
    else if (ContentResolver.SCHEME_FILE == scheme) {
      data = uri.path
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
      val cursor = context.contentResolver.query(uri, arrayOf(ImageColumns.DATA), null, null, null)
      if (null != cursor) {
        if (cursor.moveToFirst()) {
          val index = cursor.getColumnIndex(ImageColumns.DATA)
          if (index > -1) {
            data = cursor.getString(index)
          }
        }
        cursor.close()
      }
    }
    return data
  }
}