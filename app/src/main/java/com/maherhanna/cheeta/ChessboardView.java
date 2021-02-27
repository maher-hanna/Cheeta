package com.maherhanna.cheeta;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView{
    public Drawing drawing;
    private Canvas piecesCanvas;
    private Bitmap piecesBitmap;
    public ChessboardView(Context context,AttributeSet attrs){
        super(context,attrs);


    }



    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        piecesBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        piecesCanvas = new Canvas(piecesBitmap);
        if(drawing != null){
            this.drawing.updateDrawingRects(new Rect(0,0,w,h));

        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(piecesBitmap,0,0,null);


    }



    public void drawPiece(Bitmap bitmap, RectF rect){
        if(piecesCanvas != null){
            piecesCanvas.drawBitmap(bitmap,null,rect,null);
            invalidate();
        }


    }
}