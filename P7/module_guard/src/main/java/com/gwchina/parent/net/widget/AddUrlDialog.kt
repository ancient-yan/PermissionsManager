package com.gwchina.parent.net.widget

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.android.base.kotlin.ifNull
import com.android.base.utils.android.ResourceUtils.getString
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.isContainEmoji
import com.gwchina.sdk.base.utils.subText
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.net_dialog_add_url.*


/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-17 16:32
 *      Desc : 添加网址dialog
 */

class AddUrlDialogBuilder(val context: Context) {

    companion object {
        internal const val add_blacklist_url = 0
        internal const val update_blacklist_url = 1
        internal const val add_whitelist_url = 2
        internal const val update_whitelist_url = 3
    }

    var url: String = ""
    var urlName: String? = null
    var type: Int = add_blacklist_url
    var positiveListener: ((dialog: AddUrlDialog, url: String, urlName: String) -> Unit)? = null
    var closeListener: ((dialog: AddUrlDialog) -> Unit)? = null

}

fun showAddUrlDialog(context: Context, builder: AddUrlDialogBuilder.() -> Unit): Dialog {
    val addUrlDialog = AddUrlDialogBuilder(context)
    builder.invoke(addUrlDialog)
    val dialog = AddUrlDialog(addUrlDialog)
    dialog.show()
    return dialog
}

class AddUrlDialog(builder: AddUrlDialogBuilder) : BaseDialog(builder.context) {

    var type: Int = AddUrlDialogBuilder.add_blacklist_url

    init {
        setContentView(R.layout.net_dialog_add_url)
        applyBuilder(builder)
    }

    fun setSubmitEnable(enable: Boolean) {
        net_tv_positive.isEnabled = enable
    }

    private fun applyBuilder(builder: AddUrlDialogBuilder) {
        setCanceledOnTouchOutside(false)
        this.type = builder.type

        net_tv_positive.setOnClickListener {

            val urlStr = net_et_url.text.toString().trim()
            val urlNameStr = net_et_name.text.toString().trim()
            if(urlStr.isContainEmoji()) {
                ToastUtils.showShort("非法字符")
                return@setOnClickListener
            }
            builder.positiveListener?.let {
                it(this, urlStr, urlNameStr)
            }
        }

        net_iv_close.setOnClickListener {
            dismiss()
            builder.closeListener?.let {
                it(this)
            }
        }

        when (builder.type) {
            AddUrlDialogBuilder.add_blacklist_url -> {
                net_tv_title.text = builder.context.getString(R.string.add_blacklist_url)
                net_et_url.hint = getString(R.string.blacklist_input_hint)
                net_ll_urlname_edit.visibility = View.GONE
            }

            AddUrlDialogBuilder.add_whitelist_url -> {
                net_tv_title.text = builder.context.getString(R.string.add_whitelist_url)
                net_et_url.hint = getString(R.string.whitelist_input_hint)
                net_ll_urlname_edit.visibility = View.GONE
            }

            AddUrlDialogBuilder.update_blacklist_url -> {
                net_tv_title.text = builder.context.getString(R.string.edit_url)
                net_ll_urlname_edit.visibility = View.VISIBLE
            }

            AddUrlDialogBuilder.update_whitelist_url -> {
                net_tv_title.text = builder.context.getString(R.string.edit_url)
                net_ll_urlname_edit.visibility = View.VISIBLE
            }
        }

        net_iv_url_clear.setOnClickListener {
            net_et_url.setText("")
        }

        net_et_url.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (net_et_url.text.toString().isNotEmpty()) {
                    // 获得焦点
                    net_iv_url_clear.visibility = View.VISIBLE
                } else {
                    net_iv_url_clear.visibility = View.GONE
                }
                net_ll_url_edit.setBackgroundResource(R.drawable.shape_gray_solid_stroke_round_5)
            } else {
                // 失去焦点
                net_iv_url_clear.visibility = View.GONE
                net_ll_url_edit.setBackgroundResource(R.drawable.shape_gray_solid_round_5)
            }
        }

        net_et_url.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val content = it.toString()
                    if (content.isEmpty()) {
                        net_tv_positive.isEnabled = false
                        net_iv_url_clear.visibility = View.GONE
                    } else {
                        net_tv_positive.isEnabled = true
                        if (net_et_url.hasFocus()) {
                            net_iv_url_clear.visibility = View.VISIBLE
                        }
                    }
                }.ifNull {
                    net_iv_url_clear.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        net_iv_name_clear.setOnClickListener {
            net_et_name.setText("")
        }

        net_et_name.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (net_et_name.text.toString().isNotEmpty()) {
                    // 获得焦点
                    net_iv_name_clear.visibility = View.VISIBLE
                } else {
                    net_iv_name_clear.visibility = View.GONE
                }
                net_ll_urlname_edit.setBackgroundResource(R.drawable.shape_gray_solid_stroke_round_5)
            } else {
                // 失去焦点
                net_iv_name_clear.visibility = View.GONE
                net_ll_urlname_edit.setBackgroundResource(R.drawable.shape_gray_solid_round_5)
            }

        }
        net_et_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = s?.let {
                val content = it.toString()
                if (content.isEmpty()) {
                    net_iv_name_clear.visibility = View.GONE
                } else if (net_et_name.hasFocus()) {
                    net_iv_name_clear.visibility = View.VISIBLE
                    val subText = content.subText(10, "")
                    if (subText != content) {
                        net_et_name.setText(subText)
                        net_et_name.setSelection(subText.length)
                    }
                }
            }.ifNull {
                net_iv_name_clear.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        net_et_url.setText(builder.url)
        net_et_url.setSelection(builder.url.length)
        net_et_name.setText(builder.urlName)
        net_et_name.setSelection(net_et_name.text.toString().length)

        tagClick()
    }

    fun cannotAddedTips(isShow: Boolean, message: String? = null) {
        if (isShow) {
            net_tv_cannot_added_tips.visibility = View.VISIBLE
            net_tv_cannot_added_tips.text = message
//            when (type) {
//                AddUrlDialogBuilder.add_blacklist_url,
//                AddUrlDialogBuilder.update_blacklist_url -> {
//                    net_tv_cannot_added_tips.text = context.getString(R.string.cannot_be_added_to_blacklist)
//                }
//                else -> {
//                    net_tv_cannot_added_tips.text = context.getString(R.string.cannot_be_added_to_whitelist)
//                }
//            }
        } else {
            net_tv_cannot_added_tips.visibility = View.GONE
        }
    }


    private fun tagClick() {
        net_tv_tag1.setOnClickListener {
            if (net_et_url.hasFocus())
                net_et_url.append(net_tv_tag1.text)
        }

        net_tv_tag2.setOnClickListener {
            if (net_et_url.hasFocus())
                net_et_url.append(net_tv_tag2.text)
        }


        net_tv_tag3.setOnClickListener {
            if (net_et_url.hasFocus())
                net_et_url.append(net_tv_tag3.text)
        }


        net_tv_tag4.setOnClickListener {
            if (net_et_url.hasFocus())
                net_et_url.append(net_tv_tag4.text)
        }

        net_tv_tag5.setOnClickListener {
            if (net_et_url.hasFocus())
                net_et_url.append(net_tv_tag5.text)
        }
    }

    override fun show() {
        super.show()
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        window!!.decorView.setPadding(0, 0, 0, 0)

        window!!.attributes = layoutParams
    }

}