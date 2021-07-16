package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.*
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.PhoneApprovalInfo
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.*
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.sdk.base.data.models.childCount
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.layoutCommonEdge
import kotlinx.android.synthetic.main.home_card_approval.view.*
import kotlinx.android.synthetic.main.home_card_approval_item_app.*
import kotlinx.android.synthetic.main.home_card_approval_item_app_abbreviation.*
import kotlinx.android.synthetic.main.home_card_approval_item_app_header.*
import kotlinx.android.synthetic.main.home_card_approval_item_phone.*
import me.drakeet.multitype.register

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 20:31
 */
class ApprovalCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var interactor: CardInteractor
    private val adapter = MultiTypeAdapter(context)
    private var approvalList: List<Any>? = null

    init {
        orientation = VERTICAL
        setPadding(layoutCommonEdge, 0, layoutCommonEdge, 0)
        View.inflate(context, R.layout.home_card_approval, this)
    }

    private fun setupList() {
        adapter.register(ApprovalClassItemViewBinder())
        val multiSoftApprovalItemViewBinder = MultiSoftApprovalItemViewBinder(interactor.host)
        adapter.register(multiSoftApprovalItemViewBinder)
        val softApprovalAbbreviationItemViewBinder = SoftApprovalAbbreviationItemViewBinder(interactor.host)
        adapter.register(softApprovalAbbreviationItemViewBinder)
        val multiPhoneApprovalItemViewBinder = PhoneApprovalItemViewBinder(interactor.host.requireContext()) {
            interactor.usingUser.childCount() > 1
        }
        adapter.register(multiPhoneApprovalItemViewBinder)
        val phoneApprovalAbbreviationItemViewBinder = PhoneApprovalAbbreviationItemViewBinder(interactor.host)
        adapter.register(phoneApprovalAbbreviationItemViewBinder)

        multiSoftApprovalItemViewBinder.setOnAllowListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_AUDITING_ALLOW)
            interactor.navigator.openAppApprovalPage(it)
        }

        multiSoftApprovalItemViewBinder.setOnForbidListener {
            interactor.cardDataProvider.forbidNewApp(it)
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_AUDITING_PROHIBIT)
        }

        softApprovalAbbreviationItemViewBinder.onCheckMoreListener = {
            interactor.navigator.openAppApprovalListPage()
        }

        multiPhoneApprovalItemViewBinder.onAllowListener = {
            interactor.cardDataProvider.approvalNewNumber(it, true)
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_FAMILYNUMBER_ALLOW)
        }

        multiPhoneApprovalItemViewBinder.onForbidListener = {
            interactor.cardDataProvider.approvalNewNumber(it, false)
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_FAMILYNUMBER_PROHIBIT)
        }

        phoneApprovalAbbreviationItemViewBinder.onCheckMoreListener = {
            interactor.navigator.openPhoneApprovalListPage()
        }

        rvHomeApproval.addItemDecoration(ApprovalCardItemDecoration(context, adapter))
        rvHomeApproval.adapter = adapter
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        setupList()
        subscribeData(cardInteractor)
    }

    private fun subscribeData(cardInteractor: CardInteractor) {
        cardInteractor.cardDataProvider.observeUser {
            if (!it.logined()) {
                adapter.setDataSource(emptyList(), true)
            }
        }

        cardInteractor.cardDataProvider.observeHomeData {
            if (approvalList != it?.approvalInfo?.approvalList) {
                adapter.setDataSource(it?.approvalInfo?.approvalList ?: emptyList(), true)
                approvalList = it?.approvalInfo?.approvalList
            }
        }
    }

}

private class ApprovalCardItemDecoration(context: Context, private val adapter: MultiTypeAdapter) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.colorFromId(R.color.gray_cutting_line)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        val item = adapter.getItem(childAdapterPosition) ?: return
        when {
            item is ApprovalClass && childAdapterPosition != 0 -> {
                outRect.set(0, dip(40), 0, 0)
            }
            item is MultiSoftApproval -> {
                outRect.set(dip(29), dip(18), dip(14), 0)
            }
            item is SoftApprovalAbbreviation || item is PhoneApprovalInfo || item is PhoneApprovalAbbreviation -> {
                outRect.set(dip(29), dip(12), dip(14), 0)
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        c.drawLine(dip(10F), 0F, dip(10F), parent.measuredHeight.toFloat(), paint)
    }

}

internal class ApprovalClassItemViewBinder : SimpleItemViewBinder<ApprovalClass>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = AppCompatTextView(parent.context).apply {
        compoundDrawablePadding = dip(9)
        setTextColor(colorFromId(R.color.gray_level2))
        textSize = 12F
        gravity = Gravity.CENTER_VERTICAL
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: ApprovalClass) {
        with((holder.itemView as TextView)) {
            if (item.type == ApprovalClass.PHONE) {
                setText(R.string.new_phone_approval_notice)
                leftDrawable(R.drawable.home_icon_phone_approval)
            } else {
                leftDrawable(R.drawable.home_icon_app_approval)
                setText(R.string.new_app_approval_notice)
            }
        }
    }
}

internal class MultiSoftApprovalItemViewBinder(host: Fragment) : SimpleItemViewBinder<MultiSoftApproval>() {

    private val softApprovalItemViewBinder = SoftApprovalItemViewBinder(host)

    fun setOnForbidListener(listener: ((SoftWrapper) -> Unit)?) {
        softApprovalItemViewBinder.onForbidListener = listener
    }

