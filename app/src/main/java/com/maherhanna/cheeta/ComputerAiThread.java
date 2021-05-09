package com.maherhanna.cheeta;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class ComputerAiThread extends AsyncTask<ChessBoard, Void, Move> {
    private static int LOSE_SCORE = -1000000;
    private static int WIN_SCORE = 1000000;
    private boolean foundCheckMate;
    long evaluations;
    int maxingPlayer;

    @Override
    protected Move doInBackground(ChessBoard... chessBoards) {
        long startTime = System.nanoTime();
        //convert maximum search time from seconds to nano seconds
        long maxSearchTime = Game.COMPUTER_MAX_SEARCH_TIME * 1000000000;
        foundCheckMate = false;
        evaluations = 0;
        ChessBoard startChessBoard = new ChessBoard(chessBoards[0]);
        maxingPlayer = startChessBoard.toPlayColor;

        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(startChessBoard,
                maxingPlayer);
        toPlayLegalMoves = sortMoves(toPlayLegalMoves);


        int maxDepth = 0;
        long timeLeft;
        int moveIndex = 0;
        do {
            long previousEvaluations = evaluations;
            evaluations = 0;
            timeLeft = (startTime + maxSearchTime) - System.nanoTime();
            maxDepth++;
            int currentDepthMoveIndex = search(startChessBoard, toPlayLegalMoves, timeLeft, maxDepth);
            if (currentDepthMoveIndex == ChessBoard.NO_SQUARE) {
                maxDepth--;
                evaluations = previousEvaluations;
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
//        maxScore = LOSE_SCORE;
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
        return toPlayLegalMoves.get(moveIndex);

    }

    public int search(ChessBoard chessBoard, LegalMoves moves, long timeLeft, int maxDepth) {
        int maxScore = LOSE_SCORE;
        boolean timeFinished = false;
        long searchStart = System.nanoTime();
        int score = 0;
        int maxIndex = ChessBoard.NO_SQUARE;
        int currentMaxIndex = 0;
        float progress = 0;

        int alpha = LOSE_SCORE;
        int beta = WIN_SCORE;


        for (int i = 0; i < moves.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.move(moves.get(i));
            score = -negaMax(chessBoardAfterMove, -beta,
                    -alpha, maxDepth - 1, false);

            if (score >= beta) {
                currentMaxIndex = i;
                break;

            } else if (score > alpha) {
                alpha = score;
                currentMaxIndex = i;

            }


            if (((System.nanoTime() - searchStart) > timeLeft)) {
                timeFinished = true;
                break;

            }
        }
        if (!timeFinished) {
            maxIndex = currentMaxIndex;
        }
        return maxIndex;
    }

    private int quiescence(ChessBoard chessBoard, int alpha, int beta, boolean maxing) {
        evaluations++;
        int toPlayColor = chessBoard.toPlayColor;

        int eval = getScoreFor(chessBoard, toPlayColor);
        if (eval >= beta) {
            return beta;
        } else if (alpha < eval) {
            alpha = eval;
        }


        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(chessBoard,
                toPlayColor);
        Game.GameStatus gameStatus = chessBoard.checkStatus(toPlayLegalMoves);
        if (isGameFinished(gameStatus)) {
            return getScoreFor(chessBoard, toPlayColor, gameStatus);
        } else {
            toPlayLegalMoves.removeNonTake();
        }


        if (toPlayLegalMoves.size() == 0) {
            evaluations++;

            return getScoreFor(chessBoard, toPlayColor);

        }


        toPlayLegalMoves = sortMoves(toPlayLegalMoves);


        for (int i = 0; i < toPlayLegalMoves.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.move(toPlayLegalMoves.get(i));
            int score = -quiescence(chessBoardAfterMove, -beta, -alpha,
                    !maxing);
            if (score >= beta) {
                return beta;
            } else if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;


    }

    public int negaMax(ChessBoard chessBoard, int alpha, int beta, float depth, boolean maxing) {
        int toPlayColor = chessBoard.toPlayColor;

        LegalMoves toPlayLegalMoves = Game.moveGenerator.getLegalMovesFor(chessBoard,
                toPlayColor);
        Game.GameStatus gameStatus = chessBoard.checkStatus(toPlayLegalMoves);
        if (depth == 0) {
            evaluations++;
            if (isGameFinished(gameStatus)) {
                return getGameFinishedScoreFor(gameStatus, toPlayColor);
            } else {
                return quiescence(chessBoard, alpha, beta, maxing);
            }
        }

        if (isGameFinished(gameStatus)) {
            evaluations++;

            return getGameFinishedScoreFor(gameStatus, toPlayColor);
        }

        toPlayLegalMoves = sortMoves(toPlayLegalMoves);

        evaluations++;


        int maxScore = LOSE_SCORE;
        for (int i = 0; i < toPlayLegalMoves.size(); i++) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            chessBoardAfterMove.move(toPlayLegalMoves.get(i));
            int score = -negaMax(chessBoardAfterMove, -beta, -alpha,
                    depth - 1, !maxing);
            maxScore = Math.max(maxScore, score);
            alpha = Math.max(alpha, score);

            if (score >= beta) {
                return beta;
            } else if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;

    }

    public ArrayList<MoveScore> scoreMoves(LegalMoves moves) {
        ArrayList<MoveScore> scores = new ArrayList<>();
        int currentScore = 0;
        Move currentMove;
        for (int i = 0; i < moves.size(); i++) {
            currentMove = moves.get(i);
            currentScore = 0;
            if (currentMove.isTake()) {
                int victim = currentMove.getTakenPieceType();
                int attacker = currentMove.getPieceType();
                currentScore = mvv_lva[(attacker * 6) + victim];

            }

            scores.add(new MoveScore(currentScore, i));
        }
        return scores;
    }


    public LegalMoves sortMoves(LegalMoves moves) {
        ArrayList<MoveScore> scores = scoreMoves(moves);
        Collections.sort(scores);
        LegalMoves sortedMoves = new LegalMoves();
        for (int i = 0; i < moves.size(); i++) {
            sortedMoves.add(moves.get(scores.get(i).moveIndex));
        }
        return sortedMoves;
    }

    private boolean isGameFinished(Game.GameStatus gameStatus) {
        return gameStatus != Game.GameStatus.NOT_FINISHED;
    }

    int getGameFinishedWhiteScore(Game.GameStatus gameStatus) {
        int value = 0;
        switch (gameStatus) {
            case FINISHED_WIN_WHITE:
                value = WIN_SCORE;
                break;
            case FINISHED_WIN_BLACK:
                value = LOSE_SCORE;
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
                value = LOSE_SCORE;
                break;
            case FINISHED_WIN_BLACK:
                value = WIN_SCORE;
                break;
            case FINISHED_DRAW:
                value = 0;
                break;
        }
        return value;
    }

    private int getGameFinishedScoreFor(Game.GameStatus gameStatus, int maxingPlayer) {
        if (maxingPlayer == Piece.WHITE) {
            return getGameFinishedWhiteScore(gameStatus);
        } else {
            return getGameFinishedBlackScore(gameStatus);
        }
    }

    int getWhiteScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.WHITE);
        return value;
    }


    int getBlackScore(ChessBoard chessBoard) {
        int value = getPiecesValueFor(chessBoard, Piece.BLACK);
        return value;
    }

    int getScoreFor(ChessBoard chessBoard, int color) {
        if (color == Piece.WHITE) {
            return getWhiteScore(chessBoard) - getBlackScore(chessBoard);
        } else {
            return getBlackScore(chessBoard) - getWhiteScore(chessBoard);
        }
    }

    int getScoreFor(ChessBoard chessBoard, int color, Game.GameStatus gameStatus) {
        if (isGameFinished(gameStatus)) {
            return getGameFinishedScoreFor(gameStatus, color);
        } else {
            if (color == Piece.WHITE) {
                return getWhiteScore(chessBoard) - getBlackScore(chessBoard);
            } else {
                return getBlackScore(chessBoard) - getWhiteScore(chessBoard);
            }
        }

    }

    public int getPiecesValueFor(ChessBoard chessboard, int color) {
        int value = 0;
        ArrayList<Integer> squares;
        if (color == Piece.WHITE) {
            squares = Game.moveGenerator.getWhitePositions(chessboard);
        } else {
            squares = Game.moveGenerator.getBlackPositions(chessboard);
        }

        for (int square : squares) {
            value += getPositionalValue(chessboard, square);

        }
        return value;
    }

    public int getPiecesValueMinusKingFor(ChessBoard chessboard, int color) {
        int value = 0;
        ArrayList<Integer> squares;
        if (color == Piece.WHITE) {
            squares = Game.moveGenerator.getWhitePositions(chessboard);
        } else {
            squares = Game.moveGenerator.getBlackPositions(chessboard);
        }

        for (int square : squares) {
            if (chessboard.pieceType(square) == Piece.KING) continue;
            value += getPositionalValue(chessboard, square);

        }
        return value;
    }

    private int getPositionalValue(ChessBoard chessBoard, int square) {
        int value = 0;
        int piecePositionOnTable = ChessBoard.OUT;
        int file = ChessBoard.GetFile(square);
        int rank = ChessBoard.GetRank(square);
        if (chessBoard.pieceColor(square) == Piece.WHITE) {
            rank = 7 - rank;
        } else {
            file = 7 - file;
        }
        piecePositionOnTable = ChessBoard.GetPosition(file, rank);
        switch (chessBoard.pieceType(square)) {
            case Piece.PAWN:
                value += Piece.PAWN_VALUE + PAWN_SQUARES_TABLE[piecePositionOnTable];
                break;
            case Piece.ROOK:
                value += Piece.ROOK_VALUE + ROOK_SQUARES_TABLE[piecePositionOnTable];
                break;
            case Piece.KNIGHT:
                value += Piece.KNIGHT_VALUE + KNIGHT_SQUARES_TABLE[piecePositionOnTable];
                break;
            case Piece.BISHOP:
                value += Piece.BISHOP_VALUE + BISHOP_SQUARES_TABLE[piecePositionOnTable];
                break;
            case Piece.QUEEN:
                value += Piece.QUEEN_VALUE + QUEEN_SQUARES_TABLE[piecePositionOnTable];
                break;
            case Piece.KING:
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
        int whitePiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.WHITE);
        int blackPiecesValue = getPiecesValueMinusKingFor(chessBoard, Piece.BLACK);
        if (Math.abs(whitePiecesValue - blackPiecesValue) >= Piece.QUEEN_VALUE) {
            if (whitePiecesValue < Piece.QUEEN_VALUE) endGame = true;
            if (blackPiecesValue < Piece.QUEEN_VALUE) endGame = true;
        }

        return endGame;

    }

    private int getPieceValue(int pieceType) {
        int value = 0;
        switch (pieceType) {
            case Piece.PAWN:
                value += Piece.PAWN_VALUE;
                break;
            case Piece.ROOK:
                value += Piece.ROOK_VALUE;
                break;
            case Piece.KNIGHT:
                value += Piece.KNIGHT_VALUE;
                break;
            case Piece.BISHOP:
                value += Piece.BISHOP_VALUE;
                break;
            case Piece.QUEEN:
                value += Piece.QUEEN_VALUE;
                break;
            case Piece.KING:
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


    private static int[] mvv_lva = {
            105, 205, 305, 405, 505, 605,
            104, 204, 304, 404, 504, 604,
            103, 203, 303, 403, 503, 603,
            102, 202, 302, 402, 502, 602,
            101, 201, 301, 401, 501, 601,
            100, 200, 300, 400, 500, 600,
    };
}




