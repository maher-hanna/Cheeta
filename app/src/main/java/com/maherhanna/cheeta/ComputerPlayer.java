package com.maherhanna.cheeta;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class ComputerPlayer extends Player {
    public int playDelay;

    public ComputerPlayer(Piece.Color color, ChessBoard chessBoard, Player opponent) {
        super(color, chessBoard, opponent);
        //delay in milliseconds before the computer plays
        playDelay = 100;
    }


    @Override
    public void play() {
        super.play();
        Random random = new Random();
        int numPieces = pieces.size();
        int numOfLegalMoves = 0;
        int randomPieceIndex = 0;
        int randomPiecePosition = 0;
        do {
            randomPieceIndex = random.nextInt(numPieces);
            randomPiecePosition = pieces.get(randomPieceIndex).getPosition();
            numOfLegalMoves = legalMoves.get(randomPiecePosition).size();

        } while (numOfLegalMoves == 0);

        int randomLegalMove = random.nextInt(numOfLegalMoves);
        int randomMove = legalMoves.get(randomPiecePosition).get(randomLegalMove);
        movePice(randomPiecePosition, randomMove);
        chessBoard.drawing.drawAllPieces();

        if(opponent instanceof ComputerPlayer){
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    opponent.play();
                }
            }, playDelay);
        }
        else {
            opponent.play();
        }

    }
}
