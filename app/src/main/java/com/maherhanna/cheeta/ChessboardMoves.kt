package com.maherhanna.cheeta

import com.maherhanna.cheeta.Piece.Companion.GetOppositeColor

class ChessboardMoves {
    private val moves: ArrayList<Move>

    constructor() {
        moves = ArrayList()
    }

    constructor(copy: ChessboardMoves) {
        moves = ArrayList(copy.moves)
    }

    fun add(move: Move) {
        moves.add(move)
    }

    fun removeLastMove() {
        moves.removeAt(moves.size - 1)
    }

    val lastMove: Move?
        get() = if (moves.size == 0) {
            null
        } else moves[moves.size - 1]
    val toPlayNow: Int
        get() = GetOppositeColor(lastPlayed)
    val lastPlayed: Int
        get() = if (moves.size == 0) {
            Piece.BLACK
        } else {
            lastMove!!.color
        }

    fun notEmpty(): Boolean {
        return moves.size != 0
    }

    fun size(): Int {
        return moves.size
    }

    operator fun get(index: Int): Move {
        return moves[index]
    }
}
