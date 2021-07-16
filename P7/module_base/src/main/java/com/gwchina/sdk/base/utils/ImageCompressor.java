package com.gwchina.sdk.base.utils;

import android.support.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.config.DirectoryManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import id.zelory.compressor.Compressor;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 图片压缩工具
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-05-15 11:42
 */
public class ImageCompressor {

    private ImageCompressor() {
        throw new UnsupportedOperationException();
    }

    public static Single<List<File>> compress(@NonNull List<String> photos) {
        return Flowable.fromIterable(photos)
                .subscribeOn(Schedulers.io())
                .map(ImageCompressor::doCompress)
                .toList()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Flowable<File> compress(@NonNull String photo) {
        return Flowable.just(photo)
                .subscribeOn(Schedulers.io())
                .map(s -> doCompress(photo))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static File doCompress(@NonNull String photo) throws IOException {
        return new Compressor(AppContext.getContext()).compressToFile(new File(photo), DirectoryManager.createTempPicturePath(FileUtils.getFileExtension(photo)));
    }

}