package com.maherhanna.cheeta.core

class GameHistory {
    private val moves: ArrayList<Move>

    constructor() {
        moves = ArrayList()
    }

    constructor(copy: GameHistory) {
        moves = ArrayList(copy.moves)
    }

    fun add(move: Move) {
        moves.add(move)
    }

    fun removeLastMove() {
        moves.removeAt(moves.size - 1)
    }
    fun isEmpty():Boolean{
        return moves.isEmpty()
    }

    val notation : String
        get() = moves.joinToString(" ") { it.notation }

    val lastMove: Move?
        get() = if (moves.size == 0) {
            null
        } else moves[moves.size - 1]

    val lastPlayed: Int
        get() = if (moves.size == 0) {
            Piece.BLACK
        } else {
            lastMove!!.color
        }

    operator fun get(index: Int): Move {
        return moves[index]
    }
}
