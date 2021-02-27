package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 63;


    private Piece[] squares;
    Player firstPlayer;
    Player secondPlayer;


    public ChessBoard(Player firstPlayer, Player secondPlayer) {
        squares = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            squares[i] = null;
        }
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;

        setUpBoard();
    }

    private void setUpBoard() {
        setUpFirstPlayerPieces(firstPlayer.color);

        setUpSecondPlayerPieces(secondPlayer.color);



    }


    private void setUpFirstPlayerPieces(Piece.PieceColor color) {

        for (int i = 0; i < 8; ++i) {
            int index = getIndex(i, 1);
            this.squares[index] = new Piece(Piece.PieceType.PAWN, color, index);
        }

        this.squares[0] = new Piece(Piece.PieceType.ROOK, color,0);
        this.squares[1] = new Piece(Piece.PieceType.KNIGHT, color,1);
        this.squares[2] = new Piece(Piece.PieceType.BISHOP, color,2);
        if (color == Piece.PieceColor.WHITE) {
            this.squares[3] = new Piece(Piece.PieceType.QUEEN, color,3);
            this.squares[4] = new Piece(Piece.PieceType.KING, color,4);
        } else {
            this.squares[3] = new Piece(Piece.PieceType.KING, color,3);
            this.squares[4] = new Piece(Piece.PieceType.QUEEN, color,4);
        }

        this.squares[5] = new Piece(Piece.PieceType.BISHOP, color,5);
        this.squares[6] = new Piece(Piece.PieceType.KNIGHT, color,6);
        this.squares[7] = new Piece(Piece.PieceType.ROOK, color,7);

    }

    private void setUpSecondPlayerPieces(Piece.PieceColor color) {
        for (int i = 0; i < 8; ++i) {
            this.squares[getIndex(i, 6)] = new Piece(Piece.PieceType.PAWN, color,getIndex(i, 6));
        }

        this.squares[getIndex(0, 7)] = new Piece(Piece.PieceType.ROOK, color,getIndex(0, 7));
        this.squares[getIndex(1, 7)] = new Piece(Piece.PieceType.KNIGHT, color,getIndex(1, 7));
        this.squares[getIndex(2, 7)] = new Piece(Piece.PieceType.BISHOP, color,getIndex(2, 7));
        if (color == Piece.PieceColor.WHITE) {
            this.squares[getIndex(3, 7)] = new Piece(Piece.PieceType.KING, color,getIndex(3, 7));
            this.squares[getIndex(4, 7)] = new Piece(Piece.PieceType.QUEEN, color,getIndex(4, 7));
        } else {
            this.squares[getIndex(3, 7)] = new Piece(Piece.PieceType.QUEEN, color,getIndex(3, 7));
            this.squares[getIndex(4, 7)] = new Piece(Piece.PieceType.KING, color,getIndex(4, 7));
        }

        this.squares[getIndex(5, 7)] = new Piece(Piece.PieceType.BISHOP, color,getIndex(5, 7));
        this.squares[getIndex(6, 7)] = new Piece(Piece.PieceType.KNIGHT, color,getIndex(6, 7));
        this.squares[getIndex(7, 7)] = new Piece(Piece.PieceType.ROOK, color,getIndex(7, 7));
    }

    public int getIndex(int file, int rank) {
        return (rank * 8 )+ file;
    }
    public int getFile(int position){return position % 8; }
    public int getRank(int position){return position / 8;}

    public Piece getPieceAt(int position){
        return squares[position];
    }

}
