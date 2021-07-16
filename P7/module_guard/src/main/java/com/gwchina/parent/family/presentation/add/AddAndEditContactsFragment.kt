package com.gwchina.parent.family.presentation.add

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.utils.android.SoftKeyboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.donkingliang.labels.LabelsView
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.data.model.Phone
import com.gwchina.parent.family.presentation.home.FamilyPhoneViewModel
import com.gwchina.parent.family.widget.showAddAndUpdateGroupDialog
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.family_fragment_add_edit_contacts.*
import javax.inject.Inject


class AddAndEditContactsFragment : InjectorBaseFragment() {

    companion object {
        const val ADD_NORMAL = 1
        const val EDIT = 2
        const val ADD_GROUP = 3
    }

    private var type = ADD_NORMAL
    private val groupDatas: MutableList<GroupPhone> = mutableListOf()
    private var phone: Phone? = null

    private val viewModel: FamilyPhoneViewModel by lazy { getViewModelFromActivity<FamilyPhoneViewModel>(viewModelFactory) }

    private var selGroupPhone: GroupPhone? = null

    //编辑时候的groupid
    private var currentGroupIdForEdit: String? = null

    @Inject
    lateinit var familyEventCenter: FamilyEventCenter

    override fun provideLayout(): Any? = R.layout.family_fragment_add_edit_contacts

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            type = it.getInt("type")
//            groupDatas.addAll(it.getParcelableArrayList("datas"))
            phone = it.getParcelable("item")
        }

        editTextListener()

        groupDatas.addAll(viewModel.groupPhones.filter { it.group_name != "未分组" })
        if (type == ADD_NORMAL) {
            tvFamilytitle.text = getString(R.string.add_family_phone)
            addLabel(getString(R.string.new_add_group), 0)
            labelsFamily.selectType = LabelsView.SelectType.MULTI
            labelsFamily.maxSelect = 2
            labelsFamily.setLabels(groupDatas, LabelsView.LabelTextProvider { _, _, data ->
                return@LabelTextProvider data.group_name.foldText(7)
            })
            labelsFamily.setCompulsorys(0)
            focusName()
        } else if (type == EDIT) {
            labelsFamily.selectType = LabelsView.SelectType.SINGLE
            tvFamilytitle.text = getString(R.string.edit_family_phone)
            var position = -1
            phone?.let {
                etFamilyName.setText(it.phone_remark)
                etFamilyPhone.setText(it.phone)
                groupDatas.forEachIndexed { index, groupPhone ->
                    if (it.group_id == groupPhone.group_id) {
                        selGroupPhone = groupPhone
                        position = index
                    }
                }
                if (position != -1) { //选中的排在最前面
                    val groupPhone = groupDatas[position]
                    groupDatas.removeAt(position)
                    groupDatas.add(0, groupPhone)
                }
                currentGroupIdForEdit = it.group_id
            }
            labelsFamily.setLabels(groupDatas, LabelsView.LabelTextProvider { _, _, data ->
                return@LabelTextProvider data.group_name.foldText(7)
            })
            if (position != -1) {
                labelsFamily.setSelects(0)
            }
            gtlFamilyAddEditBack.toolbar.apply {
                inflateMenu(R.menu.family_del_menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.familyDel -> {
                            showConfirmDialog {
                                this.message = getString(R.string.del_family_num)
                                this.positiveListener = {
                                    deleteFamilyPhone()
                                }
                            }
                        }
                    }
                    true
                }
            }
        } else {
            labelsFamily.selectType = LabelsView.SelectType.SINGLE
            tvFamilytitle.text = getString(R.string.add_family_phone)
            addLabel(getString(R.string.new_add_group), 0)
            var position = -1
            val groupId = arguments?.getString("groupId")
            groupDatas.forEachIndexed { index, groupPhone ->
                if (groupId == groupPhone.group_id) {
                    selGroupPhone = groupPhone
                    position = index
                }
            }
            if (position != -1) { //选中的排在最前面
                val groupPhone = groupDatas[position]
                groupDatas.removeAt(position)
                groupDatas.add(1, groupPhone)
            }
            currentGroupIdForEdit = groupId
            labelsFamily.setLabels(groupDatas, LabelsView.LabelTextProvider { _, _, data ->
                return@LabelTextProvider data.group_name.foldText(7)
            })
            if (position != -1) {
                labelsFamily.setSelects(1)
            }
        }
        gtlFamilyAddEditBack.setOnNavigationOnClickListener {
            onBackPressed()
        }
        initListener()
    }

    //name自动获取焦点 拉起软键盘
    private fun focusName() {
        etFamilyName.isFocusable = true
        etFamilyName.isFocusableInTouchMode = true
        etFamilyName.requestFocus()
        SoftKeyboardUtils.showSoftInput(activity)
    }

    private fun deleteFamilyPhone() {
        var ruleId = ""
        phone?.let {
            ruleId = it.rule_id
        }
        viewModel.delFamilyPhone(ruleId)
                .observe(this@AddAndEditContactsFragment, Observer {
                    it?.onSuccess {
                        dismissLoadingDialog()
                        ToastUtils.showShort("已删除")
                        familyEventCenter.notifyGroupPhoneListNeedRefresh()
                        exitFragment(false)
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }?.onLoading {
                        showLoadingDialog()
                    }
                })
    }

    private fun exitByEdit(needTips: Boolean) {
        if (needTips) {
            showConfirmDialog(requireContext()) {
                messageId = R.string.edit_exit_tips
                positiveListener = {
                    exitFragment(false)
                }
            }
        } else {
            exitFragment(false)
        }
    }

    private fun initListener() {
        labelsLayoutListener()
        btnFamilyOpenClose.setOnClickListener {
            if (it.visibility == View.INVISIBLE) {
                return@setOnClickListener
            }
            if (labelsFamily.maxLines == 1) {
                labelsFamily.maxLines = 0
                btnFamilyOpenClose.text = getString(R.string.fold)
                val drawable = resources.getDrawable(R.drawable.family_icon_arrow_up)
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                btnFamilyOpenClose.setCompoundDrawables(null, null, drawable, null)
            } else {
                labelsFamily.maxLines = 1
                btnFamilyOpenClose.text = getString(R.string.open_more)
                val drawable = resources.getDrawable(R.drawable.family_icon_arrow_down)
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                btnFamilyOpenClose.setCompoundDrawables(null, null, drawable, null)
            }
            refreshGroupList()
        }

        labelsFamily.setOnLabelClickListener { label, data, position ->
            if ((type == ADD_NORMAL || type == ADD_GROUP) && position == 0) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_ADDNUMBER_ADDGROUP)
                //添加标签
                showAddAndUpdateGroupDialog(requireContext()) {
                    positiveListener = { dialog, groupName ->
                        StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_ADDNUMBER_ADDGROUP_FINISH)
                        viewModel.addGroupAndRefreshGroupList(groupName)
                                .observe(this@AddAndEditContactsFragment, Observer {
                                    it?.onSuccess { datas ->
                                        dismissLoadingDialog()
                                        groupDatas.clear()
                                        datas?.let { list -> groupDatas.addAll(list.filter { it.group_name != "未分组" }) }
                                        addLabel(getString(R.string.new_add_group), 0)
                                        //设置第0、1个标签为选中状态
                                        labelsFamily.setSelects(0,1)
                                        selGroupPhone = groupDatas[1]
                                        currentGroupIdForEdit = selGroupPhone?.group_id

                                        labelsLayoutListener()
                                        refreshGroupList()
                                        familyEventCenter.notifyGroupPhoneListNeedRefresh()
                                        dialog.dismiss()
                                        viewModel.getAllGroupPhone()
                                    }?.onError { throwable ->
                                        dismissLoadingDialog()
//                                        errorHandler.handleError(throwable)
                                        dialog.cannotAddedTips(true, errorHandler.createMessage(throwable).toString())
                                    }?.onLoading {
                                        showLoadingDialog()
                                        dialog.cannotAddedTips(false)
                                    }
                                })
                    }
                }

            } else if (label.isSelected) {
                selGroupPhone = data as GroupPhone?
                currentGroupIdForEdit = selGroupPhone?.group_id
            } else if (selGroupPhone == data as GroupPhone?) {
                selGroupPhone = null
            }
        }

        btnFamilyComplete.setOnClickListener {
            val name = etFamilyName.text.trim().toString()
            val phoneNum = etFamilyPhone.text.trim().toString()
            var groupName = ""
            var groupId = ""
            if (type == ADD_NORMAL || type == ADD_GROUP) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.FAMILYNUMBER_BTN_ADDNUMBER_FINISH)
                selGroupPhone?.let {
                    if (it.group_id.isNotBlank()) {
                        groupId = it.group_id
                    } else {
                        groupName = it.group_name?:""
                    }
                }
                viewModel.addFamilyPhone(phoneNum, name, groupName, groupId)
                        .observe(this, Observer {
                            it?.onSuccess {
                                dismissLoadingDialog()
                                ToastUtils.showShort("已添加")
                                familyEventCenter.notifyGroupPhoneListNeedRefresh()
                                //刷新分组管理
                                if (type == ADD_GROUP) {
                                    viewModel.getAllGroupPhone()
                                }
                                exitFragment(false)
                            }?.onError {
                                dismissLoadingDialog()
                                errorHandler.handleError(it)
                            }?.onLoading {
                                showLoadingDialog()
                            }
                        })
            } else {
                selGroupPhone?.let {
                    groupId = it.group_id
                }

                var ruleId = ""
                phone?.let {
                    ruleId = it.rule_id
                }

                viewModel.editFamilyPhone(phoneNum, name, groupId, ruleId)
                        .observe(this, Observer {
                            it?.onSuccess {
                                dismissLoadingDialog()
                                ToastUtils.showShort("保存成功")
                                familyEventCenter.notifyGroupPhoneListNeedRefresh()
                                exitFragment(false)
                            }?.onError {
                                dismissLoadingDialog()
                                errorHandler.handleError(it)
                            }?.onLoading {
                                showLoadingDialog()
                            }
                        })
            }
        }
        rootLayout.setOnClickListener {
            SoftKeyboardUtils.hideSoftInput(activity)
        }
    }

    private fun labelsLayoutListener() {
        labelsFamily.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                labelsFamily.viewTreeObserver.removeOnGlobalLayoutListener(this)
                btnFamilyOpenClose.visibility = if (labelsFamily.isMultipleLines) View.VISIBLE else View.INVISIBLE
            }
        })
    }

    private fun refreshGroupList() {
        val selectLabels = ArrayList<Int>(labelsFamily.selectLabels)
        labelsFamily.setLabels(groupDatas, LabelsView.LabelTextProvider { _, _, data ->
            return@LabelTextProvider data.group_name.foldText(7)
        })
        labelsFamily.setSelects(selectLabels)
    }

    private fun addLabel(groupName: String, index: Int = -1) {
        val groupPhone = GroupPhone()
        groupPhone.group_name = groupName
        if (index > -1) {
            groupDatas.add(index, groupPhone)
        } else {
            groupDatas.add(groupPhone)
        }
    }

    private fun editTextListener() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btnFamilyComplete.isEnabled = (etFamilyName.text != null && etFamilyName.text.isNotEmpty() && etFamilyPhone.text != null && etFamilyPhone.text.isNotEmpty())
                ivNameClear.visibility = if (etFamilyName.text.isNotEmpty()) View.VISIBLE else View.GONE
                ivPhoneClear.visibility = if (etFamilyPhone.text.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
        etFamilyName.addTextChangedListener(textWatcher)
        etFamilyPhone.addTextChangedListener(textWatcher)
        ivNameClear.setOnClickListener { etFamilyName.setText("") }
        ivPhoneClear.setOnClickListener { etFamilyPhone.setText("") }
    }


    private fun checkHasChanged(): Boolean {
        return etFamilyName.text.toString() != phone?.phone_remark || etFamilyPhone.text.toString() != phone?.phone || phone?.group_id != currentGroupIdForEdit
    }

    private fun checkHaveContent(): Boolean {
        return etFamilyName.text.toString().isNotEmpty() || etFamilyPhone.text.toString().isNotEmpty()
    }

    override fun onBackPressed(): Boolean {
        SoftKeyboardUtils.hideSoftInput(activity)
        return if (type == EDIT) {
            exitByEdit(checkHasChanged())
            true
        } else {
            exitByEdit(checkHaveContent())
            true
        }
    }

}