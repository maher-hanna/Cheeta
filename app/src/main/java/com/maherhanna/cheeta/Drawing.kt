package com.maherhanna.cheeta

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.Piece

class Drawing(activity: GameActivity) {
    @JvmField
    var game: Game? = null
    private val chessboardView: ChessboardView
    @JvmField
    var chessBoard: ChessBoard? = null
    @JvmField
    var currentMove: Move? = null
    @JvmField
    var xTouchStart = 0f
    @JvmField
    var yTouchStart = 0f

    //for dragging a piece
    @JvmField
    var dragFrom = ChessBoard.NO_SQUARE
    private var dragFromHighlight = dragFrom
    @JvmField
    var selectedSquare = ChessBoard.NO_SQUARE
    private var selectedSquareHighlight = ChessBoard.NO_SQUARE
    @JvmField
    var touchSquare = ChessBoard.NO_SQUARE
    @JvmField
    var x = 0f
    @JvmField
    var y = 0f
    var kingInCheck = ChessBoard.NO_SQUARE

    //---------------------
    //pieces drawables
    private val blackPawnBitmap: Bitmap
    private val whitePawnBitmap: Bitmap
    private val blackBishopBitmap: Bitmap
    private val whiteBishopBitmap: Bitmap
    private val blackKnightBitmap: Bitmap
    private val whiteKnightBitmap: Bitmap
    private val blackRookBitmap: Bitmap
    private val whiteRookBitmap: Bitmap
    private val blackQueenBitmap: Bitmap
    private val whiteQueenBitmap: Bitmap
    private val blackKingBitmap: Bitmap
    private val whiteKingBitmap: Bitmap

    //pieces drawing dimensions
    private var pawnDrawingRect: RectF? = null
    private var bishopDrawingRect: RectF? = null
    private var knightDrawingRect: RectF? = null
    private var rookDrawingRect: RectF? = null
    private var queenDrawingRect: RectF? = null
    private var kingDrawingRect: RectF? = null

    //chess board dimensions
    private var chessBoardViewRect: RectF? = null
    @JvmField
    var squareSize = 0f

    //----------------
    private var isWaitingHumanToPlay = false

