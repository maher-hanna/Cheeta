package com.maherhanna.cheeta.core

class PlayerLegalMoves {
    // presized: a position rarely has more than 64 legal moves, so the list seldom grows
    private val legalMoves: ArrayList<Move> = ArrayList(64)

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
        val targets = getLegalTargetsFor(from)
        if (targets.isEmpty()) return false
        return targets.contains(to)
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

    fun getMove(basicMove: Move): Move? = searchMove(basicMove)

    /** Finds the legal move from/to the given squares; a promotion defaults to a queen. */
    fun searchMove(from: Int, to: Int): Move? = searchMove(from, to, Piece.QUEEN)

    /** Finds the legal move matching from/to and, for promotions, the requested piece (queen if none). */
    fun searchMove(pseudoMove: Move): Move? = searchMove(
        pseudoMove.from,
        pseudoMove.to,
        if (pseudoMove.isPromote) pseudoMove.promotionPieceType else Piece.QUEEN
    )

    private fun searchMove(from: Int, to: Int, promotionPieceType: Int): Move? {
        for (move in legalMoves) {
            if (move.from == from && move.to == to &&
                (!move.isPromote || move.promotionPieceType == promotionPieceType)
            ) {
                return move
            }
        }
        return null
    }
    fun removeNonTake() {
        // single pass; removing through an iterator shifted the array on every removal
        legalMoves.removeAll { !it.isCapture }
    }

    fun isOpponentKingInCheck(opponentKingPosition: Int): Boolean {
        return legalMoves.map { it.to }.contains(opponentKingPosition)
    }
}
