package com.gwchina.parent.family.presentation.group

import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.view.*
import com.android.base.app.fragment.BaseDialogFragment
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.colorFromId
import com.android.base.utils.android.SoftKeyboardUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.family_fragment_modify_group.*
import java.util.regex.Pattern


/**
 *@author Wangwb
 *      Email: 253123123@qq.com
 *      Date : 2018-12-31 17:05
 *      Modified by Ztiany at 2019-03-18
 */
class ModifyGroupDialogFragment : BaseDialogFragment() {

    companion object {

        internal const val GROUP_NAME_KEY = "group_nick_name"
        private const val REG = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t"

        fun show(originName: String, fragmentManager: FragmentManager, interactor: (name: String, isSuccess: Boolean) -> LiveData<Resource<Any>>): ModifyGroupDialogFragment {
            return ModifyGroupDialogFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putString(GROUP_NAME_KEY, originName)
                        }
                        _interactor = interactor
                        show(fragmentManager, ModifyGroupDialogFragment::class.qualifiedName)
                    }
        }
    }

    private var _interactor: ((name: String,isSuccess:Boolean) -> LiveData<Resource<Any>>)? = null
    private lateinit var originGroupname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog_Common)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.run {
            setBackgroundDrawable(ColorDrawable(colorFromId(R.color.gray_bg)))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val params = attributes
            params.windowAnimations = R.style.Style_Anim_Fragment_Scale_In
            this.attributes = params
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.family_fragment_modify_group, container, false)
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        arguments?.run {
            originGroupname = getString(GROUP_NAME_KEY, "")
            cetEnterNickname.editText.run {
                setText(originGroupname)
                setSelection(length())
            }
        }

        view?.postDelayed({
            SoftKeyboardUtils.showSoftInput(cetEnterNickname.editText)
        }, 500)

        gtlEnterNickname.menu.add(R.string.save)
                .setOnMenuItemClickListener {
                    doUpdateChecked()
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        gtlEnterNickname.setOnNavigationOnClickListener {
            if (originGroupname != cetEnterNickname.editText.textStr()) {
                showConfirmDialog {
                    messageId = R.string.save_group_name_tips
                    positiveListener = {
                        dismiss()
                    }
                }
            } else {
                dismiss()
            }
        }
    }

    private fun doUpdateChecked() {
        val content = cetEnterNickname.editText.textStr().trim()

        if (Pattern.compile(REG).matcher(content).find()) {
            cetEnterNickname.editText.error = getString(R.string.group_name_tips)
            return
        }

        if (TextUtils.isEmpty(content)) {
            showMessage(getString(R.string.group_name_empty_tips))
            return
        }

        val interactor = _interactor
        if (interactor == null) {
            dismiss()
        } else {
            SoftKeyboardUtils.hideSoftInput(cetEnterNickname.editText)
            realDoUpdate(content,interactor.invoke(content,false))
        }
    }

    private fun realDoUpdate(content:String,liveData: LiveData<Resource<Any>>) {
        liveData.observe(this, Observer {
            it?.onLoading {
                showLoadingDialog(false)
            }?.onError { error ->
                AppContext.errorHandler().handleError(error)
                dismissLoadingDialog()
            }?.onSuccess {
                dismissLoadingDialog()
                dismiss()
                showMessage(R.string.save_successfully)
                _interactor?.invoke(content,true)
            }
        })
    }

    override fun dismiss() {
        SoftKeyboardUtils.hideSoftInput(cetEnterNickname.editText)
        super.dismiss()
    }

    override fun onResume() {
        super.onResume()
        if (_interactor == null) {
            dismiss()
        }
    }

}