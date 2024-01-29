package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.ChessBoard.Companion.GetFile
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetPosition
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetRank
import java.util.logging.Level
import java.util.logging.Logger

open class ChessEngine {
    private var foundCheckMate = false
    private var evaluations: Long = 0
    private var betaCutOffs: Long = 0
    private var maxingPlayer = 0
    private var searchTimeFinished = false
    private var alpha = LOSE_SCORE
    private var beta = WIN_SCORE
    var moveGenerator = MoveGenerator()
    var isUciMode = false

    //killerMove[id][ply]
    private var killerMove = Array(2) { arrayOfNulls<Move>(64) }

    fun reset() {
        foundCheckMate = false
        evaluations = 0
        betaCutOffs = 0
        maxingPlayer = 0
        searchTimeFinished = false
        alpha = LOSE_SCORE
        beta = WIN_SCORE
        moveGenerator = MoveGenerator()
        isUciMode = false

        killerMove = Array(2) { arrayOfNulls<Move>(64) }
        moveGenerator.reset()
    }

    fun getMove(chessBoard: ChessBoard): Move? {
        val startTime = System.nanoTime()
        //convert maximum search time from seconds to nano seconds
        val maxSearchTime = COMPUTER_MAX_SEARCH_TIME * 1000000000
        foundCheckMate = false
        evaluations = 0
        betaCutOffs = 0
        searchTimeFinished = false
        maxingPlayer = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            maxingPlayer
        )
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, 0)
        var maxDepth = 0
        var timeLeft: Long
        var move: Move? = null
        alpha = LOSE_SCORE
        beta = WIN_SCORE
        do {
            timeLeft = startTime + maxSearchTime - System.nanoTime()
            maxDepth++
            //Log.d(Game.DEBUG, "depth: ${maxDepth}")
            val currentSearchMove =
                search(chessBoard, toPlayLegalMoves, timeLeft,maxSearchTime, maxDepth)

            if (currentSearchMove != null) {
                move = currentSearchMove
            }
        } while (!foundCheckMate && !searchTimeFinished)
        var duration = System.nanoTime() - startTime
        duration /= 1000 // convert to milli second
        if (!isUciMode) {
            Logger.getLogger(DEBUG_TAG).log(
                Level.INFO, "evaluations " + evaluations +
                        " depth " + maxDepth +
                        "\nmove " + move?.pieceName.toString() + " " + move?.fromNotation.toString() + " to " + move?.toNotation.toString()
                        + "\ntime " + duration.toFloat() / 1000000
            )

        }
