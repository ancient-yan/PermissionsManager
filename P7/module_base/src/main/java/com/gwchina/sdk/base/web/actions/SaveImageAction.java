package com.gwchina.sdk.base.web.actions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.webkit.URLUtil;

import com.android.base.utils.android.XIntentUtils;
import com.android.base.utils.common.Checker;
import com.app.base.R;
import com.blankj.utilcode.util.ImageUtils;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.config.DirectoryManager;
import com.gwchina.sdk.base.data.utils.OKHttpDownloader;
import com.gwchina.sdk.base.web.BaseWebFragment;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.io.File;

import io.reactivex.Flowable;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-01-24 14:48
 */
public final class SaveImageAction {

    private final BaseWebFragment mFragment;
    private final String[] mArgs;

    public SaveImageAction(BaseWebFragment fragment, String[] args) {
        mFragment = fragment;
        mArgs = args;
    }

    public static void action(BaseWebFragment fragment, String[] args) {
        new SaveImageAction(fragment, args).run();
    }

    private void run() {
        if (Checker.isEmpty(mArgs)) {
            return;
        }

        String imageUrl = mArgs[0];

        if (URLUtil.isNetworkUrl(imageUrl)) {
            downloadImage(imageUrl);
            //data:image/png;base64,
        } else if (imageUrl.startsWith("data:image/")) {
            trySaveImageByDecodingBase64(imageUrl.split(",")[1]);
        } else {
            mFragment.showMessage(mFragment.getString(R.string.image_save_success_tips));
        }
    }

    public void trySaveImageByDecodingBase64(String base64) {
        mFragment.showLoadingDialog();
        if (base64.startsWith("data:image/")) {
            base64 = base64.split(",")[1];
        }
        String finalBase6 = base64;
        Flowable.just(base64)
                .subscribeOn(AppContext.schedulerProvider().io())
                .map(s -> {
                    byte[] bytes = Base64.decode(finalBase6, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    String storePath = DirectoryManager.createDCIMPictureStorePath(DirectoryManager.createTempFileName(DirectoryManager.PICTURE_FORMAT_JPEG));
                    ImageUtils.save(bitmap, storePath, Bitmap.CompressFormat.JPEG);
                    return storePath;
                }).observeOn(AppContext.schedulerProvider().ui())
                .subscribe(
                        this::showImageSaveSuccess,
                        throwable -> {
                            mFragment.dismissLoadingDialog();
                            mFragment.showMessage(R.string.image_save_fail_tips);
                        });
    }

    private void downloadImage(String imageUrl) {
        mFragment.showLoadingDialog();
        OKHttpDownloader.download(imageUrl, getSaveFile(imageUrl))
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(mFragment)))
                .subscribe(
                        this::showImageSaveSuccess,
                        throwable -> {
                            mFragment.dismissLoadingDialog();
                            mFragment.showMessage(R.string.image_save_fail_tips);
                        });
    }

    private void showImageSaveSuccess(@SuppressWarnings("unused") String path) {
        mFragment.dismissLoadingDialog();
        mFragment.showMessage(mFragment.getString(R.string.image_save_success_tips));
        Context context = mFragment.getContext();
        if (context != null) {
            XIntentUtils.notifyImageSaved(context, path);
        }
    }

    private File getSaveFile(String url) {
        int index = url.lastIndexOf("/");
        String fileName = url.substring(index + 1);
        String path = DirectoryManager.createDCIMPictureStorePath(fileName);
        return new File(path);
    }

}
