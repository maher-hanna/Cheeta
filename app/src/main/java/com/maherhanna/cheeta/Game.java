package com.maherhanna.cheeta;

import java.util.Random;

class Game{
    private Drawing drawing;
    private ChessBoard chessBoard;
    private Player firstPlayer;
    private Player secondPlayer;
    private LegalMoves legalMoves;

    public Game(Drawing drawing){
        this.drawing = drawing;
        this.chessBoard = new ChessBoard();

        //give players random piece color
        Random random = new Random();
        int i = random.nextInt(2);
        if(i == 0)
        {
            firstPlayer = new Player(Piece.Color.BLACK,chessBoard,secondPlayer);
            secondPlayer = new Player(Piece.Color.WHITE,chessBoard,firstPlayer);

        }
        else {
            firstPlayer = new Player(Piece.Color.WHITE,chessBoard,secondPlayer);
            secondPlayer = new Player(Piece.Color.BLACK,chessBoard,firstPlayer);
        }
        //--------------------------------

        chessBoard.setPlayers(firstPlayer,secondPlayer);
        chessBoard.setUpBoard();
        legalMoves = new LegalMoves(chessBoard);
        drawing.chessBoard = this.chessBoard;

    }

    public void start(){
        if(firstPlayer.color == Piece.Color.WHITE){
            firstPlayer.play();
        }
        else {
            secondPlayer.play();
        }
    }
}