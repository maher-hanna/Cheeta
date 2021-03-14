package com.maherhanna.cheeta;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class Game{
    public static final String DEBUG = "game";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private int gameType;


    //game between
    public static final int COMPUTER_HUMAN = 0;
    public static final int COMPUTER_COMPUTER = 1;
    //------------

    public Game(Drawing drawing,int gameType,int computerPlayerDelay){
        this.drawing = drawing;
        this.chessBoard = new ChessBoard(drawing);
        Player playerAtTop = null;
        Player playerAtBottom = null;
        this.gameType = gameType;

        //give players random piece color
        Random random = new Random();
        int i = random.nextInt(2);
        if(i == 0)
        {

            playerAtTop = new ComputerPlayer(Square.Color.WHITE,chessBoard, playerAtBottom,computerPlayerDelay);
            if(gameType == Game.COMPUTER_HUMAN){
                playerAtBottom = new HumanPlayer(Square.Color.BLACK,chessBoard, playerAtTop);
            }
            else {
                playerAtBottom = new ComputerPlayer(Square.Color.BLACK,chessBoard, playerAtTop,computerPlayerDelay);

            }

        }
        else {
            playerAtTop = new ComputerPlayer(Square.Color.BLACK,chessBoard, playerAtBottom,computerPlayerDelay);
            if(gameType == Game.COMPUTER_HUMAN){
                playerAtBottom = new HumanPlayer(Square.Color.WHITE,chessBoard, playerAtTop);
            }
            else {
                playerAtBottom = new ComputerPlayer(Square.Color.WHITE,chessBoard, playerAtTop,computerPlayerDelay);

            }
        }
        //--------------------------------

        chessBoard.setPlayers(playerAtBottom, playerAtTop);
        chessBoard.setUpBoard();
        playerAtTop.opponent = playerAtBottom;
        playerAtBottom.opponent = playerAtTop;
        drawing.chessBoard = this.chessBoard;

    }

    public void start(){
        /* start playing with the white pieces player
        and if the starting player is the computer
        add a delay to his first move because the computer
        updates the drawing of chess board after moving
        his piece and at first the chess board view
        is not ready yet for drawing */

        if(chessBoard.playerAtBottom.color == Square.Color.WHITE){
            if(chessBoard.playerAtBottom instanceof ComputerPlayer){
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        chessBoard.playerAtBottom.play();
                    }
                },1000);
            }
            else{
                chessBoard.playerAtBottom.play();

            }
        }
        else {
            if(chessBoard.playerAtTop instanceof ComputerPlayer){
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        chessBoard.playerAtTop.play();
                    }
                },1000);
            }
            else{
                chessBoard.playerAtTop.play();

            }
        }
    }

}