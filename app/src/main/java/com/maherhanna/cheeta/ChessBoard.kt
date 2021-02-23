package com.maherhanna.cheeta

import kotlin.experimental.and

class ChessBoard(firstPlayerColor: PieceColor) {
    var squares = byteArrayOf(64,0)
    var firstPlayerPieces :Array<Piece>
    var secondPlayerPieces: Array<Piece>
    val firstPlayerColor:PieceColor

    init{
        this.firstPlayerColor = firstPlayerColor

        firstPlayerPieces =  giveFirstPlayerPieces(firstPlayerColor)
        secondPlayerPieces = giveSecondPlayerPieces(if(firstPlayerColor == PieceColor.WHITE)
        PieceColor.BLACK else PieceColor.WHITE)
    }

    private fun giveFirstPlayerPieces(color: PieceColor) : Array<Piece>{
        //this is the player at the bottom of screen
        var pieces = Array<Piece>(16,{index -> Piece() })
        //first add the pawns
        for(i in 0..7){
            pieces[i] = Piece(PieceType.PAWN,color,position(i,1))
        }
        pieces[8] = Piece(PieceType.ROOK,color,0)
        pieces[9] = Piece(PieceType.KNIGHT,color,1)
        pieces[10] = Piece(PieceType.BISHOP,color,2)
        if(color == PieceColor.WHITE){
            pieces[11] = Piece(PieceType.QUEEN,color,3)
            pieces[12] = Piece(PieceType.KING,color,4)

        }
        else
        {
            pieces[11] = Piece(PieceType.KING,color,3)
            pieces[12] = Piece(PieceType.QUEEN,color,4)

        }

        pieces[13] = Piece(PieceType.BISHOP,color,5)
        pieces[14] = Piece(PieceType.KNIGHT,color,6)
        pieces[15] = Piece(PieceType.ROOK,color,7)
        return pieces

    }
    private fun giveSecondPlayerPieces(color: PieceColor) : Array<Piece>{
        //this is the player at the top of screen
        var pieces = Array<Piece>(16,{index -> Piece() })
        //first add the pawns
        for(i in 0..7){
            pieces[i] = Piece(PieceType.PAWN,color,position(i,6))
        }
        pieces[8] = Piece(PieceType.ROOK,color,position(0,7))
        pieces[9] = Piece(PieceType.KNIGHT,color,position(1,7))
        pieces[10] = Piece(PieceType.BISHOP,color,position(2,7))
        if(color == PieceColor.WHITE){
            pieces[11] = Piece(PieceType.KING,color,position(3,7))
            pieces[12] = Piece(PieceType.QUEEN,color,position(4,7))

        }
        else
        {
            pieces[11] = Piece(PieceType.QUEEN,color,position(3,7))
            pieces[12] = Piece(PieceType.KING,color,position(4,7))

        }

        pieces[13] = Piece(PieceType.BISHOP,color,position(5,7))
        pieces[14] = Piece(PieceType.KNIGHT,color,position(6,7))
        pieces[15] = Piece(PieceType.ROOK,color,position(7,7))
        return pieces

    }

    fun isSquareEmpty(position:Byte): Boolean{
        return (squares[position.toInt()] and 128.toByte()) != 1.toByte()
    }

    fun position(file:Int,rank:Int):Byte{
        return ((rank * 8) + file).toByte()
    }

}