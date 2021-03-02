package com.maherhanna.cheeta;

import java.util.ArrayList;

public class PlayerPiece {
    private Piece piece;
    private ArrayList<Integer> moves;
    private int position;

    public PlayerPiece(Piece piece,int position){
        this.piece = piece;
        this.position = position;
        moves = new ArrayList<>();
    }

    public PlayerPiece(Piece.Type type, Piece.Color color, int position){
        this.piece = new Piece(type,color,this);
        this.position = position;
    }

    public void setPosition(int newPosition){
        if(newPosition < ChessBoard.MIN_POSITION || newPosition > ChessBoard.MAX_POSITION){
            throw new IndexOutOfBoundsException("Trying to put a piece outside of chess board");
        }
        this.position = newPosition;
    }

    public Piece getPiece(){return this.piece;}

    public int getPosition(){
        return this.position;
    }
    public int getFile(){return ChessBoard.GetFile(position);}
    public int getRank(){return ChessBoard.GetRank(position);}


}
