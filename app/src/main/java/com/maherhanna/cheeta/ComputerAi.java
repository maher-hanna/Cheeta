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
    ChessBoard startChessBoard;
    Piece.Color maxingPlayer;
    private Move move;
    private final long maxSearchTime;
    private int evaluations;
    private boolean foundCheckMate;

    public MyRunnable(ChessBoard startChessBoard, Piece.Color maxingPlayer, float maxSearchTime) {
        this.startChessBoard = startChessBoard;
        this.maxingPlayer = maxingPlayer;
        //convert seconds to nano seconds
        this.maxSearchTime = (long) (maxSearchTime * 1000000000);
        this.evaluations = 0;
        foundCheckMate = false;

    }

    public Move getMove() {
        return this.move;
    }


    @Override
    public void run() {
        long startTime = System.nanoTime();



        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(startChessBoard,
                maxingPlayer);
        ArrayList<MoveScore> moveScores = sortMoves(toPlayLegalMoves);
        Collections.sort(moveScores);

        int maxDepth = 0;
        boolean timeFinished = false;
        long timeLeft;
        int moveIndex = 0;
        boolean foundCheckMate = false;
        do {
            timeLeft = (startTime + maxSearchTime) - System.nanoTime();
            maxDepth++;
            int currentDepthMoveIndex = search(startChessBoard, toPlayLegalMoves, moveScores, timeLeft, maxDepth);
            if (currentDepthMoveIndex == ChessBoard.NO_SQUARE) {
                break;
            } else {
                moveIndex = currentDepthMoveIndex;

            }

        } while (!foundCheckMate);


        long duration = System.nanoTime() - startTime;
        duration = duration / 1000; // convert to milli second
        Log.d(Game.DEBUG, "alpha beta evaluations: " + evaluations + " move " +
                moveIndex);
        Log.d(Game.DEBUG, "Duration: " + (float) duration / 1000000 + " depth " + maxDepth);

//        evaluations = 0;
//        int maxIndex = 0;
//        maxScore = Integer.MIN_VALUE;
//        startTime = System.nanoTime();
//        for (int i = 0; i < toPlayLegalMoves.size(); i++) {
//            ChessBoard chessBoardAfterMove = new ChessBoard(startChessBoard);
//            chessBoardAfterMove.move(toPlayLegalMoves.get(i));
//            int score = miniMax(chessBoardAfterMove, maxDepth,false);
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
//
        this.move = toPlayLegalMoves.get(moveIndex);

    }


    public int search(ChessBoard chessBoard, LegalMoves moves, ArrayList<MoveScore> moveScores, long timeLeft, int maxDepth) {
        int maxScore = Integer.MIN_VALUE;
        boolean timeFinished = false;
        long searchStart = System.nanoTime();
        int score = 0;
        int maxIndex = ChessBoard.NO_SQUARE;
        int currentMaxIndex = 0;
        float progress = 0;
        for (int i = 0; i < moves.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.move(moves.get(moveScores.get(i).moveIndex));
            score = miniMax(chessBoardAfterMove, maxScore,
                    Integer.MAX_VALUE, maxDepth - 1, false);
            if (score > maxScore) {
                maxScore = score;
                currentMaxIndex = moveScores.get(i).moveIndex;
            }
            if (score == Integer.MAX_VALUE) {
                foundCheckMate = true;
                break;
            }
            progress = (float) i / moves.size();
            if ((System.nanoTime() - searchStart) > timeLeft) {
                if (progress < 0.75) {
                    timeFinished = true;
                    break;
                }

            }
        }
        if (!timeFinished) maxIndex = currentMaxIndex;
        return maxIndex;
    }


    public int miniMax(ChessBoard chessBoard, int alpha, int beta, float depth, boolean maxing) {
        Piece.Color toPlayColor = chessBoard.moves.getToPlayNow();

        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(chessBoard,
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

        ArrayList<MoveScore> scores = sortMoves(toPlayLegalMoves);
        Collections.sort(scores);

        if (maxing) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < toPlayLegalMoves.size(); i++) {
                ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
                chessBoardAfterMove.move(toPlayLegalMoves.get(scores.get(i).moveIndex));
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
                chessBoardAfterMove.move(toPlayLegalMoves.get(scores.get(i).moveIndex));
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

        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(chessBoard,
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


    public ArrayList<MoveScore> sortMoves(LegalMoves moves) {
        ArrayList<MoveScore> scores = new ArrayList<>();
        int currentScore = 0;
        Move currentMove;
        for (int i = 0; i < moves.size(); i++) {
            currentMove = moves.get(i);
            currentScore = 0;
            if (currentMove.isTake()) {
                currentScore += 1;
                int takenPieceValue = getPieceValue(currentMove.getTakenPieceType());
                int movedPieceValue = getPieceValue(currentMove.getPieceType());
                if(takenPieceValue > movedPieceValue){
                    currentScore += 2;
                }
                if(currentMove.getTakenPieceType() == Piece.Type.KING) currentScore += 10;

            }
            if (currentMove.isPromote()) {
                currentScore += 3;
            }

            scores.add(new MoveScore(currentScore, i));
        }
        return scores;
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
            squares = Game.moveGenerator.getWhitePositions(chessboard);
        } else {
            squares = Game.moveGenerator.getBlackPositions(chessboard);
        }

        for (int square : squares) {
            Piece piece = chessboard.getPieceAt(square);
            value += getPositionalValue(chessboard, piece);

        }
        return value;
    }

    public int getPiecesValueMinusKingFor(ChessBoard chessboard, Piece.Color color) {
        int value = 0;
        ArrayList<Integer> squares;
        if (color == Piece.Color.WHITE) {
            squares = Game.moveGenerator.getWhitePositions(chessboard);
        } else {
            squares = Game.moveGenerator.getBlackPositions(chessboard);
        }

        for (int square : squares) {
            Piece piece = chessboard.getPieceAt(square);
            if (piece.getType() == Piece.Type.KING) continue;
            value += getPositionalValue(chessboard, piece);

        }
        return value;
    }

    private int getPositionalValue(ChessBoard chessBoard, Piece piece) {
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

    private int getPieceValue(Piece.Type pieceType) {
        int value = 0;
        switch (pieceType) {
            case PAWN:
                value += Piece.PAWN_VALUE;
                break;
            case ROOK:
                value += Piece.ROOK_VALUE;
                break;
            case KNIGHT:
                value += Piece.KNIGHT_VALUE;
                break;
            case BISHOP:
                value += Piece.BISHOP_VALUE;
                break;
            case QUEEN:
                value += Piece.QUEEN_VALUE;
                break;
            case KING:
                value += Piece.KING_VALUE;
                break;
        }
        return value;
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

