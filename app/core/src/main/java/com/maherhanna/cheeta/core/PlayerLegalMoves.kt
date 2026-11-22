package com.maherhanna.cheeta.core

/**
 * A list of moves stored as encoded Longs (see Move's companion functions) in a growable
 * array, so generating moves allocates no objects. [get] wraps a move in a Move object
 * for callers that want one; the search uses [bits] instead. A list can be [clear]ed and
 * reused.
 */
class PlayerLegalMoves(initialCapacity: Int = 64) {
    private var moves = LongArray(initialCapacity)
    private var count = 0

    fun size(): Int = count

    fun clear() {
        count = 0
    }

    /** The encoded move at [index]. */
    fun bits(index: Int): Long = moves[index]

    fun setBits(index: Int, bits: Long) {
        moves[index] = bits
    }

    /** Keeps only the first [newSize] moves. */
    fun shrinkTo(newSize: Int) {
        count = newSize
    }

    fun swap(first: Int, second: Int) {
        val temporary = moves[first]
        moves[first] = moves[second]
        moves[second] = temporary
    }

    fun addBits(bits: Long) {
        if (count == moves.size) moves = moves.copyOf(maxOf(8, moves.size * 2))
        moves[count++] = bits
    }

    /** The move at [index] as a (newly allocated) Move object. */
    operator fun get(index: Int): Move = Move(moves[index])

    fun add(newMove: Move) = addBits(newMove.bitValue)

    fun getLegalTargetsFor(position: Int): ArrayList<Int> {
        val targetSquares = ArrayList<Int>()
        for (i in 0 until count) {
            if (Move.fromOf(moves[i]) == position) {
                targetSquares.add(Move.toOf(moves[i]))
            }
        }
        return targetSquares
    }

    fun canMove(from: Int, to: Int): Boolean = getLegalTargetsFor(from).contains(to)

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
        for (i in 0 until count) {
            val bits = moves[i]
            if (Move.fromOf(bits) == from && Move.toOf(bits) == to &&
                (!Move.isPromotion(bits) || Move.promotionPieceTypeOf(bits) == promotionPieceType)
            ) {
                return Move(bits)
            }
        }
        return null
    }

    /** Removes every move whose encoding matches [predicate], keeping the order. */
    fun removeBitsIf(predicate: (Long) -> Boolean) {
        var kept = 0
        for (i in 0 until count) {
            if (!predicate(moves[i])) moves[kept++] = moves[i]
        }
        count = kept
    }

    /** Removes every move matching [predicate] (wraps each move in a Move object). */
    fun removeMovesIf(predicate: (Move) -> Boolean) = removeBitsIf { predicate(Move(it)) }

    fun removeNonTake() = removeBitsIf { !Move.isCapture(it) }

    fun isOpponentKingInCheck(opponentKingPosition: Int): Boolean {
        for (i in 0 until count) {
            if (Move.toOf(moves[i]) == opponentKingPosition) return true
        }
        return false
    }
}
