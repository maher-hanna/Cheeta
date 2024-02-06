package com.maherhanna.cheeta

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.Piece

@Composable
fun GameScreen(humanPlayerColor: Int) {
    val chessBoard = ChessBoard(ChessBoard.positionInUse)
    var chessBoardImageSize by remember { mutableStateOf(Size.Zero) }

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
                    .fillMaxWidth()
                    ,
                painter = painterResource(R.drawable.chessboard),
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
                    val squareSize = (chessBoardImageSize.width / 8)
                    Image(
                        modifier = Modifier
                            .offset(
                                x = with(LocalDensity.current) { (ChessBoard.GetFile(i) * squareSize).toDp() },
                                y = with(LocalDensity.current) { (ChessBoard.GetRank(i) * squareSize).toDp() }
                            )
                            .size(with(LocalDensity.current) { squareSize.toDp() }),
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

