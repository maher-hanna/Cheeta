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
    private var startTime = 0L
    private var maxSearchTime = Long.MAX_VALUE
    private var nodes = 0L
    private var moveGenerator = MoveGenerator()
    var isUciMode = false

    /** Depth fully searched by the last call to getMove (read by the UI after the search). */
    @Volatile
    var lastSearchDepth = 0
        private set

    // triangular principal variation table indexed by ply
    var pvTable: Array<Array<Move?>> = Array(MAX_PLY) { arrayOfNulls(MAX_PLY) }
    private var pvLength = IntArray(MAX_PLY)
    // principal variation of the last completed iteration, used for move ordering
    private var previousPv: Array<Move?> = arrayOfNulls(0)
    // true only while the search is walking down the previous iteration's PV
    private var followPv = false
    // false when the last root search was cut short by the time limit
    private var lastIterationComplete = false


    //killerMove[id][ply]
    private var killerMove = Array(2) { arrayOfNulls<Move>(MAX_PLY) }
    private var history = Array(2) { Array(64) { IntArray(64) } }

    // transposition table stored in primitive arrays (no object allocated per write)
    private val tableKeys = LongArray(TRANSPOSITION_TABLE_SIZE.toInt())
    private val tableValues = IntArray(TRANSPOSITION_TABLE_SIZE.toInt())
    private val tableDepths = IntArray(TRANSPOSITION_TABLE_SIZE.toInt())
    private val tableFlags = ByteArray(TRANSPOSITION_TABLE_SIZE.toInt())

    private fun clearTranspositionTable() {
        tableKeys.fill(0L)
        tableDepths.fill(-1)
    }

    private fun tableIndex(hashKey: ULong): Int =
        (hashKey.toLong() and (TRANSPOSITION_TABLE_SIZE.toLong() - 1)).toInt()

    @OptIn(ExperimentalUnsignedTypes::class)
    var zobristPiecesArray = Array(2) { Array(6) { ULongArray(64) } }

    @OptIn(ExperimentalUnsignedTypes::class)
    var zobristEnPassantArray = ULongArray(8)

    @OptIn(ExperimentalUnsignedTypes::class)
    var zobristCastlingRightsArray = ULongArray(4)
    var zobristBlackToMove = 0UL

    init {
        fillZobristArrays()
        clearTranspositionTable()

    }

    fun reset() {
        foundCheckMate = false
        evaluations = 0
        betaCutOffs = 0
        maxingPlayer = 0
        searchTimeFinished = false
        moveGenerator = MoveGenerator()
        isUciMode = false

        killerMove = Array(2) { arrayOfNulls(MAX_PLY) }
        history = Array(2) { Array(64) { IntArray(64) } }
        clearTranspositionTable()
        pvTable = Array(MAX_PLY) { arrayOfNulls(MAX_PLY) }
        pvLength = IntArray(MAX_PLY)
        previousPv = arrayOfNulls(0)
        moveGenerator.reset()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun fillZobristArrays() {
        val randomGenerator = SecureRandom()
        for (color in 0..1) {
            for (pieceType in 0..5) {
                for (boardPosition in 0..63) {
                    zobristPiecesArray[color][pieceType][boardPosition] =
                        randomGenerator.nextLong().toULong()
                }
            }
        }

        for (column in 0..7) {
            zobristEnPassantArray[column] = randomGenerator.nextLong().toULong()
        }

        for (right in 0..3) {
            zobristCastlingRightsArray[right] = randomGenerator.nextLong().toULong()
        }

        zobristBlackToMove = randomGenerator.nextLong().toULong()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun getZobristHash(chessBoard: ChessBoard): ULong {
        var zobristKey = 0UL
        for (square in 0..63) {
            val pieceType = chessBoard.pieceType(square)
            val pieceColor = chessBoard.pieceColor(square)
            if (pieceType == Piece.PAWN && pieceColor == Piece.WHITE) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.PAWN - 1][square]
            } else if (pieceType == Piece.PAWN && pieceColor == Piece.BLACK) {
                zobristKey = zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.PAWN - 1][square]
            } else if (pieceType == Piece.KNIGHT && pieceColor == Piece.WHITE) {
                zobristKey =
                    zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.KNIGHT - 1][square]
            } else if (pieceType == Piece.KNIGHT && pieceColor == Piece.BLACK) {
                zobristKey =
                    zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.KNIGHT - 1][square]
            } else if (pieceType == Piece.BISHOP && pieceColor == Piece.WHITE) {
                zobristKey =
                    zobristKey xor zobristPiecesArray[Piece.WHITE][Piece.BISHOP - 1][square]
            } else if (pieceType == Piece.BISHOP && pieceColor == Piece.BLACK) {
                zobristKey =
                    zobristKey xor zobristPiecesArray[Piece.BLACK][Piece.BISHOP - 1][square]
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
        if (chessBoard.toPlayColor == Piece.BLACK) {
            zobristKey = zobristKey xor zobristBlackToMove
        }
        return zobristKey

    }

    fun probeTableEntry(
        hashKey: ULong,
        alpha: Int,
        beta: Int,
        depth: Int
    ): TranspositionTableEntry? {
        val index = tableIndex(hashKey)
        return if (tableDepths[index] >= 0 && tableKeys[index] == hashKey.toLong()) {
            TranspositionTableEntry(
                hashKey = hashKey,
                depth = tableDepths[index],
                flag = TABLE_FLAGS[tableFlags[index].toInt()],
                value = tableValues[index]
            )
        } else {
            null
        }
    }

    fun writeTableEntry(hashKey: ULong, value: Int, depth: Int, hashFlag: TranspositionTableFlag) {
        val index = tableIndex(hashKey)
        tableKeys[index] = hashKey.toLong()
        tableValues[index] = value
        tableDepths[index] = depth
        tableFlags[index] = hashFlag.ordinal.toByte()

    }

    // mate scores are stored relative to the node (not the root) so they stay correct
    // when the same position is reached at a different ply
    private fun scoreToTable(score: Int, ply: Int): Int = when {
        score > MATE_BOUND -> score + ply
        score < -MATE_BOUND -> score - ply
        else -> score
    }

    private fun scoreFromTable(score: Int, ply: Int): Int = when {
        score > MATE_BOUND -> score - ply
        score < -MATE_BOUND -> score + ply
        else -> score
    }

    // score of a finished game from the point of view of the side to move
    private fun terminalScore(gameStatus: GameStatus, ply: Int): Int {
        return if (gameStatus == GameStatus.FINISHED_DRAW) {
            0
        } else {
            // the side to move has been checkmated; prefer the longest defence / shortest mate
            LOSE_SCORE + ply
        }
    }

    private fun isTimeUp(): Boolean {
        nodes++
        if (!searchTimeFinished && (nodes and 1023L) == 0L &&
            System.currentTimeMillis() - startTime > maxSearchTime
        ) {
            searchTimeFinished = true
        }
        return searchTimeFinished
    }

    fun getMove(
        chessBoard: ChessBoard,
        maxSearchTime: Long,
        maxDepth: Long = Long.MAX_VALUE
    ): Move? {
        startTime = System.currentTimeMillis()
        this.maxSearchTime = maxSearchTime
        foundCheckMate = false
        clearTranspositionTable()
        pvTable = Array(MAX_PLY) { arrayOfNulls(MAX_PLY) }
        pvLength = IntArray(MAX_PLY)
        previousPv = arrayOfNulls(0)
        killerMove = Array(2) { arrayOfNulls(MAX_PLY) }
        history = Array(2) { Array(64) { IntArray(64) } }
        evaluations = 0
        betaCutOffs = 0
        nodes = 0
        searchTimeFinished = false
        maxingPlayer = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            maxingPlayer
        )
        // no legal moves: the game is already over
        if (toPlayLegalMoves.size() == 0) return null
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, 0)
        var currentDepth = 0
        var move: Move? = toPlayLegalMoves[0]

        while (!foundCheckMate && currentDepth < maxDepth && currentDepth < MAX_PLY - 1) {
            currentDepth++
            val currentSearchMove = search(chessBoard, toPlayLegalMoves, currentDepth)
            if (currentSearchMove == null) {
                // interrupted before any root move finished; keep the last completed depth
                currentDepth--
                break
            }
            // a move from an interrupted iteration is still usable: the previous best move is
            // searched first, so any move returned has been fully searched at this depth
            move = currentSearchMove
            if (!lastIterationComplete) {
                currentDepth--
                break
            }
            previousPv = pvTable[0].copyOf(pvLength[0])
            // search the best move of this iteration first in the next one
            toPlayLegalMoves = moveToFront(toPlayLegalMoves, currentSearchMove)
            // each depth takes several times longer than the previous one, so a new depth
            // started after half the time is used would almost certainly not finish
            if (System.currentTimeMillis() - startTime > maxSearchTime / 2) break
        }
        lastSearchDepth = currentDepth
        val duration = System.currentTimeMillis() - startTime
        Logger.getLogger(DEBUG_TAG).log(
            Level.INFO, "evaluations " + evaluations +
                    " depth " + currentDepth +
                    "\nmove " + move?.pieceName.toString() + " " + move?.fromNotation.toString() + " to " + move?.toNotation.toString()
                    + "\ntime " + duration.toFloat() + " milliseconds"
        )
        return move
    }

    private fun moveToFront(moves: PlayerLegalMoves, first: Move): PlayerLegalMoves {
        val reordered = PlayerLegalMoves()
        reordered.add(first)
        for (i in 0 until moves.size()) {
            if (moves[i] != first) reordered.add(moves[i])
        }
        return reordered
    }

    private fun updatePv(ply: Int, move: Move) {
        pvTable[ply][ply] = move
        val childLength = pvLength[ply + 1]
        for (j in ply + 1 until childLength) {
            pvTable[ply][j] = pvTable[ply + 1][j]
        }
        pvLength[ply] = maxOf(childLength, ply + 1)
    }

    // Returns the best move among the root moves searched completely. If the time limit
    // cuts the search short, lastIterationComplete is false and the result covers only the
    // moves finished so far (null if none).
    private fun search(
        chessBoard: ChessBoard,
        moves: PlayerLegalMoves,
        maxDepth: Int,
    ): Move? {
        var alpha = -INFINITY
        val beta = INFINITY
        var bestMove: Move? = null
        pvLength[0] = 0
        lastIterationComplete = false
        for (i in 0 until moves.size()) {
            chessBoard.makeMove(moves[i])
            // the first root move is the previous iteration's best: walk its PV
            followPv = i == 0 && previousPv.isNotEmpty()
            val score = -negaMax(
                chessBoard, -beta,
                -alpha, maxDepth - 1, 1
            )
            followPv = false
            chessBoard.unMakeMove()
            if (searchTimeFinished) {
                // the interrupted move's score is incomplete and is ignored
                return bestMove
            }
            if (score > alpha) {
                alpha = score
                bestMove = moves[i]
                updatePv(0, moves[i])
            }
        }
        lastIterationComplete = true
        // a forced mate was found, deeper searches cannot improve on it
        if (alpha > MATE_BOUND) {
            foundCheckMate = true
        }
        return bestMove
    }


    private fun negaMax(
        chessBoard: ChessBoard,
        alphaOriginal: Int,
        beta: Int,
        depth: Int,
        ply: Int
    ): Int {
        pvLength[ply] = ply
        // only the node reached through the PV move keeps following the PV
        val isOnPv = followPv
        followPv = false
        if (isTimeUp()) return 0
        var alpha = alphaOriginal
        val toPlayColor = chessBoard.toPlayColor

        // cheap draw checks first: repetition and insufficient material never coincide
        // with checkmate, so they can be decided before generating any moves
        if (chessBoard.insufficientMaterial() || moveGenerator.isThreefoldRepetition(chessBoard)) {
            return 0
        }
        if (depth <= 0 || ply >= MAX_PLY - 1) {
            return quiescence(chessBoard, alpha, beta, ply)
        }

        // probe the table before the (expensive) move generation
        val chessBoardHash = getZobristHash(chessBoard)
        val tableEntry =
            probeTableEntry(hashKey = chessBoardHash, alpha = alpha, beta = beta, depth = depth)
        if (tableEntry != null && tableEntry.depth >= depth) {
            val tableScore = scoreFromTable(tableEntry.value, ply)
            when (tableEntry.flag) {
                TranspositionTableFlag.EXACT -> return tableScore
                TranspositionTableFlag.LOWER_BOUND -> if (tableScore >= beta) return beta
                TranspositionTableFlag.UPPER_BOUND -> if (tableScore <= alpha) return alpha
            }
        }

        val toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        if (toPlayLegalMoves.size() == 0) {
            // checkmate or stalemate (isKingChecked is valid right after move generation)
            return if (moveGenerator.isKingChecked(toPlayColor)) LOSE_SCORE + ply else 0
        }
        // fifty move rule is checked after mate: a mate on the 100th half move still counts
        if (chessBoard.fiftyMovesDrawCount >= 100) {
            return 0
        }

        val pvMove = if (isOnPv) previousPv.getOrNull(ply) else null
        val sortedMoves = sortMoves(toPlayLegalMoves, ply, pvMove)
        var hashFlag = TranspositionTableFlag.UPPER_BOUND
        for (i in 0 until sortedMoves.size()) {
            val move = sortedMoves[i]
            chessBoard.makeMove(move)
            // the PV move is sorted first; only its subtree continues along the PV
            followPv = i == 0 && pvMove != null && move.bitValue == pvMove.bitValue
            val score = -negaMax(
                chessBoard, -beta, -alpha,
                depth - 1, ply + 1
            )
            followPv = false
            chessBoard.unMakeMove()
            if (searchTimeFinished) return 0
            if (score >= beta) {
                if (!move.isCapture) {
                    //killer moves: shift only for a new move so both slots stay different
                    if (killerMove[0][ply] != move) {
                        killerMove[1][ply] = killerMove[0][ply]
                        killerMove[0][ply] = Move(move)
                    }
                    // history moves
                    history[move.color][move.from][move.to] += depth * depth
                }
                betaCutOffs++
                writeTableEntry(
                    hashKey = chessBoardHash,
                    value = scoreToTable(beta, ply),
                    depth = depth,
                    hashFlag = TranspositionTableFlag.LOWER_BOUND
                )
                return beta
            }
            if (score > alpha) {
                alpha = score
                hashFlag = TranspositionTableFlag.EXACT
                updatePv(ply, move)
            }
        }
        writeTableEntry(
            hashKey = chessBoardHash,
            value = scoreToTable(alpha, ply),
            depth = depth,
            hashFlag = hashFlag
        )
        return alpha
    }

    private fun quiescence(chessBoard: ChessBoard, alphaArg: Int, betaArg: Int, ply: Int): Int {
        if (isTimeUp()) return 0
        evaluations++
        var quiescenceAlpha = alphaArg
        val toPlayColor = chessBoard.toPlayColor
        var toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        // read before evaluate(), which regenerates moves for the other side
        val inCheck = moveGenerator.isKingChecked(toPlayColor)
        val gameStatus = checkStatus(chessBoard, toPlayLegalMoves)
        if (isGameFinished(gameStatus)) {
            return terminalScore(gameStatus, ply)
        }
        if (ply >= MAX_PLY - 1) {
            return evaluate(chessBoard, toPlayColor, gameStatus, toPlayLegalMoves)
        }
        if (!inCheck) {
            // stand pat is only allowed when not in check
            val eval = evaluate(chessBoard, toPlayColor, gameStatus, toPlayLegalMoves)
            if (eval >= betaArg) {
                return betaArg
            }
            if (quiescenceAlpha < eval) {
                quiescenceAlpha = eval
            }
            toPlayLegalMoves.removeNonTake()
        }
        // when in check all evasions are searched
        toPlayLegalMoves = sortMoves(toPlayLegalMoves, ply)
        for (i in 0 until toPlayLegalMoves.size()) {
            chessBoard.makeMove(toPlayLegalMoves[i])
            val score = -quiescence(
                chessBoard, -betaArg, -quiescenceAlpha,
                ply + 1
            )
            chessBoard.unMakeMove()
            if (searchTimeFinished) return 0
            if (score >= betaArg) {
                return betaArg
            }
            if (score > quiescenceAlpha) {
                quiescenceAlpha = score
            }
        }
        return quiescenceAlpha
    }


    private fun scoreMoves(moves: PlayerLegalMoves, ply: Int,pvNode: Move?): ArrayList<MoveScore> {
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
                if (ply < MAX_PLY && killerMove[0][ply] != null && killerMove[0][ply] == moves[i]) {
                    currentScore += 9000
                } else if (ply < MAX_PLY && killerMove[1][ply] != null && killerMove[1][ply] == moves[i]) {
                    currentScore += 8000
                }

                // history score, capped so it never outranks killers or captures
                currentScore += minOf(
                    history[currentMove.color][currentMove.from][currentMove.to],
                    MAX_HISTORY_SCORE
                )
            }

            // promotion score
            if (currentMove.isPromote) {
                currentScore += if (currentMove.promotionPieceType == Piece.QUEEN) 10000 else 0
            }
            //pv move
            if(currentMove.bitValue == pvNode?.bitValue){
                currentScore += 500000
            }

            scores.add(MoveScore(currentScore, i))
        }
        return scores
    }

    private fun sortMoves(moves: PlayerLegalMoves, ply: Int,pvNode:Move? = null): PlayerLegalMoves {
        val scores = scoreMoves(moves, ply,pvNode)
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
        color: Int
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

        // mobility = number of squares each side attacks (empty or holding an enemy piece);
        // much cheaper than generating the full legal move lists of both sides
        val whiteMobility = BitMath.countSetBits(
            moveGenerator.getAttacksWithCaptures(chessBoard, Piece.WHITE, chessBoard.allBlackPieces)
        )
        val blackMobility = BitMath.countSetBits(
            moveGenerator.getAttacksWithCaptures(chessBoard, Piece.BLACK, chessBoard.allWhitePieces)
        )
        val mobilityScore = MOBILITY_SCORE * (whiteMobility - blackMobility)
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
            evaluate(chessBoard, color)
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

    // same result as summing getPositionalValue over the pieces, but walks the piece
    // bitboards directly instead of building a list of squares and looking up each piece
    private fun getPiecesPositionalValueFor(chessboard: ChessBoard, color: Int): Int {
        val kingTable = if (isEndGame(chessboard)) {
            KING_END_GAME_SQUARES_TABLE
        } else {
            KING_MIDDLE_GAME_SQUARES_TABLE
        }
        return if (color == Piece.WHITE) {
            tableValue(chessboard.whitePawns, PAWN_SQUARES_TABLE, color) +
                    tableValue(chessboard.whiteKnights, KNIGHT_SQUARES_TABLE, color) +
                    tableValue(chessboard.whiteBishops, BISHOP_SQUARES_TABLE, color) +
                    tableValue(chessboard.whiteRooks, ROOK_SQUARES_TABLE, color) +
                    tableValue(chessboard.whiteQueens, QUEEN_SQUARES_TABLE, color) +
                    tableValue(chessboard.whiteKing, kingTable, color)
        } else {
            tableValue(chessboard.blackPawns, PAWN_SQUARES_TABLE, color) +
                    tableValue(chessboard.blackKnights, KNIGHT_SQUARES_TABLE, color) +
                    tableValue(chessboard.blackBishops, BISHOP_SQUARES_TABLE, color) +
                    tableValue(chessboard.blackRooks, ROOK_SQUARES_TABLE, color) +
                    tableValue(chessboard.blackQueens, QUEEN_SQUARES_TABLE, color) +
                    tableValue(chessboard.blackKing, kingTable, color)
        }
    }

    private fun tableValue(pieces: Long, table: IntArray, color: Int): Int {
        var remaining = pieces
        var value = 0
        while (remaining != 0L) {
            val square = java.lang.Long.numberOfTrailingZeros(remaining)
            remaining = remaining and (remaining - 1)
            // same table orientation as getPositionalValue
            val tableSquare = if (color == Piece.WHITE) {
                (7 - GetRank(square)) * 8 + GetFile(square)
            } else {
                GetRank(square) * 8 + (7 - GetFile(square))
            }
            value += table[tableSquare]
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

    fun checkStatus(chessBoard: ChessBoard, legalMoves: PlayerLegalMoves?): GameStatus {
        return moveGenerator.checkStatus(chessBoard, legalMoves)
    }


    companion object {
        const val DEBUG_TAG = "Cheeta_Debug"
        // number of table entries; must be a power of two (about 9 MB)
        const val TRANSPOSITION_TABLE_SIZE = 524288UL
        private val TABLE_FLAGS = TranspositionTableFlag.entries.toTypedArray()
        private const val LOSE_SCORE = -1000000
        private const val WIN_SCORE = 1000000
        const val NO_ENTRY = LOSE_SCORE - 1
        private const val INFINITY = 2000000
        // scores beyond this bound mean a forced mate
        private const val MATE_BOUND = WIN_SCORE - 1000

        // maximum search ply (main search + quiescence)
        const val MAX_PLY = 128

        private const val MAX_HISTORY_SCORE = 7000

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
