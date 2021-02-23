package com.maherhanna.cheeta

import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

enum class PieceType(val type:Byte) {PAWN(1), ROOK(2), KNIGHT(4),
    BISHOP(8),QUEEN(16),KING(32)}

enum class PieceColor(val color:Byte) {BLACK(0), WHITE(64)}

public class Piece {

    private var bitValue:Byte = PieceType.PAWN.type or PieceColor.BLACK.color
    //position in chessboard array with values from 0 to 63
    private var position:Byte = 0


    constructor(pieceType: PieceType = PieceType.PAWN
    , pieceColor: PieceColor = PieceColor.BLACK,
    position: Byte = 0){
        setType(pieceType)
        setColor(pieceColor)

    }

    constructor(bitValue:Byte,position: Byte){
        this.bitValue = this.bitValue
        setPosition(position)
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

    fun getPosition(): Byte{
        return this.position
    }
    fun setPosition(position: Byte){
        if(position < 0 || position > 63)
            throw IndexOutOfBoundsException("Given position of piece is out of board range")
        else
            this.position = position
    }

    fun getValue():Byte{
        return this.bitValue
    }
    fun setValue(bitValue: Byte){
        this.bitValue = bitValue

    }

}