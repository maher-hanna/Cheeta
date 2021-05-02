package com.maherhanna.cheeta;

public class Move {

    private long bitValue;


    public Move(int pieceType,int pieceColor, int from, int to) {
        bitValue = 0;
        setPieceType(pieceType);
        setPieceColor(pieceColor);
        setFrom(from);
        setTo(to);
    }

    public Move(int pieceType,int pieceColor, int from, int to,int takenPieceType) {
        this(pieceType,pieceColor, from, to);
        setTakes(takenPieceType);
    }

    public Move(Move copy) {
        bitValue = copy.bitValue;
    }


    public boolean isCastling() {
        return BitMath.getBitsValue(bitValue, CASTLING_START
                , CASTLING_MASK) == 1;
    }

    public CastlingType getCastlingType() {
        return CastlingType.values()
                [(int) BitMath.getBitsValue(bitValue, CASTLING_TYPE_START, CASTLING_TYPE_MASK)];
    }

    public boolean isTake() {
        return BitMath.getBitsValue(bitValue, TAKE_START
                , TAKE_MASK) == 1;
    }

    public int getTakenPieceType() {
        return (int) BitMath.getBitsValue(bitValue, TAKE_TYPE_START, TAKE_TYPE_MASK);
    }

    public boolean isPromote() {
        return BitMath.getBitsValue(bitValue, PROMOTE_START
                , PROMOTE_MASK) == 1;
    }

    public int getPromotionPieceType() {
        return (int) BitMath.getBitsValue(bitValue, PROMOTE_TYPE_START, PROMOTE_TYPE_MASK);

    }

    public boolean isEnPasant() {
        return BitMath.getBitsValue(bitValue, ENPASSANT_START
                , ENPASSANT_MASK) == 1;
    }

    public boolean isPawnDoubleMove() {
        return BitMath.getBitsValue(bitValue, PAWN_DOUBLE_PUSH_START, PAWN_DOUBLE_PUSH_MASK) == 1;
    }

    public int getPreviousFiftyMoves() {
        return (int) BitMath.getBitsValue(bitValue, PREVIOUS_FIFTY_MOVES_START,
                PREVIOUS_FIFTY_MOVES_MASK);
    }


    public int getFrom() {
        return (int) BitMath.getBitsValue(bitValue, FROM_START, FROM_MASK);
    }

    public void setFrom(int position) {
        bitValue = BitMath.setBitsValue(bitValue, FROM_START, FROM_MASK, position);
    }

    public int getTo() {
        return (int) BitMath.getBitsValue(bitValue, TO_START, TO_MASK);
    }

    public void setTo(int position) {
        bitValue = BitMath.setBitsValue(bitValue, TO_START, TO_MASK, position);
    }

    public int getColor() {
        return (int) BitMath.getBitsValue(bitValue, COLOR_START, COLOR_MASK);
    }

    public int getPieceType() {
        return (int) BitMath.getBitsValue(bitValue, TYPE_START, TYPE_MASK);

    }

    public void setPieceType(int type) {
        bitValue = BitMath.setBitsValue(bitValue, TYPE_START, TYPE_MASK, type);
    }

    public void setPieceColor(int color) {
        bitValue = BitMath.setBitsValue(bitValue, COLOR_START, COLOR_MASK, color);
    }

    public void setCastling() {
        bitValue = BitMath.setBitsValue(bitValue, CASTLING_START, CASTLING_MASK, 1);
    }

    public void setCastlingType(CastlingType castlingType) {
        int value = castlingType.ordinal();
        bitValue = BitMath.setBitsValue(bitValue, CASTLING_TYPE_START, CASTLING_TYPE_MASK, value);
    }

    public void setCastling(CastlingType castlingType) {
        setCastling();
        setCastlingType(castlingType);
    }

    public void setTakes() {
        bitValue = BitMath.setBitsValue(bitValue, TAKE_START, TAKE_MASK, 1);
    }

    public void setTakenPieceType(int pieceType) {
        bitValue = BitMath.setBitsValue(bitValue, TAKE_TYPE_START, TAKE_TYPE_MASK, pieceType);
    }

    public void setTakes(int pieceType) {
        setTakes();
        setTakenPieceType(pieceType);
    }

    public void setPromotes() {
        bitValue = BitMath.setBitsValue(bitValue, PROMOTE_START, PROMOTE_MASK, 1);
    }

    public void setPromotionPieceType(int promotionPieceType) {
        bitValue = BitMath.setBitsValue(bitValue, PROMOTE_TYPE_START, PROMOTE_TYPE_MASK, promotionPieceType);
    }

    public void setPromotes(int promotionPieceType) {
        setPromotes();
        setPromotionPieceType(promotionPieceType);
    }

    public void setEnPasant() {
        bitValue = BitMath.setBitsValue(bitValue, ENPASSANT_START, ENPASSANT_MASK, 1);
        setTakes(Piece.PAWN);
    }

    public void setPawnDoublePush() {
        bitValue = BitMath.setBitsValue(bitValue, PAWN_DOUBLE_PUSH_START, PAWN_DOUBLE_PUSH_MASK, 1);
    }

    public void setPreviousFiftyMoves(int fiftyMoves) {
        bitValue = BitMath.setBitsValue(bitValue, PREVIOUS_FIFTY_MOVES_START, PREVIOUS_FIFTY_MOVES_MASK, fiftyMoves);
    }


    public enum CastlingType {CASTLING_kING_SIDE, CASTLING_QUEEN_SIDE}

    private static final long TYPE_MASK = 7;
    private static final long TYPE_START = 0;

    private static final long COLOR_MASK = 8;
    private static final long COLOR_START = 3;

    private static final long FROM_MASK = 1008;
    private static final long FROM_START = 4;

    private static final long TO_MASK = 64512;
    private static final long TO_START = 10;

    private static final long CASTLING_MASK = 65536;
    private static final long CASTLING_START = 16;

    private static final long CASTLING_TYPE_MASK = 131072;
    private static final long CASTLING_TYPE_START = 17;

    private static final long TAKE_MASK = 262144;
    private static final long TAKE_START = 18;

    private static final long TAKE_TYPE_MASK = 3670016;
    private static final long TAKE_TYPE_START = 19;

    private static final long PROMOTE_MASK = 4194304;
    private static final long PROMOTE_START = 22;

    private static final long PROMOTE_TYPE_MASK = 58720256;
    private static final long PROMOTE_TYPE_START = 23;

    private static final long ENPASSANT_MASK = 67108864;
    private static final long ENPASSANT_START = 26;

    private static final long PAWN_DOUBLE_PUSH_MASK = 134217728;
    private static final long PAWN_DOUBLE_PUSH_START = 27;

    private static final long PREVIOUS_FIFTY_MOVES_MASK = 16911433728L;
    private static final long PREVIOUS_FIFTY_MOVES_START = 28;


}
