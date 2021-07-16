package com.gwchina.parent.member.presentation.center

import android.support.v7.widget.GridLayoutManager
import android.util.Log
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.kotlin.ifNonNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.MemberPlan
import com.gwchina.parent.member.presentation.purchase.PurchaseDataMapper
import com.gwchina.sdk.base.data.models.isMember
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.member_fragment_center.*
import me.drakeet.multitype.register


/**
 * 会员中心页面构造器
 *
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 10:37
 */
internal class CenterLayoutManager(private val host: MemberCenterFragment, private val interactor: MemberCenterInteractor) {
    private val context = host.context ?: throw NullPointerException("host is not attached")

    private val memberRvContent = host.memberRvContent
    private val contentAdapter = MultiTypeAdapter(context)

    init {
        buildData()
        initContentListView()
    }

    companion object {
        const val json = """
           [{
	"gnw_appid": null,
	"sign": null,
	"version": null,
	"app_token": null,
	"source": null,
	"plan_id": "030cf31bce064d8da1e85efb5e874c32",
	"plan_code": "MP07011600JBD4",
	"plan_type": "01",
	"plan_type_label": "公开",
	"plan_name": "@6个月啦啦",
	"plan_label": "test92",
	"original_price": 588.6,
	"discount_price": 0.01,
	"valid_days": 180,
	"member_level": "001",
	"purchase_quota": 0,
	"discount_start_time": 1567008000000,
	"discount_end_time": 1608220800000,
	"row_order": 1,
	"is_delete": "0",
	"is_listing": "1",
	"is_listing_label": "已上架",
	"create_time": 1561968026000,
	"remark": "限时五折",
	"is_member": "0",
	"device_type": "03",
	"device_type_label": "Android",
	"discount_flag": "1",
	"member_item_list": [{
		"member_item_name": "守护时间",
		"member_item_desc": "设置孩子设备的使用时间计划",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhusj.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护应用",
		"member_item_desc": "设置应用的使用规则",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhuyy.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护报告",
		"member_item_desc": "支持查看近7天的守护报告",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhubg.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护多设备",
		"member_item_desc": "可同时守护多个孩子多个设备",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhudsb.png",
		"member_item_flag": "Y"
	}]
}, {
	"gnw_appid": null,
	"sign": null,
	"version": null,
	"app_token": null,
	"source": null,
	"plan_id": "810ad2f035644c7f8f4b6bbb2f8a1e3c",
	"plan_code": "MP09261628MwXV",
	"plan_type": "01",
	"plan_type_label": "公开",
	"plan_name": "测试一次",
	"plan_label": "123",
	"original_price": 1.0,
	"discount_price": 0.01,
	"valid_days": 7,
	"member_level": "001",
	"purchase_quota": 1,
	"discount_start_time": 1567526400000,
	"discount_end_time": 1640188800000,
	"row_order": 1,
	"is_delete": "0",
	"is_listing": "1",
	"is_listing_label": "已上架",
	"create_time": 1569486484000,
	"remark": "12个月会员+OPPO A5学生手机",
	"is_member": "0",
	"device_type": "03",
	"device_type_label": "Android",
	"discount_flag": "1",
	"member_item_list": [{
		"member_item_name": "守护时间",
		"member_item_desc": "设置孩子设备的使用时间计划",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhusj.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护应用",
		"member_item_desc": "设置应用的使用规则",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhuyy.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护报告",
		"member_item_desc": "支持查看近7天的守护报告",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhubg.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护多设备",
		"member_item_desc": "可同时守护多个孩子多个设备",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhudsb.png",
		"member_item_flag": "Y"
	}]
}, {
	"gnw_appid": null,
	"sign": null,
	"version": null,
	"app_token": null,
	"source": null,
	"plan_id": "037fbf5963764f5489e5e8dcf6cf9e4b",
	"plan_code": "MP07011604DTm8",
	"plan_type": "01",
	"plan_type_label": "公开",
	"plan_name": "12个月",
	"plan_label": "送2个月",
	"original_price": 998.0,
	"discount_price": 488.0,
	"valid_days": 425,
	"member_level": "001",
	"purchase_quota": 0,
	"discount_start_time": 1561824000000,
	"discount_end_time": 1563120000000,
	"row_order": 2,
	"is_delete": "0",
	"is_listing": "1",
	"is_listing_label": "已上架",
	"create_time": 1561968265000,
	"remark": "限时五折",
	"is_member": "0",
	"device_type": "03",
	"device_type_label": "Android",
	"discount_flag": "0",
	"member_item_list": [{
		"member_item_name": "守护时间",
		"member_item_desc": "设置孩子设备的使用时间计划",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhubg.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护多设备",
		"member_item_desc": "可同时守护多个孩子多个设备",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhudsb.png",
		"member_item_flag": "Y"
	}]
}, {
	"gnw_appid": null,
	"sign": null,
	"version": null,
	"app_token": null,
	"source": null,
	"plan_id": "03286d4bcfe24903b338058f35e37a71",
	"plan_code": "MP07011606woFi",
	"plan_type": "01",
	"plan_type_label": "公开",
	"plan_name": "24个月",
	"plan_label": "送12个月",
	"original_price": 1998.0,
	"discount_price": 998.0,
	"valid_days": 1095,
	"member_level": "001",
	"purchase_quota": 0,
	"discount_start_time": 1559318400000,
	"discount_end_time": 1561996800000,
	"row_order": 3,
	"is_delete": "0",
	"is_listing": "1",
	"is_listing_label": "已上架",
	"create_time": 1561968383000,
	"remark": "限时五折",
	"is_member": "0",
	"device_type": "03",
	"device_type_label": "Android",
	"discount_flag": "0",
	"member_item_list": [{
		"member_item_name": "守护时间",
		"member_item_desc": "设置孩子设备的使用时间计划",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhusj.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护应用",
		"member_item_desc": "设置应用的使用规则",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhuyy.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护报告",
		"member_item_desc": "支持查看近7天的守护报告",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhubg.png",
		"member_item_flag": "Y"
	}, {
		"member_item_name": "守护多设备",
		"member_item_desc": "可同时守护多个孩子多个设备",
		"member_item_icon": "https://file.gwchina.cn/greenguard/vip_icon_shouhudsb.png",
		"member_item_flag": "Y"
	}]
}] 
        """
    }

