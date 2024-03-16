package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.ChessBoard.Companion.CASTLING_KING_SIDE
import com.maherhanna.cheeta.core.ChessBoard.Companion.CASTLING_QUEEN_SIDE
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetFile
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetPosition
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetRank
import java.security.SecureRandom
import java.util.logging.Level
import java.util.logging.Logger

open class ChessEngine {
    private var foundCheckMate = false
    private var evaluations: Long = 0
    private var betaCutOffs: Long = 0
    private var maxingPlayer = 0
    private var searchTimeFinished = false
    private var moveGenerator = MoveGenerator()
    var isUciMode = false

    //killerMove[id][ply]
    private var killerMove = Array(2) { arrayOfNulls<Move>(MAX_KILLER_MOVE_PLY) }
    private var history = Array(2) { Array(64) { LongArray(64) } }

    var zobristPiecesArray = Array(2) { Array(6) { LongArray(64) } }
    var zobristEnPassantArray = LongArray(8)
    var zobristCastlingRightsArray = LongArray(4)
    var zobristBlackToMove = 0L

    init {
        fillZobristArrays()

    }

    fun reset() {
        foundCheckMate = false
        evaluations = 0
        betaCutOffs = 0
        maxingPlayer = 0
        searchTimeFinished = false
        moveGenerator = MoveGenerator()
        isUciMode = false

        killerMove = Array(2) { arrayOfNulls(124) }
        history = Array(2) { Array(64) { LongArray(64) } }
        moveGenerator.reset()
    }

    fun fillZobristArrays() {
        val randomGenerator = SecureRandom()
        for (color in 0..1) {
            for (pieceType in 0..5) {
                for (boardPosition in 0..63) {
                    zobristPiecesArray[color][pieceType][boardPosition] = randomGenerator.nextLong()
                }
            }
        }

        for (column in 0..7) {
            zobristEnPassantArray[column] = randomGenerator.nextLong()
        }

        for (right in 0..3) {
            zobristCastlingRightsArray[right] = randomGenerator.nextLong()
        }

        zobristBlackToMove = randomGenerator.nextLong()
    }

