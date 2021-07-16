package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.Observer
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import kotlinx.android.synthetic.main.migration_fragment_adding.*
import javax.inject.Inject

/**
 * 添加孩子
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 15:24
 */
class AddingChildFragment : InjectorBaseFragment() {

    @Inject lateinit var migrationNavigator: MigrationNavigator

    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    private val childAdapter by lazy {
        ChildAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.migration_fragment_adding

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        rvMigrationChild.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.bottom = dip(20)
            }
        })

        rvMigrationChild.adapter = childAdapter

        tvMigrationContinue.setOnClickListener {
            migrationNavigator.openChildInfoCollectingPage()
        }

        btnMigrationNextStep.setOnClickListener {
            migrationNavigator.openBelongingDevicePage()
        }
    }

    override fun onResume() {
        super.onResume()
        migrationViewModel.updateMigrationStep(MigratingData.MIGRATING_STEP_ADDING)
    }

    private fun subscribeViewModel() {
        migrationViewModel.childList.observe(this, Observer {
            childAdapter.setDataSource(it ?: emptyList(), true)
            if ((it?.size ?: 0 < 3)) {
                tvMigrationContinue.setTextColor(colorFromId(R.color.green_level1))
                tvMigrationContinue.setText(R.string.continue_to_add_child)
                tvMigrationContinue.isEnabled = true
            } else {
                tvMigrationContinue.setTextColor(colorFromId(R.color.gray_level2))
                tvMigrationContinue.text = getString(R.string.max_child_count_tips)
                tvMigrationContinue.isEnabled = false
            }
        })
    }

}