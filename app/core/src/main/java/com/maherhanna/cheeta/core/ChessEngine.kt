package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.ChessBoard.Companion.GetFile
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetPosition
import com.maherhanna.cheeta.core.ChessBoard.Companion.GetRank
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs

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

    // search features, all on by default; they can be switched off to compare versions
    var useCheckExtension = true
    var usePvs = true
    var useLateMoveReductions = true // only applies when usePvs is on
    var useNullMove = true
    var useSee = true

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
    // best (or cutoff) move found in the position, as Move.bitValue; 0 = none
    private val tableMoves = LongArray(TRANSPOSITION_TABLE_SIZE.toInt())

    private fun clearTranspositionTable() {
        tableKeys.fill(0L)
        tableDepths.fill(-1)
        tableMoves.fill(0L)
    }

    private fun tableIndex(hashKey: ULong): Int =
        (hashKey.toLong() and (TRANSPOSITION_TABLE_SIZE.toLong() - 1)).toInt()


    init {
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

    // the board keeps its Zobrist hash up to date as moves are made (see Zobrist)
    fun getZobristHash(chessBoard: ChessBoard): ULong = chessBoard.zobristHash.toULong()

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

    fun writeTableEntry(
        hashKey: ULong,
        value: Int,
        depth: Int,
        hashFlag: TranspositionTableFlag,
        bestMove: Long = 0L
    ) {
        val index = tableIndex(hashKey)
        tableKeys[index] = hashKey.toLong()
        tableValues[index] = value
        tableDepths[index] = depth
        tableFlags[index] = hashFlag.ordinal.toByte()
        tableMoves[index] = bestMove

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
        // the transposition table is kept between moves (positions from the previous
        // search often come up again); it is cleared by reset() for a new game
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
        toPlayLegalMoves = sortMoves(chessBoard, toPlayLegalMoves, 0)
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
        depthArg: Int,
        ply: Int,
        allowNullMove: Boolean = true
    ): Int {
        pvLength[ply] = ply
        // only the node reached through the PV move keeps following the PV
        val isOnPv = followPv
        followPv = false
        if (isTimeUp()) return 0
        var alpha = alphaOriginal
        var depth = depthArg
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
        val tableSlot = tableIndex(chessBoardHash)
        // best move stored for this position; used for ordering even when the stored
        // depth is too shallow to use the score
        var hashMove = 0L
        if (tableDepths[tableSlot] >= 0 && tableKeys[tableSlot] == chessBoardHash.toLong()) {
            hashMove = tableMoves[tableSlot]
            if (tableDepths[tableSlot] >= depth) {
                val tableScore = scoreFromTable(tableValues[tableSlot], ply)
                when (TABLE_FLAGS[tableFlags[tableSlot].toInt()]) {
                    TranspositionTableFlag.EXACT -> return tableScore
                    TranspositionTableFlag.LOWER_BOUND -> if (tableScore >= beta) return beta
                    TranspositionTableFlag.UPPER_BOUND -> if (tableScore <= alpha) return alpha
                }
            }
        }

        val toPlayLegalMoves = moveGenerator.generateLegalMovesFor(
            chessBoard,
            toPlayColor
        )
        // isKingChecked is valid right after move generation
        val inCheck = moveGenerator.isKingChecked(toPlayColor)
        if (toPlayLegalMoves.size() == 0) {
            // checkmate or stalemate
            return if (inCheck) LOSE_SCORE + ply else 0
        }
        // fifty move rule is checked after mate: a mate on the 100th half move still counts
        if (chessBoard.fiftyMovesDrawCount >= 100) {
            return 0
        }

        // check extension: search one ply deeper when in check, so forcing lines are
        // not cut off at the horizon
        if (useCheckExtension && inCheck) {
            depth++
        }

        // null-move pruning: if passing the turn still fails high with a reduced search,
        // a real move will too. Not used in check, on the PV, twice in a row, near mate
        // scores, or with only pawns left (zugzwang positions break the idea)
        if (useNullMove && allowNullMove && !inCheck && !isOnPv &&
            depth >= NULL_MOVE_MIN_DEPTH && abs(beta) < MATE_BOUND &&
            hasNonPawnMaterial(chessBoard, toPlayColor)
        ) {
            chessBoard.makeNullMove()
            val nullScore = -negaMax(
                chessBoard, -beta, -beta + 1,
                depth - 1 - NULL_MOVE_REDUCTION, ply + 1, allowNullMove = false
            )
            chessBoard.unMakeNullMove()
            if (searchTimeFinished) return 0
            if (nullScore >= beta) {
                return beta
            }
        }

        val pvMove = if (isOnPv) previousPv.getOrNull(ply) else null
        val sortedMoves = sortMoves(chessBoard, toPlayLegalMoves, ply, pvMove, hashMove)
        var hashFlag = TranspositionTableFlag.UPPER_BOUND
        // on a fail low no move is known to be best; keep the previously stored one
        var bestMove = hashMove
        for (i in 0 until sortedMoves.size()) {
            val move = sortedMoves[i]
            chessBoard.makeMove(move)
            var score: Int
            if (i == 0 || !usePvs) {
                // the first (expected best) move is searched with the full window;
                // the PV move is sorted first and only its subtree continues along the PV
                followPv = i == 0 && pvMove != null && move.bitValue == pvMove.bitValue
                score = -negaMax(chessBoard, -beta, -alpha, depth - 1, ply + 1)
                followPv = false
            } else {
                // late move reduction: quiet moves sorted late rarely turn out best, so
                // search them one ply shallower first
                var reduction = 0
                if (useLateMoveReductions && i >= LMR_FULL_DEPTH_MOVES &&
                    depth >= LMR_MIN_DEPTH && !inCheck && !move.isCapture &&
                    !move.isPromote && !isKillerMove(move, ply) &&
                    !moveGenerator.isKingAttacked(chessBoard, chessBoard.toPlayColor)
                ) {
                    reduction = 1
                }
                // principal variation search: prove with a zero-width window that the
                // move is not better than the best so far
                score = -negaMax(chessBoard, -alpha - 1, -alpha, depth - 1 - reduction, ply + 1)
                if (score > alpha && reduction > 0) {
                    // the reduced search looked good: verify at full depth
                    score = -negaMax(chessBoard, -alpha - 1, -alpha, depth - 1, ply + 1)
                }
                if (score > alpha && score < beta) {
                    // it is better: search again with the full window for its exact score
                    score = -negaMax(chessBoard, -beta, -alpha, depth - 1, ply + 1)
                }
            }
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
                    hashFlag = TranspositionTableFlag.LOWER_BOUND,
                    bestMove = move.bitValue
                )
                return beta
            }
            if (score > alpha) {
                alpha = score
                hashFlag = TranspositionTableFlag.EXACT
                bestMove = move.bitValue
                updatePv(ply, move)
            }
        }
        writeTableEntry(
            hashKey = chessBoardHash,
            value = scoreToTable(alpha, ply),
            depth = depth,
            hashFlag = hashFlag,
            bestMove = bestMove
        )
        return alpha
    }

    private fun isKillerMove(move: Move, ply: Int): Boolean =
        killerMove[0][ply] == move || killerMove[1][ply] == move

    private fun hasNonPawnMaterial(chessBoard: ChessBoard, color: Int): Boolean =
        if (color == Piece.WHITE) {
            (chessBoard.whiteKnights or chessBoard.whiteBishops or
                    chessBoard.whiteRooks or chessBoard.whiteQueens) != 0L
        } else {
            (chessBoard.blackKnights or chessBoard.blackBishops or
                    chessBoard.blackRooks or chessBoard.blackQueens) != 0L
        }

    private fun quiescence(chessBoard: ChessBoard, alphaArg: Int, betaArg: Int, ply: Int): Int {
        if (isTimeUp()) return 0
        evaluations++
        var quiescenceAlpha = alphaArg
        val toPlayColor = chessBoard.toPlayColor
        if (chessBoard.insufficientMaterial() || moveGenerator.isThreefoldRepetition(chessBoard)) {
            return 0
        }
        // only captures are searched here unless in check, so generate just the captures
        // (this also tells us whether the side to move is in check)
        var toPlayLegalMoves = moveGenerator.generateLegalCapturesFor(chessBoard, toPlayColor)
        val inCheck = moveGenerator.isKingChecked(toPlayColor)
        if (inCheck) {
            // in check every evasion is searched, and no moves means checkmate
            toPlayLegalMoves = moveGenerator.generateLegalMovesFor(chessBoard, toPlayColor)
            if (toPlayLegalMoves.size() == 0) return LOSE_SCORE + ply
        } else if (chessBoard.fiftyMovesDrawCount >= 100) {
            return 0
        }
        if (ply >= MAX_PLY - 1) {
            return evaluate(chessBoard, toPlayColor)
        }
        if (!inCheck) {
            // stand pat is only allowed when not in check (stalemate is not detected here:
            // that would need the quiet moves too)
            val eval = evaluate(chessBoard, toPlayColor)
            if (eval >= betaArg) {
                return betaArg
            }
            if (quiescenceAlpha < eval) {
                quiescenceAlpha = eval
            }
            if (useSee) {
                // captures that lose material cannot improve on standing pat
                toPlayLegalMoves.removeMovesIf { move ->
                    !move.isPromote && isPossiblyLosingCapture(move) &&
                            staticExchangeEvaluation(chessBoard, move) < 0
                }
            }
        }
        toPlayLegalMoves = sortMoves(chessBoard, toPlayLegalMoves, ply)
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

    // ------------------------------------------------------- static exchange evaluation

    // a capture can only lose material if the capturing piece is worth more than the victim
    private fun isPossiblyLosingCapture(move: Move): Boolean =
        SEE_VALUES[move.pieceType] > SEE_VALUES[move.takenPieceType]

    /**
     * Static exchange evaluation: the material balance (from the mover's side) after the
     * best sequence of captures and recaptures on the target square, where each side
     * may stop capturing when continuing would lose material.
     */
    fun staticExchangeEvaluation(chessBoard: ChessBoard, move: Move): Int {
        val target = move.to
        val gain = IntArray(32)
        var depth = 0
        gain[0] = SEE_VALUES[move.takenPieceType]
        var occupied = chessBoard.allPieces and (1L shl move.from).inv()
        if (move.isEnPasant) {
            val capturedPawn = if (move.color == Piece.WHITE) target - 8 else target + 8
            occupied = occupied and (1L shl capturedPawn).inv()
        }
        // value of the piece now standing on the target square
        var pieceOnTarget = SEE_VALUES[move.pieceType]
        var side = Piece.GetOppositeColor(move.color)
        while (depth < gain.size - 1) {
            val attackers = attackersTo(chessBoard, target, occupied) and occupied
            val attacker = leastValuableAttacker(chessBoard, attackers, side)
            if (attacker < 0) break
            depth++
            gain[depth] = pieceOnTarget - gain[depth - 1]
            // neither side can gain by continuing
            if (maxOf(-gain[depth - 1], gain[depth]) < 0) break
            pieceOnTarget = SEE_VALUES[chessBoard.pieceType(attacker)]
            // removing the attacker also uncovers sliders behind it (x-rays)
            occupied = occupied and (1L shl attacker).inv()
            side = Piece.GetOppositeColor(side)
        }
        while (depth > 0) {
            gain[depth - 1] = -maxOf(-gain[depth - 1], gain[depth])
            depth--
        }
        return gain[0]
    }

    // all pieces (both colours) attacking [square], with sliders blocked by [occupied]
    private fun attackersTo(chessBoard: ChessBoard, square: Int, occupied: Long): Long {
        var attackers = 0L
        val file = GetFile(square)
        val rank = GetRank(square)
        // white pawns attack diagonally upwards, black pawns downwards
        if (rank > 0) {
            if (file > 0) attackers = attackers or (chessBoard.whitePawns and (1L shl (square - 9)))
            if (file < 7) attackers = attackers or (chessBoard.whitePawns and (1L shl (square - 7)))
        }
        if (rank < 7) {
            if (file > 0) attackers = attackers or (chessBoard.blackPawns and (1L shl (square + 7)))
            if (file < 7) attackers = attackers or (chessBoard.blackPawns and (1L shl (square + 9)))
        }
        attackers = attackers or
                (KNIGHT_ATTACKS[square] and (chessBoard.whiteKnights or chessBoard.blackKnights))
        attackers = attackers or
                (KING_ATTACKS[square] and (chessBoard.whiteKing or chessBoard.blackKing))
        val rooksAndQueens = chessBoard.whiteRooks or chessBoard.blackRooks or
                chessBoard.whiteQueens or chessBoard.blackQueens
        val bishopsAndQueens = chessBoard.whiteBishops or chessBoard.blackBishops or
                chessBoard.whiteQueens or chessBoard.blackQueens
        for (direction in 0 until 8) {
            val fileStep = DIRECTION_FILE_STEPS[direction]
            val rankStep = DIRECTION_RANK_STEPS[direction]
            val sliders = if (direction < 4) rooksAndQueens else bishopsAndQueens
            var f = file + fileStep
            var r = rank + rankStep
            while (f in 0..7 && r in 0..7) {
                val bit = 1L shl (r * 8 + f)
                if (occupied and bit != 0L) {
                    // the first piece on the ray: an attacker if it slides this way
                    attackers = attackers or (sliders and bit)
                    break
                }
                f += fileStep
                r += rankStep
            }
        }
        return attackers
    }

    // square of the cheapest piece of [color] among [attackers], or -1
    private fun leastValuableAttacker(chessBoard: ChessBoard, attackers: Long, color: Int): Int {
        val white = color == Piece.WHITE
        var candidates = attackers and (if (white) chessBoard.whitePawns else chessBoard.blackPawns)
        if (candidates == 0L) candidates = attackers and (if (white) chessBoard.whiteKnights else chessBoard.blackKnights)
        if (candidates == 0L) candidates = attackers and (if (white) chessBoard.whiteBishops else chessBoard.blackBishops)
        if (candidates == 0L) candidates = attackers and (if (white) chessBoard.whiteRooks else chessBoard.blackRooks)
        if (candidates == 0L) candidates = attackers and (if (white) chessBoard.whiteQueens else chessBoard.blackQueens)
        if (candidates == 0L) candidates = attackers and (if (white) chessBoard.whiteKing else chessBoard.blackKing)
        return if (candidates == 0L) -1 else java.lang.Long.numberOfTrailingZeros(candidates)
    }

    private fun scoreMove(
        chessBoard: ChessBoard,
        currentMove: Move,
        ply: Int,
        pvNode: Move?,
        hashMove: Long
    ): Int {
        var currentScore = 0

        // capture score
        if (currentMove.isCapture) {
            val victim = currentMove.takenPieceType
            val attacker = currentMove.pieceType
            val mvvLva = mvv_lva[(attacker - 1) * 6 + (victim - 1)]
            currentScore += if (useSee && isPossiblyLosingCapture(currentMove) &&
                staticExchangeEvaluation(chessBoard, currentMove) < 0
            ) {
                // losing captures go after killer moves
                mvvLva + LOSING_CAPTURE_SCORE
            } else {
                mvvLva + 10000
            }
        } else {
            // killer move score
            if (ply < MAX_PLY && killerMove[0][ply] != null && killerMove[0][ply] == currentMove) {
                currentScore += 9000
            } else if (ply < MAX_PLY && killerMove[1][ply] != null && killerMove[1][ply] == currentMove) {
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
        // best move stored in the transposition table, tried right after the PV move
        if (hashMove != 0L && currentMove.bitValue == hashMove) {
            currentScore += 400000
        }

        return currentScore
    }

    private fun sortMoves(
        chessBoard: ChessBoard,
        moves: PlayerLegalMoves,
        ply: Int,
        pvNode: Move? = null,
        hashMove: Long = 0L
    ): PlayerLegalMoves {
        val count = moves.size()
        val scores = IntArray(count) { scoreMove(chessBoard, moves[it], ply, pvNode, hashMove) }
        // stable insertion sort of the move indices by descending score (lists are short,
        // and no objects are allocated per move)
        val order = IntArray(count) { it }
        for (i in 1 until count) {
            val index = order[i]
            val score = scores[index]
            var j = i - 1
            while (j >= 0 && scores[order[j]] < score) {
                order[j + 1] = order[j]
                j--
            }
            order[j + 1] = index
        }
        val sortedMoves = PlayerLegalMoves(ArrayList(count))
        for (index in order) {
            sortedMoves.add(moves[index])
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

        // null-move pruning: minimum remaining depth and the extra depth reduction
        private const val NULL_MOVE_MIN_DEPTH = 3
        private const val NULL_MOVE_REDUCTION = 2

        // late move reductions: moves searched at full depth first, and minimum depth
        private const val LMR_FULL_DEPTH_MOVES = 4
        private const val LMR_MIN_DEPTH = 3

        // ordering score of captures that lose material (below killer moves)
        private const val LOSING_CAPTURE_SCORE = 5000

        // piece values for static exchange evaluation, indexed by piece type
        private val SEE_VALUES = intArrayOf(0, 100, 320, 330, 500, 900, 20000)

        // ray directions: the first four are straight (rook), the last four diagonal (bishop)
        private val DIRECTION_FILE_STEPS = intArrayOf(0, 0, 1, -1, 1, 1, -1, -1)
        private val DIRECTION_RANK_STEPS = intArrayOf(1, -1, 0, 0, 1, -1, 1, -1)

        private fun leaperAttacks(fileSteps: IntArray, rankSteps: IntArray): LongArray =
            LongArray(64) { square ->
                var attacks = 0L
                for (k in fileSteps.indices) {
                    val f = square % 8 + fileSteps[k]
                    val r = square / 8 + rankSteps[k]
                    if (f in 0..7 && r in 0..7) attacks = attacks or (1L shl (r * 8 + f))
                }
                attacks
            }

        private val KNIGHT_ATTACKS = leaperAttacks(
            intArrayOf(1, 2, 2, 1, -1, -2, -2, -1),
            intArrayOf(2, 1, -1, -2, -2, -1, 1, 2)
        )
        private val KING_ATTACKS = leaperAttacks(
            intArrayOf(0, 1, 1, 1, 0, -1, -1, -1),
            intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        )

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
