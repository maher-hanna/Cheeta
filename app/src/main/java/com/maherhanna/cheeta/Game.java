package com.maherhanna.cheeta;

import java.util.Random;

class Game{
    public static final String DEBUG = "game";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private Player playerAtBottom;
    private Player playerAtTop;
    private LegalMoves legalMoves;


    public Game(Drawing drawing){
        this.drawing = drawing;
        this.chessBoard = new ChessBoard();


        //give players random piece color
        Random random = new Random();
        int i = random.nextInt(2);
        if(i == 0)
        {
            playerAtBottom = new HumanPlayer(Piece.Color.BLACK,chessBoard, playerAtTop);
            playerAtTop = new Player(Piece.Color.WHITE,chessBoard, playerAtBottom);

        }
        else {
            playerAtBottom = new HumanPlayer(Piece.Color.WHITE,chessBoard, playerAtTop);
            playerAtTop = new Player(Piece.Color.BLACK,chessBoard, playerAtBottom);
        }
        //--------------------------------

        chessBoard.setPlayers(playerAtBottom, playerAtTop);
        chessBoard.setUpBoard();
        drawing.chessBoard = this.chessBoard;

    }

    public void start(){
        if(playerAtBottom.color == Piece.Color.WHITE){
            playerAtBottom.play();
        }
        else {
            playerAtTop.play();
        }
    }

    public void humanPlayed(){
        playerAtTop.play();
    }
}