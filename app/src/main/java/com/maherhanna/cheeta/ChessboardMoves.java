package com.maherhanna.cheeta;

import java.util.ArrayList;

public class ChessboardMoves {
    private ArrayList<Move> moves;

    public ChessboardMoves(){
        moves = new ArrayList<>();
    }

    public ChessboardMoves(ChessboardMoves copy){
        this.moves = (ArrayList<Move>)copy.moves.clone();
    }

    public void add(Move move) {
        this.moves.add(move);
    }

    public boolean hasPieceMoved(int initialPosition){
        boolean hasMoved = false;
        for(Move move : moves){
            if(move.getFrom() == initialPosition){
                hasMoved = true;
                break;
            }
        }
        return hasMoved;
    }


}
