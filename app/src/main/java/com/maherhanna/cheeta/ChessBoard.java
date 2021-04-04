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


    private Piece[] pieces;
    public ChessboardMoves moves;

    LegalMoves blackLegalMoves;
    LegalMoves whiteLegalMoves;

    public ChessBoard(ChessBoard copy) {
        this.pieces = copy.pieces.clone();
        this.moves = new ChessboardMoves(copy.moves);
    }


    public ChessBoard(Drawing drawing) {
        pieces = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            pieces[i] = null;
        }

        moves = new ChessboardMoves();
        blackLegalMoves = new LegalMoves();
        whiteLegalMoves = new LegalMoves();


    }

    public void setUpBoard() {
        setupWhitePieces();
        setupBlackPieces();

        updateWhiteLegalMoves(false);
        updateBlackLegalMoves(false);

    }

    private void setupWhitePieces() {

        Piece square;
        Piece piece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            setPieceAt(position, Piece.Type.PAWN, Piece.Color.WHITE);
        }

        setPieceAt(0, Piece.Type.ROOK, Piece.Color.WHITE);
        setPieceAt(1, Piece.Type.KNIGHT, Piece.Color.WHITE);
        setPieceAt(2, Piece.Type.BISHOP, Piece.Color.WHITE);
        setPieceAt(3, Piece.Type.QUEEN, Piece.Color.WHITE);
        setPieceAt(4, Piece.Type.KING, Piece.Color.WHITE);
        setPieceAt(5, Piece.Type.BISHOP, Piece.Color.WHITE);
        setPieceAt(6, Piece.Type.KNIGHT, Piece.Color.WHITE);
        setPieceAt(7, Piece.Type.ROOK, Piece.Color.WHITE);


    }

    private void setupBlackPieces() {
        for (int i = 0; i < 8; ++i) {
            setPieceAt(GetPosition(i, 6), Piece.Type.PAWN, Piece.Color.BLACK);

        }

        setPieceAt(GetPosition(0, 7), Piece.Type.ROOK, Piece.Color.BLACK);
        setPieceAt(GetPosition(1, 7), Piece.Type.KNIGHT, Piece.Color.BLACK);
        setPieceAt(GetPosition(2, 7), Piece.Type.BISHOP, Piece.Color.BLACK);
        setPieceAt(GetPosition(3, 7), Piece.Type.QUEEN, Piece.Color.BLACK);
        setPieceAt(GetPosition(4, 7), Piece.Type.KING, Piece.Color.BLACK );
        setPieceAt(GetPosition(5, 7), Piece.Type.BISHOP, Piece.Color.BLACK );
        setPieceAt(GetPosition(6, 7), Piece.Type.KNIGHT, Piece.Color.BLACK );
        setPieceAt(GetPosition(7, 7), Piece.Type.ROOK, Piece.Color.BLACK );

    }





    public void updateBlackLegalMoves(boolean kingInCheck) {
        blackLegalMoves = LegalMovesChecker.getBlackLegalMoves(this, kingInCheck);
    }


    public void updateWhiteLegalMoves(boolean kingInCheck) {
        whiteLegalMoves = LegalMovesChecker.getWhiteLegalMoves(this, kingInCheck);

    }

    public void updateLegalMovesFor(Piece.Color playerColor, boolean kingInCheck) {
        if (playerColor == Piece.Color.WHITE) {
            updateWhiteLegalMoves(kingInCheck);
        } else {
            updateBlackLegalMoves(kingInCheck);
        }
    }

    public ArrayList<Integer> getBlackPositions() {
        ArrayList<Integer> blackPositions = new ArrayList<>();
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            if (isSquareEmpty(i)) continue;
            if (isPieceBlackAt(i)) blackPositions.add(i);
        }
        return blackPositions;
    }

    public ArrayList<Integer> getWhitePositions() {
        ArrayList<Integer> whitePositions = new ArrayList<>();
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            if (isSquareEmpty(i)) continue;
            if (isPieceWhiteAt(i)) whitePositions.add(i);
        }
        return whitePositions;
    }

    public ArrayList<Integer> getPositionsFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhitePositions();
        } else {
            return getBlackPositions();
        }
    }

    public LegalMoves getLegalMovesFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return whiteLegalMoves;
        } else {
            return blackLegalMoves;
        }
    }


    public ArrayList<Integer> getLegalTargetsFor(int position) {
        if (getPieceAt(position).color == Piece.Color.WHITE) {
            return whiteLegalMoves.getLegalTargetsFor(position);
        } else {
            return blackLegalMoves.getLegalTargetsFor(position);
        }
    }




    public boolean isKingInCheck(Piece.Color kingColor) {
        if (kingColor == Piece.Color.WHITE) {
            return blackLegalMoves.contains(getKingPosition(kingColor));

        } else {
            return whiteLegalMoves.contains(getKingPosition(kingColor));
        }
    }

    public int getKingPosition(Piece.Color kingColor) {
        int kingPosition = OUT_OF_BOARD;
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            Piece piece = getPieceAt(i);
            if (piece != null && piece.type == Piece.Type.KING && piece.color == kingColor) {
                kingPosition = i;
                break;
            }
        }
        return kingPosition;
    }

    public boolean canMove(int fromSquare, int toSquare) {
        boolean isLegal = false;
        if (isPieceBlackAt(fromSquare)) {
            if (blackLegalMoves.canMove(fromSquare, toSquare)) {
                isLegal = true;
            } else {
                isLegal = false;
            }

        } else {
            if (whiteLegalMoves.canMove(fromSquare, toSquare)) {
                isLegal = true;
            } else {
                isLegal = false;
            }
        }
        return isLegal;

    }



    public void movePiece(Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        setPieceAt(toSquare, getPieceAt(fromSquare));
        setPieceAt(fromSquare, null);
        getPieceAt(toSquare).position = toSquare;


        if (move.isCastling()) {
            int rookPosition;
            int rookCastlingTarget;
            Piece.Color moveColor = move.getColor();

            if (move.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = LegalMovesChecker.getInitialRookKingSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() + 1;

            } else {
                rookPosition = LegalMovesChecker.getInitialRookQueenSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() -1;
            }
            setPieceAt(rookCastlingTarget, getPieceAt(rookPosition) );
            setPieceAt(rookPosition, null);
            getPieceAt(rookCastlingTarget).position = rookCastlingTarget;
        }
        if(move.isPromote()){
            getPieceAt(toSquare).type = move.getPromotionPieceType();
        }
        if(move.isEnPasant()){
            if(move.getColor() == Piece.Color.WHITE){
                setPieceAt(ChessBoard.offsetRank(move.getTo(),-1),null);
            }
            else {
                setPieceAt(ChessBoard.offsetRank(move.getTo(),1),null);
            }
        }

        moves.add(move);

    }

    //get and set a square info
    public Piece getPieceAt(int position) {

        return pieces[position];
    }

    public Piece getPieceAt(int file, int rank) {
        return pieces[GetPosition(file, rank)];
    }

    public void setPieceAt(int position, Piece.Type pieceType, Piece.Color pieceColor) {
        pieces[position] = new Piece(pieceType, pieceColor, position);
    }

    public void setPieceAt(int position, Piece piece) {
        if(piece == null){
            pieces[position] = null;
            return;
        }
        pieces[position] = new Piece(piece);
    }

    public boolean isSquareEmpty(int position) {
        return getPieceAt(position) == null;
    }

    public boolean isPieceBlackAt(int position) {
        return pieces[position].color == Piece.Color.BLACK;
    }

    public boolean isPieceWhiteAt(int position) {
        return pieces[position].color == Piece.Color.WHITE;
    }

    public Piece.Color getPieceColor(int position){
        return pieces[position].color;
    }
    public Piece.Type getPieceType(int position){
        return pieces[position].type;
    }

    public boolean isPieceAt(int square, Piece.Type type, Piece.Color color){
        if(square == OUT_OF_BOARD) return false;
        if(isSquareEmpty(square)) return false;
        if(getPieceColor(square) == color && getPieceType(square) == type) return true;
        else return false;
    }

    public static int GetPosition(int file, int rank) {
        if (file < FILE_A || file > FILE_H) return OUT_OF_BOARD;
        if (rank < RANK_1 || rank > RANK_8) return OUT_OF_BOARD;

        return (rank * 8) + file;
    }

    public static int GetFile(int position) {
        return position % 8;
    }

    public static int GetRank(int position) {
        return position / 8;
    }

    public static int offset(int square, int file, int rank){
        int newFile = GetFile(square) + file;
        int newRank = GetRank(square) + rank;
        if(newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT_OF_BOARD;
        if(newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) return ChessBoard.OUT_OF_BOARD;

        return (newRank * 8 )  + newFile;
    }
    public static int offsetFile(int square,int file){
        return offset(square,file,0);
    }
    public static int offsetRank(int square,int rank){
        return offset(square,0,rank);
    }
    public static Piece.Color GetSquareColor(int square){
        if((square % 2) == 0){
            return Piece.Color.BLACK;
        } else {
            return Piece.Color.WHITE;
        }
    }

    //------------------------


    //this function is for debugging
    public void print() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" 0,");
        for (int row = 7; row >= 0; row--) {
            for (int column = 0; column < 8; column++) {
                if (getPieceAt(column, row) == null) {
                    stringBuilder.append(" 0,");
                    continue;
                }
                stringBuilder.append(String.format("%2d,", getPieceAt(column, row).position));
            }
            stringBuilder.append('\n');


        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }


}
