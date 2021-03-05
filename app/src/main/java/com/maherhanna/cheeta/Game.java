package com.maherhanna.cheeta;

import java.util.Random;

class Game{
    public static final String DEBUG = "game";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private int gameType;

    private LegalMoves legalMoves;

    //game between
    public static final int COMPUTER_HUMAN = 0;
    public static final int COMPUTER_COMPUTER = 1;
    //------------

    public Game(Drawing drawing,int gameType){
        this.drawing = drawing;
        this.chessBoard = new ChessBoard();
        Player playerAtBottom = null;
        Player playerAtTop = null;
        this.gameType = gameType;

        //give players random piece color
        Random random = new Random();
        int i = random.nextInt(2);
        if(i == 0)
        {

            playerAtTop = new ComputerPlayer(Piece.Color.WHITE,chessBoard, playerAtBottom);
            if(gameType == Game.COMPUTER_HUMAN){
                playerAtBottom = new HumanPlayer(Piece.Color.BLACK,chessBoard, playerAtTop);
            }
            else {
                playerAtBottom = new ComputerPlayer(Piece.Color.BLACK,chessBoard, playerAtTop);

            }

        }
        else {
            playerAtTop = new ComputerPlayer(Piece.Color.BLACK,chessBoard, playerAtBottom);
            if(gameType == Game.COMPUTER_HUMAN){
                playerAtBottom = new HumanPlayer(Piece.Color.WHITE,chessBoard, playerAtTop);
            }
            else {
                playerAtBottom = new ComputerPlayer(Piece.Color.WHITE,chessBoard, playerAtTop);

            }
        }
        //--------------------------------

        chessBoard.setPlayers(playerAtBottom, playerAtTop);
        chessBoard.setUpBoard();
        drawing.chessBoard = this.chessBoard;

    }

    public void start(){
        if(chessBoard.playerAtBottom.color == Piece.Color.WHITE){
            chessBoard.playerAtBottom.play();
        }
        else {
            chessBoard.playerAtTop.play();
        }
    }

}