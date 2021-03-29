package com.maherhanna.cheeta;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView {
    public Drawing drawing;
    private Canvas piecesCanvas;
    private Bitmap piecesBitmap;
    private Paint highlightPaint;
    private Paint legalSquarePaint;
    private Paint legalSquarePaintHasPiece;

    //for dragging a piece
    int draggedSquare;
    int selectedSquare;
    float xTouchStart;
    float yTouchStart;
    //---------------------

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        xTouchStart = 0;
        yTouchStart = 0;

        //-1 means no dragging or selection
        draggedSquare = -1;
        selectedSquare = -1;

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(100);

        legalSquarePaint = new Paint();
        legalSquarePaint.setColor(Color.GRAY);
        legalSquarePaint.setAlpha(150);

        legalSquarePaintHasPiece = new Paint();
        legalSquarePaintHasPiece.setColor(Color.GRAY);
        legalSquarePaintHasPiece.setAlpha(150);
        legalSquarePaintHasPiece.setStrokeWidth(15);
        legalSquarePaintHasPiece.setStyle(Paint.Style.STROKE);


    }


    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float horizontalPadding = (float) (getPaddingLeft() + getPaddingRight());
        float verticalPadding = (float) (getPaddingTop() + getPaddingBottom());
        float drawingWidth = w - horizontalPadding;
        float drawingHeight = h - verticalPadding;
        piecesBitmap = Bitmap.createBitmap((int) drawingWidth, (int) drawingHeight, Bitmap.Config.ARGB_8888);
        piecesCanvas = new Canvas(piecesBitmap);
        if (drawing != null) {
            this.drawing.updateDrawingRects(new RectF(0, 0, drawingWidth, drawingHeight));

        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(piecesBitmap, 0, 0, null);


    }


    public void drawPiece(Bitmap bitmap, RectF rect) {
        if (piecesCanvas != null) {
            piecesCanvas.drawBitmap(bitmap, null, rect, null);

        }

    }

    public void drawHighlight(RectF highlightRect) {
        if (piecesCanvas != null) {
            piecesCanvas.drawRect(highlightRect, highlightPaint);
        }


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(drawing.isGameFinished()) return true;

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        boolean humanPlayed = false;
        Move humanMove = null;

        //keep x an y inside chessboard
        x = Math.max(0, Math.min(getWidth() - 1, x));
        y = Math.max(0, Math.min(getHeight() - 1, y));

        int targetPosition = getTouchSquare(x, y);
        Piece targetPiece = drawing.chessBoard.getPieceAt(targetPosition);

        switch (action) {

            case MotionEvent.ACTION_DOWN:

                if (drawing.canSelect(targetPosition) == false) {
                    draggedSquare = -1;

                    break;
                }
                draggedSquare = targetPosition;
                selectedSquare = targetPosition;
                xTouchStart = x;
                yTouchStart = y;
                drawing.drawAllPieces(targetPosition);
                break;


            case MotionEvent.ACTION_MOVE:

                if (draggedSquare != -1) {
                    drawing.dragPiece(draggedSquare,targetPosition, x - xTouchStart, y - yTouchStart);

                }
                break;


            case MotionEvent.ACTION_UP:
                xTouchStart = 0;
                yTouchStart = 0;


                if (draggedSquare != ChessBoard.OUT_OF_BOARD) {
                    //check for selecting a square
                    if (draggedSquare == targetPosition) {
                        selectedSquare = draggedSquare;
                        draggedSquare = -1;
                        drawing.drawAllPieces(selectedSquare);
                        break;
                    }


                    if (drawing.canMove(draggedSquare, targetPosition)) {
                        humanPlayed = true;
                        humanMove = new Move(draggedSquare,targetPosition);
                        drawing.drawAllPieces(humanMove);
                        draggedSquare = -1;
                        selectedSquare = -1;

                        break;
                    }
                    drawing.drawAllPieces(draggedSquare);
                    draggedSquare = -1;

                    break;
                }

                //if piece is selected
                if (selectedSquare != ChessBoard.OUT_OF_BOARD) {
                    if (selectedSquare == targetPosition) break;
                    //check for selecting other piece
                    if (drawing.chessBoard.canMove(selectedSquare, targetPosition)) {
                        humanPlayed = true;
                        humanMove = new Move(selectedSquare,targetPosition);
                        drawing.drawAllPieces(humanMove);
                        selectedSquare = -1;
                        break;

                    } else {
                        if (targetPiece == null) break;
                        if (targetPiece.color == drawing.chessBoard.bottomPlayerColor) {
                            selectedSquare = targetPosition;
                            drawing.drawAllPieces(selectedSquare);
                            break;
                        }

                    }


                }

                //--------------------

                break;


            default:

        }

        if(selectedSquare != ChessBoard.OUT_OF_BOARD || draggedSquare != ChessBoard.OUT_OF_BOARD){
            int sourceSquare = Math.max(selectedSquare,draggedSquare);
            ArrayList<Integer> squareLegalMoves = drawing.getLegalMoves(sourceSquare);
            for(int square: squareLegalMoves){
                drawing.drawLegalSquare(square);
            }

        }
        if(humanPlayed){
            drawing.humanPlayed(humanMove);

        }

        return true;

    }

    private int getTouchSquare(float x, float y) {
        //chess board y starts at bottom
        y = getHeight() - y - 1;
        int touchFile = (int) Math.floor(x / drawing.squareSize);
        int touchRank = (int) Math.floor(y / drawing.squareSize);
        return ChessBoard.GetPosition(touchFile, touchRank);

    }

    public void clearBoard() {
        if (piecesCanvas != null) {
            piecesCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        }
    }

    public void finishGame(Piece.Color color,int gameType,boolean humanWon) {
        AlertDialog gameFinishedDialog = new AlertDialog.Builder(getContext()).create();
        gameFinishedDialog.setTitle(getContext().getString(R.string.game_finished));
        String message = "";
        if(gameType == Game.COMPUTER_COMPUTER){
            if(color == Piece.Color.WHITE) message = getContext().getString(R.string.message_white_won);
            else message = getContext().getString(R.string.message_black_won);
        }
        else {
            if(humanWon) message = getContext().getString(R.string.message_you_won);
            else message = getContext().getString(R.string.message_computer_won);
        }
        gameFinishedDialog.setMessage(message);
        gameFinishedDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        gameFinishedDialog.show();
    }

    public void drawLegalSquare(RectF squareRect, boolean hasPiece) {
        if (piecesCanvas != null) {

            if(hasPiece){

                piecesCanvas.drawCircle(squareRect.centerX(),squareRect.centerY(),
                        squareRect.width() / 2.3f,legalSquarePaintHasPiece);
            } else{
                piecesCanvas.drawCircle(squareRect.centerX(),squareRect.centerY(),
                        squareRect.width() /4,legalSquarePaint);

            }
        }
    }
}