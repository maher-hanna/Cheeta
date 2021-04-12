package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

class Game {
    public static final String DEBUG = "Cheeta_Debug";
    private static final int COMPUTER_MAX_SEARCH_DEPTH = 3;
    private final Drawing drawing;
    private final ChessBoard chessBoard;
    private final ComputerAi computerAi;
    public int gameType;
    private final int computerPlayDelayMilli;
    public boolean paused;
    Piece.Color currentPlayer;
    public Piece.Color bottomScreenPlayerColor;
    private boolean gameFinished = false;


    //game between
    public static final int COMPUTER_HUMAN = 0;
    public static final int COMPUTER_COMPUTER = 1;
    //------------

    public Game(Drawing drawing, int gameType, int computerPlayerDelay) {
        this.drawing = drawing;
        this.gameType = gameType;
        this.computerPlayDelayMilli = computerPlayerDelay;
        computerAi = new ComputerAi();
        paused = false;

        //give players random color
        Random random = new Random();
        int i = random.nextInt(2);
        if (i == 0) {
            bottomScreenPlayerColor = Piece.Color.WHITE;
        } else {
            this.bottomScreenPlayerColor = Piece.Color.BLACK;
        }
        //--------------------------------

        this.chessBoard = new ChessBoard(drawing);

        chessBoard.setUpBoard();

        drawing.chessBoard = this.chessBoard;

    }


    public void start() {
        if (gameType == COMPUTER_HUMAN) {
            if (bottomScreenPlayerColor == Piece.Color.WHITE) {
                currentPlayer = bottomScreenPlayerColor;
                drawing.waitHumanToPlay();
            } else {
                playComputer(Piece.Color.WHITE);

            }
        } else {
            playComputer(Piece.Color.WHITE);
        }

    }

    public void resume() {
        if (gameType == COMPUTER_HUMAN) {
            if (currentPlayer == bottomScreenPlayerColor) {
                drawing.waitHumanToPlay();
                return;
            }
        }
        playComputer(currentPlayer);


    }


    public GameStatus checkGameFinished(Piece.Color lastPlayed) {
        return chessBoard.checkStatus();

    }



    public void humanPlayed(Move humanMove) {
        humanMove = chessBoard.getLegalMovesFor(bottomScreenPlayerColor).getMove(humanMove);
        chessBoard.movePiece(humanMove);
        drawing.drawAllPieces(humanMove);
        drawing.show();
        currentPlayer = bottomScreenPlayerColor.getOpposite();
        chessBoard.updateLegalMovesFor(bottomScreenPlayerColor, false);
        Piece.Color opponentColor = currentPlayer;
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));

        GameStatus gameStatus = checkGameFinished(bottomScreenPlayerColor);
        if (gameStatus == GameStatus.NOT_FINISHED) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playComputer(opponentColor);
                }
            },100);



        } else {
            setGameFinished();
            drawing.finishGame(gameStatus, gameType);
            return;

        }

    }

    public void computerPlayed(Move computerMove, Piece.Color color) {
        drawing.drawAllPieces(computerMove);
        currentPlayer = color.getOpposite();
        GameStatus gameStatus = checkGameFinished(color);
        if (gameStatus == GameStatus.NOT_FINISHED) {
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
        } else {
            //game finished
            setGameFinished();
            drawing.finishGame(gameStatus, gameType);

            return;
        }


    }

    public void playComputer(Piece.Color color) {
        if (paused) return;
        Move computerMove = computerAi.getMove(chessBoard, color,COMPUTER_MAX_SEARCH_DEPTH);
        chessBoard.movePiece(computerMove);
        chessBoard.updateLegalMovesFor(color, false);
        Piece.Color opponentColor = color.getOpposite();
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));
        computerPlayed(computerMove, color);
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished() {
        gameFinished = true;
    }

    public enum GameStatus {NOT_FINISHED, FINISHED_DRAW, FINISHED_WIN_WHITE, FINISHED_WIN_BLACK}


}