package com.maherhanna.cheeta

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.Piece

@Composable
fun GameScreen() {
    val chessBoard = ChessBoard(ChessBoard.positionInUse)
    var chessBoardImageSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    chessBoardImageSize = coordinates.size.toSize()
                },
            painter = painterResource(R.drawable.chessboard),
            contentDescription = stringResource(R.string.chessboard),
            contentScale = ContentScale.FillWidth
        )

        for (i in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {
            if (!chessBoard.isSquareEmpty(i)) {
                //drawPiece(chessBoard.pieceType(i), chessBoard.pieceColor(i), i, 0f, 0f, 1f)
                Image(
                    modifier = Modifier
                        .offset(
                            x = (ChessBoard.GetFile(i) * (chessBoardImageSize.width / 8)).dp,
                            y = (ChessBoard.GetRank(i) * (chessBoardImageSize.height / 8)).dp
                        )
                        .size((chessBoardImageSize.width / 8).dp),
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

