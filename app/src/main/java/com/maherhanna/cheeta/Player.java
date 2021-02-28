package com.maherhanna.cheeta;

import java.util.HashMap;

class Player {

    public Piece.Color color;
    private ChessBoard chessBoard;
    private boolean myTurn;
    private Player opponent;
    private HashMap<Integer,PlayerPiece> pieces;

    public Player(Piece.Color color, ChessBoard chessBoard, Player opponent){
        this.color = color;
        this.chessBoard = chessBoard;
        myTurn = false;
        this.opponent = opponent;
        this.pieces = new HashMap<>();
    }

    public void addPiece(PlayerPiece piece){
        pieces.put(piece.getPosition(),piece);
        chessBoard.setPieceAt(piece.getPosition(),new Piece(piece.getPiece()));

    }



    public void play(){
        myTurn = true;

    }

    public boolean isHisTurn(){
        return myTurn;
    }


    public PlayerPiece getPieceAt(int position){
        return pieces.get(position);
    }


}