package com.maherhanna.cheeta


import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

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

    var playerLegalMoves by remember {
        mutableStateOf(PlayerLegalMoves())
    }
    var computerLegalMoves by remember {
        mutableStateOf(PlayerLegalMoves())
    }
    var playerSelectedPieceLegalTargets by remember {
        mutableStateOf(emptyList<Int>())
    }
    // depth reached by the computer's last search (read once the search has finished)
    var searchDepth by remember { mutableIntStateOf(0) }
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
        // the board changes when the computer has moved; pick up how deep it searched
        searchDepth = uci.engine.lastSearchDepth
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
        LastCapturedPiece(lastCapture = chessBoard.moves.lastCapture)

        // the board must never be mirrored: in right-to-left languages (e.g. Arabic)
        // offsets and alignment would be mirrored while touch positions are not
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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

        // game result shown under the board instead of a dialog
        GameResult(
            gameStatus = gameStatus,
            playerColor = playerColor,
            onNewGame = { (context as Activity).recreate() }
        )

        SearchDepthIndicator(
            depth = searchDepth,
            isThinking = !playerTurn && gameStatus == GameStatus.NOT_FINISHED,
            visible = searchDepth > 0
        )
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
}

// fixed height so the board does not move when the first capture appears
private val INFO_ROW_HEIGHT = 56.dp

@Composable
private fun LastCapturedPiece(lastCapture: Move?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(INFO_ROW_HEIGHT)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lastCapture != null) {
            Text(
                text = stringResource(id = R.string.last_captured),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                modifier = Modifier.size(40.dp),
                painter = painterResource(
                    id = getPieceDrawableId(
                        lastCapture.takenPieceType,
                        // the captured piece belongs to the side that did not move
                        Piece.GetOppositeColor(lastCapture.color)
                    )
                ),
                contentDescription = stringResource(id = R.string.last_captured)
            )
        }
    }
}

@Composable
private fun GameResult(gameStatus: GameStatus, playerColor: Int, onNewGame: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(INFO_ROW_HEIGHT)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (gameStatus != GameStatus.NOT_FINISHED) {
            Text(
                text = stringResource(
                    id = getGameStatusStringResourceId(
                        playerColor = playerColor,
                        gameStatus = gameStatus
                    )
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onNewGame) {
                Text(stringResource(id = R.string.new_game))
            }
        }
    }
}

private const val DEPTH_BAR_COUNT = 10

/**
 * Search depth display. While the computer thinks, the bars play a looping wave that is
 * purely visual (the engine is not asked for progress). Once the move is made, the depth
 * the engine reached is shown and the bars fill up to it one after another.
 */
@Composable
private fun SearchDepthIndicator(depth: Int, isThinking: Boolean, visible: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!visible && !isThinking) return@Row
        Column {
            Text(
                text = stringResource(
                    id = if (isThinking) R.string.engine_thinking else R.string.search_depth
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(
                visible = !isThinking,
                enter = slideInVertically { height -> height } + fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = depth.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isThinking) ThinkingBars() else DepthBars(depth = depth)
    }
}

@Composable
private fun DepthBar(index: Int, modifier: Modifier, color: Color) {
    // bars get taller from the first to the last depth
    val barHeight = 14.dp + 26.dp * (index + 1) / DEPTH_BAR_COUNT
    Box(
        modifier = Modifier
            .width(8.dp)
            .height(barHeight)
            .then(modifier)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
private fun ThinkingBars() {
    // a wave travelling across the bars; read inside graphicsLayer so only the
    // drawing layer updates each frame, not the composition
    val phase = rememberInfiniteTransition(label = "thinking").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave phase"
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (index in 0 until DEPTH_BAR_COUNT) {
            DepthBar(
                index = index,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer {
                    val wave = (sin(2 * PI * (phase.value - index.toFloat() / DEPTH_BAR_COUNT)) + 1) / 2
                    scaleY = 0.45f + 0.55f * wave.toFloat()
                    alpha = 0.35f + 0.65f * wave.toFloat()
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
            )
        }
    }
}

@Composable
private fun DepthBars(depth: Int) {
    val filledColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (index in 0 until DEPTH_BAR_COUNT) {
            val isFilled = index < depth
            // 0 = empty, 1 = filled; EaseOutBack overshoots slightly for a small bounce
            val progress = remember { Animatable(0f) }
            LaunchedEffect(depth) {
                // fill the bars one after another
                delay(index * 70L)
                progress.animateTo(
                    targetValue = if (isFilled) 1f else 0f,
                    animationSpec = tween(durationMillis = 350, easing = EaseOutBack)
                )
            }
            DepthBar(
                index = index,
                color = lerp(emptyColor, filledColor, progress.value.coerceIn(0f, 1f)),
                modifier = Modifier.graphicsLayer {
                    scaleY = 0.55f + 0.45f * progress.value
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
            )
        }
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
            uci.parseInput("go movetime 4000")
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
