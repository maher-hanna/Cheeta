package com.maherhanna.cheeta;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;

class ChessboardView extends androidx.appcompat.widget.AppCompatImageView {
    public Drawing drawing;
    private Canvas piecesCanvas;
    private Bitmap piecesBitmap;
    private final Paint highlightPaint;
    private final Paint checkHighlightPaint;
    private final Paint legalSquarePaint;
    private final Paint legalSquarePaintHasPiece;
    private RadialGradient kingCheckHighlight;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setAlpha(100);



        //king highlight
        checkHighlightPaint = new Paint();
        checkHighlightPaint.setColor(Color.RED);
        checkHighlightPaint.setDither(true);


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

        if (drawing.isGameFinished()) return true;

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        boolean humanPlayed = false;
        Move humanMove = null;

        //keep x an y inside chessboard
        x = Math.max(0, Math.min(getWidth() - 1, x));
        y = Math.max(0, Math.min(getHeight() - 1, y));

        drawing.x = x;
        drawing.y = y;
        drawing.touchSquare = getTouchSquare(x, y);
        int targetSquare = drawing.touchSquare;
        if (drawing.isChessBoardFlipped()) targetSquare = drawing.flip(drawing.touchSquare);

        switch (action) {

            case MotionEvent.ACTION_DOWN:

                if (!drawing.canSelect(targetSquare)) {
                    drawing.dragFrom = -1;

                    break;
                }

                drawing.dragFrom = targetSquare;
                drawing.selectedSquare = targetSquare;

                drawing.xTouchStart = x;
                drawing.yTouchStart = y;
                break;


            case MotionEvent.ACTION_MOVE:


                break;


            case MotionEvent.ACTION_UP:
                drawing.xTouchStart = 0;
                drawing.yTouchStart = 0;


                if (drawing.dragFrom != ChessBoard.OUT) {
                    //check for selecting a square
                    if (drawing.dragFrom == targetSquare) {
                        drawing.selectedSquare = drawing.dragFrom;
                        drawing.dragFrom = -1;
                        break;
                    }


                    if (drawing.canMove(drawing.dragFrom, targetSquare)) {
                        humanPlayed = true;
                        humanMove = new Move(drawing.chessBoard.pieceType(drawing.dragFrom),
                                drawing.chessBoard.pieceColor(drawing.dragFrom), drawing.dragFrom, targetSquare);
                        drawing.dragFrom = -1;
                        drawing.selectedSquare = -1;

                        break;
                    }
                    drawing.dragFrom = -1;

                    break;
                }

                //if piece is selected
                if (drawing.selectedSquare != ChessBoard.OUT) {
                    if (drawing.selectedSquare == targetSquare) break;
                    //check for selecting other piece
                    if (drawing.canMove(drawing.selectedSquare, targetSquare)) {
                        humanPlayed = true;
                        humanMove = new Move(drawing.chessBoard.pieceType(drawing.selectedSquare),
                                drawing.chessBoard.pieceColor(drawing.selectedSquare), drawing.selectedSquare, targetSquare);
                        drawing.selectedSquare = -1;
                        break;

                    } else {
                        if (drawing.chessBoard.isSquareEmpty(targetSquare)) break;
                        if (drawing.chessBoard.pieceColor(targetSquare) == drawing.getBottomScreenPlayerColor()) {
                            drawing.selectedSquare = targetSquare;
                            break;
                        }

                    }


                }

                //--------------------

                break;


            default:

        }


        if (humanPlayed) {
            drawing.currentMove = humanMove;
            drawing.humanPlayed(humanMove);

        }

        drawing.drawAllPieces();


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

    public void finishGame(int messageId) {
        AlertDialog gameFinishedDialog = new AlertDialog.Builder(getContext()).create();
        gameFinishedDialog.setTitle(getContext().getString(R.string.game_finished));
        String message = getContext().getString(messageId);
        gameFinishedDialog.setMessage(message);
        gameFinishedDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.game_finished_ok_button),
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

            if (hasPiece) {

                piecesCanvas.drawCircle(squareRect.centerX(), squareRect.centerY(),
                        squareRect.width() / 2.3f, legalSquarePaintHasPiece);
            } else {
                piecesCanvas.drawCircle(squareRect.centerX(), squareRect.centerY(),
                        squareRect.width() / 4, legalSquarePaint);

            }
        }
    }

    public void drawCheckHighlight(RectF highlightRect) {
        if (piecesCanvas != null) {
            kingCheckHighlight = new RadialGradient(highlightRect.centerX(),highlightRect.centerY(),
                    highlightRect.width() ,Color.RED,0,Shader.TileMode.CLAMP);
            checkHighlightPaint.setShader(kingCheckHighlight);
            piecesCanvas.drawRect(highlightRect,checkHighlightPaint);

        }
    }
}