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
                pieceMoves = getRookMoves(position);
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
        //--------------------------


        //the player at the top of chess board
        if (pawn.color == chessBoard.playerAtTop.color) {
            if (pawn.getRank() == ChessBoard.RANK_1) return pawnLegalMoves;

            //check if the lower square is empty
            int oneSquareDown = pawn.offsetRank(-1);
            if (chessBoard.isSquareEmpty(oneSquareDown)) {
                pawnLegalMoves.add(oneSquareDown);
                //first move for the pawn can be tow squares up
                if (pawn.getRank() == ChessBoard.RANK_7) {
                    int twoSquaresDown = pawn.offsetRank(-2);
                    if (chessBoard.isSquareEmpty(twoSquaresDown)) {
                        pawnLegalMoves.add(twoSquaresDown);
                    }
                }
            }


            //check for takes
            int lowerRightSquare = pawn.offset(1, -1);
            if (lowerRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerRightSquare)) {
                if (chessBoard.getPieceAt(lowerRightSquare).color != pawn.color) {
                    pawnLegalMoves.add(lowerRightSquare);
                }
            }
            int lowerLeftSquare = pawn.offset(-1, -1);
            if (lowerLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerLeftSquare)) {
                if (chessBoard.getPieceAt(lowerLeftSquare).color != pawn.color) {
                    pawnLegalMoves.add(lowerLeftSquare);
                }
            }
        }


        return pawnLegalMoves;

    }

    private ArrayList<Integer> getRookMoves(int position) {
        ArrayList<Integer> rookLegalMoves = new ArrayList<Integer>();
        Piece rook = chessBoard.getPieceAt(position);
        int offset = 0;

        //check the rank
        //to the right of the rook
        offset = 1;
        while (true) {
            int toRightSquare = rook.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                rookLegalMoves.add(toRightSquare);
            } else {
                //check take
                if (rook.color != chessBoard.getPieceAt(toRightSquare).color) {
                    rookLegalMoves.add(toRightSquare);
                }
                break;

            }
            offset++;
        }
        //to the left of the rook
        offset = -1;
        while (true) {
            int toLeftSquare = rook.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                rookLegalMoves.add(toLeftSquare);
            } else {
                //check take
                if (rook.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    rookLegalMoves.add(toLeftSquare);

                }
                break;

            }
            offset--;
        }
        //-----------------


        //check the file
        //to the top of the rook
        offset = 1;
        while (true) {
            int toTopSquare = rook.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                rookLegalMoves.add(toTopSquare);
            } else {
                //check take
                if (rook.color != chessBoard.getPieceAt(toTopSquare).color) {
                    rookLegalMoves.add(toTopSquare);
                }
                break;

            }
            offset++;
        }
        //to the bottom of the rook
        offset = -1;
        while (true) {
            int toBottomSquare = rook.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                rookLegalMoves.add(toBottomSquare);
            } else {
                //check take
                if (rook.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    rookLegalMoves.add(toBottomSquare);
                }

                break;

            }
            offset--;
        }
        //-----------------

        return rookLegalMoves;

    }


}