    fun setOnAllowListener(listener: ((SoftWrapper) -> Unit)?) {
        softApprovalItemViewBinder.onAllowListener = listener
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = RecyclerView(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutManager = LinearLayoutManager(parent.context)
        setPadding(dip(10), dip(20), dip(10), dip(20))
        isNestedScrollingEnabled = false
        setBackgroundResource(R.drawable.home_sel_approval_item_bg)
        adapter = MultiTypeAdapter(parent.context).apply {
            register(softApprovalItemViewBinder)
            register(SoftApprovalRelationItemViewBinder())
        }
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.top = dip((20))
                } else {
                    outRect.top = 0
                }
            }
        })
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: MultiSoftApproval) {
        ((holder.itemView as RecyclerView).adapter as MultiTypeAdapter).setDataSource(item.softList, true)
    }

}

internal class SoftApprovalItemViewBinder(private val host: Fragment) : SimpleItemViewBinder<SoftWrapper>() {

    var onForbidListener: ((SoftWrapper) -> Unit)? = null
    var onAllowListener: ((SoftWrapper) -> Unit)? = null

    private val _forbidClickListener = View.OnClickListener {
        onForbidListener?.invoke(it.tag as SoftWrapper)
    }

    private val _allowClickListener = View.OnClickListener {
        onAllowListener?.invoke(it.tag as SoftWrapper)
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_approval_item_app

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: SoftWrapper) {
        val softAudit = item.soft
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivHomeAuditsAppIcon, softAudit.soft_icon)
        viewHolder.tvHomeApprovalAppName.text = softAudit.soft_name.foldText(5)
        viewHolder.tvHomeApprovalAppCategory.text = softAudit.type_name
        viewHolder.tvHomeItemProhibit.tag = item
        viewHolder.tvHomeItemProhibit.setOnClickListener(_forbidClickListener)
        viewHolder.tvHomeItemAllow.tag = item
        viewHolder.tvHomeItemAllow.setOnClickListener(_allowClickListener)
    }

}

internal open class SoftApprovalRelationItemViewBinder : SimpleItemViewBinder<SoftApprovalRelation>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_approval_item_app_header

    override fun onBindViewHolder(holder: KtViewHolder, item: SoftApprovalRelation) {
        holder.ivHomeItemApprovalTitle.text = item.childName
        if (item.deviceIndex > 0) {
            holder.ivHomeItemApprovalIndex.visible()
            holder.ivHomeItemApprovalIndex.text = item.deviceIndex.toString()
        } else {
            holder.ivHomeItemApprovalIndex.invisible()
        }
    }

}

internal open class SoftApprovalAbbreviationItemViewBinder(private val host: Fragment) : SimpleItemViewBinder<SoftApprovalAbbreviation>() {

    var onCheckMoreListener: (() -> Unit)? = null

    private val onItemClickListener = View.OnClickListener {
        onCheckMoreListener?.invoke()
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_approval_item_app_abbreviation

    override fun onBindViewHolder(holder: KtViewHolder, item: SoftApprovalAbbreviation) {
        with(ImageLoaderFactory.getImageLoader()) {
            displayAppIcon(host, holder.tvHomeAppIcon1, item.appIcons[0])
            displayAppIcon(host, holder.tvHomeAppIcon2, item.appIcons[1])
            displayAppIcon(host, holder.tvHomeAppIcon3, item.appIcons[2])
        }
        holder.tvHomeMoreAppTips.text = host.getString(R.string.app_pending_approval_count_mask, item.size)

        holder.itemView.setOnClickListener(onItemClickListener)
    }

}

internal open class PhoneApprovalItemViewBinder(private val context: Context, private val showRequesterName: () -> Boolean) : SimpleItemViewBinder<PhoneApprovalInfo>() {

    var onForbidListener: ((PhoneApprovalInfo) -> Unit)? = null
    var onAllowListener: ((PhoneApprovalInfo) -> Unit)? = null

    private val _forbidClickListener = View.OnClickListener {
        onForbidListener?.invoke(it.tag as PhoneApprovalInfo)
    }

    private val _allowClickListener = View.OnClickListener {
        onAllowListener?.invoke(it.tag as PhoneApprovalInfo)
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_approval_item_phone

    override fun onBindViewHolder(holder: KtViewHolder, item: PhoneApprovalInfo) {

        if (showRequesterName()) {
            holder.tvHomePhoneApproval.text = context.getString(R.string.application_phone_content_mask, "“${item.user_name}”", item.phone_remark ?: "", item.phone)
        } else {
            holder.tvHomePhoneApproval.text = context.getString(R.string.application_phone_content_mask, "", item.phone_remark ?: "", item.phone)
        }

        holder.tvHomePhoneApprovalReason.text = context.getString(R.string.application_reason_mask, item.approval_reason)

        holder.tvHomeItemAllowPhone.setOnClickListener(_allowClickListener)
        holder.tvHomeItemAllowPhone.tag = item
        holder.tvHomeItemProhibitPhone.setOnClickListener(_forbidClickListener)
        holder.tvHomeItemProhibitPhone.tag = item
    }

}

internal open class PhoneApprovalAbbreviationItemViewBinder(private val host: Fragment) : SimpleItemViewBinder<PhoneApprovalAbbreviation>() {

    var onCheckMoreListener: (() -> Unit)? = null

    private val onItemClickListener = View.OnClickListener {
        onCheckMoreListener?.invoke()
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = AppCompatTextView(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        compoundDrawablePadding = dip(3)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dip(10), dip(20), dip(5), dip(20))
        setTextColor(colorFromId(R.color.gray_level1))
        textSize = 12F
        rightDrawable(R.drawable.icon_more)
        setBackgroundResource(R.drawable.home_sel_approval_item_bg)
        setOnClickListener(onItemClickListener)
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: PhoneApprovalAbbreviation) {
        (holder.itemView as TextView).text = host.getString(R.string.phone_pending_approval_count_mask, item.size)
    }

}