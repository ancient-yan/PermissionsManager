package com.gwchina.sdk.debug

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.BaseFragment
import com.android.base.rx.AutoDisposeLifecycleOwnerEx
import com.android.base.rx.observeOnUI
import com.android.sdk.mediaselector.MediaSelector
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import kotlinx.android.synthetic.main.base_debug_file_upload.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-15 14:24
 */
class FileUploadFragment : BaseFragment(), AutoDisposeLifecycleOwnerEx {

    private lateinit var mediaSelector: MediaSelector

    private val fileUploadService = AppContext.serviceManager().newFileUploadService()

    override fun provideLayout() = R.layout.base_debug_file_upload

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {

        mediaSelector = MediaSelector(this, object : MediaSelector.Callback {
            override fun onTakeMultiPictureSuccess(pictures: MutableList<String>?) {
                if (!pictures.isNullOrEmpty()) {
                    doUpload(pictures)
                }
            }
        })

        baseBtnUploadFile.setOnClickListener {
            mediaSelector.takeMultiPicture(false, 9)
        }
    }

    private fun doUpload(pictures: MutableList<String>) {
        baseTvUploadResult.text = "正在上传"

        val builder = StringBuilder("上传前：\n")

        pictures.forEachIndexed { index, pic ->
            Timber.d("$index - $pic")
            builder.append("$index - $pic").append("\n")
        }
        fileUploadService.uploadFiles(pictures)
                .observeOnUI()
                .autoDispose()
                .subscribe(
                        {
                            builder.append("上传后：\n")
                            it.forEachIndexed { index, info ->
                                Timber.d("$index - ${info.file_name}-${info.url}")
                                builder.append("$index - ${info.file_name}-${info.url}").append("\n")
                            }
                            baseTvUploadResult.text = builder.toString()
                        },
                        {
                            baseTvUploadResult.text = it.message
                        }
                )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mediaSelector.onActivityResult(requestCode, resultCode, data)
    }

}