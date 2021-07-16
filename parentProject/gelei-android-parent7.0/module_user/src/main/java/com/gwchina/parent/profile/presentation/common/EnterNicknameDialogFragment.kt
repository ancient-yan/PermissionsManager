package com.gwchina.parent.profile.presentation.common

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
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.profile_fragment_modify_nickname.*
import java.util.regex.Pattern


/**
 *@author Wangwb
 *      Email: 253123123@qq.com
 *      Date : 2018-12-31 17:05
 *      Modified by Ztiany at 2019-03-18
 */
class EnterNicknameDialogFragment : BaseDialogFragment() {

    companion object {

        internal const val NICKNAME_KEY = "extra_nick_name"
        private const val REG = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t"

        fun show(originName: String, fragmentManager: FragmentManager, interactor: (name: String) -> LiveData<Resource<Any>>): EnterNicknameDialogFragment {
            return EnterNicknameDialogFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putString(NICKNAME_KEY, originName)
                        }
                        _interactor = interactor
                        show(fragmentManager, EnterNicknameDialogFragment::class.qualifiedName)
                    }
        }
    }

    private var _interactor: ((name: String) -> LiveData<Resource<Any>>)? = null
    private lateinit var originNickname: String

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
        return inflater.inflate(R.layout.profile_fragment_modify_nickname, container, false)
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        arguments?.run {
            originNickname = getString(NICKNAME_KEY, "")
            cetEnterNickname?.editText?.run {
                setText(originNickname)
                setSelection(length())
            }
        }

        view?.postDelayed({
            SoftKeyboardUtils.showSoftInput(cetEnterNickname?.editText)
        }, 500)

        gtlEnterNickname.menu.add(R.string.save)
                .setOnMenuItemClickListener {
                    doUpdateChecked()
                    true
                }.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        gtlEnterNickname.setOnNavigationOnClickListener {
            if (originNickname != cetEnterNickname.editText.textStr()) {
                showConfirmDialog {
                    messageId = R.string.save_nick_name_tips
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
            cetEnterNickname.editText.error = getString(R.string.child_nick_name_tips)
            return
        }

        if (TextUtils.isEmpty(content)) {
            showMessage(getString(R.string.nick_name_empty_tips))
            return
        }

        val interactor = _interactor
        if (interactor == null) {
            dismiss()
        } else {
            SoftKeyboardUtils.hideSoftInput(cetEnterNickname.editText)
            realDoUpdate(interactor.invoke(content))
        }
    }

    private fun realDoUpdate(liveData: LiveData<Resource<Any>>) {
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