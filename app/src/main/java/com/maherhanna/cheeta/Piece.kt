package com.maherhanna.cheeta

import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

enum class PieceType(val value:Byte) {EMPTY(0),PAWN(1), ROOK(2), KNIGHT(4),
    BISHOP(8),QUEEN(16),KING(32)}

enum class PieceColor(val value:Byte) {BLACK(0), WHITE(64)}

public class Piece(pieceType: PieceType,pieceColor: PieceColor = PieceColor.BLACK) {

    private var bitValue:Byte = pieceType.value or PieceColor.BLACK.value

    init {
        setType(pieceType)
        setColor(pieceColor)
    }

    fun getType(): PieceType{
        return when(bitValue){
            1.toByte() -> PieceType.PAWN
            2.toByte() -> PieceType.ROOK
            4.toByte() -> PieceType.KNIGHT
            8.toByte() -> PieceType.BISHOP
            16.toByte() -> PieceType.QUEEN
            32.toByte() -> PieceType.KING
            else -> throw Exception("invalid internal piece value")
        }

    }
    fun setType(pieceType: PieceType){
        //store the piece type and color using one byte
        //the seven'th bit is the color

        //first clear all bits but save color
        // bit to add it after setting type bit
        val colorbit:Byte = bitValue or 64
        bitValue = 0

        when(pieceType){
            PieceType.PAWN -> bitValue = 1
            PieceType.ROOK -> bitValue = 2
            PieceType.KNIGHT -> bitValue = 4
            PieceType.BISHOP -> bitValue = 8
            PieceType.QUEEN -> bitValue = 16
        }

        bitValue = bitValue or colorbit

        //last bit is set to 1 to indicate that square is not empty
        //when the bitValue is written to board
        bitValue = bitValue or 128.toByte()

    }

    fun getColor():PieceColor{
        return if((bitValue and 64) == 1.toByte())
            PieceColor.WHITE
        else
            PieceColor.BLACK

    }

    fun setColor(pieceColor: PieceColor){
        //set the seven'th bit to 1 if color is white
        //or to 0 if color is black
        bitValue = if(pieceColor == PieceColor.WHITE)
            bitValue or 64
        else
            bitValue xor 64
    }



}