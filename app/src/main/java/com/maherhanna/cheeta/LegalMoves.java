package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class LegalMoves {
    private HashMap<Integer, ArrayList<Integer>> legalMoves;
    public LegalMoves(){
        legalMoves = new HashMap<>();
    }
    public LegalMoves(LegalMoves copy){
        this.legalMoves = new HashMap<>(copy.legalMoves);
    }

    public void addMovesFor(int position, ArrayList<Integer> moves){
        legalMoves.put(position,moves);
    }

    public ArrayList<Integer> getLegalMovesFor(int position){
        return legalMoves.get(position);
    }

    public boolean contains(int position){
        boolean found = false;
        for(ArrayList<Integer> pieceLegalMoves: legalMoves.values()){
            if(pieceLegalMoves.contains(position)){
                found = true;
                break;
            }
        }
        return found;
    }

    public boolean canMove(int from, int to){
        if(legalMoves.get(from).contains(to)){
            return true;
        }
        else {
            return false;
        }
    }

    public int getNumberOfMoves(){
        int numberOfMoves = 0;
        for(ArrayList<Integer> pieceLegalMoves: legalMoves.values()){
            numberOfMoves += pieceLegalMoves.size();
        }
        return numberOfMoves;
    }
}
