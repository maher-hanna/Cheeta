package com.maherhanna.cheeta;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Random;

class Game {
    public static final String DEBUG = "Cheeta_Debug";
    private Drawing drawing;
    private ChessBoard chessBoard;
    private ComputerAi computerAi;
    public int gameType;
    private int computerPlayDelayMilli;
    public boolean paused;
    Piece.Color currentPlayer;
    public Piece.Color bottomScreenPlayerColor;
    private boolean gameFinished = false;


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

    public void resume() {
        if (gameType == COMPUTER_HUMAN) {
            if (currentPlayer == bottomScreenPlayerColor) {
                drawing.waitHumanToPlay();
                return;
            }
        }
        playComputer(currentPlayer);


    }


    public GameStatus checkGameFinished(Piece.Color lastPlayed) {
        GameStatus gameStatus = GameStatus.NOT_FINISHED;
        Piece.Color currentToPlayColor = lastPlayed.getOpposite();
        LegalMoves currentToPlayLegalMoves = chessBoard.getLegalMovesFor(currentToPlayColor);

        if (chessBoard.isKingInCheck(currentToPlayColor)) {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {
                //win
                if (lastPlayed == Piece.Color.WHITE) {
                    gameStatus = GameStatus.FINISHED_WIN_WHITE;
                } else {
                    gameStatus = GameStatus.FINISHED_WIN_BLACK;

                }
            }
        } else {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {

                //draw stalemate
                gameStatus = GameStatus.FINISHED_DRAW;
            }
            if (insufficientMaterial()) {
                gameStatus = GameStatus.FINISHED_DRAW;
            }

        }

        return gameStatus;

    }

    private boolean insufficientMaterial() {
        ArrayList<Integer> whitePieces = chessBoard.getWhitePositions();
        ArrayList<Integer> blackPieces = chessBoard.getBlackPositions();
        int whitePiecesNumber = whitePieces.size();
        int blackPiecesNumber = blackPieces.size();

        // tow kings remaining
        if (whitePiecesNumber + blackPiecesNumber == 2) {
            return true;
        }

        if (whitePiecesNumber + blackPiecesNumber == 3) {

            Piece.Type remainingPieceType = Piece.Type.PAWN;
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (chessBoard.getPieceAt(i) != null && chessBoard.getPieceAt(i).type != Piece.Type.KING) {
                    remainingPieceType = chessBoard.getPieceType(i);
                }
            }

            // tow kings and a bishop or knight
            if(remainingPieceType == Piece.Type.BISHOP || remainingPieceType == Piece.Type.KNIGHT){
                return true;
            }

        }

        if (whitePiecesNumber + blackPiecesNumber == 4) {

            ArrayList<Piece> remainingPieces = new ArrayList<>();
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (chessBoard.getPieceAt(i) != null && chessBoard.getPieceAt(i).type != Piece.Type.KING) {
                    remainingPieces.add(chessBoard.getPieceAt(i));
                }
            }
            Piece firstPiece = remainingPieces.get(0);
            Piece secondPiece = remainingPieces.get(1);

            // tow king and tow bishops of the same square color
            if(firstPiece.type == Piece.Type.BISHOP && secondPiece.type == Piece.Type.BISHOP){
                if(firstPiece.color != secondPiece.color){
                    if(ChessBoard.GetSquareColor(firstPiece.position) ==
                            ChessBoard.GetSquareColor(secondPiece.position)){
                        return true;
                    }
                }
            }



        }



        return false;
    }


    public void humanPlayed(Move humanMove) {
        humanMove = chessBoard.getLegalMovesFor(bottomScreenPlayerColor).getMove(humanMove);
        chessBoard.movePiece(humanMove);
        drawing.drawAllPieces(humanMove);
        currentPlayer = bottomScreenPlayerColor.getOpposite();
        chessBoard.updateLegalMovesFor(bottomScreenPlayerColor, false);
        Piece.Color opponentColor = currentPlayer;
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));

        GameStatus gameStatus = checkGameFinished(bottomScreenPlayerColor);
        if (gameStatus == GameStatus.NOT_FINISHED) {
            playComputer(opponentColor);


        } else {
            setGameFinished();
            drawing.finishGame(gameStatus, gameType);
            return;

        }

    }

    public void computerPlayed(Move computerMove, Piece.Color color) {
        drawing.drawAllPieces(computerMove);
        currentPlayer = color.getOpposite();
        GameStatus gameStatus = checkGameFinished(color);
        if (gameStatus == GameStatus.NOT_FINISHED) {
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
        } else {
            //game finished
            setGameFinished();
            drawing.finishGame(gameStatus, gameType);

            return;
        }


    }

    public void playComputer(Piece.Color color) {
        if (paused) return;
        Move computerMove = computerAi.getMove(chessBoard, color);
        chessBoard.movePiece(computerMove);
        chessBoard.updateLegalMovesFor(color, false);
        Piece.Color opponentColor = color.getOpposite();
        chessBoard.updateLegalMovesFor(opponentColor, chessBoard.isKingInCheck(opponentColor));
        computerPlayed(computerMove, color);
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished() {
        gameFinished = true;
    }

    public enum GameStatus {NOT_FINISHED, FINISHED_DRAW, FINISHED_WIN_WHITE, FINISHED_WIN_BLACK}

    ;


}