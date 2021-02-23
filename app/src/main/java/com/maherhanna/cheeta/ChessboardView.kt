package com.maherhanna.cheeta

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import java.util.jar.Attributes

class ChessboardView(context: Context,attrs: AttributeSet): androidx.appcompat.widget.AppCompatImageView(context,attrs) {


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


    }
}