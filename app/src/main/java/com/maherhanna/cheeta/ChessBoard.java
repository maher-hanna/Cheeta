package com.maherhanna.cheeta;

import android.util.Log;
import android.view.CollapsibleActionView;

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


    private Piece[] pieces;
    private ArrayList<Move> moves;
    Piece.Color bottomPlayerColor;
    Piece.Color topPlayerColor;

    LegalMoves blackLegalMoves;
    LegalMoves whiteLegalMoves;
    private boolean gameFinished = false;

    public ChessBoard(ChessBoard copy){
        this.pieces = copy.pieces.clone();
        this.bottomPlayerColor = copy.bottomPlayerColor;
        this.topPlayerColor = copy.topPlayerColor;
        this.gameFinished = copy.gameFinished;
        this.moves = (ArrayList<Move>)copy.moves.clone();
    }




    public ChessBoard(Drawing drawing, Piece.Color bottomPlayerColor) {
        pieces = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            pieces[i] = null;
        }
        this.bottomPlayerColor = bottomPlayerColor;
        this.topPlayerColor = bottomPlayerColor.getOpposite();

        moves = new ArrayList<Move>();
        blackLegalMoves = new LegalMoves();
        whiteLegalMoves = new LegalMoves();


    }

    public boolean isPieceForPlayerAtTop(int position){
        if(getPieceAt(position).color == topPlayerColor){
            return true;
        }
        else {
            return false;
        }
    }



    public void setUpBoard() {
        setUpBottomPlayerPieces(bottomPlayerColor);

        setUpTopPlayerPieces(bottomPlayerColor.getOpposite());

        updateWhiteLegalMoves(false);
        updateBlackLegalMoves(false);

    }

    public boolean isGameFinished(){
       return gameFinished;
    }

    public void setGameFinished(){
        gameFinished = true;
    }

    public void updateBlackLegalMoves(boolean kingInCheck) {
           blackLegalMoves = LegalMovesChecker.getBlackLegalMoves(this,kingInCheck);
    }


    public void updateWhiteLegalMoves(boolean kingInCheck) {
        whiteLegalMoves = LegalMovesChecker.getWhiteLegalMoves(this,kingInCheck);

    }

    public void updateLegalMovesFor(Piece.Color playerColor,boolean kingInCheck){
        if(playerColor == Piece.Color.WHITE){
            updateWhiteLegalMoves(kingInCheck);
        }
        else{
            updateBlackLegalMoves(kingInCheck);
        }
    }

    public ArrayList<Integer> getBlackPositions() {
        ArrayList<Integer> blackPositions = new ArrayList<>();
        for(int i = MIN_POSITION; i <= MAX_POSITION;i++) {
            if(isSquareEmpty(i)) continue;
            if(isPieceBlackAt(i)) blackPositions.add(i);
        }
        return blackPositions;
    }

    public ArrayList<Integer> getWhitePositions() {
        ArrayList<Integer> whitePositions = new ArrayList<>();
        for (int i = MIN_POSITION; i <= MAX_POSITION;i++) {
            if (isSquareEmpty(i)) continue;
            if (isPieceWhiteAt(i)) whitePositions.add(i);
        }
        return whitePositions;
    }

    public ArrayList<Integer> getPositionsFor(Piece.Color color){
        if(color == Piece.Color.WHITE){
            return getWhitePositions();
        }
        else{
            return getBlackPositions();
        }
    }

    public LegalMoves getLegalMovesFor(Piece.Color color){
        if(color == Piece.Color.WHITE){
            return whiteLegalMoves;
        }
        else{
            return blackLegalMoves;
        }
    }


    public ArrayList<Integer> getLegalMovesFor(int position){
        if(getPieceAt(position).color == Piece.Color.WHITE){
            return whiteLegalMoves.getLegalMovesFor(position);
        }
        else{
            return blackLegalMoves.getLegalMovesFor(position);
        }
    }


    private void setUpBottomPlayerPieces(Piece.Color color) {

        Piece square;
        Piece piece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            setPieceAt(position, new Piece(Piece.Type.PAWN,color,position));


        }

        setPieceAt(0, new Piece(Piece.Type.ROOK,color,0));
        setPieceAt(1, new Piece(Piece.Type.KNIGHT,color,1));
        setPieceAt(2, new Piece(Piece.Type.BISHOP,color,2));


        if (color == Piece.Color.WHITE) {

            setPieceAt(3, new Piece(Piece.Type.QUEEN,color,3));
            setPieceAt(4, new Piece(Piece.Type.KING,color,4));


        } else {
            setPieceAt(3, new Piece(Piece.Type.KING,color,3));
            setPieceAt(4, new Piece(Piece.Type.QUEEN,color,4));        }

        setPieceAt(5, new Piece(Piece.Type.BISHOP,color,5));

        setPieceAt(6, new Piece(Piece.Type.KNIGHT,color,6));

        setPieceAt(7, new Piece(Piece.Type.ROOK,color,7));



}

    private void setUpTopPlayerPieces(Piece.Color color) {
        for (int i = 0; i < 8; ++i) {
            setPieceAt(GetPosition(i, 6), new Piece(Piece.Type.PAWN,color,GetPosition(i, 6)));

    }

        setPieceAt(GetPosition(0, 7), new Piece(Piece.Type.ROOK,color,GetPosition(0, 7)));

        setPieceAt(GetPosition(1, 7), new Piece(Piece.Type.KNIGHT,color,GetPosition(1, 7)));

        setPieceAt(GetPosition(2, 7), new Piece(Piece.Type.BISHOP,color,GetPosition(2, 7)));


        if (color == Piece.Color.WHITE) {
            setPieceAt(GetPosition(3, 7), new Piece(Piece.Type.KING,color,GetPosition(3, 7)));
            setPieceAt(GetPosition(4, 7), new Piece(Piece.Type.QUEEN,color,GetPosition(4, 7)));


        } else {
            setPieceAt(GetPosition(3, 7), new Piece(Piece.Type.QUEEN,color,GetPosition(3, 7)));
            setPieceAt(GetPosition(4, 7), new Piece(Piece.Type.KING,color,GetPosition(4, 7)));
        }

        setPieceAt(GetPosition(5, 7), new Piece(Piece.Type.BISHOP,color,GetPosition(5, 7)));

        setPieceAt(GetPosition(6, 7), new Piece(Piece.Type.KNIGHT,color,GetPosition(6, 7)));

        setPieceAt(GetPosition(7, 7), new Piece(Piece.Type.ROOK,color,GetPosition(7, 7)));


    }



    public boolean isKingInCheck(Piece.Color kingColor){
        if(kingColor == Piece.Color.WHITE){
            return blackLegalMoves.contains(getKingPosition(kingColor));

        }
        else {
            return whiteLegalMoves.contains(getKingPosition(kingColor));
        }
    }

    public int getKingPosition(Piece.Color kingColor){
        int kingPosition = OUT_OF_BOARD;
        for(int i = MIN_POSITION; i <= MAX_POSITION; i++){
            Piece piece = getPieceAt(i);
            if(piece != null && piece.type == Piece.Type.KING && piece.color == kingColor){
                kingPosition =  i;
                break;
            }
        }
        return kingPosition;
    }

    public boolean canMove(int fromSquare, int toSquare) {
        boolean isLegal = false;
        if(isPieceBlackAt(fromSquare)) {
            if(blackLegalMoves.canMove(fromSquare,toSquare)) {
                isLegal = true;
            }
            else {
                isLegal = false;
            }

        }
        else {
            if(whiteLegalMoves.canMove(fromSquare,toSquare)) {
                isLegal = true;
            }
            else {
                isLegal = false;
            }
        }
        return isLegal;

    }


    public void movePiece(int fromSquare, int toSquare) {
        setPieceAt(toSquare, getPieceAt(fromSquare));
        setPieceAt(fromSquare, null);
        getPieceAt(toSquare).position = toSquare;
        moves.add(new Move(fromSquare,toSquare));
    }

    public void movePiece(Move move){
        movePiece(move.from,move.to);
    }

    //get and set a square info
    public Piece getPieceAt(int position){

        return pieces[position];
    }
    public Piece getPieceAt(int file, int rank){
        return pieces[GetPosition(file,rank)];
    }
    public void setPieceAt(int position, Piece piece){
        if(piece == null)
        {
            pieces[position] = null;
            return;
        }
        pieces[position] = new Piece(piece);
    }
    public boolean isSquareEmpty(int position){return getPieceAt(position) == null;}
    public boolean isPieceBlackAt(int position){return pieces[position].color == Piece.Color.BLACK;}
    public boolean isPieceWhiteAt(int position){return pieces[position].color == Piece.Color.WHITE;}
    public static int GetPosition(int file, int rank) {
        if(file < FILE_A || file > FILE_H) return OUT_OF_BOARD;
        if(rank < RANK_1 || rank > RANK_8) return OUT_OF_BOARD;

        return (rank * 8 )+ file;
    }
    public static int GetFile(int position){return position % 8; }
    public static int GetRank(int position){return position / 8;}

    //------------------------


    //this function is for debugging
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



}
