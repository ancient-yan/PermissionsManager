@file:JvmName("Dialogs")

package com.gwchina.sdk.base.widget.dialog

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.ArrayRes
import android.support.annotation.StringRes
import android.support.transition.Slide
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import com.android.base.kotlin.colorFromId
import com.android.base.utils.android.ActFragWrapper
import com.app.base.R
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.TimePeriod

private val internalHandler = Handler(Looper.getMainLooper())

///////////////////////////////////////////////////////////////////////////
// Loading
///////////////////////////////////////////////////////////////////////////
@JvmOverloads
fun createLoadingDialog(context: Context, autoShow: Boolean = false): Dialog {
    val loadingDialog = LoadingDialog(context)
    loadingDialog.setCanceledOnTouchOutside(false)
    loadingDialog.setMessage(R.string.dialog_loading)
    if (autoShow) {
        loadingDialog.show()
    }
    return loadingDialog
}

///////////////////////////////////////////////////////////////////////////
// Tips
///////////////////////////////////////////////////////////////////////////
class TipsDialogBuilder(private val actFragWrapper: ActFragWrapper) {
    companion object {
        internal const val TYPE_SUCCESS = 1
        internal const val TYPE_ERROR = 2
    }

    fun success() = TYPE_SUCCESS

    fun error() = TYPE_ERROR

    /*消息*/
    @StringRes
    var messageId = BaseDialogBuilder.NO_ID
        set(value) {
            message = actFragWrapper.context.getText(value)
        }
    var message: CharSequence = "debug：请设置message"

    var type = 0
    var autoDismissMillisecond: Long = 3000
}

fun showTipsDialog(actFragWrapper: ActFragWrapper, builder: TipsDialogBuilder.() -> Unit): Dialog {
    val tipsDialogBuilder = TipsDialogBuilder(actFragWrapper)
    builder(tipsDialogBuilder)
    return showTipsDialogImpl(actFragWrapper, tipsDialogBuilder)
}

fun Fragment.showTipsDialog(builder: TipsDialogBuilder.() -> Unit): Dialog {
    return showTipsDialog(ActFragWrapper.create(this), builder)
}

fun FragmentActivity.showTipsDialog(builder: TipsDialogBuilder.() -> Unit): Dialog {
    return showTipsDialog(ActFragWrapper.create(this), builder)
}

private fun showTipsDialogImpl(actFragWrapper: ActFragWrapper, tipsDialogBuilder: TipsDialogBuilder): Dialog {
    val tipsDialog = TipsDialog(actFragWrapper.context,
            when {
                tipsDialogBuilder.type == TipsDialogBuilder.TYPE_SUCCESS -> R.drawable.img_window_success
                tipsDialogBuilder.type == TipsDialogBuilder.TYPE_ERROR -> R.drawable.img_page_fail
                else -> throw IllegalArgumentException("you should define the type of tips ")
            }
    )

    tipsDialog.setMessage(tipsDialogBuilder.message)
    tipsDialog.setCancelable(false)

    val autoDismiss = Runnable {
        try {
            tipsDialog.dismiss()
        } catch (ignore: Exception) {
        }
    }

    val lifecycleOwner: LifecycleOwner = if (actFragWrapper.fragment != null) {
        actFragWrapper.fragment
    } else {
        actFragWrapper.context as FragmentActivity
    }

    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            tipsDialog.dismiss()
            internalHandler.removeCallbacks(autoDismiss)
        }
    })

    tipsDialog.show()

    internalHandler.postDelayed(autoDismiss, tipsDialogBuilder.autoDismissMillisecond)

    return tipsDialog
}

///////////////////////////////////////////////////////////////////////////
// Confirm or Cancel
///////////////////////////////////////////////////////////////////////////
open class BaseDialogBuilder(val context: Context) {

    companion object {
        internal const val NO_ID = 0
    }

    /**样式*/
    var style: Int = R.style.Theme_Dialog_Common_Transparent_Floating

    /**确认按钮*/
    @StringRes
    var positiveId: Int = NO_ID
        set(value) {
            positiveText = if (value == NO_ID) {
                null
            } else {
                context.getText(value)
            }
        }
    var positiveText: CharSequence? = context.getText(R.string.sure)

    /**否认按钮*/
    @StringRes
    var negativeId: Int = NO_ID
        set(value) {
            negativeText = if (value == NO_ID) {
                null
            } else {
                context.getText(value)
            }
        }
    var negativeText: CharSequence? = context.getText(R.string.cancel_)
    fun noNegative() {
        negativeText = null
    }

    /**自动取消*/
    var autoDismiss: Boolean = true
}

interface DialogController {
    var positiveEnable: Boolean
}

