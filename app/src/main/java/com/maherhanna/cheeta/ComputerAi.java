package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class ComputerAi {
    public Move getMove(ChessBoard chessBoard, Piece.Color toPlayNow, float maxSearchTime) {
        MyRunnable myRunnable = new MyRunnable(chessBoard, toPlayNow, maxSearchTime);
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
    private final long maxSearchTime;
    private int evaluations;

    public MyRunnable(ChessBoard chessBoard, Piece.Color maxingPlayer, float maxSearchTime) {
        this.chessBoard = chessBoard;
        this.maxingPlayer = maxingPlayer;
        //convert seconds to nano seconds
        this.maxSearchTime = (long) (maxSearchTime * 1000000000);
        this.evaluations = 0;
    }

    public Move getMove() {
        return this.move;
    }


    @Override
    public void run() {
        long startTime = System.nanoTime();

        LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard, Game.moveGenerator,
                maxingPlayer);
        ArrayList<MoveScore> moveScores;
        ArrayList<MoveScore> moveScoresAfterSearch = new ArrayList<>();

        int maxDepth = 0;
        boolean timeFinished = false;
        int bestMoveIndex = 0;
        long timeLeft = maxSearchTime;
        for (int i = 0; i < toPlayLegalMoves.size(); i++) {
            moveScoresAfterSearch.add(new MoveScore(0, i));
        }
        do {
            timeLeft = (startTime + maxSearchTime) - System.nanoTime();
            maxDepth++;
            moveScores = search(chessBoard, toPlayLegalMoves, moveScoresAfterSearch, timeLeft, maxDepth);
            if (moveScores.isEmpty()) {
                break;
            }
            moveScoresAfterSearch = new ArrayList<>(moveScores);
            Collections.sort(moveScoresAfterSearch);
            bestMoveIndex = moveScoresAfterSearch.get(0).moveIndex;
            moveScores.clear();
        } while (!timeFinished);


        long duration = System.nanoTime() - startTime;
        duration = duration / 1000; // convert to milli second
        Log.d(Game.DEBUG, "alpha beta evaluations: " + evaluations + " move " +
                bestMoveIndex);
        Log.d(Game.DEBUG, "Duration: " + (float) duration / 1000000 + " depth " + maxDepth);

//
//        evaluations = 0;
//        int maxIndex = 0;
//        int maxScore = Integer.MIN_VALUE;
//        startTime = System.nanoTime();
//        for (int i = 0; i < toPlayLegalMoves.size(); i++) {
//            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
//            chessBoardAfterMove.move(toPlayLegalMoves.get(i));
//            int score = miniMax(chessBoardAfterMove, 2,false);
//            if (score > maxScore) {
//                maxScore = score;
//                maxIndex = i;
//            }
//        }
//        duration = System.nanoTime() - startTime;
//        duration = duration / 1000; // convert to milli second
//        Log.d(Game.DEBUG, "minimax evaluations: " + evaluations + " move " +
//                maxIndex);
//        Log.d(Game.DEBUG,"Duration: " + (float)duration / 1000000);

        this.move = toPlayLegalMoves.get(bestMoveIndex);

    }


    public ArrayList<MoveScore> search(ChessBoard chessBoard, LegalMoves moves, ArrayList<MoveScore> moveScores, long timeLeft, int maxDepth) {
        int maxScore = Integer.MIN_VALUE;
        boolean timeFinished = false;
        long searchStart = System.nanoTime();
        int score = 0;
        float progress = 0;
        ArrayList<MoveScore> currentMovesScores = new ArrayList<>();
        for (int i = 0; i < moveScores.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.move(moves.get(moveScores.get(i).moveIndex));
            score = miniMax(chessBoardAfterMove, maxScore,
                    Integer.MAX_VALUE, maxDepth - 1, false);
            currentMovesScores.add(new MoveScore(score, moveScores.get(i).moveIndex));
            progress = (float) i / moveScores.size();
            if ((System.nanoTime() - searchStart) > timeLeft) {
                if (progress < 0.75) {
                    timeFinished = true;
                    break;
                }

            }
        }
        if (timeFinished) currentMovesScores.clear();
        return currentMovesScores;
    }


    public int miniMax(ChessBoard chessBoard, int alpha, int beta, float depth, boolean maxing) {
        Piece.Color toPlayColor = chessBoard.moves.getToPlayNow();

        LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard, Game.moveGenerator,
                toPlayColor);
        Game.GameStatus gameStatus = chessBoard.checkStatus(toPlayLegalMoves);
        if (depth == 0) {
            evaluations++;
            if (gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
                    || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK) {
                return getGameFinishedScoreFor(gameStatus, maxingPlayer);
            } else {
                return getScoreFor(chessBoard, maxingPlayer);
            }
        }

        if (gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
                || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK) {
            evaluations++;

            return getGameFinishedScoreFor(gameStatus, maxingPlayer);
        }


        if (maxing) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < toPlayLegalMoves.size(); i++) {
                ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                chessBoardAfterMove.move(toPlayLegalMoves.get(i));
                int score = miniMax(chessBoardAfterMove, alpha, beta,
                        depth - 1, !maxing);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);

                if (alpha >= beta) {
                    break;
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < toPlayLegalMoves.size(); i++) {
                ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                chessBoardAfterMove.move(toPlayLegalMoves.get(i));
                int score = miniMax(chessBoardAfterMove, alpha, beta,
                        depth - 1, !maxing);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);

                if (alpha >= beta) {
                    break;
                }
            }
            return minScore;
        }

    }


    public int miniMax(ChessBoard chessBoard, int depth, boolean maxing) {
        Piece.Color toPlayColor = chessBoard.moves.getToPlayNow();

        LegalMoves toPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(chessBoard, Game.moveGenerator,
                toPlayColor);
        Game.GameStatus gameStatus = chessBoard.checkStatus(toPlayLegalMoves);
        if (depth == 0) {
            evaluations++;
            if (gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
                    || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK) {
                return getGameFinishedScoreFor(gameStatus, maxingPlayer);
            } else {
                return getScoreFor(chessBoard, maxingPlayer);
            }
        }

        if (gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
                || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK) {
            evaluations++;

            return getGameFinishedScoreFor(gameStatus, maxingPlayer);
        }


        if (maxing) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < toPlayLegalMoves.size(); i++) {
                ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                chessBoardAfterMove.move(toPlayLegalMoves.get(i));
                int score = miniMax(chessBoardAfterMove,
                        depth - 1, !maxing);
                maxScore = Math.max(maxScore, score);

            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < toPlayLegalMoves.size(); i++) {
                ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                chessBoardAfterMove.move(toPlayLegalMoves.get(i));
                int score = miniMax(chessBoardAfterMove,
                        depth - 1, !maxing);
                minScore = Math.min(minScore, score);

            }
            return minScore;
        }
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

    int getGameFinishedWhiteScore(Game.GameStatus gameStatus) {
        int value = 0;
        switch (gameStatus) {
            case FINISHED_WIN_WHITE:
                value = Integer.MAX_VALUE;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MIN_VALUE;
                break;
            case FINISHED_DRAW:
                value = 0;
                break;
        }
        return value;
    }

    int getGameFinishedBlackScore(Game.GameStatus gameStatus) {
        int value = 0;
        switch (gameStatus) {
            case FINISHED_WIN_WHITE:
                value = Integer.MIN_VALUE;
                break;
            case FINISHED_WIN_BLACK:
                value = Integer.MAX_VALUE;
                break;
            case FINISHED_DRAW:
                value = 0;
                break;
        }
        return value;
    }

    private int getGameFinishedScoreFor(Game.GameStatus gameStatus, Piece.Color maxingPlayer) {
        if (maxingPlayer == Piece.Color.WHITE) {
            return getGameFinishedWhiteScore(gameStatus);
        } else {
            return getGameFinishedBlackScore(gameStatus);
        }
    }

    int getWhiteScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.Color.WHITE);
        return value;
    }


    int getBlackScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.Color.BLACK);
        return value;
    }

    int getScoreFor(ChessBoard chessBoard, Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhiteScore(chessBoard) - getBlackScore(chessBoard);
        } else {
            return getBlackScore(chessBoard) - getWhiteScore(chessBoard);
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
            if (piece.getType() == Piece.Type.KING) continue;
            value += getPositionalValue(piece);

        }
        return value;
    }

    private int getPositionalValue(Piece piece) {
        int value = 0;
        int piecePositionOnTable = ChessBoard.OUT;
        int file = ChessBoard.GetFile(piece.getPosition());
        int rank = ChessBoard.GetRank(piece.getPosition());
        if (piece.getColor() == Piece.Color.WHITE) {
            rank = 7 - rank;
        } else {
            file = 7 - file;
        }
        piecePositionOnTable = ChessBoard.GetPosition(file, rank);
        switch (piece.getType()) {
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
                if (isEndGame(chessBoard)) {
                    value += Piece.KING_VALUE + KING_END_GAME_SQUARES_TABLE[piecePositionOnTable];

                } else {
                    value += Piece.KING_VALUE + KING_MIDDLE_GAME_SQUARES_TABLE[piecePositionOnTable];
                }
                break;
        }
        return value;
    }

    boolean isEndGame(ChessBoard chessBoard) {
        boolean endGame = false;
        int whitePiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.Color.WHITE);
        int blackPiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.Color.BLACK);
        if (Math.abs(whitePiecesValue - blackPiecesValue) >= Piece.QUEEN_VALUE) {
            if (whitePiecesValue < Piece.QUEEN_VALUE) endGame = true;
            if (blackPiecesValue < Piece.QUEEN_VALUE) endGame = true;
        }

        return endGame;

    }

    private int getInitialPiecesValue() {
        return Piece.KING_VALUE + Piece.QUEEN_VALUE + Piece.ROOK_VALUE + Piece.BISHOP_VALUE +
                Piece.KNIGHT_VALUE + Piece.PAWN_VALUE;
    }

    public static int[] PAWN_SQUARES_TABLE = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
    };
    public static int[] KNIGHT_SQUARES_TABLE = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50
    };
    public static int[] BISHOP_SQUARES_TABLE = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20
    };
    public static int[] ROOK_SQUARES_TABLE = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0
    };

    public static int[] QUEEN_SQUARES_TABLE = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20
    };

    public static int[] KING_MIDDLE_GAME_SQUARES_TABLE = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
    };
    public static int[] KING_END_GAME_SQUARES_TABLE = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };


}

class MoveScore implements Comparable<MoveScore> {
    private int score;
    public int moveIndex;

    public MoveScore(int score, int moveIndex) {
        this.score = score;
        this.moveIndex = moveIndex;
    }

    @Override
    public int compareTo(MoveScore other) {
        return other.score - this.score;
    }
}

