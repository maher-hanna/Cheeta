package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.Random;

class Game {
    public static final String DEBUG = "Cheeta_Debug";
    public static final String startPosition =  "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 ";
    private static final String trickyPosition = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1 ";
    private static final String killerPosition = "rnbqkb1r/pp1p1pPp/8/2p1pP2/1P1P4/3P3P/P1P1P3/RNBQKBNR w KQkq e6 0 1";
    private static final String cmkPosition = "r2q1rk1/ppp2ppp/2n1bn2/2b1p3/3pP3/3P1NPP/PPP1NPB1/R1BQ1RK1 b - - 0 9 ";
    private static final String positionInUse = startPosition;
    public static final long COMPUTER_MAX_SEARCH_TIME = 4;

    private final Drawing drawing;
    private final ChessBoard chessBoard;
    private ComputerAiThread computerAi;
    public static MoveGenerator moveGenerator = new MoveGenerator();
    public boolean paused;
    int currentPlayer;
    public int humanPlayerColor;
    private boolean gameFinished = false;


    public Game(Drawing drawing, int humanPlayerColor) {
        this.drawing = drawing;
        computerAi = new ComputerAi();
        paused = false;
        this.humanPlayerColor = humanPlayerColor;


        this.chessBoard = new ChessBoard(positionInUse);

        drawing.chessBoard = this.chessBoard;

    }


    public void start() {

        if (humanPlayerColor == chessBoard.toPlayColor) {
            currentPlayer = humanPlayerColor;
            drawing.waitHumanToPlay();
        } else {
            playComputer(Piece.GetOppositeColor(humanPlayerColor));

        }

    }

    public void resume() {
        if (chessBoard.toPlayColor == humanPlayerColor) {
            drawing.waitHumanToPlay();
            return;
        }


    }


    public GameStatus checkGameFinished(int lastPlayed) {
        return chessBoard.checkStatus(chessBoard.getLegalMovesFor(Piece.GetOppositeColor(lastPlayed)));

    }


    public void humanPlayed(Move humanMove) {
        humanMove = chessBoard.getLegalMovesFor(humanPlayerColor).getMove(humanMove);
        chessBoard.move(humanMove);
        currentPlayer = Piece.GetOppositeColor(humanPlayerColor);
        chessBoard.updateLegalMovesFor(humanPlayerColor, false);
        int opponentColor = currentPlayer;
        boolean isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor);
        if(isOpponentKingInCheck){
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard,opponentColor);
        } else{
            drawing.kingInCheck = ChessBoard.NO_SQUARE;
        }
        drawing.drawAllPieces();
        chessBoard.updateLegalMovesFor(opponentColor, isOpponentKingInCheck);
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
        drawing.currentMove = computerMove;
        computerAi.cancel(true);
        chessBoard.move(computerMove);

        int color = computerMove.getColor();
        chessBoard.updateLegalMovesFor(color, false);
        int opponentColor = Piece.GetOppositeColor(color);
        boolean isOpponentKingInCheck = chessBoard.isKingInCheck(opponentColor);
        if(isOpponentKingInCheck){
            drawing.kingInCheck = moveGenerator.getKingPosition(chessBoard,opponentColor);
        } else{
            drawing.kingInCheck = ChessBoard.NO_SQUARE;
        }
        drawing.drawAllPieces();

        chessBoard.updateLegalMovesFor(opponentColor, isOpponentKingInCheck);

        currentPlayer = Piece.GetOppositeColor(color);
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

    public void playComputer(int color) {
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