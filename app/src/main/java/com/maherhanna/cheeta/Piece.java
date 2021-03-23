package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.HashMap;

public class Piece {
    private Square square;
    private ArrayList<Integer> moves;
    public ArrayList<Integer> legalMoves;


    public Piece(Square square){
        this.square = new Square(square);
        moves = new ArrayList<>();
        moves.add(square.position);
        legalMoves = new ArrayList<>();
    }

    public Piece(Square.Type type, Square.Color color, int position){
        this(new Square(type,color,position));

    }



    protected void updateLegalMoves(LegalMovesChecker legalMovesChecker,boolean kingInCheck){
        legalMoves.clear();
        legalMoves = legalMovesChecker.getLegalMoves(this,kingInCheck);

    }

    public void moveTo(int newPosition){
        this.square.position = newPosition;
        moves.add(newPosition);
    }
    public boolean canMoveTo(int position){
        return legalMoves.contains(position);
    }

    public Square getSquare(){return this.square;}

    public int getPosition(){
        return this.square.position;
    }
    public int getFile(){return ChessBoard.GetFile(square.position);}
    public int getRank(){return ChessBoard.GetRank(square.position);}


}
