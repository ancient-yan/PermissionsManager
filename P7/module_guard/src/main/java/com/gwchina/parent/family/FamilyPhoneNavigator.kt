package com.gwchina.parent.family

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.support.v4.app.FragmentManager
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.data.Resource
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.data.model.Phone
import com.gwchina.parent.family.presentation.add.AddAndEditContactsFragment
import com.gwchina.parent.family.presentation.approval.FamilyPendingApprovalListFragment
import com.gwchina.parent.family.presentation.group.*
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject


@ActivityScope
class FamilyPhoneNavigator @Inject constructor(
        private val familyPhoneActivity: FamilyPhoneActivity,
        private val appRouter: AppRouter
) {

    fun openAddContactsPage(groupId: String? = null) {
        familyPhoneActivity.inFragmentTransaction {
            val addAndEditContactsFragment = AddAndEditContactsFragment()
            val bundle = Bundle()
            if (groupId != null) {
                bundle.putInt("type", AddAndEditContactsFragment.ADD_GROUP)
                bundle.putString("groupId", groupId)
            } else {
                bundle.putInt("type", AddAndEditContactsFragment.ADD_NORMAL)
            }
            addAndEditContactsFragment.arguments = bundle
            replaceWithStack(fragment = addAndEditContactsFragment)
        }
    }

    fun openEditContactsPage(phone: Phone) {
        familyPhoneActivity.inFragmentTransaction {
            val addAndEditContactsFragment = AddAndEditContactsFragment()
            val bundle = Bundle()
            bundle.putParcelable("item", phone)
            bundle.putInt("type", AddAndEditContactsFragment.EDIT)
            addAndEditContactsFragment.arguments = bundle
            replaceWithStack(fragment = addAndEditContactsFragment)
        }
    }

    fun openGroupManagePage() {
        familyPhoneActivity.inFragmentTransaction {
            val groupManageFragment = GroupManageFragment()
            replaceWithStack(fragment = groupManageFragment)
        }
    }

    fun openDelGroupPage() {
        familyPhoneActivity.inFragmentTransaction {
            val deleteGroupFragment = DeleteGroupFragment()
            replaceWithStack(fragment = deleteGroupFragment)
        }
    }

    fun openDelPhonePage(groupPhone: GroupPhone) {
        familyPhoneActivity.inFragmentTransaction {
            replaceWithStack(fragment = DeletePhoneFragment.getInstance(groupPhone))
        }
    }

    fun openGroupSettingPage(groupPhone: GroupPhone) {
        familyPhoneActivity.inFragmentTransaction {
            val groupSettingFragment = GroupSettingFragment()
            val bundle = Bundle()
            bundle.putParcelable("groupPhone", groupPhone)
            groupSettingFragment.arguments = bundle
            replaceWithStack(fragment = groupSettingFragment)
        }
    }

    fun openApprovalPage() {
        familyPhoneActivity.inFragmentTransaction {
            replaceWithStack(fragment = FamilyPendingApprovalListFragment())
        }
    }

    fun openGroupRangeManagePage() {
        familyPhoneActivity.inFragmentTransaction {
            replaceWithStack(fragment = GroupRangeManageFragment())
        }
    }


    fun openGroupModifyPage(groupName: String, childFragmentManager: FragmentManager, interactor: (content: String,isSuccess:Boolean) -> LiveData<Resource<Any>>) {
        ModifyGroupDialogFragment.show(groupName, childFragmentManager, interactor)
    }

    fun openMemberCenter() {
        appRouter.build(RouterPath.MemberCenter.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                .navigation()
    }
}