package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.Random;

class Game {
    public static final String DEBUG = "game";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private ComputerAi computerAi;
    public int gameType;
    private int computerPlayDelayMilli;


    //game between
    public static final int COMPUTER_HUMAN = 0;
    public static final int COMPUTER_COMPUTER = 1;
    //------------

    public Game(Drawing drawing, int gameType, int computerPlayerDelay) {
        this.drawing = drawing;
        this.gameType = gameType;
        this.computerPlayDelayMilli = computerPlayerDelay;
        computerAi = new ComputerAi();

        //give players random color
        Random random = new Random();
        int i = random.nextInt(2);
        if (i == 0) {
            this.chessBoard = new ChessBoard(drawing, Piece.Color.BLACK);
        } else {
            this.chessBoard = new ChessBoard(drawing, Piece.Color.WHITE);

        }
        //--------------------------------

        chessBoard.setUpBoard();

        drawing.chessBoard = this.chessBoard;

    }

    public void start() {


        drawing.clearBoard();
        drawing.drawAllPieces();
        drawing.show();

        if (gameType == COMPUTER_HUMAN) {
            if (chessBoard.topPlayerColor == Piece.Color.WHITE) {
                playComputer(Piece.Color.WHITE);
            } else {
                drawing.waitHumanToPlay();
            }
        } else {


            playComputer(Piece.Color.WHITE);


        }

    }


    public void humanPlayed(Move humanMove) {
        chessBoard.movePiece(humanMove);
        chessBoard.updateLegalMovesFor(chessBoard.bottomPlayerColor,false);
        chessBoard.updateWhiteLegalMoves(false);
        playComputer(chessBoard.topPlayerColor);

    }

    public void computerPlayed(Move computerMove, Piece.Color color) {
        Piece.Color opponentColor = color.getOpposite();
        if (gameType == COMPUTER_COMPUTER) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playComputer(opponentColor);
                }
            }, computerPlayDelayMilli);

        } else {
            drawing.waitHumanToPlay();
        }
    }

    public void playComputer(Piece.Color color) {
        Move computerMove = computerAi.getMove(chessBoard, color);
        chessBoard.movePiece(computerMove);
        chessBoard.updateBlackLegalMoves(false);
        chessBoard.updateWhiteLegalMoves(false);
        drawing.clearBoard();
        drawing.drawMoveHighlight(computerMove);
        drawing.drawAllPieces();
        drawing.show();
        computerPlayed(computerMove, color);
    }


}