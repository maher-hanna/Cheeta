package com.maherhanna.cheeta;

import java.util.Timer;
import java.util.TimerTask;

public class HumanPlayer extends Player{
    public HumanPlayer(Piece.Color color, ChessBoard chessBoard,Player opponent) {
        super(color, chessBoard,opponent);
    }

    @Override
    public void play() {
        super.play();
    }


    @Override
    public void movePice(int fromSquare, int toSquare) {
        super.movePice(fromSquare, toSquare);



    }
}