//        Log.d(
//            DEBUG_TAG, "evaluations: " + evaluations +
//                    "\n beta cutoffs: " + betaCutOffs +
//                    "\n move " + move.pieceName + " from: " + move.from + " move to " + move.to
//        )
//        Log.d(
//            DEBUG_TAG,
//            "Duration: " + duration.toFloat() / 1000000 + " depth " + maxDepth
//        )
        return move

    }

    fun makeMove(chessBoard: ChessBoard, moveNotation: String): Move? {
        val playerColor = chessBoard.toPlayColor
        val toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            playerColor
        )
        val move = toPlayLegalMoves.getMove(Move(moveNotation))
        if (move != null) {
            chessBoard.makeMove(move)
        }
        return move
    }

    private fun search(
        chessBoard: ChessBoard,
        moves: PlayerLegalMoves,
        timeLeft: Long,
        maxSearchTime : Long,
        maxDepth: Int,
    ): Move? {
        if(timeLeft > maxSearchTime){
            searchTimeFinished = true
            return null
        }
        val searchStart = System.nanoTime()
        var score: Int
        var bestMove: Move? = null
        for (i in 0 until moves.size()) {
            chessBoard.makeMove(moves[i])
            score = -negaMax(
                chessBoard, -beta,
                -alpha, (maxDepth - 1).toFloat(), 1
            )
            chessBoard.unMakeMove()
            if (score >= beta) {
                bestMove = moves[i]
                foundCheckMate = true
                break
            }
            if (score > alpha) {
                alpha = score
                bestMove = moves[i]
            }
            if (System.nanoTime() - searchStart > timeLeft) {
                searchTimeFinished = true
                break
            }
        }

        return bestMove
    }

    private fun quiescence(chessBoard: ChessBoard, alphaArg: Int, betaArg: Int, ply: Int): Int {
        var quiescenceAlpha = alphaArg
        val toPlayColor = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        val gameStatus = checkStatus(chessBoard)
        val eval = evaluate(chessBoard, toPlayColor, gameStatus, toPlayLegalMoves)
        if (eval >= betaArg) {
            return betaArg
        }
        if (quiescenceAlpha < eval) {
            quiescenceAlpha = eval
        }

        if (isGameFinished(gameStatus)) {
            return evaluate(chessBoard, toPlayColor, gameStatus, toPlayLegalMoves)
        } else {
            toPlayLegalMoves.removeNonTake()
        }
        if (toPlayLegalMoves.size() == 0) {
            return evaluate(chessBoard, toPlayColor, gameStatus, toPlayLegalMoves)
        }
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, ply)
        for (i in 0 until toPlayLegalMoves.size()) {
            chessBoard.makeMove(toPlayLegalMoves[i])
            val score = -quiescence(
                chessBoard, -betaArg, -quiescenceAlpha,
                ply + 1
            )
            chessBoard.unMakeMove()
            if (score >= betaArg) {
                return betaArg
            }
            if (score > quiescenceAlpha) {
                quiescenceAlpha = score
            }
        }
        return quiescenceAlpha
    }

    private fun negaMax(
        chessBoard: ChessBoard,
        alphaArg: Int,
        beta: Int,
        depth: Float,
        ply: Int
    ): Int {
        var alpha = alphaArg
        val toPlayColor = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        val gameStatus = checkStatus(chessBoard)
        evaluations++
        if (depth == 0f) {
            return if (isGameFinished(gameStatus)) {
                getGameFinishedScoreFor(gameStatus, toPlayColor)
            } else {
                quiescence(chessBoard, alpha, beta, ply)
            }
        }
        if (isGameFinished(gameStatus)) {
            return getGameFinishedScoreFor(gameStatus, toPlayColor)
        }
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, ply)
        var maxScore = LOSE_SCORE
        for (i in 0 until toPlayLegalMoves.size()) {
            chessBoard.makeMove(toPlayLegalMoves[i])
            val score = -negaMax(
                chessBoard, -beta, -alpha,
                depth - 1, ply + 1
            )
            chessBoard.unMakeMove()
            maxScore = maxScore.coerceAtLeast(score)
            alpha = alpha.coerceAtLeast(score)
            if (score >= beta) {
                //killer moves
                killerMove[1][ply] = killerMove[0][ply]
                killerMove[0][ply] = Move(toPlayLegalMoves[i])
                betaCutOffs++
                return beta
            }
            if (score > alpha) {
                alpha = score
            }
        }
        return alpha
    }

    private fun scoreMoves(moves: PlayerLegalMoves, ply: Int): ArrayList<MoveScore> {
        val scores = ArrayList<MoveScore>()
        var currentScore: Int
        var currentMove: Move
        for (i in 0 until moves.size()) {
            currentMove = moves[i]
            currentScore = 0

            // capture score
            if (currentMove.isTake) {
                val victim = currentMove.takenPieceType
                val attacker = currentMove.pieceType
                currentScore += mvv_lva[(attacker - 1) * 6 + (victim - 1)] + 10000
            }

            // promotion score
            if (currentMove.isPromote) {
                currentScore += 10000
            }

            // killer move score
            if (killerMove[0][ply] != null && killerMove[0][ply] == moves[i]) {
                currentScore += 9000
            } else if (killerMove[1][ply] != null && killerMove[1][ply] == moves[i]) {
                currentScore += 8000
            }

            scores.add(MoveScore(currentScore, i))
        }
        return scores
    }

    private fun sortMoves(moves: PlayerLegalMoves, ply: Int): PlayerLegalMoves {
        val scores = scoreMoves(moves, ply)
        scores.sort()
        val sortedMoves = PlayerLegalMoves()
        for (i in 0 until moves.size()) {
            sortedMoves.add(moves[scores[i].moveIndex])
        }
        return sortedMoves
    }

    private fun isGameFinished(gameStatus: GameStatus): Boolean {
        return gameStatus !== GameStatus.NOT_FINISHED
    }

    private fun getGameFinishedWhiteScore(gameStatus: GameStatus): Int {
        return when (gameStatus) {
            GameStatus.FINISHED_WIN_WHITE -> WIN_SCORE
            GameStatus.FINISHED_WIN_BLACK -> LOSE_SCORE
            GameStatus.FINISHED_DRAW -> 0
            else -> {
                0
            }
        }
    }

    private fun getGameFinishedBlackScore(gameStatus: GameStatus?): Int {
        return when (gameStatus) {
            GameStatus.FINISHED_WIN_WHITE -> LOSE_SCORE
            GameStatus.FINISHED_WIN_BLACK -> WIN_SCORE
            GameStatus.FINISHED_DRAW -> 0
            else -> {
                0
            }
        }
    }

    private fun getGameFinishedScoreFor(gameStatus: GameStatus, player: Int): Int {
        return if (player == Piece.WHITE) {
            getGameFinishedWhiteScore(gameStatus)
        } else {
            getGameFinishedBlackScore(gameStatus)
        }
    }


    private fun evaluate(
        chessBoard: ChessBoard,
        color: Int,
        whiteLegalMoves: PlayerLegalMoves,
        blackLegalMoves: PlayerLegalMoves
    ): Int {
        val numWhiteKings = BitMath.countSetBits(chessBoard.whiteKing)
        val numBlackKings = BitMath.countSetBits(chessBoard.blackKing)
        val numWhiteQueens = BitMath.countSetBits(chessBoard.whiteQueens)
        val numBlackQueues = BitMath.countSetBits(chessBoard.blackQueens)
        val numWhiteRooks = BitMath.countSetBits(chessBoard.whiteRooks)
        val numBlackRooks = BitMath.countSetBits(chessBoard.blackRooks)
        val numWhiteBishops = BitMath.countSetBits(chessBoard.whiteBishops)
        val numBlackBishops = BitMath.countSetBits(chessBoard.blackBishops)
        val numWhiteKnights = BitMath.countSetBits(chessBoard.whiteKnights)
        val numBlackKnights = BitMath.countSetBits(chessBoard.blackKnights)
        val numWhitePawns = BitMath.countSetBits(chessBoard.whitePawns)
        val numBlackPawns = BitMath.countSetBits(chessBoard.blackPawns)

        val numPiecesScore = Piece.KING_VALUE * (numWhiteKings - numBlackKings) +
                Piece.QUEEN_VALUE * (numWhiteQueens - numBlackQueues) +
                Piece.ROOK_VALUE * (numWhiteRooks - numBlackRooks) +
                Piece.BISHOP_VALUE * (numWhiteBishops - numBlackBishops) +
                Piece.KNIGHT_VALUE * (numWhiteKnights - numBlackKnights) +
                Piece.PAWN_VALUE * (numWhitePawns - numBlackPawns)

        val whitePiecePositionValue = getPiecesPositionalValueFor(chessBoard, Piece.WHITE)
        val blackPiecePositionValue = getPiecesPositionalValueFor(chessBoard, Piece.BLACK)

        val piecesPositionalValue = whitePiecePositionValue - blackPiecePositionValue

        val mobilityScore = MOBILITY_SCORE * (whiteLegalMoves.size() - blackLegalMoves.size())
        val score = numPiecesScore + piecesPositionalValue + mobilityScore

        return if (color == Piece.WHITE) {
            score
        } else {
            -score
        }
    }

    private fun evaluate(
        chessBoard: ChessBoard,
        color: Int,
        gameStatus: GameStatus,
        toPlayLegalMoves: PlayerLegalMoves
    ): Int {
        return if (isGameFinished(gameStatus)) {
            getGameFinishedScoreFor(gameStatus, color)
        } else {
            if (color == Piece.WHITE) {
                val blackLegalMoves = moveGenerator.generateBlackLegalMoves(chessBoard)
                evaluate(chessBoard, color,toPlayLegalMoves,blackLegalMoves)
            } else {
                val whiteLegalMoves = moveGenerator.generateWhiteLegalMoves(chessBoard)
                evaluate(chessBoard, color,whiteLegalMoves,toPlayLegalMoves)

            }
        }
    }


    private fun getPositionalValue(chessBoard: ChessBoard, square: Int): Int {
        var value = 0
        var file = GetFile(square)
        var rank = GetRank(square)
        if (chessBoard.pieceColor(square) == Piece.WHITE) {
            rank = 7 - rank
        } else {
            file = 7 - file
        }
        val piecePositionOnTable = GetPosition(file, rank)
        when (chessBoard.pieceType(square)) {
            Piece.PAWN -> value += PAWN_SQUARES_TABLE[piecePositionOnTable]
            Piece.ROOK -> value += ROOK_SQUARES_TABLE[piecePositionOnTable]
            Piece.KNIGHT -> value += KNIGHT_SQUARES_TABLE[piecePositionOnTable]
            Piece.BISHOP -> value += BISHOP_SQUARES_TABLE[piecePositionOnTable]
            Piece.QUEEN -> value += QUEEN_SQUARES_TABLE[piecePositionOnTable]
            Piece.KING -> value += if (isEndGame(chessBoard)) {
                KING_END_GAME_SQUARES_TABLE[piecePositionOnTable]
            } else {
                KING_MIDDLE_GAME_SQUARES_TABLE[piecePositionOnTable]
            }
        }
        return value
    }

    private fun getPiecesPositionalValueFor(chessboard: ChessBoard, color: Int): Int {
        var value = 0
        val squares = if (color == Piece.WHITE) {
            moveGenerator.getWhitePositions(chessboard)
        } else {
            moveGenerator.getBlackPositions(chessboard)
        }
        for (square in squares) {
            value += getPositionalValue(chessboard, square)
        }
        return value
    }

    private fun isEndGame(chessBoard: ChessBoard): Boolean {
//        var endGame = false
//        val whitePiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.WHITE)
//        val blackPiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.BLACK)
//        if (abs(whitePiecesValue - blackPiecesValue) >= Piece.QUEEN_VALUE) {
//            if (whitePiecesValue < Piece.QUEEN_VALUE) endGame = true
//            if (blackPiecesValue < Piece.QUEEN_VALUE) endGame = true
//        }
        val whitePiecesCount = BitMath.countSetBits(chessBoard.allWhitePieces)
        val blackPiecesCount = BitMath.countSetBits(chessBoard.allBlackPieces)
        return (whitePiecesCount + blackPiecesCount) <= 7
    }

    fun checkStatus(chessBoard: ChessBoard): GameStatus {
        val lastPlayed = chessBoard.moves.lastPlayed
        var gameStatus = GameStatus.NOT_FINISHED
        val toPlayPlayerLegalMoves =
            moveGenerator.generateLegalMovesFor(chessBoard, chessBoard.toPlayColor)
        if (toPlayPlayerLegalMoves.size() == 0) {
            return if (moveGenerator.isKingChecked(chessBoard.toPlayColor)) {
                //win
                if (lastPlayed == Piece.WHITE) {
                    GameStatus.FINISHED_WIN_WHITE
                } else {
                    GameStatus.FINISHED_WIN_BLACK
                }
            } else {
                //draw stalemate
                GameStatus.FINISHED_DRAW
            }
        }
        if (chessBoard.insufficientMaterial()) {
            gameStatus = GameStatus.FINISHED_DRAW
            return gameStatus
        }
        if (chessBoard.fiftyMovesDrawCount == 50) {
            return GameStatus.FINISHED_DRAW
        }

        //check for third repetition draw
        var repeatedPositionCount = 1
        val lastState = chessBoard.states[chessBoard.states.size - 1]
        for (i in 0 until chessBoard.states.size - 1) {
            if (lastState == chessBoard.states[i]) {
                repeatedPositionCount++
                if (repeatedPositionCount == 3) {
                    return GameStatus.FINISHED_DRAW

                }
            }
        }
        return gameStatus
    }


    companion object {
        const val DEBUG_TAG = "Cheeta_Debug"
        const val COMPUTER_MAX_SEARCH_TIME: Long = 4


        private const val LOSE_SCORE = -1000000
        private const val WIN_SCORE = 1000000

        private const val MOBILITY_SCORE = 10

        // pieces start game value tables
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

        // pieces end game value tables
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
