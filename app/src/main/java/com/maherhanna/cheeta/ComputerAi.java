package com.maherhanna.cheeta;

import java.util.ArrayList;

public class ComputerAi {
    public Move getMove(ChessBoard chessBoard, Piece.Color toPlayNow,int depth) {
        MyRunnable myRunnable = new MyRunnable(chessBoard, toPlayNow,depth);
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


class MyRunnable implements Runnable {
    ChessBoard chessBoard;
    Piece.Color maxingPlayer;
    private Move move;
    private int maxDepth;

    public MyRunnable(ChessBoard chessBoard, Piece.Color maxingPlayer,int maxDepth) {
        this.chessBoard = chessBoard;
        this.maxingPlayer = maxingPlayer;
        this.maxDepth = maxDepth;
    }

    public Move getMove() {
        return this.move;
    }


    @Override
    public void run() {
        LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard,
                chessBoard.isKingInCheck(maxingPlayer), maxingPlayer);
        ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();
        int maxIndex = -1;
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < toPlayMoves.size(); i++) {
            int score = miniMax(chessBoard, toPlayMoves.get(i), 1, maxDepth);
            if (score >= maxScore) {
                maxScore = score;
                maxIndex = i;
            }
        }
        this.move = toPlayMoves.get(maxIndex);

    }

    public int miniMax(ChessBoard chessBoard, Move move, int depth, final int maxDepth) {
        boolean maxing;

        if((depth % 2) == 0){
            maxing = true;

        }
        else {
            maxing = false;
        }

        ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
        chessBoardAfterMove.movePiece(move);


        if (depth == maxDepth || chessBoardAfterMove.checkGameFinished() == true) {
            return getScoreFor(chessBoardAfterMove, maxingPlayer);
        } else {
            LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoardAfterMove,
                    chessBoardAfterMove.isKingInCheck(move.getColor().getOpposite()),
                    move.getColor().getOpposite());
            ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();
            int maxIndex = -1;
            int maxScore = Integer.MIN_VALUE;
            ArrayList<Integer> movesScores = new ArrayList<>();
            for (int i = 0; i < toPlayMoves.size(); i++) {
                int score = miniMax(chessBoardAfterMove, toPlayMoves.get(i), depth + 1, maxDepth);
                movesScores.add(score);
            }
            int bestScoreForMaxing = 0;
            if(maxing){
                bestScoreForMaxing = getMaxScore(movesScores);
            } else {
                bestScoreForMaxing = getMinScore(movesScores);
            }
            return bestScoreForMaxing;

        }


    }

    private int getMinScore(ArrayList<Integer> moveScores) {
        int minScore = Integer.MAX_VALUE;
        int minScoreIndex = -1;
        for (int i = 0; i < moveScores.size(); i++) {
            if (moveScores.get(i) <= minScore) {
                minScore = moveScores.get(i);
                minScoreIndex = i;
            }
        }
        return minScore;
    }

    private int getMaxScore(ArrayList<Integer> moveScores) {
        int maxScore = Integer.MIN_VALUE;
        int maxScoreIndex = -1;
        for (int i = 0; i < moveScores.size(); i++) {
            if (moveScores.get(i) >= maxScore) {
                maxScore = moveScores.get(i);
                maxScoreIndex = i;
            }
        }
        return maxScore;

    }

    int getWhiteScore(ChessBoard chessBoard) {
        int value = chessBoard.getPiecesValueFor(chessboard,Piece.Color.WHITE);
        boolean gameFinished = false;

        switch (chessBoard.checkStatus()) {
            case FINISHED_WIN_WHITE:
                value = Integer.MAX_VALUE;
                gameFinished = true;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MIN_VALUE;
                gameFinished = true;
                break;
            case FINISHED_DRAW:
                value = 0;
                gameFinished = true;
                break;
            case NOT_FINISHED:
                value = value - chessBoard.getPiecesValueFor(Piece.Color.BLACK);
                break;
        }
        return value;
    }

    int getBlackScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard,Piece.Color.BLACK);
        boolean gameFinished = false;
        switch (chessBoard.checkStatus()) {
            case FINISHED_WIN_WHITE:
                value = Integer.MIN_VALUE;
                gameFinished = true;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MAX_VALUE;
                gameFinished = true;
                break;
            case FINISHED_DRAW:
                value = 0;
                gameFinished = true;
                break;
            case NOT_FINISHED:
                value = value - getPiecesValueFor(chessBoard,Piece.Color.WHITE);
                break;
        }
        return value;
    }

    int getScoreFor(ChessBoard chessBoard, Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhiteScore(chessBoard);
        } else {
            return getBlackScore(chessBoard);
        }
    }


}

    public int getPiecesValueFor(ChessBoard chessboard, Piece.Color color){
        int value = 0;
        ArrayList<Integer> squares;
        if(color == Piece.Color.WHITE){
            squares = chessboard.getWhitePositions();
        }
        else {
            squares = chessboard.getBlackPositions();
        }

        for(int square: squares){
            switch (chessboard.getPieceAt(square).type){
                case QUEEN:
                    value += 90;
                    break;
                case ROOK:
                    value += 50;
                    break;
                case BISHOP:
                    value += 30;
                    break;
                case KNIGHT:
                    value += 30;
                    break;
                case PAWN:
                    value +=10;
                    break;
            }
        }
        return value;

    }