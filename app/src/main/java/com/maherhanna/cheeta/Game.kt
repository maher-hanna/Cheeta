package com.maherhanna.cheeta

class Game(drawing: Drawing) {
    private val drawing:Drawing
    private var chessBoard: ChessBoard


    init{
        this.drawing = drawing
        chessBoard = ChessBoard(PieceColor.WHITE)
    }
}