    fun getZobristHash(chessBoard: ChessBoard):Long {
        var zobristKey = 0L
        for (square in 0..63) {
            val pieceType = chessBoard.pieceType(square)
            val pieceColor = chessBoard.pieceColor(square)
            if (pieceType == Piece.PAWN && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.PAWN - 1][square]
            } else if (pieceType == Piece.PAWN && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.PAWN - 1][square]
            } else if (pieceType == Piece.KNIGHT && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.KNIGHT - 1][square]
            } else if (pieceType == Piece.KNIGHT && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.KNIGHT - 1][square]
            } else if (pieceType == Piece.BISHOP && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.BISHOP - 1][square]
            } else if (pieceType == Piece.BISHOP && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.BISHOP - 1][square]
            } else if (pieceType == Piece.ROOK && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.ROOK - 1][square]
            } else if (pieceType == Piece.ROOK && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.ROOK - 1][square]
            } else if (pieceType == Piece.QUEEN && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.QUEEN - 1][square]
            } else if (pieceType == Piece.QUEEN && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.QUEEN - 1][square]
            } else if (pieceType == Piece.KING && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.KING - 1][square]
            } else if (pieceType == Piece.KING && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.KING - 1][square]
            }
        }

        for (column in 0..7) {
            if (ChessBoard.GetFile(chessBoard.enPassantTarget) == column) {
                zobristKey = zobristKey xor zobristEnPassantArray[column]
            }
        }

        if (chessBoard.whiteCastlingRights and CASTLING_KING_SIDE != 0) {
            zobristKey = zobristKey xor zobristCastlingRightsArray[0]
        }
        if (chessBoard.whiteCastlingRights and CASTLING_QUEEN_SIDE != 0) {
            zobristKey = zobristKey xor zobristCastlingRightsArray[1]
        }
        if (chessBoard.blackCastlingRights and CASTLING_KING_SIDE != 0) {
            zobristKey = zobristKey xor zobristCastlingRightsArray[2]
        }
        if (chessBoard.blackCastlingRights and CASTLING_QUEEN_SIDE != 0) {
            zobristKey = zobristKey xor zobristCastlingRightsArray[3]
        }
        if(chessBoard.toPlayColor == Piece.BLACK){
            zobristKey = zobristKey xor zobristBlackToMove
        }
        return zobristKey

    }

    fun getMove(
        chessBoard: ChessBoard,
        maxSearchTime: Long,
        maxDepth: Long = Long.MAX_VALUE
    ): Move? {
        val startTime = System.currentTimeMillis()
        //convert maximum search time from seconds to nano seconds
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
        var currentDepth = 0L
        var move: Move? = toPlayLegalMoves[0]
        do {
            currentDepth++
            //Log.d(Game.DEBUG, "depth: ${maxDepth}")
            val currentSearchMove =
                search(chessBoard, toPlayLegalMoves, startTime, maxSearchTime, currentDepth)

            if (currentSearchMove != null) {
                move = currentSearchMove
            } else {
                currentDepth--
                break
            }
        } while (!foundCheckMate && !searchTimeFinished && currentDepth < maxDepth)
        val duration = System.currentTimeMillis() - startTime
        Logger.getLogger(DEBUG_TAG).log(
            Level.INFO, "evaluations " + evaluations +
                    " depth " + currentDepth +
                    "\nmove " + move?.pieceName.toString() + " " + move?.fromNotation.toString() + " to " + move?.toNotation.toString()
                    + "\ntime " + duration.toFloat() + " milliseconds"
        )

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


    private fun search(
        chessBoard: ChessBoard,
        moves: PlayerLegalMoves,
        startTime: Long,
        maxSearchTime: Long,
        maxDepth: Long,
    ): Move? {
        var alpha = LOSE_SCORE
        val beta = WIN_SCORE
        var score: Int
        var bestMove: Move? = null
        for (i in 0 until moves.size()) {
            chessBoard.makeMove(moves[i])
            score = -negaMax(
                chessBoard, -beta,
                -alpha, maxDepth - 1, 1
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
            if (System.currentTimeMillis() - startTime > maxSearchTime) {
                return null
            }
        }

        return bestMove
    }


    private fun negaMax(
        chessBoard: ChessBoard,
        alphaArg: Int,
        beta: Int,
        depth: Long,
        ply: Int
    ): Int {
        var alpha = alphaArg
        val toPlayColor = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        val gameStatus = checkStatus(chessBoard)
        if (depth == 0L || isGameFinished(gameStatus)) {
            evaluations++
            return quiescence(chessBoard, alpha, beta, ply)
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
                if (!toPlayLegalMoves[i].isCapture) {
                    if (ply < MAX_KILLER_MOVE_PLY) {
                        //killer moves
                        killerMove[1][ply] = killerMove[0][ply]
                        killerMove[0][ply] = Move(toPlayLegalMoves[i])

                    }

                    // history moves
                    history[chessBoard.toPlayColor][toPlayLegalMoves[i].from][toPlayLegalMoves[i].to] += depth * depth
                }
                betaCutOffs++


                return beta
            }
            if (score > alpha) {

                alpha = score
            }
        }
        return alpha
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


        if (isGameFinished(gameStatus) || toPlayLegalMoves.size() == 0) {
            return eval
        } else {
            toPlayLegalMoves.removeNonTake()
        }
        if (eval >= betaArg) {
            return betaArg
        }
        if (quiescenceAlpha < eval) {
            quiescenceAlpha = eval
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


    private fun scoreMoves(moves: PlayerLegalMoves, ply: Int): ArrayList<MoveScore> {
        val scores = ArrayList<MoveScore>()
        var currentScore: Int
        var currentMove: Move
        for (i in 0 until moves.size()) {
            currentMove = moves[i]
            currentScore = 0

            // capture score
            if (currentMove.isCapture) {
                val victim = currentMove.takenPieceType
                val attacker = currentMove.pieceType
                currentScore += mvv_lva[(attacker - 1) * 6 + (victim - 1)] + 10000
            } else {
                // killer move score
                if (killerMove[0][ply] != null && killerMove[0][ply] == moves[i]) {
                    currentScore += 9000
                } else if (killerMove[1][ply] != null && killerMove[1][ply] == moves[i]) {
                    currentScore += 8000
                }

                //history score
                currentScore += history[currentMove.color][currentMove.from][currentMove.to].toInt()
            }

            // promotion score
            if (currentMove.isPromote) {
                currentScore += 10000
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

    fun makeMove(chessBoard: ChessBoard, moveNotation: String): Move? {
        val playerColor = chessBoard.toPlayColor
        val toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            playerColor
        )
        val move = toPlayLegalMoves.searchMove(Move(moveNotation))
        if (move != null) {
            chessBoard.makeMove(move)
        }
        return move
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
                evaluate(chessBoard, color, toPlayLegalMoves, blackLegalMoves)
            } else {
                val whiteLegalMoves = moveGenerator.generateWhiteLegalMoves(chessBoard)
                evaluate(chessBoard, color, whiteLegalMoves, toPlayLegalMoves)

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
        return moveGenerator.checkStatus(chessBoard)
    }


    companion object {
        const val DEBUG_TAG = "Cheeta_Debug"


        private const val LOSE_SCORE = -1000000
        private const val WIN_SCORE = 1000000

        private const val MAX_KILLER_MOVE_PLY = 64

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
