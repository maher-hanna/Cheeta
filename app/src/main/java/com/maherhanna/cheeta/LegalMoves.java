package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.HashMap;

public class LegalMoves {
    private ArrayList<Move> legalMoves;
    public LegalMoves(LegalMoves copy){
        this.legalMoves = new ArrayList<Move>(copy.legalMoves);
    }
    public LegalMoves(){
        legalMoves = new ArrayList<>();
    }

    public ArrayList<Integer> getLegalTargetsFor(int position)
    {
        ArrayList<Integer> targetSquares = new ArrayList<>();
        for(Move move: legalMoves){
            if(move.getFrom() == position){
                targetSquares.add(move.getTo());
            }
        }
        return targetSquares;
    }




    public boolean canMove(int from, int to){
        ArrayList<Integer> targetsForFrom = getLegalTargetsFor(from);
        if(targetsForFrom.isEmpty()) return false;
        for(int target : targetsForFrom){
            if(target == to){
                return true;
            }
        }
        return false;
    }


    public int size(){
        return legalMoves.size();
    }
    public Move get(int index){
        return legalMoves.get(index);
    }

    public void add(Move newMove){
        legalMoves.add(newMove);
    }

    public void addAll(ArrayList<Move> moves){
        legalMoves.addAll(moves);
    }


    public Move getMove(Move basicMove) {
        Move legalMove = new Move(basicMove);
        for(Move move: legalMoves){
            if((move.getFrom() == basicMove.getFrom()) &&
                    (move.getTo() == basicMove.getTo())){
                legalMove = new Move(move);

            }
        }
        return legalMove;
    }
}
