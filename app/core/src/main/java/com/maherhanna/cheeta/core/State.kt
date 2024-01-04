package com.maherhanna.cheeta.core

import java.util.Objects

class State(
    var allPieces: Long, @JvmField var enPassantTarget: Int, @JvmField var blackCastlingRights: Int,
    @JvmField var whiteCastlingRights: Int
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val state = o as State
        return allPieces == state.allPieces && enPassantTarget == state.enPassantTarget && blackCastlingRights == state.blackCastlingRights && whiteCastlingRights == state.whiteCastlingRights
    }

    override fun hashCode(): Int {
        return Objects.hash(allPieces, enPassantTarget, blackCastlingRights, whiteCastlingRights)
    }
}
