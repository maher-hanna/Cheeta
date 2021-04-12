package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class LegalMoves {
    private final HashMap<Integer, ArrayList<Move>> legalMoves;
    public LegalMoves(){
        legalMoves = new HashMap<>();
    }
    public LegalMoves(LegalMoves copy){
        this.legalMoves = new HashMap<>(copy.legalMoves);
    }

    public void addMovesFor(int position, ArrayList<Move> moves){
        legalMoves.put(position,moves);
    }

    public void addMoveFor(int position, Move move){
        legalMoves.get(position).add(move);
    }

    public ArrayList<Integer> getLegalTargetsFor(int position)
    {
        ArrayList<Integer> targetSquares = new ArrayList<>();
        for(Move move: legalMoves.get(position)){
            targetSquares.add(move.getTo());
        }
        return targetSquares;
    }

    public ArrayList<Move> getLegalMovesFor(int position){
        return legalMoves.get(position);
    }

    public boolean contains(int position){
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            for(Move move: pieceLegalMoves){
                if(move.getTo() == position){
                    return true;

                }
            }

        }
        return false;
    }
    public ArrayList<Move> getAllLegalMoves(){
        ArrayList<Move> allLegalMoves = new ArrayList<>();
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            allLegalMoves.addAll(pieceLegalMoves);
        }
        return allLegalMoves;
    }

    public boolean canMove(int from, int to){
        for(Move move: legalMoves.get(from)){
            if(move.getTo() == to){
                return true;
            }
        }
        return false;
    }


    public int getNumberOfMoves(){
        int numberOfMoves = 0;
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            numberOfMoves += pieceLegalMoves.size();
        }
        return numberOfMoves;
    }


    public Move getMove(Move basicMove) {
        Move legalMove = new Move(basicMove);
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            for(Move move: pieceLegalMoves){
                if(move.getFrom() == basicMove.getFrom() &&
                move.getTo() == basicMove.getTo()){
                    legalMove = move;

                }
            }

        }
        return legalMove;
    }
}
