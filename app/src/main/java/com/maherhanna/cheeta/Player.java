package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.HashMap;

class Player {

    public Piece.Color color;
    protected ChessBoard chessBoard;

    protected boolean myTurn;

    protected Player opponent;
    protected ArrayList<PlayerPiece> pieces;
    protected HashMap<Integer,ArrayList<Integer>> legalMoves;


    public Player(Piece.Color color, ChessBoard chessBoard, Player opponent) {
        this.color = color;
        this.chessBoard = chessBoard;
        this.opponent = opponent;
        myTurn = false;
        this.pieces = new ArrayList<>();
        legalMoves = new HashMap<>();
    }

    protected PlayerPiece getPieceAt(int square) {
        PlayerPiece piece = null;
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getPosition() == square) {
                piece = pieces.get(i);
                break;
            }
        }
        return piece;

    }

    public void addPiece(PlayerPiece piece) {
        pieces.add(piece);
        chessBoard.setPieceAt(piece.getPosition(), new Piece(piece.getPiece()));

    }


    public void play() {

        myTurn = true;
    }

    public boolean isPlaying(){
        return myTurn == true;
    }





    public boolean canMove(int fromSquare, int toSquare) {

        if(legalMoves.get(fromSquare).contains(toSquare)){
            return true;
        }
        else {
            return false;
        }

    }

    public void movePice(int fromSquare, int toSquare) {
        getPieceAt(fromSquare).moveTo(toSquare);
        if(opponent.hasPiece(toSquare)){

            opponent.removePiece(toSquare);
        }
        chessBoard.movePice(fromSquare,toSquare);


        updateLegalMoves();
        myTurn = false;

    }

    public void removePiece(int position){
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getPosition() == position) {
                pieces.remove(i);
                break;
            }
        }
    }

    public boolean hasPiece(int position){
        Piece targetPiece = chessBoard.getPieceAt(position);
        if(targetPiece == null) return false;
        if(this.color == targetPiece.color){
            return true;
        }
        else {
            return false;
        }
    }

    protected void updateLegalMoves(){
        legalMoves.clear();
        for(int i = 0; i < pieces.size();i++){
            legalMoves.put(pieces.get(i).getPosition(),
                    chessBoard.getLegalMovesFor(pieces.get(i).getPosition()));
        }

    }
}