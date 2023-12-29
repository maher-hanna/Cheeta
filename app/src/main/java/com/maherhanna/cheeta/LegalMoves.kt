package com.maherhanna.cheeta

class LegalMoves {
    private val legalMoves: ArrayList<Move>

    init {
        legalMoves = ArrayList()
    }

    fun getLegalTargetsFor(position: Int): ArrayList<Int> {
        val targetSquares = ArrayList<Int>()
        for (move in legalMoves) {
            if (move.from == position) {
                targetSquares.add(move.to)
            }
        }
        return targetSquares
    }

    fun canMove(from: Int, to: Int): Boolean {
        val targetsForFrom = getLegalTargetsFor(from)
        if (targetsForFrom.isEmpty()) return false
        return if (targetsForFrom.contains(to)) true else false
    }

    fun size(): Int {
        return legalMoves.size
    }

    operator fun get(index: Int): Move {
        return legalMoves[index]
    }

    fun add(newMove: Move) {
        legalMoves.add(newMove)
    }

    fun addAll(moves: ArrayList<Move>?) {
        legalMoves.addAll(moves!!)
    }

    fun getMove(basicMove: Move): Move {
        var legalMove = Move(basicMove)
        for (move in legalMoves) {
            if (move.from == basicMove.from && move.to == basicMove.to) {
                legalMove = Move(move)
                break
            }
        }
        return legalMove
    }

    fun removeNonTake() {
        val itr = legalMoves.iterator()
        while (itr.hasNext()) {
            val move = itr.next()
            if (!move.isTake) {
                itr.remove()
            }
        }
    }
}
