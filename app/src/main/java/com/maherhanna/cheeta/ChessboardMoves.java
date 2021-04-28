package com.maherhanna.cheeta;

import java.util.ArrayList;

public class ChessboardMoves {
    private final ArrayList<Move> moves;

    public ChessboardMoves(){
        moves = new ArrayList<>();
    }

    public ChessboardMoves(ChessboardMoves copy){
        this.moves = new ArrayList<Move>(copy.moves);
    }

    public void add(Move move) {
        this.moves.add(move);
    }
    public void removeLastMove(){
        moves.remove(moves.size() -1);
    }


    public Move getLastMove(){
        if(moves.size() == 0){return null;}
        return moves.get(moves.size() -1);
    }

    public Piece.Color getToPlayNow(){
        return getLastPlayed().getOpposite();
    }
    public Piece.Color getLastPlayed(){
        if(moves.size() == 0){
            return Piece.Color.BLACK;
        } else{
            return getLastMove().getColor();

        }
    }


    public boolean notEmpty() {
        return moves.size() != 0;
    }
    public int size(){return moves.size();}
    public Move get(int index){return moves.get(index);}
}
