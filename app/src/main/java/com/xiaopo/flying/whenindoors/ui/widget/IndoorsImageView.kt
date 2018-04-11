package com.xiaopo.flying.whenindoors.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.dp
import com.xiaopo.flying.whenindoors.kits.logd
import com.xiaopo.flying.whenindoors.model.RoomPosition

/**
 * @author wupanjie
 */
internal typealias OnPickPositionListener = (pickedRoomX: Double, pickedRoomY: Double) -> Unit

internal typealias OnFingerprintTapListener = (roomPosition: RoomPosition, roomX: Double, roomY: Double) -> Unit

class IndoorsImageView @JvmOverloads constructor(context: Context,
                                                 attr: AttributeSet? = null,
                                                 defStyle: Int = 0)
  : ImageView(context, attr, defStyle) {


  private val assignedScaleType: ScaleType = scaleType
  private val controlMatrix = Matrix()
  private var hasFrame = false

  private val drawableBounds = RectF()
  private val currentBounds = RectF()
  private val gestureKiller = GestureKiller(context)

  private var dWidth = 0
  private var dHeight = 0

  private val grid = Grid()
  var needDrawGrid = true
    set(value) {
      field = value
      invalidate()
    }

  private val marks = arrayListOf<Mark>()
  var markPositions = arrayListOf<RoomPosition>()
    set(value) {
      field.clear()
      field.addAll(value)
      convertMarkPositions()
      invalidate()
    }

  private fun convertMarkPositions() {
    marks.clear()
    markPositions.forEach {
      val mark = Mark(it, dWidth.toDouble() / roomWidth * it.x, dHeight.toDouble() / roomHeight * it.y)
      val markLength = markDrawable?.intrinsicWidth ?: 0
      mark.bounds
          .set((mark.x - markLength / 2).toInt(), (mark.y - markLength / 2).toInt(), (mark.x + markLength / 2).toInt(), (mark.y + markLength / 2).toInt())
//      mark.bounds.offset(-markLength / 2, -markLength / 2)
      marks.add(mark)
    }
  }


  var markDrawable: Drawable? = null
    set(value) {
      field = value
      invalidate()
    }

  var gridColor = Color.BLACK
    set(value) {
      field = value
      invalidate()
    }

  var gridRow = 10
    set(value) {
      field = value
      grid.row = value
      invalidate()
    }

  var gridColumn = 10
    set(value) {
      field = value
      grid.column = value
      invalidate()
    }

  var roomWidth = 0.0
    set(value) {
      field = value
      invalidate()
    }

  var roomHeight = 0.0
    set(value) {
      field = value
      invalidate()
    }

  var onPickPositionListener: OnPickPositionListener? = null
  var onFingerprintTapListener: OnFingerprintTapListener? = null

  init {
    attr?.let {
      val typedArray = context.obtainStyledAttributes(attr, R.styleable.IndoorsImageView)

      gridColor = typedArray.getColor(R.styleable.IndoorsImageView_gridColor, Color.BLACK)
      gridRow = typedArray.getInt(R.styleable.IndoorsImageView_gridRow, 10)
      gridColumn = typedArray.getInt(R.styleable.IndoorsImageView_gridColumn, 10)
      typedArray.recycle()

      grid.paint.color = gridColor
      grid.row = gridRow
      grid.column = gridColumn
    }

    logd("scale type : $assignedScaleType")
    // 自己控制Matrix
    scaleType = ScaleType.MATRIX

    initDrawable()

//    gestureKiller.onNotTouchedListener = { x, y ->
//      logd("Not Touched")
//    }
//
//    gestureKiller.onLongPressListener = { x, y ->
//      logd("Long Touched -> ($x,$y)")
//    }

    gestureKiller.onSingleTapListener = { x, y ->
      controlMatrix.mapRect(currentBounds, drawableBounds)
      val left = currentBounds.left
      val top = currentBounds.top

      marks.forEach {
        if (it.isTouched(x, y)) {
          logd("Fingerprint Position:(${(it.roomPosition.x)}, ${(it.roomPosition.y)})")
          onFingerprintTapListener?.invoke(it.roomPosition, (x - left) / currentBounds.width() * roomWidth, (y - top) / currentBounds.height() * roomHeight)
          return@forEach
        }
      }



      logd("Touched Room Position:(${(x - left) / currentBounds.width() * roomWidth}, ${(y - top) / currentBounds.height() * roomHeight})")
      onPickPositionListener?.invoke((x - left) / currentBounds.width() * roomWidth, (y - top) / currentBounds.height() * roomHeight)
    }

    gestureKiller.onDoubleTapListener = { x, y ->

    }

    gestureKiller.onDragListener = { x, y ->
      controlMatrix.postTranslate(x, y)
      changeMatrix()
    }

//    gestureKiller.onReleaseListener = { x, y, gesture ->
//
//    }

    gestureKiller.onZoomAndRotateListener = { midX, midY, deltaZoom, deltaAngle, deltaMidPointX, deltaMidPointY ->
      controlMatrix.postScale(deltaZoom, deltaZoom, midX, midY)
      controlMatrix.postTranslate(deltaMidPointX, deltaMidPointY)
//      controlMatrix.postRotate(deltaAngle, currentBounds.centerX(), currentBounds.centerY())
      changeMatrix()
    }
  }

  private fun initDrawable() {
    post {
      convertMarkPositions()

      val scale: Float
      val dx: Float
      val dy: Float

      val vwidth = width - paddingLeft - paddingRight
      val vheight = height - paddingTop - paddingBottom

      if (dWidth <= vwidth && dHeight <= vheight) {
        scale = 1.0f
      } else {
        scale = Math.min(vwidth.toFloat() / dWidth.toFloat(),
            vheight.toFloat() / dHeight.toFloat())
      }

      dx = Math.round((vwidth - dWidth * scale) * 0.5f).toFloat()
      dy = Math.round((vheight - dHeight * scale) * 0.5f).toFloat()

      controlMatrix.setScale(scale, scale)
      controlMatrix.postTranslate(dx, dy)

      logd("drawable size : ($dWidth,$dHeight),scale : $scale, delta : ($dx,$dy)")

      changeMatrix()
    }
  }

  override fun invalidateDrawable(dr: Drawable?) {
    dr?.let {
      dWidth = it.intrinsicWidth
      dHeight = it.intrinsicHeight
    }
    super.invalidateDrawable(dr)
  }

  override fun setImageDrawable(drawable: Drawable?) {
    drawable?.let {
      dWidth = it.intrinsicWidth
      dHeight = it.intrinsicHeight
      initDrawable()
    }
    super.setImageDrawable(drawable)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    grid.bounds.set(0f, 0f, dWidth.toFloat(), dHeight.toFloat())
  }

  private fun changeMatrix() {
    imageMatrix = controlMatrix
    controlMatrix.mapRect(currentBounds, drawableBounds)
  }

  override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
    val changed = super.setFrame(l, t, r, b)
    hasFrame = true
    return changed
  }


  override fun onTouchEvent(event: MotionEvent): Boolean {
    drawable?.let {
      drawableBounds.set(0f, 0f, it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
      controlMatrix.mapRect(gestureKiller.detectBounds, drawableBounds)
    }
    return gestureKiller.fuckTouchEvent(event)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (needDrawGrid) {
      val count = canvas.save()
      canvas.concat(controlMatrix)
      grid.draw(canvas)
      grid.bounds.set(drawableBounds)
      canvas.restoreToCount(count)
    }

    drawMarks(canvas)
  }

  private fun drawMarks(canvas: Canvas) {
    marks.forEach {
      val count = canvas.save()
      canvas.concat(controlMatrix)
      markDrawable?.bounds = it.bounds
      markDrawable?.draw(canvas)
      canvas.restoreToCount(count)
    }
  }

  private inner class Grid {

    val bounds = RectF()
    val paint = Paint()

    var row = 10
    var column = 10

    init {
      paint.color = Color.BLUE
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1.dp.toFloat()
    }

    fun draw(canvas: Canvas) {
      val width = bounds.width()
      val height = bounds.height()

      val rowHeight = height / row
      val columnWidth = width / column

      for (i in 1 until row) {
        canvas.drawLine(0f, rowHeight * i, width, rowHeight * i, paint)
        canvas.drawLine(columnWidth * i, 0f, columnWidth * i, height, paint)
      }

    }

  }

  private inner class Mark(val roomPosition: RoomPosition,
                           var x: Double = 0.0,
                           var y: Double = 0.0) {
    val bounds = Rect()
    val mappedBounds = RectF()

    fun isTouched(x: Float, y: Float): Boolean {
      mappedBounds.set(bounds)
      controlMatrix.mapRect(mappedBounds)
      return mappedBounds.contains(x, y)
    }
  }
}