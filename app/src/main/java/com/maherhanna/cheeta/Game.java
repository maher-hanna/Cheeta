package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.Random;

class Game {
    public static final String DEBUG = "Cheeta_Debug";
    public static final long COMPUTER_MAX_SEARCH_TIME = 4;
    private final Drawing drawing;
    private final ChessBoard chessBoard;
    private ComputerAiThread computerAi;
    public static MoveGenerator moveGenerator = new MoveGenerator();
    public boolean paused;
    Piece.Color currentPlayer;
    public Piece.Color humanPlayerColor;
    private boolean gameFinished = false;


    public Game(Drawing drawing, int humanPlayerColor) {
        this.drawing = drawing;
        computerAi = new ComputerAi();
        paused = false;
        this.humanPlayerColor = Piece.Color.values()[humanPlayerColor];


        this.chessBoard = new ChessBoard();

        chessBoard.setUpBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");


        drawing.chessBoard = this.chessBoard;

    }


    public void start() {

        if (humanPlayerColor == Piece.Color.values()[chessBoard.toPlayColor]) {
            currentPlayer = humanPlayerColor;
            drawing.waitHumanToPlay();
        } else {
            playComputer(humanPlayerColor.getOpposite());

        }

    }

    public void resume() {
        if (Piece.Color.values()[chessBoard.toPlayColor] == humanPlayerColor) {
            drawing.waitHumanToPlay();
            return;
        }


    }


    public GameStatus checkGameFinished(Piece.Color lastPlayed) {
        return chessBoard.checkStatus(chessBoard.getLegalMovesFor(lastPlayed.getOpposite()));

    }


    public void humanPlayed(Move humanMove) {
        humanMove = chessBoard.getLegalMovesFor(humanPlayerColor).getMove(humanMove);
        chessBoard.move(humanMove);
        drawing.drawAllPieces(humanMove);
        drawing.show();
        currentPlayer = humanPlayerColor.getOpposite();
        chessBoard.updateLegalMovesFor(humanPlayerColor, false);
        Piece.Color opponentColor = currentPlayer;
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));
        GameStatus gameStatus = checkGameFinished(humanPlayerColor);
        if (gameStatus == GameStatus.NOT_FINISHED) {

            playComputer(opponentColor);


        } else {
            setGameFinished();
            drawing.finishGame(gameStatus);
            return;

        }

    }

    public void computerPlayed(Move computerMove) {
        computerAi.cancel(true);
        chessBoard.move(computerMove);
        Piece.Color color = computerMove.getColor();
        chessBoard.updateLegalMovesFor(color, false);
        Piece.Color opponentColor = color.getOpposite();
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));
        drawing.drawAllPieces(computerMove);
        drawing.show();
        currentPlayer = color.getOpposite();
        GameStatus gameStatus = checkGameFinished(color);
        if (gameStatus == GameStatus.NOT_FINISHED) {
            drawing.waitHumanToPlay();

        } else {
            //game finished
            setGameFinished();
            drawing.finishGame(gameStatus);

            return;
        }


    }

    public void playComputer(Piece.Color color) {
        if (paused) return;

        computerAi = new ComputerAi();

        computerAi.execute(chessBoard);


    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished() {
        gameFinished = true;
    }

    public enum GameStatus {NOT_FINISHED, FINISHED_DRAW, FINISHED_WIN_WHITE, FINISHED_WIN_BLACK}

    private final class ComputerAi extends ComputerAiThread {
        @Override
        protected void onPostExecute(Move move) {
            computerPlayed(move);
        }
    }


}