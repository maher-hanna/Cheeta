package com.maherhanna.cheeta;

import java.util.ArrayList;

public class PlayerPiece {
    private Piece piece;
    private ArrayList<Integer> moves;

    public PlayerPiece(Piece piece,int position){
        this.piece = new Piece(piece);
        moves = new ArrayList<>();
    }

    public PlayerPiece(Piece.Type type, Piece.Color color, int position){
        this.piece = new Piece(type,color,position);
    }

    public void moveTo(int newPosition){
        this.piece.position = newPosition;
    }

    public Piece getPiece(){return this.piece;}

    public int getPosition(){
        return this.piece.position;
    }
    public int getFile(){return ChessBoard.GetFile(piece.position);}
    public int getRank(){return ChessBoard.GetRank(piece.position);}


}
