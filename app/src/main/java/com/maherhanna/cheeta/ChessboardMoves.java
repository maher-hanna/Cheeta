package com.maherhanna.cheeta;

import java.util.ArrayList;

public class ChessboardMoves {
    private final ArrayList<Move> moves;
    public int initialEnPassantTarget = ChessBoard.NO_SQUARE;

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

    public Move getLastMove(){
        return moves.get(moves.size() -1);
    }
    public Move getLastMoveFor(Piece.Color color){
        Move lastMove;
        lastMove = getLastMove();
        if(lastMove.getColor() == color){
            return lastMove;
        } else {
            return moves.get(moves.size() - 2);
        }
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
}
