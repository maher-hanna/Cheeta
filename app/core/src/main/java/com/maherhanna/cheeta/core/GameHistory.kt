package com.maherhanna.cheeta.core

/** The moves played so far, stored as encoded Longs (see Move's companion functions). */
class GameHistory {
    private var moves: LongArray
    private var count = 0

    constructor() {
        moves = LongArray(64)
    }

    constructor(copy: GameHistory) {
        moves = copy.moves.copyOf()
        count = copy.count
    }

    val size: Int
        get() = count

    fun add(move: Move) = addBits(move.bitValue)

    fun addBits(bits: Long) {
        if (count == moves.size) moves = moves.copyOf(maxOf(8, moves.size * 2))
        moves[count++] = bits
    }

    fun removeLastMove() {
        count--
    }

    fun isEmpty(): Boolean = count == 0

    /** The encoded last move; only valid when the history is not empty. */
    val lastBits: Long
        get() = moves[count - 1]

    val notation: String
        get() = (0 until count).joinToString(" ") { Move(moves[it]).notation }

    val lastMove: Move?
        get() = if (count == 0) null else Move(moves[count - 1])

    val lastPlayed: Int
        get() = if (count == 0) Piece.BLACK else Move.colorOf(moves[count - 1])

    /** The most recent capturing move (including en passant), or null if nothing was captured. */
    val lastCapture: Move?
        get() {
            for (i in count - 1 downTo 0) {
                if (Move.isCapture(moves[i])) return Move(moves[i])
            }
            return null
        }

    operator fun get(index: Int): Move = Move(moves[index])
}
