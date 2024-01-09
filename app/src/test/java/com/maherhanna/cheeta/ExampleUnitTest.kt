package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.PlayerLegalMoves
import com.maherhanna.cheeta.util.Log
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun move_generator_isCorrect() {
        val chessBoard = ChessBoard(ChessBoard.positionInUse)
        Log.d(Game.DEBUG, "chessBoard.print(): ${chessBoard.print()}")
        val moveGenerator = MoveGenerator(PlayerLegalMoves(), PlayerLegalMoves())
        chessBoard.toPlayColor = Piece.WHITE
        moveGenerator.updateWhiteLegalMoves(chessBoard)
        moveGenerator.updateBlackLegalMoves(chessBoard)
        val perftResults = listOf<ULong>(
            20UL,
            400UL,
            8902UL,
            197281UL,
            4865609UL,
            119060324UL,
            3195901860UL,
            84998978956UL,
            2439530234167UL
        )

        for (i in 1 until 10) {
            val perftResult = Perft(i,chessBoard,moveGenerator)
            assertEquals(perftResults[i - 1], perftResult)

        }

    }

    fun Perft(depth: Int, chessBoard: ChessBoard, moveGenerator: MoveGenerator): ULong {
        var nodes = 0UL;

        if (depth == 0)
            return 1UL;

        val move_list = moveGenerator.getLegalMovesFor(chessBoard,chessBoard.toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.move(move_list[i]);
            nodes += Perft(depth - 1,chessBoard,moveGenerator);
            chessBoard.unMove();
        }
        return nodes;
    }
}