package com.oliveroneill.wilt.ui

import android.graphics.*
import com.squareup.picasso.Transformation

/**
 * A Picasso transformation that will convert an image to a circular one
 */
class PicassoCircleTransform : Transformation {
    override fun key() =  "circle"

    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) source.recycle()
        return Bitmap.createBitmap(size, size, source.config).also {
            val canvas = Canvas(it)
            val paint = Paint()
            paint.shader = BitmapShader(
                squaredBitmap,
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP
            )
            paint.isAntiAlias = true
            val radius = size / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            squaredBitmap.recycle()
        }
    }
}
