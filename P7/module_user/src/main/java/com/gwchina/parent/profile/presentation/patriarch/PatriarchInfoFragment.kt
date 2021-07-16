package com.gwchina.parent.profile.presentation.patriarch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.colorFromId
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.Navigator
import com.gwchina.parent.profile.data.DeliveryAddress
import com.gwchina.parent.profile.presentation.common.EnterNicknameDialogFragment
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.models.Patriarch
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.sync.SyncStateManager
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.composeDate
import com.gwchina.sdk.base.utils.hidePhoneNumber
import com.gwchina.sdk.base.utils.mapParentAvatarBig
import com.gwchina.sdk.base.utils.splitBirthday
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.picker.maxChildDate
import com.gwchina.sdk.base.widget.picker.minPatriarchDate
import com.gwchina.sdk.base.widget.picker.selectDate
import com.gwchina.sdk.base.widget.views.*
import kotlinx.android.synthetic.main.profile_fragment_patriarch_info.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * 家长信息
 *
 *@author Wangwb
 *        Date : 2018/12/24 5:31 PM
 *        Modified by Ztiany at 2019-03-18
 */
class PatriarchInfoFragment : InjectorBaseFragment() {

    companion object {
        private const val ENTRANCE_PATRIARCH_AVATAR = 1
        private const val ENTRANCE_PATRIARCH_NICK_NAME = 2
        private const val ENTRANCE_PATRIARCH_BIRTHDAY = 3
        private const val ENTRANCE_PATRIARCH_CITY = 4
        private const val ENTRANCE_PATRIARCH_ADDRESS = 5
        private const val ENTRANCE_PATRIARCH_CELLPHONE = 6
    }

    private val mCityPickerPresenter = CityPickerPresenter(this)

    @Inject
    lateinit var navigator: Navigator

    private lateinit var patriarchInfoViewModel: PatriarchInfoViewModel

    private var showEnterNicknameAction: (() -> Unit)? = null
    private var showEnterBirthdayAction: (() -> Unit)? = null

    private var mNickName: String = ""
    private var mBirthday: String = ""
    private var mAreaCode: String = ""
    private var mPatriarch: Patriarch? = null
    private var mDeliveryAddress: DeliveryAddress? = null

    private val itemManager by lazy {
        ItemManager(requireContext(), ItemConfiguration(colorFromId(R.color.gray_cutting_line)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patriarchInfoViewModel = getViewModel(viewModelFactory)
        subscribeViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_PERSONAL)

        patriarchInfoViewModel.getPatriarchData()

        mCityPickerPresenter.init()
    }

    override fun provideLayout() = R.layout.profile_fragment_patriarch_info

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        //adapter
        buildChildInfoAdapter()
        // click：退出登录
        btnLogout.setOnClickListener {
            logout()
        }
        patriarchInfoViewModel.refreshPatriarchInfoEventCenter
                .getRefreshAddressEvent.observe(this, Observer {
            patriarchInfoViewModel.getPatriarchData()
        })
    }

    private fun buildChildInfoAdapter() {
        itemManager.setup(rvInfo, buildPatriarchInfoItems())
        itemManager.setOnItemClickedListener { _, baseItem ->
            when (baseItem.id) {
                ENTRANCE_PATRIARCH_NICK_NAME -> showEnterNicknameAction?.invoke()
                ENTRANCE_PATRIARCH_BIRTHDAY -> showEnterBirthdayAction?.invoke()
                ENTRANCE_PATRIARCH_ADDRESS -> {
                    navigator.openAddressPage(mDeliveryAddress)
                }
                ENTRANCE_PATRIARCH_CITY -> {
                    mCityPickerPresenter.showDialog()
                }
            }
        }
        mCityPickerPresenter.onCitySelectListener = { _, _, districtCode, address ->
            mAreaCode = districtCode
            itemManager.updateTextItem(ENTRANCE_PATRIARCH_CITY) {
                it.subtitle = address
            }
            doUpdate()
        }
    }

    private fun buildPatriarchInfoItems(): List<BaseItem> {
        return mutableListOf(
                AvatarItem(ENTRANCE_PATRIARCH_AVATAR, title = getString(R.string.avatar)),
                TextItem(ENTRANCE_PATRIARCH_NICK_NAME, title = getString(R.string.nickname)),
                TextItem(ENTRANCE_PATRIARCH_CELLPHONE, title = getString(R.string.login_cellphone), hasArrow = false),
                TextItem(ENTRANCE_PATRIARCH_BIRTHDAY, title = "生日", subtitle = "待完善"),
                TextItem(ENTRANCE_PATRIARCH_CITY, title = "城市", subtitle = "待完善"),
                TextItem(ENTRANCE_PATRIARCH_ADDRESS, title = "收货地址", subtitle = "待完善")
        )
    }

