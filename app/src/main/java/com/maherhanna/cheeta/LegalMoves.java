package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class LegalMoves {
    private HashMap<Integer, ArrayList<Move>> legalMoves;
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
            targetSquares.add(move.to);
        }
        return targetSquares;
    }

    public ArrayList<Move> getLegalMovesFor(int position){
        return legalMoves.get(position);
    }

    public boolean contains(int position){
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            for(Move move: pieceLegalMoves){
                if(move.to == position){
                    return true;

                }
            }

        }
        return false;
    }

    public boolean canMove(int from, int to){
        for(Move move: legalMoves.get(from)){
            if(move.to == to){
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

    public Move getMoveForHuman(Move humanMove) {
        Move legalMove = new Move(humanMove);
        for(ArrayList<Move> pieceLegalMoves: legalMoves.values()){
            for(Move move: pieceLegalMoves){
                if(move.from == humanMove.from &&
                move.to == humanMove.to){
                    legalMove = move;

                }
            }

        }
        return legalMove;
    }
}
