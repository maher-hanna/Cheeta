package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

class Player {

    public Piece.Color color;
    private ChessBoard chessBoard;

    private Player opponent;
    private ArrayList<PlayerPiece> pieces;
    private HashMap<Piece,ArrayList<Integer>> legalMoves;


    public Player(Piece.Color color, ChessBoard chessBoard, Player opponent) {
        this.color = color;
        this.chessBoard = chessBoard;
        this.opponent = opponent;
        this.pieces = new ArrayList<>();
        legalMoves = new HashMap<>();
    }

    private PlayerPiece getPieceAt(int square) {
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
        //legalMoves.put(piece.getPiece(),chessBoard.getLegalMovesFor(piece.getPosition()));

    }


    public void play() {

        chessBoard.playerPlaying = this;


    }

    public boolean isHisTurn() {
        return chessBoard.playerPlaying == this;
    }


    public void movePice(int fromSquare, int toSquare) {
        getPieceAt(fromSquare).setPosition(toSquare);

        chessBoard.setPieceAt(toSquare, chessBoard.getPieceAt(fromSquare));
        chessBoard.setPieceAt(fromSquare, null);
    }


    public boolean canMove(int fromSquare, int toSquare) {
        if(chessBoard.getLegalMovesFor(fromSquare).contains(toSquare)){
            return true;
        }
        else {
            return false;
        }

    }
}