package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.Uci
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Blunder checks: short tactical positions with an unambiguous answer. Each test runs the
 * engine the same way the app does (fixed move time) and checks that it either finds the
 * winning move or avoids the obvious blunder. Positions are tested for both colours so that
 * sign errors in the search/evaluation show up.
 */
class EngineTacticsTest {

    private fun bestMove(fen: String, moveTimeMs: Long = MOVE_TIME_MS): String {
        val uci = Uci()
        uci.parseInput("position fen $fen")
        return uci.parseInput("go movetime $moveTimeMs").trim().removePrefix("bestmove").trim()
    }

    private fun assertPlays(fen: String, vararg expected: String) {
        val move = bestMove(fen)
        assertTrue("in $fen expected one of ${expected.toList()} but engine played $move", move in expected)
    }

    private fun assertAvoids(fen: String, blunder: String) {
        val move = bestMove(fen)
        assertFalse("in $fen engine played the blunder $move", move == blunder)
    }

    // ------------------------------------------------------------ checkmates

    @Test
    fun whiteMateInOneBackRank() = assertPlays("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", "a1a8")

    @Test
    fun blackMateInOneBackRank() = assertPlays("r5k1/8/8/8/8/8/5PPP/6K1 b - - 0 1", "a8a1")

    @Test
    fun whiteMateInTwoBackRank() =
        // 1.Re8+ Rxe8 2.Rxe8#
        assertPlays("3r2k1/5ppp/8/8/8/8/4RPPP/4R1K1 w - - 0 1", "e1e8", "e2e8")

    @Test
    fun blackMateInTwoBackRank() =
        // mirror of the white position: 1...Re1+ Rxe1 2...Rxe1#
        assertPlays("4r1k1/4rppp/8/8/8/8/5PPP/3R2K1 b - - 0 1", "e8e1", "e7e1")

    @Test
    fun prefersMateOverStalemate() =
        // Qg7#, Qh7# and Qe8# mate; quiet moves such as Qf7 stalemate
        assertPlays("7k/8/6QK/8/8/8/8/8 w - - 0 1", "g6g7", "g6h7", "g6e8")

    @Test
    fun whiteFindsScholarsMate() =
        // full board: Qxf7# (the bishop on c4 protects f7)
        assertPlays("r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 4 4", "h5f7")

    @Test
    fun blackDefendsScholarsMate() =
        // full board: ...Nf6?? allows Qxf7#
        assertAvoids("r1bqkbnr/pppp1ppp/2n5/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 3 3", "g8f6")

    // ------------------------------------------------------------ win material

    @Test
    fun whiteTakesHangingQueen() = assertPlays("4k3/8/8/3q4/8/8/3R4/4K3 w - - 0 1", "d2d5")

    @Test
    fun blackTakesHangingQueen() = assertPlays("4k3/3r4/8/8/3Q4/8/8/4K3 b - - 0 1", "d7d4")

    @Test
    fun whiteKnightForkKingAndQueen() = assertPlays("q3k3/8/8/1N6/8/8/8/4K3 w - - 0 1", "b5c7")

    @Test
    fun blackKnightForkKingAndQueen() = assertPlays("4k3/8/8/8/1n6/8/8/Q3K3 b - - 0 1", "b4c2")

    @Test
    fun whitePromotesToQueen() = assertPlays("8/P7/8/8/8/8/k7/4K3 w - - 0 1", "a7a8q")

    @Test
    fun blackPromotesToQueen() = assertPlays("4k3/8/8/8/8/8/p7/7K b - - 0 1", "a2a1q")

    // ------------------------------------------------------------ avoid blunders

    @Test
    fun whiteDoesNotTakeDefendedPawnWithQueen() =
        // Qxd5?? cxd5 loses the queen for a pawn
        assertAvoids("4k3/8/2p5/3p4/8/8/8/3QK3 w - - 0 1", "d1d5")

    @Test
    fun blackDoesNotTakeDefendedPawnWithQueen() =
        assertAvoids("3qk3/8/8/8/3P4/2P5/8/4K3 b - - 0 1", "d8d4")

    @Test
    fun whiteKeepsBackRankDefender() =
        // Rxd6?? allows Re1#
        assertAvoids("4r1k1/5ppp/3b4/8/8/8/5PPP/3R2K1 w - - 0 1", "d1d6")

    @Test
    fun blackKeepsBackRankDefender() =
        // mirror: Rxd3?? allows Re8#
        assertAvoids("3r2k1/5ppp/8/8/8/3B4/5PPP/4R1K1 b - - 0 1", "d8d3")

    @Test
    fun whiteSavesAttackedQueen() {
        // the queen on d4 is attacked by a pawn; any move leaving it there loses it
        val move = bestMove("4k3/8/8/2p5/3Q4/8/8/4K3 w - - 0 1")
        assertTrue("engine left the queen en prise with $move", move.startsWith("d4"))
    }

    @Test
    fun blackSavesAttackedQueen() {
        val move = bestMove("4k3/8/8/3q4/2P5/8/8/4K3 b - - 0 1")
        assertTrue("engine left the queen en prise with $move", move.startsWith("d5"))
    }

    companion object {
        // the app searches with "go movetime 4000"; a shorter limit keeps the suite fast
        private const val MOVE_TIME_MS = 2000L
    }
}