class ConfirmDialogBuilder(context: Context) : BaseDialogBuilder(context) {

    /**标题id*/
    @StringRes
    var titleId: Int = NO_ID
        set(value) {
            title = context.getText(value)
        }
    var title: CharSequence? = null
    /**标题的字体大小，单位为 sp*/
    var titleSize = 16F
    /**标题的字体颜色*/
    var titleColor = context.colorFromId(R.color.gray_level1)

    @StringRes
    var messageId = NO_ID
        set(value) {
            message = context.getText(value)
        }
    var message: CharSequence = "debug：请设置message"
    /**消息的字体大小，单位为 sp*/
    var messageSize = 14F
    /**消息的字体颜色*/
    var messageColor = context.colorFromId(R.color.gray_level1)
    /**消息对其方式*/
    var messageGravity: Int = Gravity.CENTER

    @StringRes
    var checkBoxId = NO_ID
        set(value) {
            checkBoxText = context.getText(value)
        }
    var checkBoxText: CharSequence = ""
    var checkBoxChecked = false

    //确认与取消
    var positiveListener: ((dialog: Dialog) -> Unit)? = null
    var positiveListener2: ((dialog: Dialog, isChecked: Boolean) -> Unit)? = null
    var negativeListener: ((dialog: Dialog) -> Unit)? = null

    //顶部 icon
    var iconId = NO_ID
}

fun showConfirmDialog(context: Context, builder: ConfirmDialogBuilder.() -> Unit): Dialog {
    val confirmDialogBuilder = ConfirmDialogBuilder(context)
    builder.invoke(confirmDialogBuilder)
    val confirmDialog = ConfirmDialog(confirmDialogBuilder)
    confirmDialog.show()
    return confirmDialog
}

fun Fragment.showConfirmDialog(builder: ConfirmDialogBuilder.() -> Unit): Dialog? {
    val context = this.context ?: return null
    return showConfirmDialog(context, builder)
}

fun Activity.showConfirmDialog(builder: ConfirmDialogBuilder.() -> Unit): Dialog {
    return showConfirmDialog(this, builder)
}

///////////////////////////////////////////////////////////////////////////
// Custom
///////////////////////////////////////////////////////////////////////////
class CustomDialogBuilder(context: Context) : BaseDialogBuilder(context) {
    var layoutId: Int = NO_ID
    var view: View? = null

    var onLayoutPrepared: ((view: View) -> Unit)? = null
    var positiveListener: ((dialog: Dialog) -> Unit)? = null
    var negativeListener: ((dialog: Dialog) -> Unit)? = null
}

fun showCustomDialog(context: Context, builder: CustomDialogBuilder.() -> Unit): Dialog {
    val confirmDialogBuilder = CustomDialogBuilder(context)
    builder.invoke(confirmDialogBuilder)
    val confirmDialog = CustomDialog(confirmDialogBuilder)
    confirmDialog.show()
    return confirmDialog
}

fun Fragment.showCustomDialog(builder: CustomDialogBuilder.() -> Unit): Dialog? {
    val context = this.context ?: return null
    return showCustomDialog(context, builder)
}

fun Activity.showCustomDialog(builder: CustomDialogBuilder.() -> Unit): Dialog {
    return showCustomDialog(this, builder)
}


///////////////////////////////////////////////////////////////////////////
// List
///////////////////////////////////////////////////////////////////////////
class ListDialogBuilder(context: Context) : BaseDialogBuilder(context) {

    /**标题id*/
    @StringRes
    var titleId: Int = NO_ID
        set(value) {
            title = context.getText(value)
        }
    var title: CharSequence? = null
    /**标题的字体大小，单位为 sp*/
    var titleSize = 16F
    /**标题的字体颜色*/
    var titleColor = context.colorFromId(R.color.gray_level1)

    @ArrayRes
    var itemsId: Int = NO_ID
        set(value) {
            items = context.resources.getTextArray(value)
        }
    var items: Array<CharSequence>? = null

    var adapter: RecyclerView.Adapter<*>? = null

    var selectedPosition: Int = 0

    var positiveListener: ((which: Int, item: CharSequence) -> Unit)? = null
    var negativeListener: (() -> Unit)? = null

    /**对话框最长高度（相对于屏幕高度）*/
    var maxWidthPercent = BaseDialog.DEFAULT_WIDTH_SIZE_PERCENT

    var onDialogPreparedListener: ((dialog: Dialog, dialogController: DialogController) -> Unit)? = null

}

fun showListDialog(context: Context, builder: ListDialogBuilder.() -> Unit): Dialog {
    val listDialogBuilder = ListDialogBuilder(context)
    builder.invoke(listDialogBuilder)
    val listDialog = ListDialog(listDialogBuilder)
    listDialog.show()
    return listDialog
}

