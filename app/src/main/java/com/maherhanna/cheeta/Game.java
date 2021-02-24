package com.maherhanna.cheeta;

class Game{
    private Drawing drawing;
    private ChessBoard chessBoard;

    public Game(Drawing drawing){
        this.drawing = drawing;
        this.chessBoard = new ChessBoard(Piece.PieceColor.WHITE);
    }
}