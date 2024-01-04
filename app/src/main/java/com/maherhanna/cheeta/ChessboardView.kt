package com.maherhanna.cheeta

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.Move

internal class ChessboardView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(
    context!!, attrs
) {
    var drawing: Drawing? = null
    private var piecesCanvas: Canvas? = null
    private var piecesBitmap: Bitmap? = null
    private val highlightPaint: Paint
    private val checkHighlightPaint: Paint
    private val legalSquarePaint: Paint
    private val legalSquarePaintHasPiece: Paint
    private var kingCheckHighlight: RadialGradient? = null

    init {
        highlightPaint = Paint()
        highlightPaint.color = Color.YELLOW
        highlightPaint.alpha = 100


        //king highlight
        checkHighlightPaint = Paint()
        checkHighlightPaint.color = Color.RED
        checkHighlightPaint.isDither = true
        legalSquarePaint = Paint()
        legalSquarePaint.color = Color.GRAY
        legalSquarePaint.alpha = 150
        legalSquarePaintHasPiece = Paint()
        legalSquarePaintHasPiece.color = Color.GRAY
        legalSquarePaintHasPiece.alpha = 150
        legalSquarePaintHasPiece.strokeWidth = 15f
        legalSquarePaintHasPiece.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val horizontalPadding = (paddingLeft + paddingRight).toFloat()
        val verticalPadding = (paddingTop + paddingBottom).toFloat()
        val drawingWidth = w - horizontalPadding
        val drawingHeight = h - verticalPadding
        piecesBitmap = Bitmap.createBitmap(
            drawingWidth.toInt(),
            drawingHeight.toInt(),
            Bitmap.Config.ARGB_8888
        )
        piecesCanvas = Canvas(piecesBitmap!!)
        if (drawing != null) {
            drawing!!.updateDrawingRects(RectF(0f, 0f, drawingWidth, drawingHeight))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(piecesBitmap!!, 0f, 0f, null)
    }

    fun drawPiece(bitmap: Bitmap?, rect: RectF?) {
        if (piecesCanvas != null) {
            piecesCanvas!!.drawBitmap(bitmap!!, null, rect!!, null)
        }
    }

    fun drawHighlight(highlightRect: RectF?) {
        if (piecesCanvas != null) {
            piecesCanvas!!.drawRect(highlightRect!!, highlightPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawing!!.isGameFinished) return true
        val action = event.action
        var x = event.x
        var y = event.y
        var humanPlayed = false
        var humanMove: Move? = null

        //keep x an y inside chessboard
        x = Math.max(0f, Math.min((width - 1).toFloat(), x))
        y = Math.max(0f, Math.min((height - 1).toFloat(), y))
        drawing!!.x = x
        drawing!!.y = y
        drawing!!.touchSquare = getTouchSquare(x, y)
        var targetSquare = drawing!!.touchSquare
        if (drawing!!.isChessBoardFlipped) targetSquare = drawing!!.flip(drawing!!.touchSquare)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!drawing!!.canSelect(targetSquare)) {
                    drawing!!.dragFrom = -1
                } else {
                    drawing!!.dragFrom = targetSquare
                    drawing!!.selectedSquare = targetSquare
                    drawing!!.xTouchStart = x
                    drawing!!.yTouchStart = y
                }

            }

            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP -> {
                drawing!!.xTouchStart = 0f
                drawing!!.yTouchStart = 0f
                if (drawing!!.dragFrom != ChessBoard.OUT) {
                    //check for selecting a square
                    if (drawing!!.dragFrom == targetSquare) {
                        drawing!!.selectedSquare = drawing!!.dragFrom
                        drawing!!.dragFrom = -1
                    }
                    else if (drawing!!.canMove(drawing!!.dragFrom, targetSquare)) {
                        humanPlayed = true
                        humanMove = Move(
                            drawing!!.chessBoard!!.pieceType(drawing!!.dragFrom),
                            drawing!!.chessBoard!!.pieceColor(drawing!!.dragFrom),
                            drawing!!.dragFrom,
                            targetSquare
                        )
                        drawing!!.dragFrom = -1
                        drawing!!.selectedSquare = -1
                    } else{

                        drawing!!.dragFrom = -1
                    }
                }

                //if piece is selected
                if (drawing!!.selectedSquare != ChessBoard.OUT) {
                    if (drawing!!.selectedSquare == targetSquare) {}
                    //check for selecting other piece
                    else if (drawing!!.canMove(drawing!!.selectedSquare, targetSquare)) {
                        humanPlayed = true
                        humanMove = Move(
                            drawing!!.chessBoard!!.pieceType(drawing!!.selectedSquare),
                            drawing!!.chessBoard!!.pieceColor(drawing!!.selectedSquare),
                            drawing!!.selectedSquare,
                            targetSquare
                        )
                        drawing!!.selectedSquare = -1
                    } else {
                        if (drawing!!.chessBoard!!.isSquareEmpty(targetSquare))
                        else if (drawing!!.chessBoard!!.pieceColor(targetSquare) == drawing!!.bottomScreenPlayerColor) {
                            drawing!!.selectedSquare = targetSquare
                        }
                    }
                }
            }

            else -> {}
        }
        if (humanPlayed) {
            drawing!!.currentMove = humanMove
            drawing!!.humanPlayed(humanMove)
        }
        drawing!!.drawAllPieces()
        return true
    }

    private fun getTouchSquare(x: Float, y: Float): Int {
        //chess board y starts at bottom
        var y = y
        y = height - y - 1
        val touchFile = Math.floor((x / drawing!!.squareSize).toDouble()).toInt()
        val touchRank = Math.floor((y / drawing!!.squareSize).toDouble()).toInt()
        return ChessBoard.GetPosition(touchFile, touchRank)
    }

    fun clearBoard() {
        if (piecesCanvas != null) {
            piecesCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
    }

    fun finishGame(messageId: Int) {
        val gameFinishedDialog = AlertDialog.Builder(context).create()
        gameFinishedDialog.setTitle(context.getString(R.string.game_finished))
        val message = context.getString(messageId)
        gameFinishedDialog.setMessage(message)
        gameFinishedDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.game_finished_ok_button)
        ) { dialog, which -> dialog.dismiss() }
        gameFinishedDialog.show()
    }

    fun drawLegalSquare(squareRect: RectF, hasPiece: Boolean) {
        if (piecesCanvas != null) {
            if (hasPiece) {
                piecesCanvas!!.drawCircle(
                    squareRect.centerX(), squareRect.centerY(),
                    squareRect.width() / 2.3f, legalSquarePaintHasPiece
                )
            } else {
                piecesCanvas!!.drawCircle(
                    squareRect.centerX(), squareRect.centerY(),
                    squareRect.width() / 4, legalSquarePaint
                )
            }
        }
    }

    fun drawCheckHighlight(highlightRect: RectF) {
        if (piecesCanvas != null) {
            kingCheckHighlight = RadialGradient(
                highlightRect.centerX(), highlightRect.centerY(),
                highlightRect.width(), Color.RED, 0, Shader.TileMode.CLAMP
            )
            checkHighlightPaint.setShader(kingCheckHighlight)
            piecesCanvas!!.drawRect(highlightRect, checkHighlightPaint)
        }
    }
}