fun Fragment.showListDialog(builder: ListDialogBuilder.() -> Unit): Dialog? {
    val context = this.context ?: return null
    return showListDialog(context, builder)
}

fun Activity.showListDialog(builder: ListDialogBuilder.() -> Unit): Dialog {
    return showListDialog(this, builder)
}

///////////////////////////////////////////////////////////////////////////
// BottomSheetDialog
///////////////////////////////////////////////////////////////////////////
class BottomSheetDialogBuilder(val context: Context) {

    /**default is ""*/
    var actionTextId = BaseDialogBuilder.NO_ID
        set(value) {
            actionText = context.getText(value)
        }
    /**default is ""，it means the action view is hidden by default*/
    var actionText: CharSequence = ""

    /**default is ""，it means the title view is hidden by default*/
    var titleText: CharSequence = ""
    /**default is empty*/
    var titleTextId = BaseDialogBuilder.NO_ID
        set(value) {
            titleText = context.getText(value)
        }

    var spaceViewGone: Boolean = false

    @Slide.GravityFlag
    var itemGravity: Int = Gravity.CENTER
    var itemTextColor: Int = context.colorFromId(R.color.gray_level2)
    var itemSelectedListener: ((which: Int, item: CharSequence) -> Unit)? = null
    var actionListener: (() -> Unit)? = null
    var items: List<CharSequence>? = null

    /**if you want to apply your own list style, you can provide a [customList] callback, then other properties(like [items]) will not be effective.*/
    var customList: ((dialog: Dialog, list: RecyclerView) -> Unit)? = null
}

fun showBottomSheetListDialog(context: Context, builder: BottomSheetDialogBuilder.() -> Unit): Dialog {
    val bottomSheetDialogBuilder = BottomSheetDialogBuilder(context)
    bottomSheetDialogBuilder.builder()
    val bottomSheetDialog = BottomSheetDialog(bottomSheetDialogBuilder)
    bottomSheetDialog.show()
    return bottomSheetDialog
}

fun Fragment.showBottomSheetListDialog(builder: BottomSheetDialogBuilder.() -> Unit): Dialog? {
    val context = this.context ?: return null
    return showBottomSheetListDialog(context, builder)
}

fun Activity.showBottomSheetListDialog(builder: BottomSheetDialogBuilder.() -> Unit): Dialog {
    return showBottomSheetListDialog(this, builder)
}


///////////////////////////////////////////////////////////////////////////
// ChildSex
///////////////////////////////////////////////////////////////////////////
fun Fragment.showSelectChildSexDialog(selectedSex: Int, onSelected: (Int) -> Unit): Dialog? {
    val context = this.context ?: return null
    return showSelectChildSexDialog(context, selectedSex, onSelected)
}

fun showSelectChildSexDialog(context: Context, selectedSex: Int, onSelected: (Int) -> Unit): Dialog {
    return SelectChildSexDialog(context, selectedSex, onSelected).also {
        it.setCanceledOnTouchOutside(false)
        it.show()
    }
}

///////////////////////////////////////////////////////////////////////////
// SelectDuration
///////////////////////////////////////////////////////////////////////////
class SelectDurationDialogBuilder(context: Context) : BaseDialogBuilder(context) {
    var durationRange: TimePeriod = TimePeriod(Time(0, 0), Time(23, 50))
    var limitedDuration: Time? = null
    var initDuration: Time? = null
    var positiveListener: ((Dialog, Time) -> Unit)? = null
    var negativeListener: ((Dialog) -> Unit)? = null
    var tips: CharSequence? = null// if set tips, the tips is always shown
    var must: Boolean = false//define whether must selecting a time
    var onOverLimitedTipsFactory: ((Time) -> CharSequence)? = null//if tips is empty, will call onOverLimitedTipsFactory
}

fun showSelectDurationDialog(context: Context, builder: SelectDurationDialogBuilder.() -> Unit): Dialog {
    val selectDurationDialogBuilder = SelectDurationDialogBuilder(context)
    builder.invoke(selectDurationDialogBuilder)
    val choiceDialog = SelectDurationDialog(selectDurationDialogBuilder)
    choiceDialog.show()
    return choiceDialog
}

fun Fragment.showSelectDurationDialog(builder: SelectDurationDialogBuilder.() -> Unit): Dialog? {
    val context = this.context ?: return null
    return showSelectDurationDialog(context, builder)
}

fun Activity.showSelectDurationDialog(builder: SelectDurationDialogBuilder.() -> Unit): Dialog {
    return showSelectDurationDialog(this, builder)
}

