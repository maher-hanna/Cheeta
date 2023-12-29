package com.maherhanna.cheeta

class Piece(p: Piece) {
    var position: Int
    val file: Int
        get() = position % 8
    val rank: Int
        get() = position / 8

    fun offset(file: Int, rank: Int): Int {
        val newFile = this.file + file
        val newRank = this.rank + rank
        if (newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT
        return if (newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) ChessBoard.OUT else newRank * 8 + newFile
    }

    fun offsetFile(file: Int): Int {
        return offset(file, 0)
    }

    fun offsetRank(rank: Int): Int {
        return offset(0, rank)
    }

    init {
        position = p.position
    }

    companion object {
        const val PAWN = 0
        const val KNIGHT = 1
        const val BISHOP = 2
        const val ROOK = 3
        const val QUEEN = 4
        const val KING = 5
        const val WHITE = 0
        const val BLACK = 1

        //bit representation positions
        private const val TYPE_BITS_START = 1
        private const val TYPE_BITS_END = 3
        private const val COLOR_BITS_START = 4
        private const val COLOR_BITS_END = 4
        private const val POSITION_BITS_START = 5
        private const val POSITION_BITS_END = 10
        private const val OUT_OF_BOARD_BITS_START = 11
        private const val OUT_OF_BOARD_BITS_END = 11
        @JvmField
        var QUEEN_VALUE = 900
        @JvmField
        var ROOK_VALUE = 500
        @JvmField
        var BISHOP_VALUE = 330
        @JvmField
        var KNIGHT_VALUE = 320
        @JvmField
        var PAWN_VALUE = 100
        @JvmField
        var KING_VALUE = 20000
        @JvmStatic
        fun GetOppositeColor(color: Int): Int {
            var color = color
            return 1.let { color = color xor it; color }
        }
    }
}
