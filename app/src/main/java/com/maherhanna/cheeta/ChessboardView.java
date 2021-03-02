package com.maherhanna.cheeta;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView{
    public Drawing drawing;
    private Canvas piecesCanvas;
    private Bitmap piecesBitmap;

    //for dragging a piece
    int draggedSquare;
    float xTouchStart;
    float yTouchStart;
    //---------------------

    public ChessboardView(Context context,AttributeSet attrs){
        super(context,attrs);
        xTouchStart = 0;
        yTouchStart = 0;

        //-1 means no dragging
        draggedSquare = -1;


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

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                draggedSquare = getTouchSquare(x,y);
                if(drawing.chessBoard.getPieceAt(draggedSquare) == null)
                {
                    draggedSquare = -1;
                }
                xTouchStart = x;
                yTouchStart = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if(draggedSquare != -1) {
                    drawing.dragPiece(draggedSquare, x - xTouchStart, y - yTouchStart);
                }
                break;
            case MotionEvent.ACTION_UP:
                xTouchStart = 0;
                yTouchStart = 0;
                if(draggedSquare == -1)break;
                drawing.chessBoard.requestMove(draggedSquare,getTouchSquare(x,y));
                clearBoard();
                drawing.drawAllPieces();
                draggedSquare = -1;
                break;

            default:

        }
        return true;

    }

    private int getTouchSquare(float x,float y){
        //cheass board y starts at bottom
        y = getHeight() - y - 1;
        int touchFile = (int) Math.floor(x / drawing.squareSize);
        int touchRank = (int) Math.floor(y /drawing.squareSize);
        return ChessBoard.GetPosition(touchFile,touchRank);

    }
    public void clearBoard(){
        piecesCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
    }
}