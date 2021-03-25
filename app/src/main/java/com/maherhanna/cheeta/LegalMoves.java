package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class LegalMoves {
    private HashMap<Integer, ArrayList<Integer>> legalMoves;
    public LegalMoves(){
        legalMoves = new HashMap<>();
    }

    public void addMovesFor(int position, ArrayList<Integer> moves){
        legalMoves.put(position,moves);
    }

    public ArrayList<Integer> getLegalMovesFor(int position){
        return legalMoves.get(position);
    }

    public boolean contains(int position){
        boolean found = false;
        for(int i = 0; i < legalMoves.size();i++){
            if(legalMoves.get(i).contains(position)){
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
}
