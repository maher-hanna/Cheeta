package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.GameStatus
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Uci
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression tests for rule and search bugs found in the engine audit. */
class EngineRegressionTest {

    private fun board(fen: String) = ChessBoard("$fen ")

    private fun legalMoves(board: ChessBoard): List<String> {
        val moves = MoveGenerator().generateLegalMovesFor(board, board.toPlayColor)
        return (0 until moves.size()).map { moves[it].notation }
    }

    private fun legalMoves(fen: String) = legalMoves(board(fen))

    private fun boardAfter(fenAndMoves: String): ChessBoard {
        val uci = Uci()
        uci.parseInput("position fen $fenAndMoves")
        return uci.chessBoard
    }

    private fun perft(depth: Int, board: ChessBoard, generator: MoveGenerator): Long {
        if (depth == 0) return 1
        val moves = generator.generateLegalMovesFor(board, board.toPlayColor)
        if (depth == 1) return moves.size().toLong()
        var nodes = 0L
        for (i in 0 until moves.size()) {
            board.makeMove(moves[i])
            nodes += perft(depth - 1, board, generator)
            board.unMakeMove()
        }
        return nodes
    }

    private fun assertPerft(fen: String, vararg expected: Long) {
        val b = board(fen)
        val generator = MoveGenerator()
        expected.forEachIndexed { i, nodes ->
            assertEquals("perft depth ${i + 1} of $fen", nodes, perft(i + 1, b, generator))
        }
    }

    // ------------------------------------------------------------- hashing

    @Test
    fun incrementalHashMatchesFullHashInRandomGames() {
        val random = java.util.Random(12345)
        val starts = listOf(
            ChessBoard.startPosition,
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        )
        val generator = MoveGenerator()
        for (fen in starts) {
            repeat(30) {
                val b = board(fen)
                val hashes = mutableListOf(b.zobristHash)
                assertEquals(com.maherhanna.cheeta.core.Zobrist.hash(b), b.zobristHash)
                // play random moves, checking the incremental hash after each one
                for (ply in 0 until 40) {
                    val moves = generator.generateLegalMovesFor(b, b.toPlayColor)
                    if (moves.size() == 0) break
                    b.makeMove(moves[random.nextInt(moves.size())])
                    assertEquals("after make", com.maherhanna.cheeta.core.Zobrist.hash(b), b.zobristHash)
                    hashes.add(b.zobristHash)
                }
                // undo everything, checking the hash is restored at every step
                while (hashes.size > 1) {
                    b.unMakeMove()
                    hashes.removeAt(hashes.size - 1)
                    assertEquals("after unmake", hashes.last(), b.zobristHash)
                    assertEquals(com.maherhanna.cheeta.core.Zobrist.hash(b), b.zobristHash)
                }
            }
        }
    }

    @Test
    fun transpositionsHaveTheSameHash() {
        val a = boardAfter("${ChessBoard.startPosition} moves g1f3 g8f6 b1c3 b8c6")
        val b = boardAfter("${ChessBoard.startPosition} moves b1c3 b8c6 g1f3 g8f6")
        assertEquals(a.zobristHash, b.zobristHash)
        // same pieces but different side to move must differ
        val c = boardAfter("${ChessBoard.startPosition} moves g1f3 g8f6 f3g1 f6g8")
        assertEquals(board(ChessBoard.startPosition).zobristHash, c.zobristHash)
        assertNotEquals(a.zobristHash, board(ChessBoard.startPosition).zobristHash)
    }

    @Test
    fun enPassantSquareOnlyHashedWhenCapturePossible() {
        // no black pawn can take on e3: same position as without the en passant square
        assertEquals(
            board("4k3/8/8/8/4P3/8/8/4K3 b - e3 0 1").zobristHash,
            board("4k3/8/8/8/4P3/8/8/4K3 b - - 0 1").zobristHash
        )
        // a black pawn on d4 can take on e3: the en passant square matters
        assertNotEquals(
            board("4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1").zobristHash,
            board("4k3/8/8/8/3pP3/8/8/4K3 b - - 0 1").zobristHash
        )
    }

