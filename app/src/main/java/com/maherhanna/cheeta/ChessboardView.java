package com.maherhanna.cheeta;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView{
    public Drawing drawing;
    private Canvas piecesCanvas;
    private Bitmap piecesBitmap;
    private Paint highlightPaint;

    //for dragging a piece
    int draggedSquare;
    int selectedSquare;
    float xTouchStart;
    float yTouchStart;
    //---------------------

    public ChessboardView(Context context,AttributeSet attrs){
        super(context,attrs);
        xTouchStart = 0;
        yTouchStart = 0;

        //-1 means no dragging or selection
        draggedSquare = -1;
        selectedSquare = -1;

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(100);


    }



    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float horizontalPadding = (float)(getPaddingLeft() + getPaddingRight());
        float verticalPadding = (float)(getPaddingTop() + getPaddingBottom());
        float drawingWidth = w - horizontalPadding;
        float drawingHeight = h - verticalPadding;
        piecesBitmap = Bitmap.createBitmap((int)drawingWidth,(int)drawingHeight,Bitmap.Config.ARGB_8888);
        piecesCanvas = new Canvas(piecesBitmap);
        if(drawing != null){
            this.drawing.updateDrawingRects(new RectF(0,0,drawingWidth,drawingHeight));

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
    public void drawHighlight(RectF highlightRect){
        if(piecesCanvas != null){
            piecesCanvas.drawRect(highlightRect,highlightPaint);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        int targetPosition = getTouchSquare(x,y);
        Piece targetPiece = drawing.chessBoard.getPieceAt(targetPosition);

        boolean playerAtBottomPlayed = false;

        switch (action){
            case MotionEvent.ACTION_DOWN:


                if(targetPiece == null ||
                drawing.chessBoard.playerAtBottom instanceof ComputerPlayer ||
                targetPiece.color == drawing.chessBoard.playerAtTop.color)
                {
                    draggedSquare = -1;
                    break;
                }
                draggedSquare = targetPosition;
                selectedSquare = targetPosition;
                xTouchStart = x;
                yTouchStart = y;
                break;



            case MotionEvent.ACTION_MOVE:
                if(draggedSquare != -1) {
                    drawing.clearBoard();
                    drawing.drawHighlight(draggedSquare);
                    drawing.dragPiece(draggedSquare, x - xTouchStart, y - yTouchStart);
                    drawing.show();
                }
                break;



            case MotionEvent.ACTION_UP:
                xTouchStart = 0;
                yTouchStart = 0;



                if(draggedSquare != ChessBoard.OUT_OF_BOARD){
                    //check for selecting a square
                    if(draggedSquare == targetPosition){
                        selectedSquare = draggedSquare;
                        draggedSquare = -1;
                        drawing.clearBoard();
                        drawing.drawHighlight(selectedSquare);
                        drawing.drawAllPieces();
                        drawing.show();
                        break;
                    }


                    if(drawing.chessBoard.requestMove(draggedSquare,targetPosition)){
                        playerAtBottomPlayed = true;
                        drawing.clearBoard();
                        drawing.drawMoveHighlight(draggedSquare,targetPosition);
                        drawing.drawAllPieces();
                        draggedSquare = -1;
                        drawing.show();
                        break;
                    }
                    drawing.clearBoard();
                    drawing.drawHighlight(draggedSquare);
                    drawing.drawAllPieces();
                    draggedSquare = -1;
                    drawing.show();
                    break;
                }

                //if piece is selected
                if(selectedSquare != ChessBoard.OUT_OF_BOARD){
                    if(selectedSquare == targetPosition) break;
                    //check for selecting other piece
                    if(drawing.chessBoard.requestMove(selectedSquare,targetPosition)){
                        playerAtBottomPlayed = true;
                        drawing.clearBoard();
                        drawing.drawMoveHighlight(selectedSquare,targetPosition);
                        drawing.drawAllPieces();
                        drawing.show();
                        selectedSquare = -1;
                        break;

                    }
                    else {
                        if(targetPiece == null) break;
                        if(targetPiece.color == drawing.chessBoard.playerAtBottom.color){
                            selectedSquare = targetPosition;
                            clearBoard();
                            drawing.drawHighlight(selectedSquare);
                            drawing.drawAllPieces();
                            drawing.show();
                            break;
                        }

                    }


                }

                //--------------------

                break;



            default:

        }
        if(playerAtBottomPlayed == true){
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    drawing.chessBoard.playerAtTop.play();

                }
            },((ComputerPlayer)drawing.chessBoard.playerAtTop).playDelay);
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
        if(piecesCanvas != null){
            piecesCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);

        }
    }
}