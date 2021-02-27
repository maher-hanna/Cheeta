package com.maherhanna.cheeta;

class Player {

    public Piece.PieceColor color;
    private ChessBoard chessBoard;

    public Player(Piece.PieceColor color, ChessBoard chessBoard){
        this.color = color;
        this.chessBoard = chessBoard;
    }
}