    /**模拟数据*/
    private fun testBuyData(): MemPlan {

        val list = com.gwchina.sdk.base.data.utils.JsonUtils.parseArray(json, MemberPlan::class.java)
        Log.e("result", list.toString())

        val list2 = PurchaseDataMapper(context).buildMemberPlanVO(list)

        return MemPlan(memberPlan=list2)
    }


    private fun buildData() {
        contentAdapter.replaceAll(mutableListOf<Any>(Cards.Top(), Cards.GoodsItemList(testBuyData()), Cards.PrivilegeGroupHeader(host.getString(R.string.guard_privileges))))
        interactor.member.ifNonNull {
            memberItemList.ifNonNull {
                for (item in this) {
                    contentAdapter.add(Cards.Privileges(item))
                }
            }

            memberServiceItemList.ifNonNull {
                contentAdapter.add(Cards.PrivilegeGroupHeader(host.getString(R.string.guard_service)))
                for (item in this) {
                    contentAdapter.add(Cards.Privileges(item))
                }
            }
        }
        contentAdapter.add(Cards.BottomTip())
    }

    private fun initContentListView() {
        val gridLayoutManager = GridLayoutManager(context, 3)
        memberRvContent.layoutManager = gridLayoutManager

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(i: Int): Int {
                //返回每个item占用的单位数量（每行有3个单位）
                val itemViewType = contentAdapter.getItemViewType(i)
                return if (itemViewType == 3) 1 else gridLayoutManager.spanCount
            }
        }

        with(contentAdapter) {
            //            setHasStableIds(true)
            register(TopItemViewBinder(interactor))
            register(GoodsItemViewBinder(interactor))
            register(PrivilegeGroupHeaderItemViewBinder(interactor))
            register(PrivilegeItemViewBinder(interactor))
            register(BottomTip(interactor))
        }

        memberRvContent.adapter = contentAdapter
    }

    fun bindDataVO(member: MemberCenterVO) {
        interactor.member = member
        buildData()
        contentAdapter.notifyDataSetChanged()
//        setupBtn(member)
    }

    private fun setupBtn(member: MemberCenterVO) {
        member.ifNonNull {
            if (member.user.isMember) {
                //已生效：续费会员
                host.btnMemberOpen.text = host.resources.getText(R.string.user_member_renew_member)
                host.btnMemberOpen.setOnClickListener { interactor.openMember() }
            } else {
                //已失效：开通会员
                host.btnMemberOpen.text = host.resources.getText(R.string.open_member)
                host.btnMemberOpen.setOnClickListener {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.MEMBERCENTER_BTN_RENEW)
                    interactor.openMember()
                }
            }
        }
    }
}