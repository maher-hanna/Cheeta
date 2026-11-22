package com.maherhanna.cheeta.core

/**
 * Snapshot of a position used for castling/en passant bookkeeping and repetition detection.
 * [hash] is the position's Zobrist hash, which covers the pieces, the side to move, the
 * castling rights and a capturable en passant square. Two states are the same position
 * when their hashes are equal (the chance of two different positions sharing a 64-bit
 * hash is negligible), so no copy of the piece bitboards is kept.
 */
class State(
    @JvmField val toPlayColor: Int,
    @JvmField var enPassantTarget: Int,
    @JvmField var blackCastlingRights: Int,
    @JvmField var whiteCastlingRights: Int,
    @JvmField val hash: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return hash == (other as State).hash
    }

    override fun hashCode(): Int = hash.hashCode()
}
