package com.gwchina.sdk.debug;

import android.text.TextUtils;

import com.android.base.rx.FlowableRetryDelay;
import com.android.base.rx.RxKit;
import com.android.base.utils.android.DevicesUtils;
import com.android.base.utils.android.SpCache;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.utils.GwDevices;

import java.util.UUID;

import timber.log.Timber;

/**
 * 调试模式下的 AppContext ,直接模拟一个device sn 上报。
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-30 14:14
 */
public abstract class DebugAppContext extends AppContext {

    @Override
    public void onCreate() {
        super.onCreate();
        uploadDevice();
    }

    private void uploadDevice() {
        String deviceId = appDataSource().deviceId();
        if (TextUtils.isEmpty(deviceId)) {
            appDataSource()
                    .uploadDevice(GwDevices.DEVICE_TYPE, createFakeSN(), DevicesUtils.getModel())
                    .retryWhen(new FlowableRetryDelay(Integer.MAX_VALUE, 3000, null))
                    .subscribe(
                            this::syncUserDataIfLogined,
                            Timber::e);
        }
    }

    private void syncUserDataIfLogined() {
        if (appDataSource().userLogined()) {
            appDataSource()
                    .syncUser()
                    .subscribe(RxKit.logCompletedHandler(), RxKit.logErrorHandler());
        }
    }

    private String createFakeSN() {
        SpCache spCache = new SpCache("fake_app_token");
        final String fake_sn_key = "FAKE_SN_KEY";
        String fake_sn = spCache.getString(fake_sn_key, "");
        if (TextUtils.isEmpty(fake_sn)) {
            String uuid = UUID.randomUUID().toString();
            fake_sn = uuid.substring(0, uuid.length() / 2);
            spCache.putString(fake_sn_key, fake_sn);
        }
        return fake_sn;
    }

}
