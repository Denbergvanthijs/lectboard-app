package com.denbergvanthijs.lectboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.denbergvanthijs.lectboard.MenuActivity.Companion.localIpAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.abs


class MyCanvasView(context: Context) : View(context) {
    private var path = Path()

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorBlack, null)
    private val drawGridColor = ResourcesCompat.getColor(resources, R.color.colorGridPaint, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorOffWhite, null)
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 14f
    }

    private val gridPaint = Paint().apply {
        color = drawGridColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f
    }

    /**
     * Don't draw every single pixel.
     * If the finger has has moved less than this distance, don't draw. scaledTouchSlop, returns
     * the distance in pixels a touch can wander before we think the user is scrolling.
     */
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    /**
     * Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        for (i in 1..9) {
            extraCanvas.drawLine((i * width / 10).toFloat(), 0f, (i * width / 10).toFloat(), height.toFloat(), gridPaint);
            extraCanvas.drawLine(0f, (i * height / 10).toFloat(), width.toFloat(), (i * height / 10).toFloat(), gridPaint);
        }

        scope.launch {
            sendPost(bitmapToByteArray(), localIpAddress)
        }
    }


    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    /**
     * No need to call and implement MyCanvasView#performClick, because MyCanvasView custom view
     * does not handle click actions.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    /**
     * The following methods factor out what happens for different touch events,
     * as determined by the onTouchEvent() when statement.
     * This keeps the when conditional block
     * concise and makes it easier to change what happens for each event.
     * No need to call invalidate because we are not drawing anything.
     */
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to save it.
            extraCanvas.drawPath(path, paint)
        }
        // Invalidate() is inside the touchMove() under ACTION_MOVE because there are many other
        // types of motion events passed into this listener, and we don't want to invalidate the
        // view for those.
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()

        scope.launch {
            sendPost(bitmapToByteArray(), localIpAddress)
        }

    }


    private fun bitmapToByteArray(): ByteArray {
        val bitmap = extraBitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    private fun sendPost(payload: ByteArray, ip: String) {
        val okHttpClient = OkHttpClient()
        val requestBody = payload.toRequestBody()
        val request = Request.Builder()
                .method("POST", requestBody)
                .url("http://$ip:5000/image")
                .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failure: $e")
            }

            override fun onResponse(call: Call, response: Response) {
//                println("Response: $response")
            }
        })
    }

}

