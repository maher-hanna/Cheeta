package com.maherhanna.cheeta.core

import java.util.Objects

class Move {
    private var bitValue: Long
    var from: Int
        get() = BitMath.getBitsValue(bitValue, FROM_START, FROM_MASK).toInt()
        set(position) {
            bitValue = BitMath.setBitsValue(bitValue, FROM_START, FROM_MASK, position)
        }

    val fromNotation: String
        get() = ChessBoard.GetFileNotation(from) + ChessBoard.GetRankNotation(from)

    val toNotation: String
        get() = ChessBoard.GetFileNotation(to) + ChessBoard.GetRankNotation(to)

    var to: Int
        get() = BitMath.getBitsValue(bitValue, TO_START, TO_MASK).toInt()
        set(position) {
            bitValue = BitMath.setBitsValue(bitValue, TO_START, TO_MASK, position)
        }
    val color: Int
        get() = BitMath.getBitsValue(bitValue, COLOR_START, COLOR_MASK).toInt()
    var pieceType: Int
        get() = BitMath.getBitsValue(bitValue, TYPE_START, TYPE_MASK).toInt()
        set(type) {
            bitValue = BitMath.setBitsValue(bitValue, TYPE_START, TYPE_MASK, type)
        }
    val pieceName: String
        get() =
            when(pieceType){
                Piece.PAWN -> "Pawn"
                Piece.KNIGHT -> "Knight"
                Piece.BISHOP -> "Bishop"
                Piece.ROOK -> "Rook"
                Piece.QUEEN -> "Queen"
                Piece.KING -> "King"
                else -> "Unknown"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val move = other as Move
        return bitValue == move.bitValue
    }

    override fun hashCode(): Int {
        return Objects.hash(bitValue)
    }

    constructor(pieceType: Int, pieceColor: Int, from: Int, to: Int) {
        bitValue = 0
        this.pieceType = pieceType
        setPieceColor(pieceColor)
        this.from = from
        this.to = to
    }

    constructor(pieceType: Int, pieceColor: Int, from: Int, to: Int, takenPieceType: Int) : this(
        pieceType,
        pieceColor,
        from,
        to
    ) {
        setTakes(takenPieceType)
    }

    constructor(copy: Move) {
        bitValue = copy.bitValue
    }

    val isCastling: Boolean
        get() = BitMath.getBitsValue(
            bitValue, CASTLING_START, CASTLING_MASK
        ) == 1L
    var castlingType: CastlingType
        get() = CastlingType.entries[BitMath.getBitsValue(
            bitValue,
            CASTLING_TYPE_START,
            CASTLING_TYPE_MASK
        ).toInt()]
        set(castlingType) {
            val value = castlingType.ordinal
            bitValue =
                BitMath.setBitsValue(bitValue, CASTLING_TYPE_START, CASTLING_TYPE_MASK, value)
        }
    val isTake: Boolean
        get() = BitMath.getBitsValue(
            bitValue, TAKE_START, TAKE_MASK
        ) == 1L
    var takenPieceType: Int
        get() = BitMath.getBitsValue(bitValue, TAKE_TYPE_START, TAKE_TYPE_MASK).toInt()
        set(pieceType) {
            bitValue = BitMath.setBitsValue(bitValue, TAKE_TYPE_START, TAKE_TYPE_MASK, pieceType)
        }
    val isPromote: Boolean
        get() = BitMath.getBitsValue(
            bitValue, PROMOTE_START, PROMOTE_MASK
        ) == 1L
    var promotionPieceType: Int
        get() = BitMath.getBitsValue(bitValue, PROMOTE_TYPE_START, PROMOTE_TYPE_MASK).toInt()
        set(promotionPieceType) {
            bitValue = BitMath.setBitsValue(
                bitValue,
                PROMOTE_TYPE_START,
                PROMOTE_TYPE_MASK,
                promotionPieceType
            )
        }
    val isEnPasant: Boolean
        get() = BitMath.getBitsValue(
            bitValue, ENPASSANT_START, ENPASSANT_MASK
        ) == 1L
    val isPawnDoubleMove: Boolean
        get() = BitMath.getBitsValue(bitValue, PAWN_DOUBLE_PUSH_START, PAWN_DOUBLE_PUSH_MASK) == 1L
    var previousFiftyMoves: Int
        get() = BitMath.getBitsValue(
            bitValue, PREVIOUS_FIFTY_MOVES_START,
            PREVIOUS_FIFTY_MOVES_MASK
        ).toInt()
        set(fiftyMoves) {
            bitValue = BitMath.setBitsValue(
                bitValue,
                PREVIOUS_FIFTY_MOVES_START,
                PREVIOUS_FIFTY_MOVES_MASK,
                fiftyMoves
            )
        }


    fun setPieceColor(color: Int) {
        bitValue = BitMath.setBitsValue(bitValue, COLOR_START, COLOR_MASK, color)
    }

    fun setCastling() {
        bitValue = BitMath.setBitsValue(bitValue, CASTLING_START, CASTLING_MASK, 1)
    }

    fun setCastling(castlingType: CastlingType) {
        setCastling()
        this.castlingType = castlingType
    }

    fun setTakes() {
        bitValue = BitMath.setBitsValue(bitValue, TAKE_START, TAKE_MASK, 1)
    }

    fun setTakes(pieceType: Int) {
        setTakes()
        takenPieceType = pieceType
    }

    fun setPromotes() {
        bitValue = BitMath.setBitsValue(bitValue, PROMOTE_START, PROMOTE_MASK, 1)
    }

    fun setPromotes(promotionPieceType: Int) {
        setPromotes()
        this.promotionPieceType = promotionPieceType
    }

    fun setEnPasant() {
        bitValue = BitMath.setBitsValue(bitValue, ENPASSANT_START, ENPASSANT_MASK, 1)
        setTakes(Piece.PAWN)
    }

    fun setPawnDoublePush() {
        bitValue = BitMath.setBitsValue(bitValue, PAWN_DOUBLE_PUSH_START, PAWN_DOUBLE_PUSH_MASK, 1)
    }

    enum class CastlingType {
        CASTLING_KING_SIDE,
        CASTLING_QUEEN_SIDE
    }

    companion object {
        private const val TYPE_MASK: Long = 7
        private const val TYPE_START: Long = 0
        private const val COLOR_MASK: Long = 8
        private const val COLOR_START: Long = 3
        private const val FROM_MASK: Long = 1008
        private const val FROM_START: Long = 4
        private const val TO_MASK: Long = 64512
        private const val TO_START: Long = 10
        private const val CASTLING_MASK: Long = 65536
        private const val CASTLING_START: Long = 16
        private const val CASTLING_TYPE_MASK: Long = 131072
        private const val CASTLING_TYPE_START: Long = 17
        private const val TAKE_MASK: Long = 262144
        private const val TAKE_START: Long = 18
        private const val TAKE_TYPE_MASK: Long = 3670016
        private const val TAKE_TYPE_START: Long = 19
        private const val PROMOTE_MASK: Long = 4194304
        private const val PROMOTE_START: Long = 22
        private const val PROMOTE_TYPE_MASK: Long = 58720256
        private const val PROMOTE_TYPE_START: Long = 23
        private const val ENPASSANT_MASK: Long = 67108864
        private const val ENPASSANT_START: Long = 26
        private const val PAWN_DOUBLE_PUSH_MASK: Long = 134217728
        private const val PAWN_DOUBLE_PUSH_START: Long = 27
        private const val PREVIOUS_FIFTY_MOVES_MASK = 16911433728L
        private const val PREVIOUS_FIFTY_MOVES_START: Long = 28
    }
}
