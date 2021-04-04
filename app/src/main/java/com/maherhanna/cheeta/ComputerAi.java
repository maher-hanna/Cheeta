package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Random;

public class ComputerAi {
    public Move getMove(ChessBoard chessBoard, Piece.Color toPlayNow){
        MyRunnable myRunnable = new MyRunnable(chessBoard,toPlayNow);
        Thread thread = new Thread(myRunnable);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return myRunnable.getMove();


    }
}


class MyRunnable implements Runnable{
    ChessBoard chessBoard;
    Piece.Color toPlayNow;
    private Move move;

    public MyRunnable(ChessBoard chessBoard, Piece.Color toPlayNow){
        this.chessBoard = chessBoard;
        this.toPlayNow = toPlayNow;
    }

    public Move getMove(){
        return this.move;
    }


    @Override
    public void run() {
        Random random = new Random();
        ArrayList<Integer> piecesPositions = chessBoard.getPositionsFor(toPlayNow);
        int numPieces = piecesPositions.size();
        int numOfLegalMoves = 0;
        int randomPieceIndex = 0;
        int randomPiecePosition = 0;
        do {
            randomPieceIndex = random.nextInt(numPieces);
            randomPiecePosition = piecesPositions.get(randomPieceIndex);
            numOfLegalMoves = chessBoard.getLegalTargetsFor(randomPiecePosition).size();

        } while (numOfLegalMoves == 0);

        int randomLegalMove = random.nextInt(numOfLegalMoves);
        int randomMove = chessBoard.getLegalTargetsFor(randomPiecePosition).get(randomLegalMove);

        Move resultMove = new Move(chessBoard.getPieceAt(randomPiecePosition), randomPiecePosition, randomMove);
        resultMove = chessBoard.getLegalMovesFor(resultMove.getColor()).getMove(resultMove);
        this.move = resultMove;

    }
}