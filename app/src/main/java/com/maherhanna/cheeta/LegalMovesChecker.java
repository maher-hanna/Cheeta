package com.maherhanna.cheeta;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class LegalMovesChecker {

    public static LegalMoves getBlackLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> blackPositions = chessBoard.getBlackPositions();
        for (int i = 0; i < blackPositions.size(); i++) {
            int position = blackPositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Integer> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);
            legalMoves.addMovesFor(position, pieceLegalMoves);
        }
        return legalMoves;
    }

    public static LegalMoves getWhiteLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> whitePositions = chessBoard.getWhitePositions();
        for (int i = 0; i < whitePositions.size(); i++) {
            int position = whitePositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Integer> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);
            legalMoves.addMovesFor(position, pieceLegalMoves);
        }
        return legalMoves;
    }


    private static boolean isKingExposedToCheck(ChessBoard chessBoard, Piece.Color kingColor) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> opponentPositions;
        opponentPositions = chessBoard.getPositionsFor(kingColor);

        for (int i = 0; i < opponentPositions.size(); i++) {
            int position = opponentPositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Integer> pieceLegalMoves = getPieceMoves(chessBoard, piece, false);
            legalMoves.addMovesFor(position,
                    getPieceMoves(chessBoard, piece, false));
        }
        if(legalMoves.contains(chessBoard.getKingPosition(kingColor))){
            return true;
        }
        else {
            return false;
        }
    }


    private static void removeMovesThatExposeKing(ChessBoard chessBoard, ArrayList<Integer> pieceLegalMoves, Piece piece) {

        Iterator<Integer> itr = pieceLegalMoves.iterator();
        while (itr.hasNext()){
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);
            int position = itr.next();
            chessBoardAfterMove.movePiece(piece.getPosition(), position);
            if(isKingExposedToCheck(chessBoardAfterMove,piece.color)){
                itr.remove();
            }
        }

    }


    public static ArrayList<Integer> getPieceMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> pieceLegalMoves = new ArrayList<Integer>();

        switch (piece.type) {
            case PAWN:
                pieceLegalMoves = getPawnMoves(chessBoard, piece, kingInCheck);
                break;
            case ROOK:
                pieceLegalMoves = getRookMoves(chessBoard, piece, kingInCheck);
                break;
            case KNIGHT:
                pieceLegalMoves = getKnightMoves(chessBoard, piece, kingInCheck);
                break;
            case BISHOP:
                pieceLegalMoves = getBishopMoves(chessBoard, piece, kingInCheck);

                break;
            case QUEEN:
                pieceLegalMoves = getQueenMoves(chessBoard, piece, kingInCheck);

                break;
            case KING:
                pieceLegalMoves = getKingMoves(chessBoard, piece, kingInCheck);
                break;
        }
        return pieceLegalMoves;
    }


    private static ArrayList<Integer> getKnightMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> knightLegalMoves = new ArrayList<Integer>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                int targetSquare = piece.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(targetSquare);
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(targetSquare);
                    }
                }
            }
        }

        for (rankOffset = -2; rankOffset <= 2; rankOffset += 4) {
            for (fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                int targetSquare = piece.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(targetSquare);
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(targetSquare);
                    }
                }
            }
        }

        return knightLegalMoves;

    }

    private static ArrayList<Integer> getKingMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> kingLegalMoves = new ArrayList<Integer>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                int adjacentSquare = piece.offset(fileOffset, rankOffset);
                if (adjacentSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(adjacentSquare)) {
                    kingLegalMoves.add(adjacentSquare);
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(adjacentSquare).color) {
                        kingLegalMoves.add(adjacentSquare);
                    }
                }
            }
        }
        return kingLegalMoves;
    }

    private static ArrayList<Integer> getQueenMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> queenLegalMoves = new ArrayList<Integer>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                queenLegalMoves.add(upperRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
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
            int upperLeftSquare = piece.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                queenLegalMoves.add(upperLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
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
            int lowerRightSquare = piece.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                queenLegalMoves.add(lowerRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
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
            int lowerLeftSquare = piece.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                queenLegalMoves.add(lowerLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
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
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                queenLegalMoves.add(toRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    queenLegalMoves.add(toRightSquare);
                }
                break;

            }
            offset++;
        }
        //to the left of the queen
        offset = -1;
        while (true) {
            int toLeftSquare = piece.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                queenLegalMoves.add(toLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
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
            int toTopSquare = piece.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                queenLegalMoves.add(toTopSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    queenLegalMoves.add(toTopSquare);
                }
                break;

            }
            offset++;
        }
        //to the bottom of the queen
        offset = -1;
        while (true) {
            int toBottomSquare = piece.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                queenLegalMoves.add(toBottomSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    queenLegalMoves.add(toBottomSquare);
                }

                break;

            }
            offset--;
        }
        //-----------------

        return queenLegalMoves;
    }


    private static ArrayList<Integer> getPawnMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> pawnLegalMoves = new ArrayList<Integer>();

        //the player at the bottom of chess board
        if (piece.color == chessBoard.bottomPlayerColor) {
            if (piece.getRank() == ChessBoard.RANK_8) return pawnLegalMoves;

            //check if the upper square is empty
            int oneSquareUp = piece.offsetRank(1);
            if (chessBoard.isSquareEmpty(oneSquareUp)) {
                pawnLegalMoves.add(oneSquareUp);
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_2) {
                    int twoSquaresUp = piece.offsetRank(2);
                    if (chessBoard.isSquareEmpty(twoSquaresUp)) {
                        pawnLegalMoves.add(twoSquaresUp);
                    }
                }
            }


            //check for takes
            int upperRightSquare = piece.offset(1, 1);
            if (upperRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperRightSquare)) {
                if (chessBoard.getPieceAt(upperRightSquare).color != piece.color) {
                    pawnLegalMoves.add(upperRightSquare);
                }
            }
            int upperLeftSquare = piece.offset(-1, 1);
            if (upperLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperLeftSquare)) {
                if (chessBoard.getPieceAt(upperLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(upperLeftSquare);
                }
            }
        }
        //--------------------------


        //the player at the top of chess board
        if (piece.color == chessBoard.topPlayerColor) {
            if (piece.getRank() == ChessBoard.RANK_1) return pawnLegalMoves;

            //check if the lower square is empty
            int oneSquareDown = piece.offsetRank(-1);
            if (chessBoard.isSquareEmpty(oneSquareDown)) {
                pawnLegalMoves.add(oneSquareDown);
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_7) {
                    int twoSquaresDown = piece.offsetRank(-2);
                    if (chessBoard.isSquareEmpty(twoSquaresDown)) {
                        pawnLegalMoves.add(twoSquaresDown);
                    }
                }
            }


            //check for takes
            int lowerRightSquare = piece.offset(1, -1);
            if (lowerRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerRightSquare)) {
                if (chessBoard.getPieceAt(lowerRightSquare).color != piece.color) {
                    pawnLegalMoves.add(lowerRightSquare);
                }
            }
            int lowerLeftSquare = piece.offset(-1, -1);
            if (lowerLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerLeftSquare)) {
                if (chessBoard.getPieceAt(lowerLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(lowerLeftSquare);
                }
            }
        }


        return pawnLegalMoves;

    }

    private static ArrayList<Integer> getRookMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> rookLegalMoves = new ArrayList<Integer>();
        int offset = 0;

        //check the rank
        //to the right of the rook
        offset = 1;
        while (true) {
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                rookLegalMoves.add(toRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    rookLegalMoves.add(toRightSquare);
                }
                break;

            }
            offset++;
        }
        //to the left of the rook
        offset = -1;
        while (true) {
            int toLeftSquare = piece.offsetFile(offset);
            if (toLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toLeftSquare)) {
                rookLegalMoves.add(toLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
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
            int toTopSquare = piece.offsetRank(offset);
            if (toTopSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toTopSquare)) {
                rookLegalMoves.add(toTopSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    rookLegalMoves.add(toTopSquare);
                }
                break;

            }
            offset++;
        }
        //to the bottom of the rook
        offset = -1;
        while (true) {
            int toBottomSquare = piece.offsetRank(offset);
            if (toBottomSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toBottomSquare)) {
                rookLegalMoves.add(toBottomSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    rookLegalMoves.add(toBottomSquare);
                }

                break;

            }
            offset--;
        }
        //-----------------

        return rookLegalMoves;

    }

    private static ArrayList<Integer> getBishopMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Integer> bishopLegalMoves = new ArrayList<Integer>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                bishopLegalMoves.add(upperRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
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
            int upperLeftSquare = piece.offset(fileOffset, rankOffset);
            if (upperLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperLeftSquare)) {
                bishopLegalMoves.add(upperLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
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
            int lowerRightSquare = piece.offset(fileOffset, rankOffset);
            if (lowerRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerRightSquare)) {
                bishopLegalMoves.add(lowerRightSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
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
            int lowerLeftSquare = piece.offset(fileOffset, rankOffset);
            if (lowerLeftSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(lowerLeftSquare)) {
                bishopLegalMoves.add(lowerLeftSquare);
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
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
