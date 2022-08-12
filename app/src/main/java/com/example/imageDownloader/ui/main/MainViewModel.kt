package com.example.imageDownloader.ui.main

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.random.Random


class MainViewModel : ViewModel() {

    private val imageUrls = mutableListOf<String>()
    var progressLiveData: MutableLiveData<Int> = MutableLiveData<Int>();
    var isDownloadInProgress = MutableLiveData<Boolean>();
    val progressBarMaxLimit = MutableLiveData<Int>()
    val displayError = MutableLiveData<String>()
    lateinit var context: Context


    fun startDownload(
        downloadManager: DownloadManager,
        url: String,
        isExternalStorage: Boolean = false
    ) {
        viewModelScope.launch(exceptionHandler + Dispatchers.IO) {
            val document: Document =
                Jsoup.connect(url).get()
            val imageElements: Elements = document.select("img")
            for (imageElement in imageElements) {

                imageUrls.add(imageElement.absUrl("src"))

            }
            withContext(Dispatchers.Main) {
                isDownloadInProgress.value = true
                progressBarMaxLimit.value = imageUrls.size
            }
            //download image one by one
            downloadImages(downloadManager, imageUrls, isExternalStorage)
        }
    }

    private fun downloadImages(
        downloadManager: DownloadManager,
        imageUrls: List<String>,
        isExternalStorage: Boolean = false
    ) {
        for ((index, image) in imageUrls.withIndex()) {
            downloadFile(
                downloadManager,
                "file+${Random.nextInt(0, imageUrls.size)}",
                "File downloading",
                image, isExternalStorage
            )
            progressLiveData.postValue(index)
        }
        isDownloadInProgress.postValue(false)
    }

    private fun downloadFile(
        downloadManager: DownloadManager,
        fileName: String,
        desc: String,
        url: String,
        isExternalStorage: Boolean = false
    ) {
        // fileName -> fileName with extension
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle(fileName)
            .setDescription(desc)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)



        if (isExternalStorage) {
            request.setDestinationInExternalFilesDir(context, "/image_dir", "$fileName.jpg")
        } else {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "$fileName.jpg"
            )
        }
        downloadManager.enqueue(request)
    }

    fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        return connectivityManager.activeNetwork != null && connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        ) != null;
    }

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        viewModelScope.launch(Dispatchers.Main) {
            displayError.value = throwable.toString()
        }
    }

}