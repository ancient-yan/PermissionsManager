package com.android.base.widget;

import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.view.View;

import com.android.base.app.ui.StateLayoutConfig;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-05-17 17:25
 */
public abstract class StateProcessor {

    protected abstract void onInitialize(SimpleMultiStateView simpleMultiStateView);

    protected abstract void onParseAttrs(TypedArray typedArray);

    protected abstract void processStateInflated(@StateLayoutConfig.ViewState int viewState, @NonNull View view);

    protected abstract StateLayoutConfig getStateLayoutConfigImpl();

}
