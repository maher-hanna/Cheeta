package com.maherhanna.cheeta.core

/**
 * Snapshot of a position used for castling/en passant bookkeeping and repetition detection.
 * Two states are equal only if every piece bitboard, the side to move, the en passant
 * target and the castling rights are equal.
 */
class State(
    private val pieces: LongArray,
    @JvmField val toPlayColor: Int,
    @JvmField var enPassantTarget: Int,
    @JvmField var blackCastlingRights: Int,
    @JvmField var whiteCastlingRights: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val state = other as State
        return toPlayColor == state.toPlayColor &&
                enPassantTarget == state.enPassantTarget &&
                blackCastlingRights == state.blackCastlingRights &&
                whiteCastlingRights == state.whiteCastlingRights &&
                pieces.contentEquals(state.pieces)
    }

    override fun hashCode(): Int {
        var result = pieces.contentHashCode()
        result = 31 * result + toPlayColor
        result = 31 * result + enPassantTarget
        result = 31 * result + blackCastlingRights
        result = 31 * result + whiteCastlingRights
        return result
    }
}
