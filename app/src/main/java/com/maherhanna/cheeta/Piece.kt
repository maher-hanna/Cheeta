package com.maherhanna.cheeta

import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

enum class PieceType(val type:Byte) {PAWN(1), ROOK(2), KNIGHT(3),
    BISHOP(4),QUEEN(6),KING(7)}

enum class PieceColor(val color:Byte) {BLACK(0), WHITE(1)}

public class Piece(pieceType: PieceType,pieceColor: PieceColor){
    private var value:Byte = pieceType.type or pieceColor.color
    //position in chessboard array with values from 0 to 63
    private var position:Byte = 0


    fun getType(): PieceType{
        return when(value){
            1.toByte() -> PieceType.PAWN
            2.toByte() -> PieceType.ROOK
            3.toByte() -> PieceType.KNIGHT
            4.toByte() -> PieceType.BISHOP
            5.toByte() -> PieceType.QUEEN
            6.toByte() -> PieceType.KING
            else -> throw Exception("invalid internal piece value")
        }

    }
    fun setType(pieceType: PieceType){
        //store the piece type and color using one byte
        //the seven'th bit is the color

        //first clear all bits but save color
        // bit to add it after setting type bit
        val colorbit:Byte = value or 7
        value = 0

        when(pieceType){
            PieceType.PAWN -> value = 1
            PieceType.ROOK -> value = 2
            PieceType.KNIGHT -> value = 3
            PieceType.BISHOP -> value = 4
            PieceType.QUEEN -> value = 5
        }

        value = value or colorbit

    }

    fun getColor():PieceColor{
        return if((value and 7) == 1.toByte())
            PieceColor.WHITE
        else
            PieceColor.BLACK

    }

    fun setColor(pieceColor: PieceColor){
        //set the seven'th bit to 1 if color is white
        //or to 0 if color is black
        value = if(pieceColor == PieceColor.WHITE)
            value or 7
        else
            value xor 7
    }

}