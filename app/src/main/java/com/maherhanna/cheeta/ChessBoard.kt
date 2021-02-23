package com.maherhanna.cheeta

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
        var pieces = Array<Piece>(16,{index -> Piece(PieceType.PAWN,color) })
        return pieces

    }
    private fun giveSecondPlayerPieces(color: PieceColor) : Array<Piece>{
        var pieces = Array<Piece>(16,{index -> Piece(PieceType.PAWN,color) })
        return pieces

    }
}