package com.maherhanna.cheeta

import android.os.AsyncTask
import android.util.Log
import com.maherhanna.cheeta.ChessBoard.Companion.GetFile
import com.maherhanna.cheeta.ChessBoard.Companion.GetPosition
import com.maherhanna.cheeta.ChessBoard.Companion.GetRank
import com.maherhanna.cheeta.Game.GameStatus
import java.util.Collections

open class ComputerAiThread : AsyncTask<ChessBoard?, Void?, Move>() {
    private var foundCheckMate = false
    var evaluations: Long = 0
    var maxingPlayer = 0

    //killerMove[id][ply]
    var killerMove = Array(2) { arrayOfNulls<Move>(64) }
    protected override fun doInBackground(vararg chessBoards: ChessBoard?): Move {
        val startTime = System.nanoTime()
        //convert maximum search time from seconds to nano seconds
        val maxSearchTime = Game.COMPUTER_MAX_SEARCH_TIME * 1000000000
        foundCheckMate = false
        evaluations = 0
        val startChessBoard = ChessBoard(chessBoards[0])
        maxingPlayer = startChessBoard.toPlayColor
        var toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(
            startChessBoard,
            maxingPlayer
        )
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, 0)
        var maxDepth = 0
        var timeLeft: Long
        var moveIndex = 0
        do {
            val previousEvaluations = evaluations
            evaluations = 0
            timeLeft = startTime + maxSearchTime - System.nanoTime()
            maxDepth++
            val currentDepthMoveIndex =
                search(startChessBoard, toPlayLegalMoves, timeLeft, maxDepth)
            if (currentDepthMoveIndex == ChessBoard.NO_SQUARE) {
                maxDepth--
                evaluations = previousEvaluations
                break
            } else {
                moveIndex = currentDepthMoveIndex
            }
        } while (!foundCheckMate)
        var duration = System.nanoTime() - startTime
        duration = duration / 1000 // convert to milli second
        Log.d(
            Game.DEBUG, "alpha beta evaluations: " + evaluations + " move " +
                    moveIndex
        )
        Log.d(Game.DEBUG, "Duration: " + duration.toFloat() / 1000000 + " depth " + maxDepth)
        return toPlayLegalMoves[moveIndex]
    }

    fun search(chessBoard: ChessBoard?, moves: LegalMoves, timeLeft: Long, maxDepth: Int): Int {
        val maxScore = LOSE_SCORE
        var timeFinished = false
        val searchStart = System.nanoTime()
        var score = 0
        var maxIndex = ChessBoard.NO_SQUARE
        var currentMaxIndex = 0
        val progress = 0f
        var alpha = LOSE_SCORE
        val beta = WIN_SCORE
        for (i in 0 until moves.size()) {
            val chessBoardAfterMove = ChessBoard(chessBoard!!)
            chessBoardAfterMove.move(moves[i])
            score = -negaMax(
                chessBoardAfterMove, -beta,
                -alpha, (maxDepth - 1).toFloat(), 1
            )
            if (score >= beta) {
                currentMaxIndex = i
                foundCheckMate = true
                break
            }
            if (score > alpha) {
                alpha = score
                currentMaxIndex = i
            }
            if (System.nanoTime() - searchStart > timeLeft) {
                timeFinished = true
                break
            }
        }
        if (!timeFinished) {
            maxIndex = currentMaxIndex
        }
        return maxIndex
    }

    private fun quiescence(chessBoard: ChessBoard, alpha: Int, beta: Int, ply: Int): Int {
        var alpha = alpha
        evaluations++
        val toPlayColor = chessBoard.toPlayColor
        val eval = getScoreFor(chessBoard, toPlayColor)
        if (eval >= beta) {
            return beta
        }
        if (alpha < eval) {
            alpha = eval
        }
        var toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        val gameStatus = chessBoard.checkStatus(toPlayLegalMoves)
        if (isGameFinished(gameStatus)) {
            return getScoreFor(chessBoard, toPlayColor, gameStatus)
        } else {
            toPlayLegalMoves.removeNonTake()
        }
        if (toPlayLegalMoves.size() == 0) {
            evaluations++
            return getScoreFor(chessBoard, toPlayColor)
        }
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, ply)
        for (i in 0 until toPlayLegalMoves.size()) {
            val chessBoardAfterMove = ChessBoard(chessBoard)
            chessBoardAfterMove.move(toPlayLegalMoves[i])
            val score = -quiescence(
                chessBoardAfterMove, -beta, -alpha,
                ply + 1
            )
            if (score >= beta) {
                return beta
            }
            if (score > alpha) {
                alpha = score
            }
        }
        return alpha
    }

    fun negaMax(chessBoard: ChessBoard, alpha: Int, beta: Int, depth: Float, ply: Int): Int {
        var alpha = alpha
        val toPlayColor = chessBoard.toPlayColor
        var toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        val gameStatus = chessBoard.checkStatus(toPlayLegalMoves)
        if (depth == 0f) {
            evaluations++
            return if (isGameFinished(gameStatus)) {
                getGameFinishedScoreFor(gameStatus, toPlayColor)
            } else {
                quiescence(chessBoard, alpha, beta, ply)
            }
        }
        if (isGameFinished(gameStatus)) {
            evaluations++
            return getGameFinishedScoreFor(gameStatus, toPlayColor)
        }
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, ply)
        evaluations++
        var maxScore = LOSE_SCORE
        for (i in 0 until toPlayLegalMoves.size()) {
            val chessBoardAfterMove = ChessBoard(chessBoard)
            chessBoardAfterMove.move(toPlayLegalMoves[i])
            val score = -negaMax(
                chessBoardAfterMove, -beta, -alpha,
                depth - 1, ply + 1
            )
            maxScore = Math.max(maxScore, score)
            alpha = Math.max(alpha, score)
            if (score >= beta) {
                //killer moves
                killerMove[1][ply] = killerMove[0][ply]
                killerMove[0][ply] = Move(toPlayLegalMoves[i])
                return beta
            }
            if (score > alpha) {
                alpha = score
            }
        }
        return alpha
    }

    fun scoreMoves(moves: LegalMoves, ply: Int): ArrayList<MoveScore> {
        val scores = ArrayList<MoveScore>()
        var currentScore = 0
        var currentMove: Move
        for (i in 0 until moves.size()) {
            currentMove = moves[i]
            currentScore = 0
            if (currentMove.isTake) {
                val victim = currentMove.takenPieceType
                val attacker = currentMove.pieceType
                currentScore = mvv_lva[attacker * 6 + victim] + 10000
            } else {
                if (killerMove[0][ply] != null && killerMove[0][ply]!!.equals(moves[i])) {
                    currentScore = 9000
                } else if (killerMove[1][ply] != null && killerMove[1][ply]!!.equals(moves[i])) {
                    currentScore = 8000
                }
            }
            scores.add(MoveScore(currentScore, i))
        }
        return scores
    }

    fun sortMoves(moves: LegalMoves, ply: Int): LegalMoves {
        val scores = scoreMoves(moves, ply)
        Collections.sort(scores)
        val sortedMoves = LegalMoves()
        for (i in 0 until moves.size()) {
            sortedMoves.add(moves[scores[i].moveIndex])
        }
        return sortedMoves
    }

    private fun isGameFinished(gameStatus: GameStatus): Boolean {
        return gameStatus !== GameStatus.NOT_FINISHED
    }

    fun getGameFinishedWhiteScore(gameStatus: GameStatus): Int {
        var value = 0
        when (gameStatus) {
            GameStatus.FINISHED_WIN_WHITE -> value = WIN_SCORE
            GameStatus.FINISHED_WIN_BLACK -> value = LOSE_SCORE
            GameStatus.FINISHED_DRAW -> value = 0
            else -> {value = 0}
        }
        return value
    }

    fun getGameFinishedBlackScore(gameStatus: GameStatus?): Int {
        var value = 0
        when (gameStatus) {
            GameStatus.FINISHED_WIN_WHITE -> value = LOSE_SCORE
            GameStatus.FINISHED_WIN_BLACK -> value = WIN_SCORE
            GameStatus.FINISHED_DRAW -> value = 0
            else -> {value = 0}
        }
        return value
    }

    private fun getGameFinishedScoreFor(gameStatus: GameStatus, player: Int): Int {
        return if (player == Piece.WHITE) {
            getGameFinishedWhiteScore(gameStatus)
        } else {
            getGameFinishedBlackScore(gameStatus)
        }
    }

    fun getWhiteScore(chessBoard: ChessBoard): Int {
        return getPiecesValueFor(chessBoard, Piece.WHITE)
    }

    fun getBlackScore(chessBoard: ChessBoard): Int {
        return getPiecesValueFor(chessBoard, Piece.BLACK)
    }

    fun getScoreFor(chessBoard: ChessBoard, color: Int): Int {
        return if (color == Piece.WHITE) {
            getWhiteScore(chessBoard) - getBlackScore(chessBoard)
        } else {
            getBlackScore(chessBoard) - getWhiteScore(chessBoard)
        }
    }

    fun getScoreFor(chessBoard: ChessBoard, color: Int, gameStatus: GameStatus): Int {
        return if (isGameFinished(gameStatus)) {
            getGameFinishedScoreFor(gameStatus, color)
        } else {
            if (color == Piece.WHITE) {
                getWhiteScore(chessBoard) - getBlackScore(chessBoard)
            } else {
                getBlackScore(chessBoard) - getWhiteScore(chessBoard)
            }
        }
    }

    fun getPiecesValueFor(chessboard: ChessBoard, color: Int): Int {
        var value = 0
        val squares: ArrayList<Int>
        squares = if (color == Piece.WHITE) {
            Game.moveGenerator.getWhitePositions(chessboard)
        } else {
            Game.moveGenerator.getBlackPositions(chessboard)
        }
        for (square in squares) {
            value += getPositionalValue(chessboard, square)
        }
        return value
    }

    fun getPiecesValueMinusKingFor(chessboard: ChessBoard, color: Int): Int {
        var value = 0
        val squares: ArrayList<Int>
        squares = if (color == Piece.WHITE) {
            Game.moveGenerator.getWhitePositions(chessboard)
        } else {
            Game.moveGenerator.getBlackPositions(chessboard)
        }
        for (square in squares) {
            if (chessboard.pieceType(square) == Piece.KING) continue
            value += getPositionalValue(chessboard, square)
        }
        return value
    }

    private fun getPositionalValue(chessBoard: ChessBoard, square: Int): Int {
        var value = 0
        var piecePositionOnTable = ChessBoard.OUT
        var file = GetFile(square)
        var rank = GetRank(square)
        if (chessBoard.pieceColor(square) == Piece.WHITE) {
            rank = 7 - rank
        } else {
            file = 7 - file
        }
        piecePositionOnTable = GetPosition(file, rank)
        when (chessBoard.pieceType(square)) {
            Piece.PAWN -> value += Piece.PAWN_VALUE + PAWN_SQUARES_TABLE[piecePositionOnTable]
            Piece.ROOK -> value += Piece.ROOK_VALUE + ROOK_SQUARES_TABLE[piecePositionOnTable]
            Piece.KNIGHT -> value += Piece.KNIGHT_VALUE + KNIGHT_SQUARES_TABLE[piecePositionOnTable]
            Piece.BISHOP -> value += Piece.BISHOP_VALUE + BISHOP_SQUARES_TABLE[piecePositionOnTable]
            Piece.QUEEN -> value += Piece.QUEEN_VALUE + QUEEN_SQUARES_TABLE[piecePositionOnTable]
            Piece.KING -> value += if (isEndGame(chessBoard)) {
                Piece.KING_VALUE + KING_END_GAME_SQUARES_TABLE[piecePositionOnTable]
            } else {
                Piece.KING_VALUE + KING_MIDDLE_GAME_SQUARES_TABLE[piecePositionOnTable]
            }
        }
        return value
    }

    fun isEndGame(chessBoard: ChessBoard): Boolean {
        var endGame = false
        val whitePiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.WHITE)
        val blackPiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.BLACK)
        if (Math.abs(whitePiecesValue - blackPiecesValue) >= Piece.QUEEN_VALUE) {
            if (whitePiecesValue < Piece.QUEEN_VALUE) endGame = true
            if (blackPiecesValue < Piece.QUEEN_VALUE) endGame = true
        }
        return endGame
    }

    private fun getPieceValue(pieceType: Int): Int {
        var value = 0
        when (pieceType) {
            Piece.PAWN -> value += Piece.PAWN_VALUE
            Piece.ROOK -> value += Piece.ROOK_VALUE
            Piece.KNIGHT -> value += Piece.KNIGHT_VALUE
            Piece.BISHOP -> value += Piece.BISHOP_VALUE
            Piece.QUEEN -> value += Piece.QUEEN_VALUE
            Piece.KING -> value += Piece.KING_VALUE
        }
        return value
    }

    companion object {
        private const val LOSE_SCORE = -1000000
        private const val WIN_SCORE = 1000000
        var PAWN_SQUARES_TABLE = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
        )
        var KNIGHT_SQUARES_TABLE = intArrayOf(
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
        )
        var BISHOP_SQUARES_TABLE = intArrayOf(
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
        )
        var ROOK_SQUARES_TABLE = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
        )
        var QUEEN_SQUARES_TABLE = intArrayOf(
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
        )
        var KING_MIDDLE_GAME_SQUARES_TABLE = intArrayOf(
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
        )
        var KING_END_GAME_SQUARES_TABLE = intArrayOf(
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
        )
        private val mvv_lva = intArrayOf(
            105, 205, 305, 405, 505, 605,
            104, 204, 304, 404, 504, 604,
            103, 203, 303, 403, 503, 603,
            102, 202, 302, 402, 502, 602,
            101, 201, 301, 401, 501, 601,
            100, 200, 300, 400, 500, 600
        )
    }
}