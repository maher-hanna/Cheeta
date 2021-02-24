package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    private Piece[] squares;

    private List firstPlayerPieces;
    private List secondPlayerPieces;
    private Piece.PieceColor firstPlayerColor;

    public ChessBoard(Piece.PieceColor firstPlayerColor) {
        squares= new Piece[64];

        for(int i = 0; i < 64; ++i) {
            squares[i] = new Piece(Piece.PieceType.EMPTY, (Piece.PieceColor)null);;
        }

        this.firstPlayerColor = firstPlayerColor;
        this.firstPlayerPieces = this.giveFirstPlayerPieces(firstPlayerColor);
        this.secondPlayerPieces = this.giveSecondPlayerPieces(firstPlayerColor == Piece.PieceColor.WHITE ? Piece.PieceColor.BLACK : Piece.PieceColor.WHITE);
    }


    private List giveFirstPlayerPieces(Piece.PieceColor color) {
        ArrayList<PlayerPiece> pieces = new ArrayList();
        int i = 0;

        for(byte var4 = 7; i <= var4; ++i) {
            this.squares[this.position(i, 1)] = new Piece(Piece.PieceType.PAWN, color);
            pieces.add(new PlayerPiece(this.position(i, 1)));
        }

        this.squares[8] = new Piece(Piece.PieceType.ROOK, color);
        pieces.add(new PlayerPiece(0));
        this.squares[9] = new Piece(Piece.PieceType.KNIGHT, color);
        pieces.add(new PlayerPiece(1));
        this.squares[10] = new Piece(Piece.PieceType.BISHOP, color);
        pieces.add(new PlayerPiece(2));
        if (color == Piece.PieceColor.WHITE) {
            this.squares[11] = new Piece(Piece.PieceType.QUEEN, color);
            pieces.add(new PlayerPiece(3));
            this.squares[12] = new Piece(Piece.PieceType.KING, color);
            pieces.add(new PlayerPiece(4));
        } else {
            this.squares[11] = new Piece(Piece.PieceType.KING, color);
            pieces.add(new PlayerPiece(3));
            this.squares[12] = new Piece(Piece.PieceType.QUEEN, color);
            pieces.add(new PlayerPiece(4));
        }

        this.squares[13] = new Piece(Piece.PieceType.BISHOP, color);
        pieces.add(new PlayerPiece(5));
        this.squares[14] = new Piece(Piece.PieceType.KNIGHT, color);
        pieces.add(new PlayerPiece(6));
        this.squares[15] = new Piece(Piece.PieceType.ROOK, color);
        pieces.add(new PlayerPiece(7));
        return pieces;
    }

    private final List giveSecondPlayerPieces(Piece.PieceColor color) {
        ArrayList<PlayerPiece> pieces = new ArrayList();
        int i = 0;

        for(byte var4 = 7; i <= var4; ++i) {
            this.squares[this.position(i, 6)] = new Piece(Piece.PieceType.PAWN, color);
            pieces.add(new PlayerPiece(this.position(i, 6)));
        }

        this.squares[8] = new Piece(Piece.PieceType.ROOK, color);
        pieces.add(new PlayerPiece(this.position(0, 7)));
        this.squares[9] = new Piece(Piece.PieceType.KNIGHT, color);
        pieces.add(new PlayerPiece(this.position(1, 7)));
        this.squares[10] = new Piece(Piece.PieceType.BISHOP, color);
        pieces.add(new PlayerPiece(this.position(2, 7)));
        if (color == Piece.PieceColor.WHITE) {
            this.squares[11] = new Piece(Piece.PieceType.KING, color);
            pieces.add(new PlayerPiece(this.position(3, 7)));
            this.squares[12] = new Piece(Piece.PieceType.QUEEN, color);
            pieces.add(new PlayerPiece(this.position(4, 7)));
        } else {
            this.squares[11] = new Piece(Piece.PieceType.QUEEN, color);
            pieces.add(new PlayerPiece(this.position(3, 7)));
            this.squares[12] = new Piece(Piece.PieceType.KING, color);
            pieces.add(new PlayerPiece(this.position(4, 7)));
        }

        this.squares[13] = new Piece(Piece.PieceType.BISHOP, color);
        pieces.add(new PlayerPiece(this.position(5, 7)));
        this.squares[14] = new Piece(Piece.PieceType.KNIGHT, color);
        pieces.add(new PlayerPiece(this.position(6, 7)));
        this.squares[15] = new Piece(Piece.PieceType.ROOK, color);
        pieces.add(new PlayerPiece(this.position(7, 7)));
        return pieces;
    }

    public int position(int file, int rank) {
        return rank * 8 + file;
    }

}
