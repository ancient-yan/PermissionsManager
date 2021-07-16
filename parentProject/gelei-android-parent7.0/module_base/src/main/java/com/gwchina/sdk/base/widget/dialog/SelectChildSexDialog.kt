package com.gwchina.sdk.base.widget.dialog

import android.content.Context
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.data.api.SEX_FEMALE
import com.gwchina.sdk.base.data.api.SEX_MALE
import kotlinx.android.synthetic.main.dialog_child_sex_selection.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-26 16:26
 */
internal class SelectChildSexDialog(
        context: Context, selectedSex: Int,
        private val onSelected: (Int) -> Unit
) : BaseDialog(context) {

    private var _selectedSex = selectedSex

    init {
        setContentView(R.layout.dialog_child_sex_selection)
        if (selectedSex != SEX_FEMALE && selectedSex != SEX_MALE) {
            _selectedSex = SEX_MALE
        }
        setupViews()
        updateUI()
    }

    private fun setupViews() {
        //bottom
        dblDialogChildSex.onPositiveClick {
            onSelected(_selectedSex)
            dismiss()
        }
        dblDialogChildSex.onNegativeClick {
            dismiss()
        }
        //sex
        ivDialogChildSexMale.setOnClickListener {
            _selectedSex = SEX_MALE
            updateUI()
        }
        ivDialogChildSexFemale.setOnClickListener {
            _selectedSex = SEX_FEMALE
            updateUI()
        }
    }

    private fun updateUI() {
        if (_selectedSex == SEX_MALE) {
            ivDialogChildSexMaleFlag.visible()
            tvDialogChildSexMale.setTextColor(context.colorFromId(R.color.gray_level1))
            ivDialogChildSexFemaleFlag.invisible()
            tvDialogChildSexFemale.setTextColor(context.colorFromId(R.color.gray_level3))
        } else {
            ivDialogChildSexMaleFlag.invisible()
            tvDialogChildSexMale.setTextColor(context.colorFromId(R.color.gray_level3))
            ivDialogChildSexFemaleFlag.visible()
            tvDialogChildSexFemale.setTextColor(context.colorFromId(R.color.gray_level1))
        }
    }

}
