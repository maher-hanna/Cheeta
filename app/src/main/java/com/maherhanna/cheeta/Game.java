package com.maherhanna.cheeta;

import java.util.Random;

class Game{
    private Drawing drawing;
    private ChessBoard chessBoard;
    private Player firstPlayer;
    private Player secondPlayer;

    public Game(Drawing drawing){
        this.drawing = drawing;

        //give players random piece color
        Random random = new Random();
        //get random 0 or 1
        int i = random.nextInt(2);
        if(i == 0)
        {
            firstPlayer = new Player(Piece.PieceColor.BLACK);
            secondPlayer = new Player(Piece.PieceColor.WHITE);

        }
        else {
            firstPlayer = new Player(Piece.PieceColor.WHITE);
            secondPlayer = new Player(Piece.PieceColor.BLACK);
        }
        //--------------------------------


        this.chessBoard = new ChessBoard(firstPlayer,secondPlayer);
    }
}