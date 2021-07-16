package com.gwchina.parent.times.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.android.base.kotlin.dip
import com.android.base.kotlin.sp
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.debug.ifOpenDebug
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.TimePeriod
import timber.log.Timber
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 时间片段表格视图：
 *
 * - 视图的高度由**行数**和**行高**决定，使用时，使用时，应该设置 TimeSegmentTableView 的高度为 wrap_content。
 * - [setSelectedSegments] 所设置的时间段必须是排过序的。
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-09 14:03
 */
class TimeSegmentTableView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //attr
    private var row = 0
    private var rowHeight = 0
    private var column = 0
    private var boxValue = 60
    private var textSize = 0F
    private var textColorNormally = Color.BLACK
    private var textColorSelected = Color.WHITE
    private var boxLineColor = Color.GRAY
    private var boxLineWidth = 1F
    private var selectedBoxColor = Color.GREEN
    private var boxStartIndex = 0

    //values
    private var realBoxWidth = 0F
    private var realBoxHeight = 0F
    private var fontMetrics: Paint.FontMetrics = Paint.FontMetrics()
    private val boxes: List<Box>

    //ui tools
    private lateinit var paint: Paint
    private lateinit var gestureDetector: GestureDetector

    //box in dragging
    private var isSingleTap = false
    private var downBox: Box? = null
    private var currentDragBox: Box? = null
    private var boxRegionOffset = 0F
    private var isDragging: Boolean = false

    var timeSegmentTableViewListener: TimeSegmentTableViewListener? = null

    private var timePeriodList: List<TimePeriod>? = null

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.TimeSegmentTableView)) {
            row = getInteger(R.styleable.TimeSegmentTableView_tstv_row, 4)
            rowHeight = getDimensionPixelSize(R.styleable.TimeSegmentTableView_tstv_row_height, dip(45))
            column = getInteger(R.styleable.TimeSegmentTableView_tstv_column, 6)
            boxValue = getInteger(R.styleable.TimeSegmentTableView_tstv_box_value, 60)
            textSize = getDimension(R.styleable.TimeSegmentTableView_tstv_text_size, sp(14F))
            textColorNormally = getColor(R.styleable.TimeSegmentTableView_tstv_text_color, Color.BLACK)
            textColorSelected = getColor(R.styleable.TimeSegmentTableView_tstv_text_color_selected, Color.WHITE)
            boxLineColor = getColor(R.styleable.TimeSegmentTableView_tstv_box_line_color, Color.GRAY)
            boxLineWidth = getDimension(R.styleable.TimeSegmentTableView_tstv_box_line_width, 1F)
            selectedBoxColor = getColor(R.styleable.TimeSegmentTableView_tstv_selected_area_color, Color.GREEN)
            boxStartIndex = getInteger(R.styleable.TimeSegmentTableView_tstv_box_index_start, 0)
            recycle()
        }

        initUIElements()

        boxes = mutableListOf<Box>().apply {
            initBoxes(this)
        }
    }

    private fun initUIElements() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.strokeWidth = boxLineWidth
        paint.textSize = textSize
        paint.getFontMetrics(fontMetrics)

        boxRegionOffset = dip(1F)

        gestureDetector = GestureDetector(context, GestureListener())
    }

    private fun initBoxes(mutableList: MutableList<Box>) {
        //boxes
        val totalBox = row * column
        if (totalBox <= 0 || row <= 0 || column <= 0) {
            return
        }

        var mold: Int

        for (i in 0 until totalBox) {

            mold = (i) % column

            mutableList.add(Box(
                    boxStartIndex + i,
                    boxValue * i,
                    boxValue * (i + 1),
                    (i / column),
                    mold
            ))
        }
    }

    //draw---------------------------------------------------------------------------------------------------------------------------------
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = row * rowHeight
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(height, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        realBoxWidth = (w.toFloat() / column)
        realBoxHeight = (h.toFloat() / row)
        boxes.forEach {
            it.confirmBoxPosition()
        }
        updateBoxesBySegments(timePeriodList)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (boxes.isNotEmpty()) {
            drawBoxLines(canvas)
            drawSelectedBoxes(canvas)
            drawBoxIndex(canvas)
        }
    }

    private fun drawBoxLines(canvas: Canvas) {
        paint.color = boxLineColor
        paint.style = Paint.Style.STROKE

        var tempPosition: Float

        for (i in 0..row) {
            tempPosition = i.toFloat() * realBoxHeight
            if (i == 0) {
                tempPosition += boxLineWidth / 2
            } else if (i == row) {
                tempPosition -= boxLineWidth * 2
            }
            canvas.drawLine(0F, tempPosition, measuredWidth.toFloat(), tempPosition, paint)
        }

        for (i in 0..column) {
            tempPosition = i.toFloat() * realBoxWidth

            if (i == 0) {
                tempPosition += boxLineWidth / 2
            } else if (i == column) {
                tempPosition -= boxLineWidth / 2
            }
            canvas.drawLine(tempPosition, 0F, tempPosition, measuredHeight.toFloat(), paint)
        }
    }

    private fun drawSelectedBoxes(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = selectedBoxColor

        boxes.forEach {
            if (isDragging) {
                if (it.inDraggingRegion) {
                    if (downBox?.selected != true) {
                        canvas.drawRect(it.left, it.top, it.right, it.bottom, paint)
                    }
                } else if (it.selected) {
                    for (i in 0 until it.segmentCount) {
                        canvas.drawRect(it.segmentPositions[i][0], it.top, it.segmentPositions[i][1], it.bottom, paint)
                    }
                }
            } else if (it.selected) {
                for (i in 0 until it.segmentCount) {
                    canvas.drawRect(it.segmentPositions[i][0], it.top, it.segmentPositions[i][1], it.bottom, paint)
                }
            }
        }
    }

    private fun drawBoxIndex(canvas: Canvas) {
        var drawingText: String

        val offset = (fontMetrics.ascent + fontMetrics.bottom) / 2

        boxes.forEach {

            drawingText = it.index.toString()

            val textWidth = paint.measureText(drawingText)

            setBoxIndexColor(it)

            canvas.drawText(drawingText, it.centerX - textWidth / 2, it.centerY - offset, paint)
        }
    }

    private fun setBoxIndexColor(box: Box) {
        if (isDragging) {
            if (box.inDraggingRegion) {
                if (downBox?.selected != true) {
                    paint.color = textColorSelected
                } else {
                    paint.color = textColorNormally
                }
            } else if (box.selected) {
                paint.color = chooseIndexColorByBoxPercent(box)
            } else {
                paint.color = textColorNormally
            }
        } else if (box.selected) {
            paint.color = chooseIndexColorByBoxPercent(box)
        } else {
            paint.color = textColorNormally
        }
    }

    private fun chooseIndexColorByBoxPercent(it: Box): Int {
        return if (it.selectedPercent > 0.6F) {
            textColorSelected
        } else {
            textColorNormally
        }
    }

    //set values---------------------------------------------------------------------------------------------------------------------------------
    fun setSelectedSegments(selectedSegments: MutableList<TimePeriod>?) {
        if (selectedSegments.isNullOrEmpty()) {
            selectedSegments?.add(TimePeriod(Time(), Time(), type = 1))
        } else {
            if (selectedSegments.none { it.type == 1 }) {
                selectedSegments.add(TimePeriod(Time(), Time(), type = 1))
            }
        }
        timePeriodList = selectedSegments
        if (measuredWidth > 0) {
            updateBoxesBySegments(selectedSegments)
            postInvalidate()
        }
    }

    private fun updateBoxesBySegments(selectedSegments: List<TimePeriod>?) {
        ifOpenDebug {
            selectedSegments?.forEach {
                Timber.d("setSelectedSegments: ${it.start.toMinutes()}-${it.end.toMinutes()}")
            }
        }

        boxes.forEach {
            it.updateSelectingStatus(false)
        }

        if (selectedSegments.isNullOrEmpty()) {
            return
        }

        var processingIndex = 0
        var timePeriod: TimePeriod
        val timePeriodSize = selectedSegments.size

        boxes.forEach {

            if (processingIndex >= timePeriodSize) {
                return@forEach
            }

            it.startUpdateValue()

            for (i in processingIndex until timePeriodSize) {
                timePeriod = selectedSegments[i]

                if (it.isIncludeTime(timePeriod.start.toMinutes(), timePeriod.end.toMinutes())) {
                    it.addSegment(timePeriod.start.toMinutes(), timePeriod.end.toMinutes())
                    if (timePeriod.end.toMinutes() > it.endValue) {
                        processingIndex = i
                        break
                    }
                } else {
                    processingIndex = i
                    break
                }
            }

            it.endUpdateValue()
        }

        ifOpenDebug {
            boxes.forEach {
                Timber.d("box: $it")
            }
        }
    }

    //touch process---------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val onTouchEvent = gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {

            isDragging = false

            if (!isSingleTap) {
                Timber.d("onEnd")
                endDragging()
            }
        }
        return onTouchEvent
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(motionEvent: MotionEvent): Boolean {
            Timber.d("onDown")
            parent.requestDisallowInterceptTouchEvent(true)
            isSingleTap = false
            isDragging = false
            downBox = findBox(motionEvent)
            currentDragBox = downBox
            return true
        }

        override fun onScroll(downEvent: MotionEvent, movingEvent: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (!isDragging) {
                isDragging = true
            }
            val box = findBox(movingEvent)
            val curDownBox = downBox
            if (curDownBox != null && box != null) {
                if (currentDragBox != box) {
                    currentDragBox = box
                    updateWhenDragging(curDownBox, box)
                }
            }
            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            Timber.d("onSingleTapUp")

            isSingleTap = true

            val box = findBox(event)
            if (box != null) {
                if (box.selected) {
                    onClickArea(box, event)
                } else {
                    selectOneBox(box)
                }
            }

            return true
        }

    }

    private fun inBoxDraggingRegion(start: Box, end: Box, box: Box): Boolean {
        val startIndex = min(start.index, end.index)
        val endIndex = max(start.index, end.index)
        return box.index in startIndex..endIndex
    }

    private fun findBox(event: MotionEvent): Box? {
        return boxes.find {
            it.inBoxRegion(event.x, event.y)
        }
    }

    private fun updateWhenDragging(curDownBox: Box, box: Box) {
        boxes.forEach {
            it.inDraggingRegion = inBoxDraggingRegion(curDownBox, box, it)
        }
        invalidate()
    }

    private fun endDragging() {
        val curDownBox = downBox
        val endBox = currentDragBox

        if (curDownBox != null && endBox != null) {
            val selected = curDownBox.selected
            boxes.forEach {
                if (inBoxDraggingRegion(curDownBox, endBox, it)) {
                    it.updateSelectingStatus(!selected)
                }
            }
        }

        invalidate()
        combineTimePeriod()
    }

    private fun selectOneBox(box: Box) {
        box.updateSelectingStatus(true)
        invalidate()
        combineTimePeriod()
    }

    private fun combineTimePeriod() {
        val mutableList = mutableListOf<TimePeriod>()

        for (box in boxes) {
            if (!box.selected) {
                continue
            }
            for (i in 0 until box.segmentCount) {
                mutableList.add(TimePeriod.fromMinutes(box.segmentValues[i][0], box.segmentValues[i][1]))
            }
        }

        if (mutableList.isEmpty()) {
            timePeriodList = mutableList
            timeSegmentTableViewListener?.onTimeSegmentChanged(mutableList)
            return
        }

        var newSelectedPeriod = mutableList[0]
        val newList = mutableListOf<TimePeriod>()

        for (next in mutableList.subList(1, mutableList.size)) {
            newSelectedPeriod = if (newSelectedPeriod.hasIntersection(next)) {
                next.merge(newSelectedPeriod)
            } else {
                newList.add(newSelectedPeriod)
                next
            }
        }

        newList.add(newSelectedPeriod)

        timePeriodList = newList
        timeSegmentTableViewListener?.onTimeSegmentChanged(newList)

        ifOpenDebug {
            timePeriodList?.forEach {
                Timber.d("After OperateTimePeriod: ${it.start.toMinutes()}-${it.end.toMinutes()}")
            }
        }
    }

    private fun onClickArea(box: Box, event: MotionEvent) {
        val periodList = timePeriodList
        val listener = timeSegmentTableViewListener

        if (periodList.isNullOrEmpty() || listener == null) {
            return
        }

        val clickPeriod = if (box.segmentCount == 1) {
            TimePeriod.fromMinutes(box.segmentValues[0][0], box.segmentValues[0][1])
        } else {
            findPeriodInBox(box, event)
        }

        periodList.find {
            it.hasIntersection(clickPeriod)
        }?.let {
            Timber.d("Click TimePeriod: ${it.start.toMinutes()}-${it.end.toMinutes()}")
            listener.onTimeSegmentClicked(it, periodList)
        }
    }

    private fun findPeriodInBox(box: Box, event: MotionEvent): TimePeriod {
        val x = event.x
        val y = event.y
        for (i in 0 until box.segmentCount) {
            if (x >= box.segmentPositions[i][0] && y <= box.segmentPositions[i][1]) {
                return TimePeriod.fromMinutes(box.segmentValues[i][0], box.segmentValues[i][0])
            }
        }
        return TimePeriod.fromMinutes(box.segmentValues[0][0], box.segmentValues[0][0])
    }

    interface TimeSegmentTableViewListener {

        fun onTimeSegmentClicked(timePeriod: TimePeriod, timePeriodList: List<TimePeriod>)

        fun onTimeSegmentChanged(timePeriodList: List<TimePeriod>)

    }

    //Box---------------------------------------------------------------------------------------------------------------------------------

    inner class Box(
            val index: Int,
            private val startValue: Int,
            val endValue: Int,
            private val row: Int,//start with 0
            private val column: Int//start with 0
    ) {

        var left: Float = 0F
            private set
        var right: Float = 0F
            private set
        var top: Float = 0F
            private set
        var bottom: Float = 0F
            private set

        val centerX: Float
            get() = left + ((right - left) / 2)

        val centerY: Float
            get() = top + ((bottom - top) / 2)

        var selected: Boolean = false
            private set
        /**该Box内，有几个片段*/
        var segmentCount = 0
            private set
        /**真实数据对应的位置*/
        var segmentPositions = Array(1) { FloatArray(2) }
            private set
        /**在该Box内真实数据的范围*/
        var segmentValues = Array(1) { IntArray(2) }
            private set
        var selectedPercent = 0F

        private var isUpdatingVal = false
        var inDraggingRegion: Boolean = false

        fun startUpdateValue() {
            segmentCount = 0
            isUpdatingVal = true
        }

        fun endUpdateValue() {
            isUpdatingVal = false

            if (selected) {
                var totalSelected = 0F
                for (i in 0 until segmentCount) {
                    totalSelected += (segmentValues[i][1] - segmentValues[i][0])
                }
                selectedPercent = totalSelected / boxValue
            } else {
                selectedPercent = 0F
            }

        }

        fun updateSelectingStatus(selectingStatus: Boolean) {
            selected = selectingStatus
            startUpdateValue()
            if (selectingStatus) {
                addSegment(startValue, endValue)
            }
            endUpdateValue()
        }

        fun confirmBoxPosition() {
            left = realBoxWidth * column
            right = realBoxWidth * (column + 1)
            top = realBoxHeight * row
            bottom = realBoxHeight * (row + 1)
        }

        fun isIncludeTime(startMinute: Int, endMinute: Int): Boolean {
            if (startMinute >= endValue || endMinute <= startValue) {
                return false
            }
            return true
        }

        /**返回 true，表示该盒子已经和该时段不相交了*/
        fun addSegment(newStartValue: Int, newEndValue: Int) {
            if (!isUpdatingVal) {
                throw IllegalStateException()
            }

            if (!isIncludeTime(newStartValue, newEndValue)) {
                return
            }

            if (!selected) {
                selected = true
            }

            if (segmentCount > segmentPositions.size - 1) {
                val oldPositions = segmentPositions
                segmentPositions = Array(segmentCount * 2) { FloatArray(2) }
                System.arraycopy(oldPositions, 0, segmentPositions, 0, oldPositions.size)

                val oldValues = segmentValues
                segmentValues = Array(segmentCount * 2) { IntArray(2) }
                System.arraycopy(oldValues, 0, segmentValues, 0, oldValues.size)
            }

            val segmentPosition = segmentPositions[segmentCount]
            val segmentValue = segmentValues[segmentCount]

            segmentCount++

            if (newStartValue <= startValue) {
                segmentPosition[0] = left
                segmentValue[0] = startValue
            } else {
                segmentPosition[0] = left + (realBoxWidth * (newStartValue - startValue).toFloat() / boxValue)
                segmentValue[0] = newStartValue
            }

            if (newEndValue >= endValue) {
                segmentPosition[1] = right
                segmentValue[1] = endValue
            } else {
                segmentPosition[1] = left + (realBoxWidth * (newEndValue - startValue).toFloat() / boxValue)
                segmentValue[1] = newEndValue
            }

        }

        fun inBoxRegion(x: Float, y: Float): Boolean {
            return x >= left + boxRegionOffset && x <= right - boxRegionOffset && y >= top + boxRegionOffset && y <= bottom - boxRegionOffset
        }

        override fun toString(): String {
            val segmentValueString = segmentValues.joinToString(",", transform = { Arrays.toString(it) })
            return "Box(index=$index, startValue=$startValue, endValue=$endValue, selected=$selected, segmentCount=$segmentCount, segmentValues=$segmentValueString, selectedPercent=$selectedPercent)"
        }

    }

}