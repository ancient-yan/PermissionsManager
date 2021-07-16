package com.gwchina.parent.member.widget

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.user.R
import kotlinx.android.synthetic.main.member_dialog_payment.*

class PaymentDialogBuilder(val context: Context) {
    companion object {
        const val WX_PAY = 0
        const val ALI_PAY = 1
    }

    /**总费用*/
    var costText: CharSequence = ""
    /**商品信息*/
    var goodsText: CharSequence = ""
    /**默认支付方式*/
    var defPayType = WX_PAY
    var actionListener: ((payType: Int) -> Unit)? = null
}

fun showPaymentDialog(context: Context, builder: PaymentDialogBuilder.() -> Unit): Dialog {
    val paymentDialogBuilder = PaymentDialogBuilder(context)
    paymentDialogBuilder.builder()
    val paymentDialog = PaymentDialog(paymentDialogBuilder)
    paymentDialog.show()
    return paymentDialog
}

/**
 * 支付对话框
 *
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-25 10:02
 */
class PaymentDialog(private val builder: PaymentDialogBuilder) : AppCompatDialog(builder.context, R.style.Theme_Dialog_Common_Transparent) {

    init {
        setContentView(R.layout.member_dialog_payment)
        setupConfirmAction()
        setupTitle()
        setupPayment()
        fixDialogHeight()
    }

    private fun fixDialogHeight() {
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()
        val realScreenWidth = if (screenWidth < screenHeight) screenWidth else screenHeight
        val realScreenHeight = if (screenWidth < screenHeight) screenHeight else screenWidth

        val maxHeight = (realScreenHeight * 0.7F).toInt()

        clDialogPay.measure(
                View.MeasureSpec.makeMeasureSpec(realScreenWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(realScreenHeight, View.MeasureSpec.AT_MOST))

        window?.let {
            // 在底部，宽度撑满
            val params = it.attributes
            params.windowAnimations = R.style.Style_Anim_Bottom_In
            params.gravity = Gravity.BOTTOM or Gravity.CENTER
            params.width = realScreenWidth
            if (clDialogPay.measuredHeight > maxHeight) {
                params.height = maxHeight
            } else {
                params.height = clDialogPay.measuredHeight
            }
            it.attributes = params
        }
    }

    private fun setupTitle() {
        tvDialogPayPrice.text = builder.costText
        tvDialogPayDesc.text = builder.goodsText
    }

    private fun setupPayment() {
        when (builder.defPayType) {
            PaymentDialogBuilder.WX_PAY -> {
                rbDialogPayWx.isChecked = true
            }
            PaymentDialogBuilder.ALI_PAY -> {
                rbDialogPayAli.isChecked = true
            }
            else -> {
                //add more
            }
        }
    }

    private fun setupConfirmAction() {
        btnDialogPay.setOnClickListener {
            dismiss()
            var payType: Int = PaymentDialogBuilder.WX_PAY
            when (rgDialogPay.checkedRadioButtonId) {
                R.id.rbDialogPayWx -> {
                    payType = PaymentDialogBuilder.WX_PAY
                }
                R.id.rbDialogPayAli -> {
                    payType = PaymentDialogBuilder.ALI_PAY
                }
                else -> {
                    //add more
                }
            }
            builder.actionListener?.invoke(payType)
        }
    }

    override fun show() {
        //https://stackoverflow.com/questions/23520892/unable-to-hide-navigation-bar-during-alertdialog-logindialog
        window?.run {
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }

        super.show()
    }

}