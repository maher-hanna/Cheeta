package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;

public class ComputerAi {
    public Move getMove(ChessBoard chessBoard, Piece.Color toPlayNow, int depth) {
        MyRunnable myRunnable = new MyRunnable(chessBoard, toPlayNow, depth);
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
    private int evaluations;

    public MyRunnable(ChessBoard chessBoard, Piece.Color maxingPlayer, int maxDepth) {
        this.chessBoard = chessBoard;
        this.maxingPlayer = maxingPlayer;
        this.maxDepth = maxDepth;
        this.evaluations = 0;
    }

    public Move getMove() {
        return this.move;
    }


    @Override
    public void run() {
        long startTime = System.nanoTime();
        LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard,
                chessBoard.isKingInCheck(maxingPlayer), maxingPlayer);
        ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();
        int maxIndex = -1;
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < toPlayMoves.size(); i++) {
            int score = miniMax(chessBoard, toPlayMoves.get(i), maxScore,
                    Integer.MAX_VALUE, 1, maxDepth);
            if (score > maxScore) {
                maxScore = score;
                maxIndex = i;
            }
        }
        long duration = System.nanoTime() - startTime;
        duration = duration / 1000; // convert to milli second
        Log.d(Game.DEBUG, "evaluations: " + String.valueOf(evaluations) + " move " +
                maxIndex);
        Log.d(Game.DEBUG,"Duration: " + String.valueOf(duration));

        this.move = toPlayMoves.get(maxIndex);


    }


    public int miniMax(ChessBoard chessBoard, Move move, int alpha, int beta,
                       int depth, final int maxDepth) {
        boolean maxing;

        if ((depth % 2) == 0) {
            maxing = true;

        } else {
            maxing = false;
        }

        ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
        chessBoardAfterMove.movePiece(move);


        if (depth == maxDepth || chessBoardAfterMove.checkGameFinished() == true) {
            evaluations++;
            return getScoreFor(chessBoardAfterMove, maxingPlayer);
        } else {
            LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoardAfterMove,
                    chessBoardAfterMove.isKingInCheck(move.getColor().getOpposite()),
                    move.getColor().getOpposite());
            ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();

            ArrayList<Integer> movesScores = new ArrayList<>();
            if (maxing) {
                int maxScore = Integer.MIN_VALUE;
                for (int i = 0; i < toPlayMoves.size(); i++) {
                    int score = miniMax(chessBoardAfterMove, toPlayMoves.get(i), alpha, beta,
                            depth + 1, maxDepth);
                    maxScore = Math.max(maxScore, score);
                    alpha = Math.max(alpha, score);

                    if (alpha >= beta) {
                        break;
                    }
                }
                return maxScore;
            } else {
                int minScore = Integer.MAX_VALUE;
                for (int i = 0; i < toPlayMoves.size(); i++) {
                    int score = miniMax(chessBoardAfterMove, toPlayMoves.get(i), alpha, beta,
                            depth + 1, maxDepth);
                    minScore = Math.min(minScore, score);
                    beta = Math.min(beta, score);

                    if (alpha >= beta) {
                        break;
                    }
                }
                return minScore;
            }


        }


    }

    private int getMinScore(ArrayList<Integer> moveScores) {
        int minScore = Integer.MAX_VALUE;
        for (int i = 0; i < moveScores.size(); i++) {
            if (moveScores.get(i) <= minScore) {
                minScore = moveScores.get(i);
            }
        }
        return minScore;
    }

    private int getMaxScore(ArrayList<Integer> moveScores) {
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < moveScores.size(); i++) {
            if (moveScores.get(i) >= maxScore) {
                maxScore = moveScores.get(i);
            }
        }
        return maxScore;

    }

    int getWhiteScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.Color.WHITE);
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
                value = evaluateDrawFor(chessBoard, Piece.Color.WHITE);
                gameFinished = true;
                break;
            case NOT_FINISHED:
                value = value - getPiecesValueFor(chessBoard, Piece.Color.BLACK);
                break;
        }
        return value;
    }

    private int evaluateDrawForWhite(ChessBoard chessBoard) {
        int piecesAdvantage = getPiecesValueFor(chessBoard, Piece.Color.WHITE) -
                getPiecesValueFor(chessBoard, Piece.Color.BLACK);
        return getInitialPiecesValue() - piecesAdvantage;
    }

    private int evaluateDrawForBlack(ChessBoard chessBoard) {
        int piecesAdvantage = getPiecesValueFor(chessBoard, Piece.Color.BLACK) -
                getPiecesValueFor(chessBoard, Piece.Color.WHITE);
        return getInitialPiecesValue() - piecesAdvantage;
    }

    private int evaluateDrawFor(ChessBoard chessBoard, Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return evaluateDrawForWhite(chessBoard);
        } else {
            return evaluateDrawForBlack(chessBoard);
        }
    }

    int getBlackScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.Color.BLACK);
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
                value = value - getPiecesValueFor(chessBoard, Piece.Color.WHITE);
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

    public int getPiecesValueFor(ChessBoard chessboard, Piece.Color color) {
        int value = 0;
        ArrayList<Integer> squares;
        if (color == Piece.Color.WHITE) {
            squares = chessboard.getWhitePositions();
        } else {
            squares = chessboard.getBlackPositions();
        }

        for (int square : squares) {
            switch (chessboard.getPieceAt(square).type) {
                case QUEEN:
                    value += Piece.QUEEN_VALUE;
                    break;
                case ROOK:
                    value += Piece.ROOK_VALUE;
                    break;
                case BISHOP:
                    value += Piece.BISHOP_VALUE;
                    break;
                case KNIGHT:
                    value += Piece.KNIGHT_VALUE;
                    break;
                case PAWN:
                    value += Piece.PAWN_VALUE;
                    break;
            }
        }
        return value;

    }

    private int getInitialPiecesValue() {
        return Piece.QUEEN_VALUE + Piece.ROOK_VALUE + Piece.BISHOP_VALUE + Piece.KNIGHT_VALUE +
                Piece.PAWN_VALUE;
    }

}

