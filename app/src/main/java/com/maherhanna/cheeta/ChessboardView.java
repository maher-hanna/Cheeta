package com.maherhanna.cheeta;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import java.util.jar.Attributes;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView{
    public ChessboardView(Context context,AttributeSet attrs){
        super(context,attrs);
    }



    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}