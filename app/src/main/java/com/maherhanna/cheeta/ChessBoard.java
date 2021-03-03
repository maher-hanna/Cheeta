package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;

public class ChessBoard {
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 63;
    public static final int OUT_OF_BOARD = -1;

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;

    public static final int FILE_A = 0;
    public static final int FILE_B = 1;
    public static final int FILE_C = 2;
    public static final int FILE_D = 3;
    public static final int FILE_E = 4;
    public static final int FILE_F = 5;
    public static final int FILE_G = 6;
    public static final int FILE_H = 7;


    private Piece[] squares;
    Player firstPlayer;
    Player secondPlayer;
    private LegalMoves legalMoves;
    public Player playerPlaying;


    public ChessBoard() {
        squares = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            squares[i] = null;
        }

        legalMoves = new LegalMoves(this);
        playerPlaying = null;
    }

    public void setPlayers(Player firstPlayer, Player secondPlayer){
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;

    }

    public void setUpBoard() {
        setUpFirstPlayerPieces(firstPlayer.color);

        setUpSecondPlayerPieces(secondPlayer.color);

    }


    private void setUpFirstPlayerPieces(Piece.Color color) {

        Piece piece;
        PlayerPiece playerPiece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.PAWN,color,position));

        }

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,0));

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,1));

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,2));



        if (color == Piece.Color.WHITE) {

            firstPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,3));
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,4));

        } else {
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,3));
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,4));
        }

            firstPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,5));
        firstPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,6));
        firstPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,7));


    }

    private void setUpSecondPlayerPieces(Piece.Color color) {
        for (int i = 0; i < 8; ++i) {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.PAWN,color,GetPosition(i, 6)));
        }

        secondPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(0, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(1, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(2, 7)));

        if (color == Piece.Color.WHITE) {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(3, 7)));
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(4, 7)));

        } else {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(3, 7)));
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(4, 7)));
        }

        secondPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(5, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(6, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(7, 7)));

    }





    public boolean requestMove(int fromSquare, int toSquare) {
        if(getPieceAt(fromSquare) == null) return false;
        if(getPieceOwner(fromSquare) == secondPlayer) return false;
        if(fromSquare == toSquare) return false;

        if(firstPlayer.canMove(fromSquare,toSquare)) {

            firstPlayer.movePice(fromSquare,toSquare);
            return true;
        }
        else {
            return false;
        }

    }

    public ArrayList<Integer> getLegalMovesFor(int position) {

        ArrayList<Integer> result = legalMoves.getLegalMoves(position);

        return result;

    }

    //get and set a square info
    public Piece getPieceAt(int position){
        if(position < MIN_POSITION || position > MAX_POSITION){
            throw new IndexOutOfBoundsException("Trying to put a piece outside of chess board");

        }
        return squares[position];
    }
    public Piece getPieceAt(int file, int rank){
        return squares[GetPosition(file,rank)];
    }
    public void setPieceAt(int position,Piece piece){
        squares[position] = piece;
    }
    public boolean isSquareEmpty(int position){return getPieceAt(position) == null;}
    public Player getPieceOwner(int position){
        if(getPieceAt(position).color == firstPlayer.color){
            return firstPlayer;
        }
        else{
            return secondPlayer;
        }
    }
    public static int GetPosition(int file, int rank) {
        if(file < FILE_A || file > FILE_H) return OUT_OF_BOARD;
        if(rank < RANK_1 || rank > RANK_8) return OUT_OF_BOARD;

        return (rank * 8 )+ file;
    }
    public static int GetFile(int position){return position % 8; }
    public static int GetRank(int position){return position / 8;}
    //------------------------
}
