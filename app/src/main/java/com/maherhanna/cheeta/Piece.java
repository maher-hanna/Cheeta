package com.maherhanna.cheeta;

import java.lang.Exception;




public class Piece {
    private byte bitValue;

    public Piece(PieceType pieceType, PieceColor pieceColor) {

        this.setType(pieceType);
        this.setColor(pieceColor);
    }

    public PieceType getType() {
        PieceType type = PieceType.EMPTY;
        switch(this.bitValue) {
            case 1:
                type = PieceType.PAWN;
            break;
            case 2:
                type = PieceType.ROOK;
            break;
            case 4:
                type = PieceType.KNIGHT;
            break;
            case 8:
                type = PieceType.BISHOP;
            break;
            case 16:
                type = PieceType.QUEEN;
            break;
            case 32:
                type = PieceType.KING;
            break;
//            default:
//            throw new Exception("invalid internal piece bitValue");
        }

        return type;
    }

    public void setType(PieceType pieceType){
        //store the piece type and color using one byte
        //the seven'th bit is the color

        //first clear all bits but save color
        // bit to add it after setting type bit
        byte colorbit = (byte)(bitValue | 64);
        bitValue = 0;

        switch (pieceType){
            case PAWN:
                bitValue = 1;
                break;
            case ROOK:
                bitValue = 2;
                break;
            case KNIGHT:
                bitValue = 4;
                break;
            case BISHOP:
                bitValue = 8;
                break;
            case QUEEN:
                bitValue = 16;
                break;
        }


        bitValue = (byte)(bitValue | colorbit);

        //last bit is set to 1 to indicate that square is not empty
        //when the bitValue is written to board
        bitValue = (byte)(bitValue | 128);

    }

    public PieceColor getColor() {
        return (this.bitValue & 64) == 1 ? PieceColor.WHITE : PieceColor.BLACK;
    }

    public void setColor(PieceColor pieceColor) {
        if (pieceColor == PieceColor.WHITE) {
            this.bitValue = (byte) (this.bitValue | 64);
        } else {

            this.bitValue = (byte)(this.bitValue ^ 64);
        }

    }


    public enum PieceType {
        EMPTY(0),
        PAWN(1),
        ROOK (2),
        KNIGHT(4),
        BISHOP(8),
        QUEEN(16),
        KING (32);

        private byte value;
        PieceType(int b) {
            this.value = (byte)b;
        }


    }

    public enum PieceColor {
        BLACK,
        WHITE;

    }

}


