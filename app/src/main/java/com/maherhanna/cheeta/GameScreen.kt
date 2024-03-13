package com.maherhanna.cheeta


import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.Move
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.PlayerLegalMoves
import com.maherhanna.cheeta.core.Uci
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor

@Composable
fun GameScreen(playerColor: Int) {
    val computerColor = Piece.GetOppositeColor(playerColor)
    val isChessBoardFlipped = playerColor == Piece.BLACK
    var chessBoard by remember {
        mutableStateOf(ChessBoard(ChessBoard.positionInUse))
    }
    val moveGenerator = MoveGenerator()
    var playerTurn by remember { mutableStateOf(playerColor == Piece.WHITE) }
    var showCancelGameDialog by remember { mutableStateOf(false) }
    var gameFinished by remember { mutableStateOf(false) }

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
    var fromSquare by remember { mutableIntStateOf(ChessBoard.NO_SQUARE) }
    var touchSquare by remember { mutableIntStateOf(ChessBoard.NO_SQUARE) }
    var gameStatus by remember { mutableStateOf(GameStatus.NOT_FINISHED) }

    var touchStartPosition by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var dragOffset by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var summedDrag by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var isDragging by remember {
        mutableStateOf(false)
    }
    val pieceScaleDownFactor = 0.85f

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(true) {
        playerLegalMoves = moveGenerator.generateLegalMovesFor(chessBoard, playerColor)
        computerLegalMoves = moveGenerator.generateLegalMovesFor(chessBoard, computerColor)
        if (playerColor == Piece.BLACK) {
            scope.launch {
                playComputer(
                    uci = uci, chessBoard = chessBoard,
                    moveGenerator = moveGenerator,
                    computerLegalMoves = computerLegalMoves
                ) { newChessBoard ->
                    chessBoard = ChessBoard(newChessBoard)
                    playerTurn = !playerTurn
                    playerLegalMoves =
                        moveGenerator.generateLegalMovesFor(
                            chessBoard,
                            playerColor
                        )
                    computerLegalMoves =
                        moveGenerator.generateLegalMovesFor(chessBoard, computerColor)
                }
            }
        }
    }

    LaunchedEffect(chessBoard) {
        gameStatus = moveGenerator.checkStatus(chessBoard)
        if (gameStatus != GameStatus.NOT_FINISHED) {
            gameFinished = true
        }
    }

    BackHandler {
        if (gameStatus == GameStatus.NOT_FINISHED) {
            showCancelGameDialog = true
        } else {
            (context as Activity).finish()

        }
    }

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

                        val tappedSquare = getTouchSquare(
                            isChessBoardFlipped = isChessBoardFlipped,
                            chessBoardHeight = chessBoardImageSize.height,
                            x = offset.x,
                            y = offset.y,
                            squareSize = squareSize,
                        )

                        if (fromSquare != ChessBoard.NO_SQUARE) {
                            if (tappedSquare != fromSquare && playerTurn) {
                                val playerMove = playerLegalMoves
                                    .searchMove(fromSquare, tappedSquare)
                                if (playerMove != null) {
                                    chessBoard.makeMove(playerMove)
                                    // force jetpack compose to recompose by coping chessboard
                                    chessBoard = ChessBoard(chessBoard)
                                    playerTurn = !playerTurn
                                    computerLegalMoves =
                                        moveGenerator.generateLegalMovesFor(
                                            chessBoard,
                                            computerColor
                                        )
                                    playerLegalMoves =
                                        moveGenerator.generateLegalMovesFor(
                                            chessBoard,
                                            playerColor
                                        )
                                    touchStartPosition = Offset(0f, 0f)
                                    fromSquare = ChessBoard.NO_SQUARE
                                    scope.launch() {
                                        playComputer(
                                            uci = uci, chessBoard = chessBoard,
                                            moveGenerator = moveGenerator,
                                            computerLegalMoves = computerLegalMoves
                                        ) { newChessBoard ->
                                            chessBoard = ChessBoard(newChessBoard)
                                            playerTurn = !playerTurn
                                            playerLegalMoves =
                                                moveGenerator.generateLegalMovesFor(
                                                    chessBoard,
                                                    playerColor
                                                )
                                            computerLegalMoves =
                                                moveGenerator.generateLegalMovesFor(
                                                    chessBoard,
                                                    computerColor
                                                )

                                        }

                                    }
                                } else {
                                    if (canSelect(
                                            chessBoard = chessBoard,
                                            position = tappedSquare,
                                            playerColor = playerColor
                                        )
                                    ) {

                                        fromSquare = tappedSquare
                                    }
                                }

                            }
                        } else {
                            if (canSelect(
                                    chessBoard = chessBoard,
                                    position = tappedSquare,
                                    playerColor = playerColor
                                )
                            ) {

                                fromSquare = tappedSquare
                            }
                        }


                        playerSelectedPieceLegalTargets =
                            playerLegalMoves.getLegalTargetsFor(
                                fromSquare
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
                    val isPieceSelected = i == fromSquare
                    Image(
                        modifier = Modifier
                            .zIndex(zIndex = if (isPieceSelected && isDragging) 2f else 1f)
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
                                    if (fromSquare != ChessBoard.NO_SQUARE && isDragging) {
                                        // limit dragged piece inside chess board borders
                                        summedDrag += offset
                                        dragOffset = Offset(
                                            x = if ((ChessBoard.GetFile(i) * squareSize + summedDrag.x) <= 0f || ((ChessBoard.GetFile(
                                                    i
                                                ) + 1) * squareSize + summedDrag.x) >= chessBoardImageSize.width
                                            ) dragOffset.x else summedDrag.x,
                                            y = if ((ChessBoard.GetRank(index) * squareSize + summedDrag.y) <= 0f || ((ChessBoard.GetRank(
                                                    index
                                                ) + 1) * squareSize + summedDrag.y) >= chessBoardImageSize.height
                                            ) dragOffset.y else summedDrag.y
                                        )
                                        touchSquare = getTouchSquare(
                                            isChessBoardFlipped = isChessBoardFlipped,
                                            chessBoardHeight = chessBoardImageSize.height,
                                            x = touchStartPosition.x + dragOffset.x,
                                            y = touchStartPosition.y + dragOffset.y,
                                            squareSize = squareSize,
                                        )
                                    }

                                }, onDragStart = {
                                    if (!playerTurn || fromSquare == ChessBoard.NO_SQUARE) {
                                        dragOffset = Offset(0f, 0f)
                                        isDragging = false
                                    } else {
                                        isDragging = true
                                    }

                                },
                                    onDragEnd = {
                                        isDragging = false
                                        summedDrag = Offset(0f, 0f)
                                        val toSquare = getTouchSquare(
                                            isChessBoardFlipped = isChessBoardFlipped,
                                            chessBoardHeight = chessBoardImageSize.height,
                                            x = touchStartPosition.x + dragOffset.x,
                                            y = touchStartPosition.y + dragOffset.y,
                                            squareSize = squareSize,
                                        )
                                        if (playerSelectedPieceLegalTargets.contains(toSquare)) {
                                            dragOffset = Offset(0f, 0f)
                                            val playerMove = playerLegalMoves
                                                .searchMove(fromSquare, toSquare)
                                            if (playerMove != null) {
                                                chessBoard.makeMove(playerMove)
                                                // force jetpack compose to recompose by coping chessboard
                                                chessBoard = ChessBoard(chessBoard)
                                                playerTurn = !playerTurn
                                                computerLegalMoves =
                                                    moveGenerator.generateLegalMovesFor(
                                                        chessBoard,
                                                        computerColor
                                                    )
                                                playerLegalMoves =
                                                    moveGenerator.generateLegalMovesFor(
                                                        chessBoard,
                                                        playerColor
                                                    )
                                                touchStartPosition = Offset(0f, 0f)
                                                fromSquare = ChessBoard.NO_SQUARE
                                                touchSquare = ChessBoard.NO_SQUARE

                                                scope.launch() {
                                                    playComputer(
                                                        uci = uci, chessBoard = chessBoard,
                                                        moveGenerator = moveGenerator,
                                                        computerLegalMoves = computerLegalMoves
                                                    ) { newChessBoard ->
                                                        chessBoard = ChessBoard(newChessBoard)
                                                        playerTurn = !playerTurn
                                                        playerLegalMoves =
                                                            moveGenerator.generateLegalMovesFor(
                                                                chessBoard,
                                                                playerColor
                                                            )
                                                        computerLegalMoves =
                                                            moveGenerator.generateLegalMovesFor(
                                                                chessBoard,
                                                                computerColor
                                                            )


                                                    }

                                                }
                                            }
                                        } else {
                                            dragOffset = Offset(0f, 0f)

                                        }

                                    }

                                )
                            }
                            .scale(if (isPieceSelected && isDragging) 1.5f else 1f),
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
                if (fromSquare != ChessBoard.NO_SQUARE) {
                    isSquareLegalTarget = playerSelectedPieceLegalTargets.contains(i)
                }
                val isSquareEmpty = chessBoard.isSquareEmpty(i)
                // for highlighting legal target squares
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
                // for highlighting from,touch squares
                if (i == fromSquare || i == touchSquare) {
                    Image(
                        modifier = Modifier
                            .zIndex(0f)
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
                            R.drawable.transparent_yellow_square
                        ),
                        contentDescription = stringResource(
                            id = R.string.from_square
                        )
                    )

                }

                // for highlighting last move squares
                if (i == chessBoard.moves.lastMove?.from || i == chessBoard.moves.lastMove?.to) {
                    Image(
                        modifier = Modifier
                            .zIndex(0f)
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
                            R.drawable.transparent_yellow_square
                        ),
                        contentDescription = stringResource(
                            id = R.string.last_move
                        )
                    )

                }
                // for highlighting last move squares
                val playerKingPosition = moveGenerator.getKingPosition(chessBoard, playerColor)
                val computerKingPosition = moveGenerator.getKingPosition(chessBoard, computerColor)
                // for highlighting king check
                if (computerLegalMoves.isOpponentKingInCheck(playerKingPosition) && i == playerKingPosition
                ) {
                    Image(
                        modifier = Modifier
                            .zIndex(0f)
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
                            R.drawable.red_gradient
                        ),
                        contentDescription = stringResource(
                            id = R.string.king_checked
                        )
                    )

                }
                if (playerLegalMoves.isOpponentKingInCheck(computerKingPosition) && i == computerKingPosition
                ) {
                    Image(
                        modifier = Modifier
                            .zIndex(0f)
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
                            R.drawable.red_gradient
                        ),
                        contentDescription = stringResource(
                            id = R.string.king_checked
                        )
                    )

                }
            }

        }

    }

    if (showCancelGameDialog) {
        AlertDialog(
            onDismissRequest = { showCancelGameDialog = false },
            title = { Text(stringResource(id = R.string.cancel_game_title)) },
            text = { Text(stringResource(id = R.string.cancel_game_message)) },
            confirmButton = {
                TextButton(onClick = { (context as Activity).finish() }) {
                    Text(stringResource(id = R.string.finish_game_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelGameDialog = false }) {
                    Text(stringResource(id = R.string.finish_game_no))
                }
            },
        )
    }
    if (gameFinished) {
        AlertDialog(
            onDismissRequest = {
                gameFinished = false
                (context as Activity).finish()
            },
            title = {
                Text(
                    stringResource(
                        id = getGameStatusStringResourceId(
                            playerColor = playerColor,
                            gameStatus = gameStatus
                        )
                    )
                )
            },
            text = { Text(stringResource(id = R.string.game_finished_message)) },
            confirmButton = {
                TextButton(onClick = {
                    gameFinished = false
                    (context as Activity).recreate()
                }) {
                    Text(stringResource(id = R.string.finish_game_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { gameFinished = false }) {
                    Text(stringResource(id = R.string.finish_game_no))
                }
            },
        )
    }
}


suspend fun playComputer(
    uci: Uci,
    chessBoard: ChessBoard,
    moveGenerator: MoveGenerator,
    computerLegalMoves: PlayerLegalMoves,
    finished: (newChessBoard: ChessBoard) -> Unit
) {
    withContext(Dispatchers.IO) {
        val gameStatus = moveGenerator.checkStatus(chessBoard)
        if (gameStatus != GameStatus.NOT_FINISHED) {
            return@withContext
        }
        if (chessBoard.moves.isEmpty()) {
            uci.parseInput("position startpos")

        } else {
            uci.parseInput("position startpos moves " + chessBoard.moves.notation)
        }
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
            // pass chessboard to copy it to original chessboard to force jetpack compose to recompose
            // to fix view not updating when computer finishes the move
            finished(chessBoard)
        }
    }
}

fun getGameStatusStringResourceId(playerColor: Int, gameStatus: GameStatus): Int {
    return if (gameStatus === GameStatus.FINISHED_DRAW) {
        R.string.message_draw
    } else {
        if (playerColor == Piece.WHITE) {
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
    isChessBoardFlipped: Boolean,
    chessBoardHeight: Float,
    x: Float,
    y: Float,
    squareSize: Float,
): Int {
    val yMirrored = chessBoardHeight - y - 1
    val xMirrored = if (isChessBoardFlipped) chessBoardHeight - x - 1 else x
    val touchFile = floor((xMirrored / squareSize).toDouble()).toInt()
    val touchRank = floor((yMirrored / squareSize).toDouble()).toInt()
    return if (isChessBoardFlipped) ChessBoard.MAX_POSITION - ChessBoard.GetPosition(
        touchFile,
        touchRank
    ) else
        ChessBoard.GetPosition(touchFile, touchRank)
}

fun canSelect(chessBoard: ChessBoard, position: Int, playerColor: Int): Boolean {
    return !chessBoard.isSquareEmpty(position) &&
            chessBoard.pieceColor(position) == playerColor
}
