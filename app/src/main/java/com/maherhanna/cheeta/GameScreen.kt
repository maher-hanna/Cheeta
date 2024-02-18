package com.maherhanna.cheeta


import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.PlayerLegalMoves
import com.maherhanna.cheeta.core.Uci
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun GameScreen(playerColor: Int) {
    val computerColor = Piece.GetOppositeColor(playerColor)
    val chessBoard by remember {
        mutableStateOf(ChessBoard(ChessBoard.positionInUse))
    }
    val moveGenerator = MoveGenerator()
    var playerTurn by remember { mutableStateOf(playerColor == Piece.WHITE) }
    var playerLegalMoves by remember {
        mutableStateOf(PlayerLegalMoves())
    }
    var computerLegalMoves by remember {
        mutableStateOf(PlayerLegalMoves())
    }
    var playerSelectedPieceLegalTargets by remember {
        mutableStateOf(emptyList<Int>())
    }
    val uci by remember {
        mutableStateOf(Uci())
    }
    var chessBoardImageSize by remember { mutableStateOf(Size.Zero) }
    val squareSize by remember {
        derivedStateOf { chessBoardImageSize.width / 8 }
    }
    var fromIndex by remember { mutableIntStateOf(ChessBoard.NO_SQUARE) }
    var touchStartPosition by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var dragOffset by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    var isDragging by remember {
        mutableStateOf(false)
    }
    val pieceScaleDownFactor = 0.85f

    val scope = rememberCoroutineScope()

    LaunchedEffect(true) {
        playerLegalMoves = moveGenerator.generateLegalMovesFor(chessBoard, playerColor)
    }
    Log.d(TAG, "isDragging: $isDragging")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset ->
                        touchStartPosition = Offset(
                            offset.x,
                            offset.y
                        )
                        fromIndex = getTouchSquare(
                            chessBoardHeight = chessBoardImageSize.height,
                            x = offset.x,
                            y = offset.y,
                            squareSize = squareSize,
                        )
                        if (!canSelect(
                                chessBoard = chessBoard,
                                position = fromIndex,
                                playerColor = playerColor
                            )
                        ) {
                            fromIndex = ChessBoard.NO_SQUARE
                        }

                        playerSelectedPieceLegalTargets =
                            playerLegalMoves.getLegalTargetsFor(
                                fromIndex
                            )
                    })
                },
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
                val index = if (playerColor == Piece.WHITE) {
                    ChessBoard.MAX_POSITION - i
                } else {
                    i
                }
                if (!chessBoard.isSquareEmpty(i)) {
                    val isPieceSelected = i == fromIndex
                    Image(
                        modifier = Modifier
                            .zIndex(zIndex = if (isPieceSelected && isDragging) 1f else 0f)
                            .size(with(LocalDensity.current) { squareSize.toDp() * pieceScaleDownFactor })
                            .offset(
                                x = with(LocalDensity.current) {
                                    (ChessBoard.GetFile(
                                        i
                                    ) * squareSize + ((1 - pieceScaleDownFactor) / 2 * squareSize) + if (isPieceSelected && isDragging) dragOffset.x else 0f).toDp()
                                },
                                y = with(LocalDensity.current) {
                                    (ChessBoard.GetRank(
                                        index
                                    ) * squareSize + ((1 - pieceScaleDownFactor) / 2 * squareSize) + if (isPieceSelected && isDragging) (dragOffset.y - squareSize / 2) else 0f).toDp()
                                }
                            )
                            .pointerInput(Unit) {
                                detectDragGestures(onDrag = { change, offset ->
                                    change.consume()
                                    if (fromIndex != ChessBoard.NO_SQUARE) {
                                        isDragging = true

                                        // limit dragged piece inside chess board borders
                                        val summed = dragOffset + offset
                                        dragOffset = Offset(
                                            x = if ((ChessBoard.GetFile(i) * squareSize + summed.x) < 0f || ((ChessBoard.GetFile(
                                                    i
                                                ) + 1) * squareSize + summed.x) > chessBoardImageSize.width
                                            ) dragOffset.x else summed.x,
                                            y = if ((ChessBoard.GetRank(index) * squareSize + summed.y) < 0f || ((ChessBoard.GetRank(
                                                    index
                                                ) + 1) * squareSize + summed.y) > chessBoardImageSize.height
                                            ) dragOffset.y else summed.y
                                        )
                                    }

                                }, onDragStart = {
                                    if (!playerTurn || fromIndex == ChessBoard.NO_SQUARE) {
                                        dragOffset = Offset(0f, 0f)

                                    }

                                },
                                    onDragEnd = {
                                        isDragging = false
                                        val toIndex = getTouchSquare(
                                            chessBoardHeight = chessBoardImageSize.height,
                                            x = touchStartPosition.x + dragOffset.x,
                                            y = touchStartPosition.y + dragOffset.y,
                                            squareSize = squareSize,
                                        )
                                        if (playerSelectedPieceLegalTargets.contains(toIndex)) {
                                            dragOffset = Offset(0f, 0f)
                                            val playerMove = playerLegalMoves
                                                .searchMove(fromIndex, toIndex)
                                            if (playerMove != null) {
                                                chessBoard.makeMove(playerMove)
                                                playerTurn = !playerTurn
                                                computerLegalMoves =
                                                    moveGenerator.generateLegalMovesFor(
                                                        chessBoard,
                                                        computerColor
                                                    )
                                                touchStartPosition = Offset(0f, 0f)
                                                fromIndex = ChessBoard.NO_SQUARE

                                            }
                                            scope.launch {
                                                uci.parseInput("position startpos moves " + chessBoard.moves.notation)
                                                var computerMoveNotation =
                                                    uci.parseInput("go infinite")
                                                computerMoveNotation = computerMoveNotation
                                                    .removePrefix("bestmove")
                                                    .trim()
                                                val computerMove = computerLegalMoves.searchMove(
                                                    Move(computerMoveNotation)
                                                )
                                                if (computerMove != null) {
                                                    chessBoard.makeMove(computerMove)
                                                    playerTurn = !playerTurn
                                                    playerLegalMoves =
                                                        moveGenerator.generateLegalMovesFor(
                                                            chessBoard,
                                                            playerColor
                                                        )
                                                }

                                            }
                                        } else {
                                            dragOffset = Offset(0f, 0f)
                                        }

                                    }

                                )
                            }
                            .scale(if (isPieceSelected && isDragging) 1.5f else 1f)

                        ,
                        painter = painterResource(
                            id = getPieceDrawableId(
                                chessBoard.pieceType(i),
                                chessBoard.pieceColor(i)
                            )
                        ),
                        contentDescription = stringResource(
                            id = R.string.chess_piece
                        )
                    )
                }
                var isSquareLegalTarget = false
                if (fromIndex != ChessBoard.NO_SQUARE) {
                    isSquareLegalTarget = playerSelectedPieceLegalTargets.contains(i)
                }
                val isSquareEmpty = chessBoard.isSquareEmpty(i)
                if (isSquareLegalTarget) {
                    Image(
                        modifier = Modifier
                            .offset(
                                x = with(LocalDensity.current) {
                                    (ChessBoard.GetFile(
                                        i
                                    ) * squareSize).toDp()
                                },
                                y = with(LocalDensity.current) {
                                    (ChessBoard.GetRank(
                                        index
                                    ) * squareSize).toDp()
                                }
                            )
                            .size(with(LocalDensity.current) { squareSize.toDp() }),
                        painter = painterResource(
                            if (isSquareEmpty) R.drawable.legal_move_quite else
                                R.drawable.legal_move_capture
                        ),
                        contentDescription = stringResource(
                            id = R.string.legal_move
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
): Int {
    val yMirrored = chessBoardHeight - y - 1
    val touchFile = floor((x / squareSize).toDouble()).toInt()
    val touchRank = floor((yMirrored / squareSize).toDouble()).toInt()
    return ChessBoard.GetPosition(touchFile, touchRank)
}

fun canSelect(chessBoard: ChessBoard, position: Int, playerColor: Int): Boolean {
    return !chessBoard.isSquareEmpty(position) &&
            chessBoard.pieceColor(position) == playerColor
}