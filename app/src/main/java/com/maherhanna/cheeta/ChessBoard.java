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

    //players
    Player playerAtBottom;
    Player playerAtTop;
    //-----

    private LegalMoves legalMoves;
    public Drawing drawing;


    public ChessBoard(Drawing drawing) {
        squares = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            squares[i] = null;
        }

        legalMoves = new LegalMoves(this);
        this.drawing = drawing;
    }

    public void setPlayers(Player playerAtBottom, Player playerAtTop){
        this.playerAtBottom = playerAtBottom;
        this.playerAtTop = playerAtTop;

    }
    public void print(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" 0,");
        for(int row = 7;row >= 0;row--){
            for(int column = 0; column < 8; column++){
                if(getPieceAt(column,row) == null) {
                    stringBuilder.append(" 0,");
                    continue;
                }
                stringBuilder.append(String.format("%2d,",getPieceAt(column,row).position));
            }
            stringBuilder.append('\n');


        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }

    public void setUpBoard() {
        setUpBottomPlayerPieces(playerAtBottom.color);

        setUpTopPlayerPieces(playerAtTop.color);

        playerAtTop.updateLegalMoves();
        playerAtBottom.updateLegalMoves();

    }


    private void setUpBottomPlayerPieces(Piece.Color color) {

        Piece piece;
        PlayerPiece playerPiece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.PAWN,color,position));

        }

        playerAtBottom.addPiece(new PlayerPiece(Piece.Type.ROOK,color,0));

        playerAtBottom.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,1));

        playerAtBottom.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,2));



        if (color == Piece.Color.WHITE) {

            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,3));
            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.KING,color,4));

        } else {
            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.KING,color,3));
            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,4));
        }

            playerAtBottom.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,5));
        playerAtBottom.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,6));
        playerAtBottom.addPiece(new PlayerPiece(Piece.Type.ROOK,color,7));


    }

    private void setUpTopPlayerPieces(Piece.Color color) {
        for (int i = 0; i < 8; ++i) {
            playerAtTop.addPiece(new PlayerPiece(Piece.Type.PAWN,color,GetPosition(i, 6)));
        }

        playerAtTop.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(0, 7)));
        playerAtTop.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(1, 7)));
        playerAtTop.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(2, 7)));

        if (color == Piece.Color.WHITE) {
            playerAtTop.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(3, 7)));
            playerAtTop.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(4, 7)));

        } else {
            playerAtTop.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(3, 7)));
            playerAtTop.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(4, 7)));
        }

        playerAtTop.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(5, 7)));
        playerAtTop.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(6, 7)));
        playerAtTop.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(7, 7)));

    }




    public boolean requestMove(int fromSquare, int toSquare) {
        if(getPieceAt(fromSquare) == null) return false;
        if(getPieceOwner(fromSquare) == playerAtTop) return false;

        if(playerAtBottom.canMove(fromSquare,toSquare)) {

            playerAtBottom.movePice(fromSquare,toSquare);
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
    public void movePice(int fromSquare, int toSquare) {
        setPieceAt(toSquare, getPieceAt(fromSquare));
        setPieceAt(fromSquare, null);
        getPieceAt(toSquare).position = toSquare;
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
        if(getPieceAt(position).color == playerAtBottom.color){
            return playerAtBottom;
        }
        else{
            return playerAtTop;
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
