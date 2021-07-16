package com.gwchina.parent.main.widget;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gwchina.lssw.parent.home.R;

import static com.android.base.kotlin.SizeExKt.dip;
import static com.gwchina.sdk.base.data.api.Business.GUARD_LEVEL_MODERATE;

/**
 * @author hujie
 * Email: hujie1991@126.com
 * Date : 2020-01-02 17:12
 */
public class SevereGuardGuidePop extends PopupWindow {

    private Activity mActivity;
    private View popview;
    private View frameLayoutBg;
    private View tvHomeTimeGuard;
    private TextView tvMineAddTips;

    private boolean canDismiss = false;

    public SevereGuardGuidePop(Activity activity) {
        super(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mActivity = activity;
        init();
    }

    public void show(View anchor, int guardLevel) {
        if (guardLevel == GUARD_LEVEL_MODERATE) {
            tvHomeTimeGuard.setVisibility(View.GONE);
            tvMineAddTips.setText(R.string.sever_guard_guide_tips1);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frameLayoutBg.getLayoutParams();
            layoutParams.weight = 1;
        }

        showBgCover();
        showAsDropDown(anchor, 0, -anchor.getHeight() - dip(5));
    }

    private void init() {
        popview = View.inflate(mActivity, R.layout.pop_severe_guard_guide, null);
        tvHomeTimeGuard = popview.findViewById(R.id.tvHomeTimeGuard);
        View tvMineMultiDeviceTipsClose = popview.findViewById(R.id.tvMineMultiDeviceTipsClose);
        frameLayoutBg = popview.findViewById(R.id.frameLayoutBg);
        tvMineAddTips = popview.findViewById(R.id.tvMineAddTips);
        setContentView(popview);

        View.OnClickListener  closeListener = v -> dismiss2();
        frameLayoutBg.setOnClickListener(closeListener);
        tvMineMultiDeviceTipsClose.setOnClickListener(closeListener);

        setTouchable(true);
        setFocusable(true);

        setOnDismissListener(this::hideBgCover);
    }

    @Override
    public void dismiss() {
        if (canDismiss) {
            super.dismiss();
        }
    }

    private void dismiss2() {
        canDismiss = true;
        dismiss();
    }

    private void showBgCover() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha=0.2f;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }

    private void hideBgCover() {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha=1.0f;
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }

}
