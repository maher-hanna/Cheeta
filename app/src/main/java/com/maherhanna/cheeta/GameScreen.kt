package com.maherhanna.cheeta

import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import kotlin.math.floor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(humanPlayerColor: Int) {
    val chessBoard = ChessBoard(ChessBoard.positionInUse)
    val moveGenerator = MoveGenerator()
    var humanTurn by remember { mutableStateOf(humanPlayerColor == Piece.WHITE) }
    var chessBoardImageSize by remember { mutableStateOf(Size.Zero) }
    val squareSize by remember {
        derivedStateOf { chessBoardImageSize.width / 8 }
    }
    var draggedPieceIndex by remember { mutableIntStateOf(ChessBoard.NO_SQUARE) }


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        chessBoardImageSize = coordinates.size.toSize()
                    }
                    .fillMaxWidth(), painter = painterResource(R.drawable.chessboard),
                contentDescription = stringResource(R.string.chessboard),
                contentScale = ContentScale.FillWidth
            )

            for (i in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {
                if (!chessBoard.isSquareEmpty(i)) {
                    val index = if (humanPlayerColor == Piece.WHITE) {
                        ChessBoard.MAX_POSITION - i
                    } else {
                        i
                    }
                    var pieceXOffset by remember {
                        mutableFloatStateOf(0f)
                    }
                    var pieceYOffset by remember {
                        mutableFloatStateOf(0f)
                    }

                    Image(
                        modifier = Modifier
                            .offset(
                                x = with(LocalDensity.current) {
                                    (ChessBoard.GetFile(
                                        i
                                    ) * squareSize + pieceXOffset).toDp()
                                },
                                y = with(LocalDensity.current) {
                                    (ChessBoard.GetRank(
                                        i
                                    ) * squareSize + pieceYOffset).toDp()
                                }
                            )
                            .size(with(LocalDensity.current) { squareSize.toDp() })
                            .pointerInput(Unit) {
                                detectDragGestures(onDrag = { change, offset ->
                                    change.consume()
                                    if (draggedPieceIndex != ChessBoard.NO_SQUARE) {
                                        pieceXOffset += offset.x
                                        pieceYOffset += offset.y
                                    }

                                }, onDragStart = {
                                    draggedPieceIndex = getTouchSquare(
                                        chessBoardHeight = chessBoardImageSize.height,
                                        x = it.x + (ChessBoard.GetFile(i) * squareSize),
                                        y = it.y + (ChessBoard.GetRank(i) * squareSize),
                                        squareSize = squareSize,
                                        humanPlayerColor = humanPlayerColor
                                    )
                                    if (!canSelect(
                                            chessBoard = chessBoard,
                                            position = draggedPieceIndex,
                                            humanPlayerColor = humanPlayerColor
                                        )
                                    ) {
                                        draggedPieceIndex = ChessBoard.NO_SQUARE
                                    }
                                    if (draggedPieceIndex == ChessBoard.NO_SQUARE) {
                                        pieceXOffset = 0f
                                        pieceYOffset = 0f
                                    }

                                },
                                    onDragEnd = {
                                        pieceXOffset = 0f
                                        pieceYOffset = 0f

                                    }

                                )
                            },
                        painter = painterResource(
                            id = getPieceDrawableId(
                                chessBoard.pieceType(index),
                                chessBoard.pieceColor(index)
                            )
                        ),
                        contentDescription = stringResource(
                            id = R.string.chess_piece
                        )
                    )
                }
            }
        }

    }
}

fun getPieceDrawableId(pieceType: Int, pieceColor: Int): Int {
    return when (pieceColor) {
        Piece.WHITE -> {
            when (pieceType) {
                Piece.PAWN -> R.drawable.white_pawn
                Piece.ROOK -> R.drawable.white_rook
                Piece.BISHOP -> R.drawable.white_bishop
                Piece.KNIGHT -> R.drawable.white_knight
                Piece.QUEEN -> R.drawable.white_queen
                Piece.KING -> R.drawable.white_king
                else -> R.drawable.white_pawn
            }

        }

        Piece.BLACK -> {
            when (pieceType) {
                Piece.PAWN -> R.drawable.black_pawn
                Piece.ROOK -> R.drawable.black_rook_transparent
                Piece.BISHOP -> R.drawable.black_bishop_transparent
                Piece.KNIGHT -> R.drawable.black_knight_transparent
                Piece.QUEEN -> R.drawable.black_queen_transparent
                Piece.KING -> R.drawable.black_king_transparent
                else -> R.drawable.black_pawn
            }
        }

        else -> R.drawable.white_pawn
    }

}

private fun getTouchSquare(
    chessBoardHeight: Float,
    x: Float,
    y: Float,
    squareSize: Float,
    humanPlayerColor: Int
): Int {
    val yMirrored = if (humanPlayerColor == Piece.WHITE) chessBoardHeight - y - 1 else y
    val touchFile = floor((x / squareSize).toDouble()).toInt()
    val touchRank = floor((yMirrored / squareSize).toDouble()).toInt()
    return ChessBoard.GetPosition(touchFile, touchRank)
}

fun canSelect(chessBoard: ChessBoard, position: Int, humanPlayerColor: Int): Boolean {
    return !chessBoard.isSquareEmpty(position) &&
            chessBoard.pieceColor(position) == humanPlayerColor
}