    init {
        chessboardView = activity.findViewById(R.id.chessboardView)
        chessboardView.drawing = this
        blackPawnBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.black_pawn)
        whitePawnBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.white_pawn)
        blackBishopBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.black_bishop_transparent)
        whiteBishopBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.white_bishop)
        blackKnightBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.black_knight_transparent)
        whiteKnightBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.white_knight)
        blackRookBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.black_rook_transparent)
        whiteRookBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.white_rook)
        blackQueenBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.black_queen_transparent)
        whiteQueenBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.white_queen)
        blackKingBitmap =
            BitmapFactory.decodeResource(activity.resources, R.drawable.black_king_transparent)
        whiteKingBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.white_king)
        updateDrawingRects(
            RectF(
                0f, 0f, chessboardView.width
                    .toFloat(), chessboardView.height.toFloat()
            )
        )
    }

    fun updateDrawingRects(newChessBoardRect: RectF?) {
        //called when the size of chessboard view change
        chessBoardViewRect = newChessBoardRect
        squareSize = chessBoardViewRect!!.height() / 8.0f


        //----------
        pawnDrawingRect =
            calculateRect(blackPawnBitmap.width.toFloat(), blackPawnBitmap.height.toFloat())
        bishopDrawingRect =
            calculateRect(blackBishopBitmap.width.toFloat(), blackBishopBitmap.height.toFloat())
        knightDrawingRect =
            calculateRect(blackKnightBitmap.width.toFloat(), blackKnightBitmap.height.toFloat())
        rookDrawingRect =
            calculateRect(blackRookBitmap.width.toFloat(), blackRookBitmap.height.toFloat())
        queenDrawingRect =
            calculateRect(blackQueenBitmap.width.toFloat(), blackQueenBitmap.height.toFloat())
        kingDrawingRect =
            calculateRect(blackKingBitmap.width.toFloat(), blackKingBitmap.height.toFloat())
        //------------
    }

    private fun calculateRect(bitmapWidth: Float, bitmapHeight: Float): RectF {
        val result: RectF
        val squareScaleDownFactor = (bitmapWidth.coerceAtLeast(bitmapHeight)
                / squareSize)
        var width = bitmapWidth / squareScaleDownFactor
        var height = bitmapHeight / squareScaleDownFactor
        width *= SCALE_PIECES_DOWN
        height *= SCALE_PIECES_DOWN
        val left = (squareSize - width) / 2
        val top = (squareSize - height) / 2
        result = RectF(0f, 0f, width, height)
        result.offset(left, top)
        return result
    }

    fun clearBoard() {
        chessboardView.clearBoard()
    }

    private fun drawHighlight(square: Int) {
        val highlightRect = getSquareRect(square)
        chessboardView.drawHighlight(highlightRect)
    }

    private fun getSquareRect(square: Int): RectF {
        val squareLeft = ChessBoard.GetFile(square) * squareSize
        val squareTop: Float = (8 - ChessBoard.GetRank(square) - 1) * squareSize
        val squareRight = squareLeft + squareSize
        val squareBottom = squareTop + squareSize
        return RectF(squareLeft, squareTop, squareRight, squareBottom)
    }

    fun flip(position: Int): Int {
        return ChessBoard.MAX_POSITION - position
    }

    fun drawAllPieces() {
        clearBoard()
        var from: Int
        var to: Int
        val flipped = isChessBoardFlipped
        var legalTargetsSquare = ChessBoard.NO_SQUARE
        dragFromHighlight = dragFrom
        selectedSquareHighlight = selectedSquare
        var kingInCheckHighlight = kingInCheck
        if (flipped) {
            dragFromHighlight = flip(dragFromHighlight)
            selectedSquareHighlight = flip(selectedSquareHighlight)
            kingInCheckHighlight = flip(kingInCheckHighlight)
        }
        if (currentMove != null) {
            from = currentMove!!.from
            to = currentMove!!.to
            if (flipped) {
                from = flip(from)
                to = flip(to)
            }
            drawHighlight(from)
            drawHighlight(to)
        }
        if (selectedSquare != ChessBoard.NO_SQUARE) {
            legalTargetsSquare = selectedSquare
            drawHighlight(selectedSquareHighlight)
        }
        if (dragFrom != ChessBoard.NO_SQUARE) {
            legalTargetsSquare = dragFrom
            drawHighlight(dragFromHighlight)
            drawHighlight(touchSquare)
        }
        if (kingInCheck != ChessBoard.NO_SQUARE) {
            drawCheckHighlight(kingInCheckHighlight)
        }
        for (i in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {

            // flip the board if the black is at the bottom of screen
            val index = if (flipped) {
                ChessBoard.MAX_POSITION - i
            } else {
                i
            }
            if (!chessBoard!!.isSquareEmpty(i)) {
                if (i == dragFrom) continue
                drawPiece(chessBoard!!.pieceType(i), chessBoard!!.pieceColor(i), index, 0f, 0f, 1f)
            }
        }
        if (dragFrom != ChessBoard.NO_SQUARE) {
            drawPiece(
                chessBoard!!.pieceType(dragFrom), chessBoard!!.pieceColor(dragFrom),
                dragFromHighlight, x - xTouchStart, y - yTouchStart, 2f
            )
        }
        if (legalTargetsSquare != ChessBoard.OUT) {
            val squareLegalMoves = getLegalMoves(legalTargetsSquare)
            for (square in squareLegalMoves) {
                drawLegalSquare(square)
            }
        }
        show()
    }

    private fun drawCheckHighlight(kingInCheckHighlight: Int) {
        val highlightRect = getSquareRect(kingInCheckHighlight)
        chessboardView.drawCheckHighlight(highlightRect)
    }

    fun show() {
        chessboardView.invalidate()
    }

    fun canMove(from: Int, to: Int): Boolean {
        return if (!isWaitingHumanToPlay) {
            false
        } else game?.chessEngine?.moveGenerator?.canMove(chessBoard!!,from, to) == true
    }

    private fun drawPiece(
        pieceType: Int,
        pieceColor: Int,
        position: Int,
        xOffset: Float,
        yOffset: Float,
        scaleFactor: Float
    ) {
        var pieceRect = getPieceDrawingRect(pieceType, position)
        pieceRect = scaleRect(pieceRect, scaleFactor)
        pieceRect.offset(xOffset, yOffset)

        //limit the piece drawing rect inside the board
        if (pieceRect.left < 0) {
            pieceRect.offset(-pieceRect.left, 0f)
        }
        if (pieceRect.top < 0) {
            pieceRect.offset(0f, -pieceRect.top)
        }
        if (pieceRect.right > chessBoardViewRect!!.width()) {
            pieceRect.offset(-(pieceRect.right - chessBoardViewRect!!.width()), 0f)
        }
        if (pieceRect.bottom > chessBoardViewRect!!.height()) {
            pieceRect.offset(0f, -(pieceRect.bottom - chessBoardViewRect!!.height()))
        }
        when (pieceType) {
            Piece.PAWN -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whitePawnBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackPawnBitmap, pieceRect)

            Piece.ROOK -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whiteRookBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackRookBitmap, pieceRect)

            Piece.KNIGHT -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whiteKnightBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackKnightBitmap, pieceRect)

            Piece.BISHOP -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whiteBishopBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackBishopBitmap, pieceRect)

            Piece.QUEEN -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whiteQueenBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackQueenBitmap, pieceRect)

            Piece.KING -> if (pieceColor == Piece.WHITE) chessboardView.drawPiece(
                whiteKingBitmap,
                pieceRect
            ) else chessboardView.drawPiece(blackKingBitmap, pieceRect)
        }
    }

    private fun scaleRect(rect: RectF, scaleFactor: Float): RectF {
        val scaledRect = RectF(rect)
        val diffHorizontal = rect.width() * (scaleFactor - 1f)
        val diffVertical = rect.height() * (scaleFactor - 1f)
        scaledRect.top -= diffVertical / 2f
        scaledRect.bottom += diffVertical / 2f
        scaledRect.left -= diffHorizontal / 2f
        scaledRect.right += diffHorizontal / 2f
        return scaledRect
    }

    private fun getPieceDrawingRect(pieceType: Int,  position: Int): RectF {
        var result = RectF()
        val x = ChessBoard.GetFile(position) * squareSize
        var y = ChessBoard.GetRank(position) * squareSize
        y = chessBoardViewRect!!.width() - y - squareSize
        when (pieceType) {
            Piece.PAWN -> {
                result = RectF(pawnDrawingRect)
                result.offset(x, y)
            }

            Piece.ROOK -> {
                result = RectF(rookDrawingRect)
                result.offset(x, y)
            }

            Piece.KNIGHT -> {
                result = RectF(knightDrawingRect)
                result.offset(x, y)
            }

            Piece.BISHOP -> {
                result = RectF(bishopDrawingRect)
                result.offset(x, y)
            }

            Piece.QUEEN -> {
                result = RectF(queenDrawingRect)
                result.offset(x, y)
            }

            Piece.KING -> {
                result = RectF(kingDrawingRect)
                result.offset(x, y)
            }
        }
        return result
    }

    fun finishGame(gameStatus: GameStatus) {
        val messageId: Int = if (gameStatus === GameStatus.FINISHED_DRAW) {
            R.string.message_draw
        } else {
            if (game!!.humanPlayerColor == Piece.WHITE) {
                if (gameStatus === GameStatus.FINISHED_WIN_WHITE) {
                    R.string.message_you_won
                } else {
                    R.string.message_computer_won
                }
            } else {
                if (gameStatus === GameStatus.FINISHED_WIN_BLACK) {
                    R.string.message_you_won
                } else {
                    R.string.message_computer_won
                }
            }
        }
        chessboardView.finishGame(messageId)
    }

    fun waitHumanToPlay() {
        isWaitingHumanToPlay = true
    }

    fun humanPlayed(humanMove: Move?) {
        game!!.humanPlayed(humanMove)
        isWaitingHumanToPlay = false
    }

    val isGameFinished: Boolean
        get() = game!!.isGameFinished

    private fun getLegalMoves(square: Int): ArrayList<Int> {
        return game?.chessEngine?.moveGenerator?.getLegalTargetsFor(chessBoard!!,square) ?: ArrayList()
    }

    fun canSelect(position: Int): Boolean {
        return !chessBoard!!.isSquareEmpty(position) &&
                chessBoard!!.pieceColor(position) == game!!.humanPlayerColor
    }

    private fun drawLegalSquare(square: Int) {
        val squareRect: RectF = if (isChessBoardFlipped) {
            getSquareRect(flip(square))
        } else {
            getSquareRect(square)
        }
        chessboardView.drawLegalSquare(squareRect, !chessBoard!!.isSquareEmpty(square))
    }

    val isChessBoardFlipped: Boolean
        get() = game!!.humanPlayerColor == Piece.BLACK
    val bottomScreenPlayerColor: Int
        get() = game!!.humanPlayerColor

    companion object {
        private const val SCALE_PIECES_DOWN = 0.8f
    }
}
