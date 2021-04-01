package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.Random;

class Game {
    public static final String DEBUG = "Cheeta_Debug";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private ComputerAi computerAi;
    public int gameType;
    private int computerPlayDelayMilli;
    public boolean paused ;
    Piece.Color currentPlayer;
    public Piece.Color bottomScreenPlayerColor;

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

    public void resume(){
        if (gameType == COMPUTER_HUMAN) {
            if(currentPlayer == bottomScreenPlayerColor){
                drawing.waitHumanToPlay();
                return;
            }
        }
        playComputer(currentPlayer);


    }


    public boolean isGameFinished(Piece.Color lastPlayed){
        boolean isFinished = false;
        Piece.Color currentToPlayColor = lastPlayed.getOpposite();
        LegalMoves currentToPlayLegalMoves = chessBoard.getLegalMovesFor(currentToPlayColor);
        if(currentToPlayLegalMoves.getNumberOfMoves() == 0){
            if(chessBoard.isKingInCheck(currentToPlayColor)){
                isFinished = true;
            }
        }
        return isFinished;

    }


    public void humanPlayed(Move humanMove) {
        humanMove = chessBoard.getLegalMovesFor(bottomScreenPlayerColor).getMoveForHuman(humanMove);
        chessBoard.movePiece(humanMove);
        drawing.drawAllPieces(humanMove);
        currentPlayer = bottomScreenPlayerColor.getOpposite();
        chessBoard.updateLegalMovesFor(bottomScreenPlayerColor,false);
        Piece.Color opponentColor = currentPlayer;
        chessBoard.updateLegalMovesFor(opponentColor,chessBoard.isKingInCheck(opponentColor));

        if(isGameFinished(bottomScreenPlayerColor))
        {
            chessBoard.setGameFinished();
            drawing.finishGame(bottomScreenPlayerColor,gameType,true);
            return;
        } else{
            playComputer(opponentColor);

        }

    }

    public void computerPlayed(Move computerMove, Piece.Color color) {
        drawing.drawAllPieces(computerMove);
        currentPlayer = color.getOpposite();
        if(isGameFinished(color)){
            chessBoard.setGameFinished();
            drawing.finishGame(color,gameType,false);

            return;
        }

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
        if(paused) return;
        Move computerMove = computerAi.getMove(chessBoard, color);
        chessBoard.movePiece(computerMove);
        chessBoard.updateLegalMovesFor(color,false);
        Piece.Color opponentColor = color.getOpposite();
        chessBoard.updateLegalMovesFor(opponentColor,chessBoard.isKingInCheck(opponentColor));
        computerPlayed(computerMove, color);
    }



}