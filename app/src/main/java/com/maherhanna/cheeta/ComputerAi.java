package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
    private final int maxDepth;
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
                maxingPlayer);
        ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();


        ArrayList<MoveScore> moveScores = new ArrayList<>();
        int maxIndex = -1;
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < toPlayMoves.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.movePiece(toPlayMoves.get(i));
            int score = miniMax(chessBoardAfterMove, maxScore,
                    Integer.MAX_VALUE, 1, 2);
            if (score > maxScore) {
                maxScore = score;
                maxIndex = i;
            }
            moveScores.add(new MoveScore(score,i));
        }
        Collections.sort(moveScores);


        maxIndex = -1;
        maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < moveScores.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.movePiece(toPlayMoves.get(moveScores.get(i).moveIndex));
            int score = miniMax(chessBoardAfterMove, maxScore,
                    Integer.MAX_VALUE, 1, maxDepth);
            if (score > maxScore) {
                maxScore = score;
                maxIndex = moveScores.get(i).moveIndex;
            }
            if(score == Integer.MAX_VALUE) break;
        }
        long duration = System.nanoTime() - startTime;
        duration = duration / 1000; // convert to milli second
        Log.d(Game.DEBUG, "alpha beta evaluations: " + evaluations + " move " +
                maxIndex);
        Log.d(Game.DEBUG,"Duration: " + duration);


        this.move = toPlayMoves.get(maxIndex);