    // -------------------------------------------- static exchange evaluation

    private fun see(fen: String, notation: String): Int {
        val b = board(fen)
        val moves = MoveGenerator().generateLegalMovesFor(b, b.toPlayColor)
        val move = (0 until moves.size()).map { moves[it] }.first { it.notation == notation }
        return com.maherhanna.cheeta.core.ChessEngine().staticExchangeEvaluation(b, move)
    }

    @Test
    fun staticExchangeEvaluation() {
        // undefended pawn: win it
        assertEquals(100, see("4k3/8/8/3p4/8/8/8/3QK3 w - - 0 1", "d1d5"))
        // pawn defended by a pawn: queen for pawn
        assertEquals(-800, see("4k3/8/2p5/3p4/8/8/8/3QK3 w - - 0 1", "d1d5"))
        // rook takes defended pawn, the rook behind it (x-ray) recaptures: R for 2P
        assertEquals(-300, see("4k3/8/2p5/3p4/8/8/3R4/3RK3 w - - 0 1", "d2d5"))
        // pawn takes defended knight: keeps the knight-for-pawn gain
        assertEquals(220, see("4k3/8/2p5/3n4/4P3/8/8/4K3 w - - 0 1", "e4d5"))
    }

    // ---------------------------------------------------------------- perft

    @Test
    fun perftKiwipete() = assertPerft(
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        48, 2039, 97862, 4085603
    )

    @Test
    fun perftPosition3() = assertPerft(
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
        14, 191, 2812, 43238, 674624
    )

    @Test
    fun perftPosition4() = assertPerft(
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        6, 264, 9467, 422333
    )

    @Test
    fun perftPosition4Mirrored() = assertPerft(
        "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1",
        6, 264, 9467, 422333
    )

    @Test
    fun perftPosition5() = assertPerft(
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
        44, 1486, 62379
    )

    @Test
    fun perftPosition6() = assertPerft(
        "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
        46, 2079, 89890
    )

    // ------------------------------------------------------------ castling

    @Test
    fun rookMovingFromOtherSquareKeepsQueenSideCastling() {
        val b = boardAfter("4k3/8/8/8/8/8/3R4/R3K3 w Q - 0 1 moves d2d3 e8f8")
        assertEquals(ChessBoard.CASTLING_QUEEN_SIDE, b.whiteCastlingRights)
        assertTrue(legalMoves(b).contains("e1c1"))
    }

    @Test
    fun capturingRookOnHomeSquareRemovesCastlingRight() {
        val b = boardAfter("4k3/8/8/8/8/8/1b5P/R3K3 b Q - 0 1 moves b2a1 h2h3")
        assertEquals(ChessBoard.NO_CASTLING, b.whiteCastlingRights)
    }

    @Test
    fun noQueenSideCastlingWithoutRook() {
        assertFalse(legalMoves("4k3/8/8/8/8/8/8/b3K3 w Q - 0 1").contains("e1c1"))
    }

    @Test
    fun castlingAllowedWhenRookIsAttacked() {
        assertTrue(legalMoves("4k2r/8/8/8/8/8/8/R3K2R w KQ - 0 1").contains("e1g1"))
    }

    @Test
    fun queenSideCastlingAllowedWhenBFileSquareIsAttacked() {
        assertTrue(legalMoves("1r2k3/8/8/8/8/8/8/R3K3 w Q - 0 1").contains("e1c1"))
    }

    // ---------------------------------------------------------- en passant

    @Test
    fun enPassantCapturesCheckingPawn() {
        assertTrue(legalMoves("4k3/8/8/3pP3/4K3/8/8/8 w - d6 0 1").contains("e5d6"))
        assertTrue(legalMoves("8/8/8/5k2/4Pp2/8/8/4K3 b - e3 0 1").contains("f4e3"))
    }

    // ---------------------------------------------------------- promotions

