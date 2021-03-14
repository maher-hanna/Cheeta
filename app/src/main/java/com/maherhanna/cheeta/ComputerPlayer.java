package com.maherhanna.cheeta;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class ComputerPlayer extends Player {
    public int playDelay;
    ComputerAi computerAi;

    public ComputerPlayer(Square.Color color, ChessBoard chessBoard, Player opponent, int playDelay) {
        super(color, chessBoard, opponent);
        //delay in milliseconds before the computer plays
        this.playDelay = playDelay;
        computerAi = new ComputerAi();

    }


    @Override
    public void play() {
        super.play();
        Move move = computerAi.getMove(pieces);
        movePiece(move.from, move.to);

        chessBoard.drawing.clearBoard();
        chessBoard.drawing.drawMoveHighlight(move);
        chessBoard.drawing.drawAllPieces();
        chessBoard.drawing.show();


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
