package com.maherhanna.cheeta

import kotlin.experimental.and

class ChessBoard(firstPlayerColor: PieceColor) {
    var squares = Array<Piece>(64,{index -> Piece(PieceType.EMPTY) })
    var firstPlayerPieces :List<PlayerPiece>
    var secondPlayerPieces: List<PlayerPiece>
    val firstPlayerColor:PieceColor

    init{
        this.firstPlayerColor = firstPlayerColor

        firstPlayerPieces =  giveFirstPlayerPieces(firstPlayerColor)
        secondPlayerPieces = giveSecondPlayerPieces(if(firstPlayerColor == PieceColor.WHITE)
        PieceColor.BLACK else PieceColor.WHITE)
    }

    private fun giveFirstPlayerPieces(color: PieceColor) : List<PlayerPiece>{
        //this is the player at the bottom of screen
        var pieces = mutableListOf<PlayerPiece>()
        //first add the pawns
        for(i in 0..7){
            squares[position(i,1)] = Piece(PieceType.PAWN,color)
            pieces.add(PlayerPiece(position(i,1)))
        }
        squares[8] = Piece(PieceType.ROOK,color)
        pieces.add(PlayerPiece(0))

        squares[9] = Piece(PieceType.KNIGHT,color)
        pieces.add(PlayerPiece(1))

        squares[10] = Piece(PieceType.BISHOP,color)
        pieces.add(PlayerPiece(2))


        if(color == PieceColor.WHITE){

            squares[11] = Piece(PieceType.QUEEN,color)
            pieces.add(PlayerPiece(3))

            squares[12] = Piece(PieceType.KING,color)
            pieces.add(PlayerPiece(4))


        }
        else
        {
            squares[11] = Piece(PieceType.KING,color)
            pieces.add(PlayerPiece(3))

            squares[12] = Piece(PieceType.QUEEN,color)
            pieces.add(PlayerPiece(4))

        }
        squares[13] = Piece(PieceType.BISHOP,color)
        pieces.add(PlayerPiece(5))

        squares[14] = Piece(PieceType.KNIGHT,color)
        pieces.add(PlayerPiece(6))

        squares[15] = Piece(PieceType.ROOK,color)
        pieces.add(PlayerPiece(7))

        return pieces

    }
    private fun giveSecondPlayerPieces(color: PieceColor) : List<PlayerPiece>{
        //this is the player at the top of screen
        var pieces = mutableListOf<PlayerPiece>()
        //first add the pawns
        for(i in 0..7){
            squares[position(i,6)] = Piece(PieceType.PAWN,color)
            pieces.add(PlayerPiece(position(i,6)))
        }
        squares[8] = Piece(PieceType.ROOK,color)
        pieces.add(PlayerPiece(position(0,7)))

        squares[9] = Piece(PieceType.KNIGHT,color)
        pieces.add(PlayerPiece(position(1,7)))

        squares[10] = Piece(PieceType.BISHOP,color)
        pieces.add(PlayerPiece(position(2,7)))

        if(color == PieceColor.WHITE){

            squares[11] = Piece(PieceType.KING,color)
            pieces.add(PlayerPiece(position(3,7)))

            squares[12] = Piece(PieceType.QUEEN,color)
            pieces.add(PlayerPiece(position(4,7)))

        }
        else
        {
            squares[11] = Piece(PieceType.QUEEN,color)
            pieces.add(PlayerPiece(position(3,7)))

            squares[12] = Piece(PieceType.KING,color)
            pieces.add(PlayerPiece(position(4,7)))

        }

        squares[13] = Piece(PieceType.BISHOP,color)
        pieces.add(PlayerPiece(position(5,7)))

        squares[14] = Piece(PieceType.KNIGHT,color)
        pieces.add(PlayerPiece(position(6,7)))

        squares[15] = Piece(PieceType.ROOK,color)
        pieces.add(PlayerPiece(position(7,7)))
        return pieces

    }


    fun position(file:Int,rank:Int):Int{
        return (rank * 8) + file
    }

}