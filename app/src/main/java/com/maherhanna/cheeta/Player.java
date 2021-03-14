package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.HashMap;

class Player {

    public Square.Color color;
    protected ChessBoard chessBoard;

    protected boolean myTurn;

    protected Player opponent;
    protected ArrayList<Piece> pieces;


    public Player(Square.Color color, ChessBoard chessBoard, Player opponent) {
        this.color = color;
        this.chessBoard = chessBoard;
        this.opponent = opponent;
        myTurn = false;
        this.pieces = new ArrayList<>();

    }

    protected Piece getPieceAt(int square) {
        Piece piece = null;
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getPosition() == square) {
                piece = pieces.get(i);
                break;
            }
        }
        return piece;

    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
        chessBoard.setPieceAt(piece.getPosition(), new Square(piece.getSquare()));

    }


    public void play() {

        myTurn = true;
    }

    public boolean isPlaying(){
        return myTurn == true;
    }


    public boolean canMove(int fromSquare, int toSquare) {

        if(getPieceAt(fromSquare).canMoveTo(toSquare)){
            return true;
        }
        else {
            return false;
        }

    }

    public void movePiece(int fromSquare, int toSquare) {
        getPieceAt(fromSquare).moveTo(toSquare);
        if(opponent.hasPiece(toSquare)){

            opponent.removePiece(toSquare);
        }
        chessBoard.movePice(fromSquare,toSquare);


        updateLegalMoves();
        opponent.updateLegalMoves();
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
        Square targetSquare = chessBoard.getPieceAt(position);
        if(targetSquare == null) return false;
        if(this.color == targetSquare.color){
            return true;
        }
        else {
            return false;
        }
    }

    protected void updateLegalMoves(){
        LegalMovesChecker legalMovesChecker = new LegalMovesChecker(chessBoard);
        for(int i = 0; i < pieces.size();i++){
            pieces.get(i).updateLegalMoves(legalMovesChecker);
        }

    }
}