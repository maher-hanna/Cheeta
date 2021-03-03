package com.maherhanna.cheeta;

import java.util.ArrayList;

public class LegalMoves {
    private ChessBoard chessBoard;

    public LegalMoves(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public ArrayList<Integer> getLegalMoves(int position) {
        ArrayList<Integer> pieceMoves = new ArrayList<Integer>();
        Piece piece = chessBoard.getPieceAt(position);

        switch (piece.type) {
            case PAWN:
                pieceMoves = getPawnMoves(position);
                break;
            case ROOK:
                break;
            case KNIGHT:
                break;
            case BISHOP:
                break;
            case QUEEN:
                break;
            case KING:
                break;
        }
        return pieceMoves;
    }

    private ArrayList<Integer> getPawnMoves(int position) {
        ArrayList<Integer> pawnLegalMoves = new ArrayList<Integer>();
        Piece pawn = chessBoard.getPieceAt(position);

        //the player at the bottom of chess board
        if (pawn.color == chessBoard.playerAtBottom.color) {
            if (pawn.getRank() == ChessBoard.RANK_8) return pawnLegalMoves;

            //check if the upper square is empty
            int oneSquareUp = pawn.offsetRank(1);
            if (chessBoard.isSquareEmpty(oneSquareUp)) {
                pawnLegalMoves.add(oneSquareUp);
                //first move for the pawn can be tow squares up
                if (pawn.getRank() == ChessBoard.RANK_2) {
                    int twoSquaresUp = pawn.offsetRank(2);
                    if (chessBoard.isSquareEmpty(twoSquaresUp)) {
                        pawnLegalMoves.add(twoSquaresUp);
                    }
                }
            }


            //check for takes
            int upperRightSquare = pawn.offset(1, 1);
            if (upperRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperRightSquare)) {
                if (chessBoard.getPieceAt(upperRightSquare).color != pawn.color) {
                    pawnLegalMoves.add(upperRightSquare);
                }
            }
            int upperLeftSquare = pawn.offset(-1, 1);
            if (upperLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperLeftSquare)) {
                if (chessBoard.getPieceAt(upperLeftSquare).color != pawn.color) {
                    pawnLegalMoves.add(upperLeftSquare);
                }
            }
        }






        return pawnLegalMoves;

    }

    private ArrayList<Integer> getRookMoves(Piece rook) {
        ArrayList<Integer> rookMoves = new ArrayList<Integer>();

        return rookMoves;

    }


}