    private fun subscribeViewModel() {
        patriarchInfoViewModel.user
                .observe(this, Observer {
                    it?.let(::showUserInfo)
                })

        patriarchInfoViewModel.patriarchDetail.observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                val patriarch = it?.patriarchDetail?.get()
                val addressList = it?.deliveryAddress?.get()
                if (addressList.isNullOrEmpty()) {
                    mDeliveryAddress = null
                    itemManager.updateTextItem(ENTRANCE_PATRIARCH_ADDRESS) {
                        it.subtitle = "待完善"
                    }
                } else {
                    mDeliveryAddress = addressList[0]
                    itemManager.updateTextItem(ENTRANCE_PATRIARCH_ADDRESS) {
                        it.subtitle = ""
                    }
                }

                patriarch?.apply {
                    mNickName = nick_name ?: ""
                    if (birthday != null)
                        mBirthday = birthday!!
                    mAreaCode = area_code ?: ""
                    setBirthday(birthday)
                    setAddress(this)
                }
                mPatriarch = patriarch
            }?.onLoading {
                showLoadingDialog()
            }?.onError {
                dismissLoadingDialog()
                errorHandler.handleError(it)
            }
        })
    }

    private fun setAddress(patriarch: Patriarch) {
        itemManager.updateTextItem(ENTRANCE_PATRIARCH_CITY) {
            it.subtitle = if ((patriarch.province_area_name + patriarch.city_area_name + patriarch.district_area_name).isNotEmpty())
                patriarch.province_area_name + patriarch.city_area_name + patriarch.district_area_name else "待完善"
        }
    }

    private fun showUserInfo(user: User) {
        if (!user.logined()) {
            activity?.supportFinishAfterTransition()
            return
        }
        itemManager.updateTextItem(ENTRANCE_PATRIARCH_NICK_NAME) {
            it.subtitle = user.patriarch.nick_name
        }

        itemManager.updateAvatarItem(ENTRANCE_PATRIARCH_AVATAR) {
            it.showAvatar = { iv ->
                iv.setImageResource(mapParentAvatarBig(user.currentChild?.p_relationship_code))
            }
        }

        itemManager.updateTextItem(ENTRANCE_PATRIARCH_CELLPHONE) {
            it.subtitle = hidePhoneNumber(user.patriarch.phone)
        }

        showEnterNicknameAction = {
            EnterNicknameDialogFragment.show(user.patriarch.nick_name ?: "", childFragmentManager) {
                mNickName = it
                doUpdate()
            }
        }
        showEnterBirthdayAction = {
            selectBirthday()
        }
    }

    private fun logout() {
        showConfirmDialog {
            message = getString(R.string.logout)
            positiveListener = {
                Timber.d("退出登录开始")
                patriarchInfoViewModel.doLogout()
                Timber.d("退出登录完成")

                //重置之前设备的状态
                SyncStateManager.reset()
            }
        }
    }

    private fun setBirthday(birthday: String?) {
        if (birthday.isNullOrEmpty() || birthday.length != 8) return
        val year = birthday.substring(0, 4)
        val monthOfYear = birthday.substring(4, 6)
        val dayOfMonth = birthday.substring(6, 8)
        itemManager.updateTextItem(ENTRANCE_PATRIARCH_BIRTHDAY) {
            it.subtitle = "$year-$monthOfYear-$dayOfMonth"
        }
    }

    private fun selectBirthday() {
        val splitBirthday: Calendar = if (mBirthday.isEmpty()) {
            splitBirthday("19850101").toCalendar()
        } else {
            splitBirthday(mBirthday).toCalendar()
        }
        selectDate {
            initDate = splitBirthday
            minDate = minPatriarchDate()
            maxDate = maxChildDate()
            onDateSelectedListener = { year: Int, monthOfYear: Int, dayOfMonth: Int ->
                //mPatriarch?.birthday = composeDate(year, monthOfYear, dayOfMonth)
                itemManager.updateTextItem(ENTRANCE_PATRIARCH_BIRTHDAY) {
                    it.subtitle = composeDate(year, monthOfYear, dayOfMonth, "-")
                }
                mBirthday = composeDate(year, monthOfYear, dayOfMonth)
                doUpdate()
                //update selected
                splitBirthday.set(Calendar.YEAR, year)
                splitBirthday.set(Calendar.MONTH, monthOfYear)
                splitBirthday.set(Calendar.DAY_OF_MONTH, dayOfMonth - 1)
            }
        }
    }

    private fun doUpdate(): LiveData<Resource<Any>> {
        return patriarchInfoViewModel.updatePatriarch(mNickName, mBirthday, mAreaCode)
    }

}