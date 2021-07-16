package com.gwchina.sdk.debug;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.base.app.fragment.BaseFragment;
import com.android.sdk.net.kit.ResultHandlers;
import com.app.base.R;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.data.app.AppDataSource;
import com.gwchina.sdk.base.data.models.UserEx;
import com.gwchina.sdk.base.third.bugly.TinkerTools;
import com.gwchina.sdk.base.utils.verify.ValidatorKt;
import com.gwchina.sdk.base.widget.dialog.TipsManager;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import org.joor.Reflect;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * 仅用于调试版本
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-07-26 18:49
 */
public class DebugFragment extends BaseFragment {

    private TextView mUserTv;
    private EditText mMobilePhoneEt;
    private EditText mPasswordEt;

    private AppDataSource mAppDataSource = AppContext.appDataSource();
    private DebugApi mDebugApi;

    @Nullable
    @Override
    protected Object provideLayout() {
        return R.layout.base_activity_debug;
    }

    @Override
    protected void onViewPrepared(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initUI();
        initUser();
        initToolViews();
    }

    private void initUI() {
        mUserTv = findViewById(R.id.debug_tv_user);
        mMobilePhoneEt = findViewById(R.id.debug_phone);
        mPasswordEt = findViewById(R.id.debug_password);
    }

    private <T extends View> T findViewById(int id) {
        View view = getView();
        assert view != null;
        return view.findViewById(id);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 用户相关
    ///////////////////////////////////////////////////////////////////////////

    @SuppressLint("SetTextI18n")
    private void initUser() {
        mDebugApi = AppContext.serviceFactory().create(DebugApi.class);

        mAppDataSource.observableUser()
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(user -> {
                    if (UserEx.logined(user)) {
                        mUserTv.setText("登录已用户：" + user.getPatriarch().getNick_name());
                    } else {
                        mUserTv.setText("没有用户登录");
                    }
                }, Timber::e);

        Button switchBtn = findViewById(R.id.debug_btn_switch);
        switchBtn.setText("显示登录视图");
        View loginView = findViewById(R.id.debug_ll_login_layout);
        switchBtn.setOnClickListener(v -> {
            if (loginView.getVisibility() == View.GONE) {
                loginView.setVisibility(View.VISIBLE);
                switchBtn.setText("隐藏登录视图");
            } else {
                switchBtn.setText("显示登录视图");
                loginView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.debug_login).setOnClickListener(v -> login());
    }

    private void login() {
        if (!ValidatorKt.validateCellphone(mMobilePhoneEt)) {
            return;
        }
        if (!ValidatorKt.validatePassword(mPasswordEt)) {
            return;
        }

        showLoadingDialog(false);

        mDebugApi.pwdLogin(mMobilePhoneEt.getText().toString(), mPasswordEt.getText().toString())
                .compose(ResultHandlers.resultExtractor())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(this::dismissLoadingDialog)
                .subscribe(
                        responseModel -> {
                            mAppDataSource.saveUser(responseModel.getLogin_info(), responseModel.getApp_token(), responseModel.getExpire_time());
                            TipsManager.showMessage("登录成功");
                        },
                        throwable -> TipsManager.showMessage("登录失败：" + throwable));
    }


    private void doRestart() {
        AppContext.getContext().restartApp(getActivity());
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    ///////////////////////////////////////////////////////////////////////////
    // 工具相关
    ///////////////////////////////////////////////////////////////////////////
    private void initToolViews() {
        findViewById(R.id.debug_switch).setOnClickListener(v -> showSwitchTips());
        findViewById(R.id.debug_start).setOnClickListener(v -> AppContext.getContext().restartApp(getActivity()));
        findViewById(R.id.debug_open_ue_tool).setOnClickListener(v -> openUETool());
        findViewById(R.id.debug_create_bug).setOnClickListener(v -> TinkerTools.testBugly());
        findViewById(R.id.debug_restart).setOnClickListener(v -> confirmRestart());
        findViewById(R.id.debug_test_upload_file).setOnClickListener(v -> showFileUploadTest());
    }

    private void confirmRestart() {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setMessage("当前环境的登录状态与数据缓存将会清空，确定要重启吗？")
                .setNegativeButton(R.string.cancel_, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.sure, (dialog, which) -> {
                    dialog.dismiss();
                    //清除所有数据
                    AppContext.appDataSource().logout();
                    AppContext.storageManager().stableStorage().clearAll();
                    //重启
                    getActivity().getWindow().getDecorView().post(this::doRestart);
                }).show();
    }

    private boolean isUEShowing = false;

    public void openUETool() {
        if (isUEShowing) {
            Reflect.on("me.ele.uetool.UETool").call("dismissUETMenu");
            isUEShowing = false;
        } else {
            Reflect.on("me.ele.uetool.UETool").call("showUETMenu");
            isUEShowing = true;
        }
    }

    private void showFileUploadTest() {
        ((DebugActivity) Objects.requireNonNull(getActivity())).showFileUploadTest();
    }

    private void showSwitchTips() {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setMessage("切换接口环境后，需要手动重启应用方能生效哦。（H5环境不需要重启）")
                .setNegativeButton(R.string.cancel_, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    dialog.dismiss();
                    ((DebugActivity) Objects.requireNonNull(getActivity())).switchHost();
                }).show();
    }

}