package com.example.android.minipaint

import android.content.Context
import android.graphics.*
import android.os.StrictMode
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.example.minipaint.R
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.URL


// Stroke width for the the paint.
private const val STROKE_WIDTH = 14f

/**
 * Custom view that follows touch events to draw on a canvas.
 */
class MyCanvasView(context: Context) : View(context) {

    // Holds the path you are currently drawing.
    private var path = Path()

    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private lateinit var frame: Rect

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
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

        // Calculate a rectangular frame around the picture.
        val inset = 40
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        // Draw a frame around the canvas.
        extraCanvas.drawRect(frame, paint)
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
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
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
        sendPost(bitmapToByteArray())
    }

    private fun bitmapToByteArray(): ByteArray {
        val bitmap = extraBitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image = stream.toByteArray()
        return image
    }
    private fun get() {
        //TODO: rewrite to coroutine
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val client = OkHttpClient()
            val url = URL("http://192.168.178.66:5000/data")

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body!!.string()
            println("Response Body: $responseBody")

            val mapperAll = ObjectMapper()  // Jackson ObjectMapper
            val objData = mapperAll.readTree(responseBody)
            objData.forEachIndexed { index, jsonNode ->
                println("$index $jsonNode")
            }
        } catch (e: Exception) {
            println("Exception: $e")
        }
    }

    private fun sendPost(payload: ByteArray) {
        //TODO: rewrite to coroutine, post happens on main thread
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
        val okHttpClient = OkHttpClient()
        val requestBody = payload.toRequestBody()
        val request = Request.Builder()
                .method("POST", requestBody)
                .url("http://192.168.178.66:5000/image")
                .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failure: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                println("Response: $response")
            }
        })
    }

}

