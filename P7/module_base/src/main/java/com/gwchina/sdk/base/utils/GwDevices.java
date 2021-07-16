package com.gwchina.sdk.base.utils;

import android.annotation.SuppressLint;
import android.os.Looper;

import com.android.base.utils.android.DevicesUtils;
import com.android.base.utils.android.compat.AndroidVersion;
import com.android.base.utils.security.MD5Utils;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.data.api.Business;

import java.util.UUID;

import io.reactivex.Observable;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-20 20:04
 */
public class GwDevices {

    public static final String DEVICE_TYPE = Business.DEVICE_TYPE_ANDROID;
    public static final String APP_PLATFORM = "11";

    /**
     * run on thread
     */
    @SuppressLint("MissingPermission")
    public static String getGwDeviceSN() {
        if (com.app.base.BuildConfig.openDebug) {
            DevicesUtils.printSystemInfo();
        }
        String deviceId = AppContext.storageManager().stableStorage().getString("initDeviceId");
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        }
        //适配安卓10
        if (AndroidVersion.atLeast(29) && Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Observable<String> observable = Observable.create(emitter -> MdidSdkHelper.InitSdk(AppContext.getContext(), true, (isSupport, idSupplier) -> {
                if (isSupport && idSupplier != null) {
                    String md5Value = MD5Utils.md5(idSupplier.getOAID());
                    emitter.onNext(md5Value);
                } else {
                    emitter.onNext("RAN" + UUID.randomUUID().toString().replaceAll("-", ""));
                }
            }));
            deviceId = observable.blockingFirst();
        } else {
            deviceId = DevicesUtils.getDeviceId();
            deviceId = deviceId.replace("-", "");
        }
        AppContext.storageManager().stableStorage().putString("initDeviceId", deviceId);
        return deviceId;
    }

}
