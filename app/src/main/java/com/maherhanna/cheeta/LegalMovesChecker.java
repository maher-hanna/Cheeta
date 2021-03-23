package com.maherhanna.cheeta;

import java.util.ArrayList;

public class LegalMovesChecker {
    private ChessBoard chessBoard;

    public LegalMovesChecker(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public ArrayList<Integer> getLegalMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> pieceLegalMoves = new ArrayList<Integer>();
        Square square = piece.getSquare();

        switch (square.type) {
            case PAWN:
                pieceLegalMoves = getPawnMoves(piece,kingInCheck);
                break;
            case ROOK:
                pieceLegalMoves = getRookMoves(piece,kingInCheck);
                break;
            case KNIGHT:
                pieceLegalMoves = getKnightMoves(piece,kingInCheck);
                break;
            case BISHOP:
                pieceLegalMoves = getBishopMoves(piece,kingInCheck);

                break;
            case QUEEN:
                pieceLegalMoves = getQueenMoves(piece,kingInCheck);

                break;
            case KING:
                pieceLegalMoves = getKingMoves(piece,kingInCheck);
                break;
        }
        return pieceLegalMoves;
    }


    private ArrayList<Integer> getKnightMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> knightLegalMoves = new ArrayList<Integer>();
        Square knight = piece.getSquare();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                int targetSquare = knight.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(targetSquare);
                } else {
                    //check take
                    if (knight.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(targetSquare);
                    }
                }
            }
        }

        for (rankOffset = -2; rankOffset <= 2; rankOffset += 4) {
            for (fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                int targetSquare = knight.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(targetSquare);
                } else {
                    //check take
                    if (knight.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(targetSquare);
                    }
                }
            }
        }

        return knightLegalMoves;

    }

    private ArrayList<Integer> getKingMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> kingLegalMoves = new ArrayList<Integer>();
        Square king = piece.getSquare();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                int adjacentSquare = king.offset(fileOffset, rankOffset);
                if (adjacentSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(adjacentSquare)) {
                    kingLegalMoves.add(adjacentSquare);
                } else {
                    //check take
                    if (king.color != chessBoard.getPieceAt(adjacentSquare).color) {
                        kingLegalMoves.add(adjacentSquare);
                    }
                }
            }
        }
        return kingLegalMoves;
    }

    private ArrayList<Integer> getQueenMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> queenLegalMoves = new ArrayList<Integer>();
        Square queen = piece.getSquare();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = queen.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                queenLegalMoves.add(upperRightSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    queenLegalMoves.add(upperRightSquare);
                }
                break;

            }
            fileOffset++;
            rankOffset++;
        }
        //--------------------

        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            int upperLeftSquare = queen.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                queenLegalMoves.add(upperLeftSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    queenLegalMoves.add(upperLeftSquare);
                }
                break;

            }
            fileOffset--;
            rankOffset++;
        }
        //--------------------


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            int lowerRightSquare = queen.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                queenLegalMoves.add(lowerRightSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    queenLegalMoves.add(lowerRightSquare);
                }
                break;

            }
            fileOffset++;
            rankOffset--;
        }
        //--------------------

        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            int lowerLeftSquare = queen.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                queenLegalMoves.add(lowerLeftSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    queenLegalMoves.add(lowerLeftSquare);
                }
                break;

            }
            fileOffset--;
            rankOffset--;
        }
        //--------------------


        //check the rank
        //to the right of the queen
        int offset = 1;
        while (true) {
            int toRightSquare = queen.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                queenLegalMoves.add(toRightSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(toRightSquare).color) {
                    queenLegalMoves.add(toRightSquare);
                }
                break;

            }
            offset++;
        }
        //to the left of the queen
        offset = -1;
        while (true) {
            int toLeftSquare = queen.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                queenLegalMoves.add(toLeftSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    queenLegalMoves.add(toLeftSquare);

                }
                break;

            }
            offset--;
        }
        //-----------------


        //check the file
        //to the top of the queen
        offset = 1;
        while (true) {
            int toTopSquare = queen.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                queenLegalMoves.add(toTopSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(toTopSquare).color) {
                    queenLegalMoves.add(toTopSquare);
                }
                break;

            }
            offset++;
        }
        //to the bottom of the queen
        offset = -1;
        while (true) {
            int toBottomSquare = queen.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                queenLegalMoves.add(toBottomSquare);
            } else {
                //check take
                if (queen.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    queenLegalMoves.add(toBottomSquare);
                }

                break;

            }
            offset--;
        }
        //-----------------

        return queenLegalMoves;
    }


    private ArrayList<Integer> getPawnMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> pawnLegalMoves = new ArrayList<Integer>();
        Square pawn = piece.getSquare();

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

    private ArrayList<Integer> getRookMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> rookLegalMoves = new ArrayList<Integer>();
        Square rook = piece.getSquare();
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

    private ArrayList<Integer> getBishopMoves(Piece piece, boolean kingInCheck) {
        ArrayList<Integer> bishopLegalMoves = new ArrayList<Integer>();
        Square bishop = piece.getSquare();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = bishop.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                bishopLegalMoves.add(upperRightSquare);
            } else {
                //check take
                if (bishop.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    bishopLegalMoves.add(upperRightSquare);
                }
                break;

            }
            fileOffset++;
            rankOffset++;
        }
        //--------------------

        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            int upperLeftSquare = bishop.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                bishopLegalMoves.add(upperLeftSquare);
            } else {
                //check take
                if (bishop.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    bishopLegalMoves.add(upperLeftSquare);
                }
                break;

            }
            fileOffset--;
            rankOffset++;
        }
        //--------------------


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            int lowerRightSquare = bishop.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                bishopLegalMoves.add(lowerRightSquare);
            } else {
                //check take
                if (bishop.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    bishopLegalMoves.add(lowerRightSquare);
                }
                break;

            }
            fileOffset++;
            rankOffset--;
        }
        //--------------------

        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            int lowerLeftSquare = bishop.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                bishopLegalMoves.add(lowerLeftSquare);
            } else {
                //check take
                if (bishop.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    bishopLegalMoves.add(lowerLeftSquare);
                }
                break;

            }
            fileOffset--;
            rankOffset--;
        }
        //--------------------


        return bishopLegalMoves;


    }



}
