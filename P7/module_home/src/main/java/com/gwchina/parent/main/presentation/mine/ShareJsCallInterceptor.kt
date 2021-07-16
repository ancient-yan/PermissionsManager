package com.gwchina.parent.main.presentation.mine

import android.app.Dialog
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import com.android.base.app.fragment.BaseDialogFragment
import com.android.base.kotlin.sp
import com.android.base.permission.AutoPermissionRequester
import com.android.base.utils.android.XIntentUtils
import com.android.sdk.qrcode.zxing.QRCodeEncoder
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.DirectoryManager
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.data.utils.OKHttpDownloader
import com.gwchina.sdk.base.third.share.SharePlatform
import com.gwchina.sdk.base.third.share.ShareURL
import com.gwchina.sdk.base.third.share.SocialShareUtils
import com.gwchina.sdk.base.web.BaseCustomJsCallInterceptor
import com.gwchina.sdk.base.web.BaseWebFragment
import com.gwchina.sdk.base.web.ResultReceiver
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.main_share_layout.*
import timber.log.Timber
import java.io.File

/*
json:

{"shareType":"3","sharePlatform":"1","shareWebUrl":"http://192.168.2.96:5000/invited_friends.html?USERID=9647f26d944b4321af9f91801222ec76&CHANNELID=01","shareImageUrl":"","shareTitle":"我正在使用“格雷守护”，推荐给你!","shareSubTitle":"帮助孩子健康地使用手机，现在加入，还可以免费领取会员"}

wechat sharePlatform = 1
moment sharePlatform = 2
qq sharePlatform = 3
 */
private data class ShareContent(
        var shareImageUrl: String?,
        val sharePlatform: String?,
        val shareSubTitle: String?,
        val shareTitle: String?,
        val shareType: String?,
        val shareWebUrl: String?
)

/*

生成海报

{"imageUrl":"https://file.gwchina.cn/greenguard/h5/7.0/invitation_friends/pic_invite_card_BG.png","webUrl":"http://192.168.2.96:5000/invited_friends.html?USERID=9647f26d944b4321af9f91801222ec76&CHANNELID=04"}

 */
@Parcelize
private data class PosterContent(
        val imageUrl: String?,
        val webUrl: String?
) : Parcelable

/**
 * 该类使用到了反射调用，修改位置的话[SchemeJumper]中也需要修改
 */
class ShareJsCallInterceptor : BaseCustomJsCallInterceptor {

    private lateinit var activity: FragmentActivity

    override fun onInit(host: BaseWebFragment, bundle: Bundle?) {
        activity = host.requireActivity()
    }

    override fun intercept(method: String, args: Array<String>?, resultReceiver: ResultReceiver?): Boolean {
        if ("shareAndConfigInfo" == method) {
            args?.firstOrNull()?.let { doShare(it) }
            return true
        }

        if ("skipCompositePosterPage" == method) {
            args?.firstOrNull()?.let { showPoster(it) }
            return true
        }

        return false
    }

    private fun showPoster(share: String) {
        val posterContent = JsonUtils.fromJson(share, PosterContent::class.java)
        Timber.d("posterContent $posterContent")
        showPosterDialog(activity.supportFragmentManager, posterContent)
    }

    private fun doShare(share: String) {
        val shareContent = JsonUtils.fromJson(share, ShareContent::class.java)
        val sharePlatform = when {
            shareContent.sharePlatform == "1" -> SharePlatform.WeChat
            shareContent.sharePlatform == "2" -> SharePlatform.WeChatMoment
            else -> SharePlatform.QQ
        }
        if (shareContent.shareImageUrl.isNullOrEmpty() || shareContent.shareImageUrl?.startsWith("http") == false) {
            shareContent.shareImageUrl = "https://file.gwchina.cn/greenguard/7.0/logo/logo64x64.ico"
        }
        val shareURL = ShareURL(sharePlatform, shareContent.shareWebUrl
                ?: "", shareContent.shareTitle ?: "", shareContent.shareSubTitle
                ?: "", if (shareContent.shareImageUrl.isNullOrEmpty()) null else shareContent.shareImageUrl)
        SocialShareUtils.share(activity, shareURL, null)
    }

}

private const val POSTER_CONTENT_KEY = "PosterContentKey"

private fun showPosterDialog(fragmentManager: FragmentManager, posterContent: PosterContent) {
    PosterDialogFragment()
            .apply {
                arguments = Bundle().apply { putParcelable(POSTER_CONTENT_KEY, posterContent) }
            }
            .show(fragmentManager, PosterDialogFragment::class.java.name)
}

