package com.example.imageDownloader.ui.main

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.imageDownloader.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: FragmentMainBinding? = null
    private lateinit var viewModel: MainViewModel
    private var useExternalStorage: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.context = requireContext().applicationContext
        observerOnProgress()
        observeOnButtonState()
        observerOnProgressBarMaxLimit()
        observerOnError()
    }

    override fun onResume() {
        super.onResume()
        downloadButtonClickListner()

        _binding?.previewBtn?.setOnClickListener {
            _binding?.webview?.webViewClient = WebViewClient();
            _binding?.webview?.loadUrl(_binding?.textViewUrl?.text.toString())
        }

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            _binding?.checkBox?.visibility = View.VISIBLE
        } else {
            _binding?.checkBox?.visibility = View.GONE
        }
        _binding?.checkBox?.setOnCheckedChangeListener { _, isChecked ->
            useExternalStorage = true
        }
    }

    private fun downloadButtonClickListner() {
        _binding?.downloadBtn?.setOnClickListener {
            val url = _binding?.textViewUrl?.text.toString()
            if (viewModel.isNetworkAvailable(requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)) {
                val downloadManager =
                    requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                viewModel.startDownload(downloadManager, url, useExternalStorage)
            } else {
                Snackbar.make(
                    _binding?.main?.rootView!!,
                    "Please connect to a network",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    private fun observeOnButtonState() {
        viewModel.isDownloadInProgress.observe(viewLifecycleOwner, Observer {
            _binding?.downloadBtn?.isEnabled = !it
            Snackbar.make(
                _binding?.main?.rootView!!,
                "Download completed",
                Snackbar.LENGTH_LONG
            )
                .show()
            _binding?.progressBar?.progress = 0
            _binding?.textViewUrl?.text?.clear()
        })
    }

    private fun observerOnProgress() {
        viewModel.progressLiveData.observe(viewLifecycleOwner, Observer {
            _binding?.progressBar?.setProgress(it, true)
        })
    }

    private fun observerOnProgressBarMaxLimit() {
        viewModel.progressBarMaxLimit.observe(viewLifecycleOwner, Observer {
            _binding?.progressBar?.max = it - 1

        })
    }

    private fun observerOnError() {
        viewModel.displayError.observe(viewLifecycleOwner, Observer {
            Snackbar.make(
                _binding?.main?.rootView!!,
                it,
                Snackbar.LENGTH_LONG
            )
                .show()
            _binding?.progressBar?.progress = 0
            _binding?.textViewUrl?.text?.clear()
            _binding?.downloadBtn?.isEnabled = true
        })

    }
}