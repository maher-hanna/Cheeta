package com.maherhanna.cheeta.core

/**
 * Snapshot of a position used for castling/en passant bookkeeping and repetition detection.
 * Two states are equal only if every piece bitboard, the side to move, the en passant
 * target and the castling rights are equal. [hash] is the position's Zobrist hash.
 */
class State(
    private val pieces: LongArray,
    @JvmField val toPlayColor: Int,
    @JvmField var enPassantTarget: Int,
    @JvmField var blackCastlingRights: Int,
    @JvmField var whiteCastlingRights: Int,
    @JvmField val hash: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val state = other as State
        // equal positions always have equal hashes, so this rejects almost all
        // non-matches before comparing the piece arrays
        return hash == state.hash &&
                toPlayColor == state.toPlayColor &&
                enPassantTarget == state.enPassantTarget &&
                blackCastlingRights == state.blackCastlingRights &&
                whiteCastlingRights == state.whiteCastlingRights &&
                pieces.contentEquals(state.pieces)
    }

    override fun hashCode(): Int = hash.hashCode()
}