class PosterDialogFragment : BaseDialogFragment() {
    private var bitmap: Bitmap? = null
    private var tempFile: File? = null
    private var realHeight = 0f
    private val screenWidth = ScreenUtils.getScreenWidth()
    private val uiWidth = 347f
    private val uiHeight = 485f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog_Common)
        val posterContent = arguments?.getParcelable<PosterContent>(POSTER_CONTENT_KEY)
        val imgPath = posterContent?.imageUrl
        val tempPath = DirectoryManager.createTempPicturePath(FileUtils.getFileExtension(imgPath))
        val qrCode = Observable.just(posterContent).subscribeOn(AppContext.schedulerProvider().io()).map {
            QRCodeEncoder.syncEncodeQRCode(it.webUrl, (70 * (screenWidth.toFloat() / uiWidth)).toInt()) //设计图尺寸是347 70
        }
        tempFile = File(tempPath)
        val resource = OKHttpDownloader.download(posterContent?.imageUrl, tempFile).map {
            BitmapFactory.decodeFile(it)
        }
        Observable.zip(resource, qrCode, BiFunction<Bitmap, Bitmap, Bitmap> { bgBitmap, qrCodeBitmap ->
            createBitmap(bgBitmap, qrCodeBitmap)
        }).subscribeOn(AppContext.schedulerProvider().io())
                .observeOn(AppContext.schedulerProvider().ui()).subscribe {
                    bitmap = it
                    imageView.background = BitmapDrawable(resources, it)
                }
    }

    override fun provideLayout(): Any? {
        return R.layout.main_share_layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        saveImage.setOnClickListener {
            if (tempFile != null && tempFile!!.exists()) tempFile!!.delete()
            AutoPermissionRequester.with(this)
                    .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted {
                        bitmap?.let { it1 -> saveBitmapFile(this, it1) }
                    }
                    .request()
        }
        gwTitleLayout.setOnNavigationOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.run {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val params = attributes
            params.windowAnimations = R.style.Style_Anim_Fragment_Scale_In
            this.attributes = params
        }

        return dialog
    }

    override fun onDestroy() {
        if (tempFile != null && tempFile!!.exists()) tempFile!!.delete()
        super.onDestroy()
    }

    //返回照片的尺寸694*970
    private fun createBitmap(bgBitmap: Bitmap, qrCodeBitmap: Bitmap): Bitmap {
        val width = bgBitmap.width
        val height = bgBitmap.height
        val result = Bitmap.createBitmap(screenWidth, (height * (screenWidth.toFloat() / width)).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        //画背景
        realHeight = height * (screenWidth.toFloat() / width)
        canvas.drawBitmap(bgBitmap, null, RectF(0f, 0f, screenWidth.toFloat(), realHeight), null)
        //画二维码
        canvas.drawBitmap(qrCodeBitmap, screenWidth - qrCodeBitmap.width - getRealWidth(15f), getRealHeight(360f), null)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = ContextCompat.getColor(context!!, R.color.gray_level1)
        textPaint.textSize = sp(18f)

        val shareName = getTitle(getString(R.string.home_share_text0_mask, AppContext.appDataSource().user().patriarch.nick_name), textPaint, screenWidth, qrCodeBitmap.width)

        val shareNameHeight = getTextHeight(textPaint)
        canvas.drawText(shareName, getRealWidth(15f), getRealHeight(360f) + shareNameHeight, textPaint)

        textPaint.color = ContextCompat.getColor(context!!, R.color.gray_level2)
        textPaint.textSize = sp(14f)
        val text1 = context!!.resources.getString(R.string.home_share_text1)
        val text1Bottom = getRealHeight(380f) + shareNameHeight + getTextHeight(textPaint)
        canvas.drawText(text1, getRealWidth(15f), text1Bottom, textPaint)
        canvas.drawText(context!!.resources.getString(R.string.home_share_text2), getRealWidth(15f), text1Bottom + getTextHeight(textPaint), textPaint)
        //长按二维码加入
        textPaint.textSize = sp(11f)
        val longPressText = context!!.resources.getString(R.string.home_share_long_press)
        canvas.drawText(longPressText, screenWidth - qrCodeBitmap.width / 2.0f - getRealWidth(15f) - getTextLength(longPressText, textPaint) / 2.0f, getRealHeight(366f) + qrCodeBitmap.height + getTextHeight(textPaint), textPaint)
        //现在加入
        textPaint.color = ContextCompat.getColor(context!!, R.color.yellow_level1)
        textPaint.textSize = sp(12f)
        val joinText = context!!.resources.getString(R.string.home_share_text3)
        canvas.drawText(joinText, (screenWidth - getTextLength(joinText, textPaint)) / 2.0f, realHeight - getRealHeight(10f), textPaint)
        return result
    }

    private fun getTitle(content: String, paint: Paint, width: Int, qrWidth: Int): String {
        val spaceWidth = width - qrWidth/* - getRealWidth(30f)*/
        val contentLength = getTextLength(content, paint)
        if (contentLength > spaceWidth) {
            //原本字符个数
            val charCount = content.count()
            val end = (spaceWidth * charCount / contentLength).toInt()
            return content.substring(0, end - 3) + "..."
        }
        return content
    }

    private fun getTextLength(text: String, paint: Paint): Float {
        return paint.measureText(text)
    }

    /**
     * [originHeight]: ui设计图尺寸
     */
    private fun getRealHeight(originHeight: Float): Float {
        return (originHeight / uiHeight) * realHeight
    }

    private fun getRealWidth(originWidth: Float): Float {
        return originWidth / uiWidth * screenWidth
    }

    private fun getTextHeight(paint: Paint): Float {
        val fm = paint.fontMetrics
        return fm.bottom - fm.top
    }

    private fun saveBitmapFile(fragment: BaseDialogFragment, bitmap: Bitmap) {
        Flowable.just(bitmap).subscribeOn(AppContext.schedulerProvider().io()).map { bitmap1 ->
            val storePath = DirectoryManager.createDCIMPictureStorePath(DirectoryManager.createTempFileName(DirectoryManager.PICTURE_FORMAT_JPEG))
            ImageUtils.save(bitmap1, storePath, Bitmap.CompressFormat.PNG)
            storePath
        }.observeOn(AppContext.schedulerProvider().ui()).subscribe(
                { path ->
                    showImageSaveSuccess(fragment, path)
                },
                { ToastUtils.showLong(getString(R.string.image_save_fail_tips)) })
    }

    private fun showImageSaveSuccess(mFragment: BaseDialogFragment, path: String) {
        mFragment.showMessage(mFragment.getString(com.app.base.R.string.image_save_success_tips))
        val context = mFragment.context
        if (context != null) {
            XIntentUtils.notifyImageSaved(context, path)
        }
    }
}