//        maxIndex = -1;
//        maxScore = Integer.MIN_VALUE;
//        startTime = System.nanoTime();
//        for (int i = 0; i < toPlayMoves.size(); i++) {
//            int score = miniMax(chessBoard, toPlayMoves.get(i), 1, maxDepth);
//            if (score > maxScore) {
//                maxScore = score;
//                maxIndex = i;
//            }
//        }
//        duration = System.nanoTime() - startTime;
//        duration = duration / 1000; // convert to milli second
//        Log.d(Game.DEBUG, "minimax evaluations: " + evaluations + " move " +
//                maxIndex);
//        Log.d(Game.DEBUG,"Duration: " + duration);



    }



    public int miniMax(ChessBoard chessBoard, int alpha, int beta,
                       int depth, final int maxDepth) {
        boolean maxing;
        Piece.Color toPlayColor = Piece.Color.BLACK;

        maxing = (depth % 2) == 0;
        if(maxing) {
            if(maxingPlayer == Piece.Color.WHITE) toPlayColor = Piece.Color.WHITE;
        }else{
            if(maxingPlayer == Piece.Color.BLACK) toPlayColor = Piece.Color.WHITE;
        }




        if (depth == maxDepth || chessBoard.checkGameFinished() == true) {
            evaluations++;
            return getScoreFor(chessBoard, maxingPlayer);
        } else {

            LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard,
                    toPlayColor);
            ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();

            if (maxing) {
                int maxScore = Integer.MIN_VALUE;
                for (int i = 0; i < toPlayMoves.size(); i++) {
                    ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                    chessBoardAfterMove.movePiece(toPlayMoves.get(i));
                    int score = miniMax(chessBoardAfterMove, alpha, beta,
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
                    ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                    chessBoardAfterMove.movePiece(toPlayMoves.get(i));
                    int score = miniMax(chessBoardAfterMove, alpha, beta,
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

    public int miniMax(ChessBoard chessBoard, Move move,int depth, final int maxDepth) {
        boolean maxing;

        maxing = (depth % 2) == 0;

        ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
        chessBoardAfterMove.movePiece(move);


        if (depth == maxDepth || chessBoardAfterMove.checkGameFinished() == true) {
            evaluations++;
            return getScoreFor(chessBoardAfterMove, maxingPlayer);
        } else {
            LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoardAfterMove,
                    move.getColor().getOpposite());
            ArrayList<Move> toPlayMoves = toPlayLegalMoves.getAllLegalMoves();

            ArrayList<Integer> movesScores = new ArrayList<>();
            if (maxing) {
                int maxScore = Integer.MIN_VALUE;
                for (int i = 0; i < toPlayMoves.size(); i++) {
                    int score = miniMax(chessBoardAfterMove, toPlayMoves.get(i),depth + 1, maxDepth);
                    maxScore = Math.max(maxScore, score);

                }
                return maxScore;
            } else {
                int minScore = Integer.MAX_VALUE;
                for (int i = 0; i < toPlayMoves.size(); i++) {
                    int score = miniMax(chessBoardAfterMove, toPlayMoves.get(i),depth + 1, maxDepth);
                    minScore = Math.min(minScore, score);
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
        switch (chessBoard.checkStatus()) {
            case FINISHED_WIN_WHITE:
                value = Integer.MAX_VALUE;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MIN_VALUE;
                break;
            case FINISHED_DRAW:
                value = 0;
                break;
            case NOT_FINISHED:
                value = value - getPiecesValueFor(chessBoard, Piece.Color.BLACK);
                break;
        }
        return value;
    }




    int getBlackScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.Color.BLACK);
        switch (chessBoard.checkStatus()) {
            case FINISHED_WIN_WHITE:
                value = Integer.MIN_VALUE;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MAX_VALUE;
                break;
            case FINISHED_DRAW:
                value = 0;
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
            Piece piece = chessboard.getPieceAt(square);
            value += getPositionalValue(piece);

        }
        return value;
    }
    public int getPiecesValueMinusKingFor(ChessBoard chessboard, Piece.Color color) {
        int value = 0;
        ArrayList<Integer> squares;
        if (color == Piece.Color.WHITE) {
            squares = chessboard.getWhitePositions();
        } else {
            squares = chessboard.getBlackPositions();
        }

        for (int square : squares) {
            Piece piece = chessboard.getPieceAt(square);
            if(piece.getType() == Piece.Type.KING) continue;
            value += getPositionalValue(piece);

        }
        return value;
    }

    private int getPositionalValue(Piece piece) {
        int value = 0;
        int piecePositionOnTable = ChessBoard.OUT_OF_BOARD;
        int file = ChessBoard.GetFile(piece.getPosition());
        int rank = ChessBoard.GetRank(piece.getPosition());
        if(piece.getColor() == Piece.Color.WHITE){
            rank = 7 - rank;
        } else {
            file = 7 - file;
        }
        piecePositionOnTable = ChessBoard.GetPosition(file,rank);
        switch (piece.getType()){
            case PAWN:
                value += Piece.PAWN_VALUE + PAWN_SQUARES_TABLE[piecePositionOnTable];
                break;
            case ROOK:
                value += Piece.ROOK_VALUE + ROOK_SQUARES_TABLE[piecePositionOnTable];
                break;
            case KNIGHT:
                value += Piece.KNIGHT_VALUE + KNIGHT_SQUARES_TABLE[piecePositionOnTable];
                break;
            case BISHOP:
                value += Piece.BISHOP_VALUE + BISHOP_SQUARES_TABLE[piecePositionOnTable];
                break;
            case QUEEN:
                value += Piece.QUEEN_VALUE + QUEEN_SQUARES_TABLE[piecePositionOnTable];
                break;
            case KING:
                if(isEndGame(chessBoard)){
                    value += Piece.KING_VALUE + KING_END_GAME_SQUARES_TABLE[piecePositionOnTable];

                } else {
                    value += Piece.KING_VALUE + KING_MIDDLE_GAME_SQUARES_TABLE[piecePositionOnTable];
                }
                break;
        }
        return value;
    }

    boolean isEndGame(ChessBoard chessBoard){
        boolean endGame = false;
        int whitePiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.Color.WHITE) ;
        int blackPiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.Color.BLACK) ;
        if(Math.abs(whitePiecesValue - blackPiecesValue) >= Piece.QUEEN_VALUE){
            if(whitePiecesValue < Piece.QUEEN_VALUE) endGame = true;
            if(blackPiecesValue < Piece.QUEEN_VALUE) endGame = true;
        }

        return endGame;

    }

    private int getInitialPiecesValue() {
        return Piece.KING_VALUE + Piece.QUEEN_VALUE + Piece.ROOK_VALUE + Piece.BISHOP_VALUE +
                Piece.KNIGHT_VALUE + Piece.PAWN_VALUE;
    }

    public static int[] PAWN_SQUARES_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0
    };
    public static int[] KNIGHT_SQUARES_TABLE = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };
    public static int[] BISHOP_SQUARES_TABLE = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };
    public static int[] ROOK_SQUARES_TABLE = {
            0,  0,  0,  0,  0,  0,  0,  0,
            5, 10, 10, 10, 10, 10, 10,  5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            -5,  0,  0,  0,  0,  0,  0, -5,
            0,  0,  0,  5,  5,  0,  0,  0
    };

    public static int[] QUEEN_SQUARES_TABLE = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    public static int[] KING_MIDDLE_GAME_SQUARES_TABLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 30, 10,  0,  0, 10, 30, 20
    };
    public static int[] KING_END_GAME_SQUARES_TABLE = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50
    };



}

class MoveScore implements Comparable<MoveScore>{
    private int score;
    public int moveIndex;
    public MoveScore(int score,int moveIndex){
        this.score = score;
        this.moveIndex = moveIndex;
    }

    @Override
    public int compareTo(MoveScore other) {
        return other.score - this.score;
    }
}