    @Test
    fun allPromotionsAreGenerated() {
        val moves = legalMoves("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        assertTrue(moves.containsAll(listOf("a7a8q", "a7a8r", "a7a8b", "a7a8n")))
    }

    @Test
    fun uciUnderPromotionIsApplied() {
        val b = boardAfter("4k3/P7/8/8/8/8/8/4K3 w - - 0 1 moves a7a8n")
        assertEquals(com.maherhanna.cheeta.core.Piece.KNIGHT, b.pieceType(56))
    }

    // ---------------------------------------------------------- game status

    @Test
    fun checkmateFromFenReportsCorrectWinner() {
        val b = board("R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1")
        assertEquals(GameStatus.FINISHED_WIN_WHITE, MoveGenerator().checkStatus(b))
    }

    @Test
    fun fiftyMoveRuleNeedsHundredHalfMoves() {
        assertEquals(
            GameStatus.NOT_FINISHED,
            MoveGenerator().checkStatus(boardAfter("4k3/8/8/8/8/8/8/R3K3 w - - 49 60 moves a1a2"))
        )
        assertEquals(
            GameStatus.FINISHED_DRAW,
            MoveGenerator().checkStatus(boardAfter("4k3/8/8/8/8/8/8/R3K3 w - - 99 60 moves a1a2"))
        )
    }

    @Test
    fun halfMoveClockSurvivesMakeUnmake() {
        val b = board("4k3/8/8/8/8/8/8/R3K3 w - - 70 80")
        val move = MoveGenerator().generateLegalMovesFor(b, b.toPlayColor)[0]
        val bitsBefore = move.bitValue
        b.makeMove(move)
        b.unMakeMove()
        assertEquals(70, b.fiftyMovesDrawCount)
        assertEquals("make/unmake must not modify the move", bitsBefore, move.bitValue)
    }

    @Test
    fun repetitionStateComparesPiecesAndSideToMove() {
        assertNotEquals(
            board("4k3/8/8/8/8/8/8/R3K3 w - - 0 1").states[0],
            board("4k3/8/8/8/8/8/8/N3K3 w - - 0 1").states[0]
        )
        assertNotEquals(
            board("4k3/8/8/8/8/8/8/R3K3 w - - 0 1").states[0],
            board("4k3/8/8/8/8/8/8/R3K3 b - - 0 1").states[0]
        )
    }

    @Test
    fun threefoldRepetitionIsDraw() {
        val b = boardAfter(
            "4k3/8/8/8/8/8/8/R3K3 w - - 0 1 moves a1a2 e8d8 a2a1 d8e8 a1a2 e8d8 a2a1 d8e8"
        )
        assertEquals(GameStatus.FINISHED_DRAW, MoveGenerator().checkStatus(b))
    }

    // --------------------------------------------------------------- search

    @Test
    fun deepSearchDoesNotCrash() {
        val uci = Uci()
        uci.parseInput("position fen 8/8/8/4k3/8/8/4P3/4K3 w - - 0 1")
        assertTrue(uci.parseInput("go depth 9").startsWith("bestmove "))
    }

    @Test
    fun searchOnFinishedGameReturnsNullMove() {
        val uci = Uci()
        uci.parseInput("position fen R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1")
        assertEquals("bestmove 0000", uci.parseInput("go depth 3").trim())
    }

    @Test
    fun findsMateInOne() {
        val uci = Uci()
        uci.parseInput("position fen 6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1")
        assertEquals("bestmove a1a8", uci.parseInput("go depth 4").trim())
    }

    @Test
    fun findsMateInTwo() {
        // back rank: 1.Re8+ Rxe8 2.Rxe8#
        val uci = Uci()
        uci.parseInput("position fen 3r2k1/5ppp/8/8/8/8/4RPPP/4R1K1 w - - 0 1")
        val best = uci.parseInput("go depth 4").trim()
        // either rook can start the mate
        assertTrue(best, best == "bestmove e1e8" || best == "bestmove e2e8")
    }

    @Test
    fun respectsMoveTime() {
        val uci = Uci()
        uci.parseInput("position fen ${ChessBoard.trickyPosition}")
        val start = System.currentTimeMillis()
        uci.parseInput("go movetime 1000")
        val elapsed = System.currentTimeMillis() - start
        assertTrue("search took $elapsed ms", elapsed < 1300)
    }
}
