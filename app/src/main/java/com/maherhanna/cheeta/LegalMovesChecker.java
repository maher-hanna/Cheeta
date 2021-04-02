package com.maherhanna.cheeta;

import java.util.ArrayList;
import java.util.Iterator;

public class LegalMovesChecker {

    public static LegalMoves getBlackLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> blackPositions = chessBoard.getBlackPositions();


        for (int i = 0; i < blackPositions.size(); i++) {
            int position = blackPositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);

            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);
            legalMoves.addMovesFor(position, pieceLegalMoves);

        }
        checkCastling(chessBoard, legalMoves, Piece.Color.BLACK, kingInCheck);

        return legalMoves;
    }

    public static LegalMoves getWhiteLegalMoves(ChessBoard chessBoard, boolean kingInCheck) {
        LegalMoves legalMoves = new LegalMoves();
        ArrayList<Integer> whitePositions = chessBoard.getWhitePositions();
        for (int i = 0; i < whitePositions.size(); i++) {
            int position = whitePositions.get(i);
            Piece piece = chessBoard.getPieceAt(position);
            ArrayList<Move> pieceLegalMoves = getPieceMoves(chessBoard, piece, kingInCheck);
            removeMovesThatExposeKing(chessBoard, pieceLegalMoves, piece);

            legalMoves.addMovesFor(position, pieceLegalMoves);
        }
        checkCastling(chessBoard, legalMoves, Piece.Color.WHITE, kingInCheck);
        return legalMoves;
    }


    private static void checkCastling(ChessBoard chessBoard, LegalMoves legalMoves,
                                      Piece.Color color, boolean kingInCheck) {

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);
        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard, color);
        int kingTarget = 0;
        if (canCastleKingSide(chessBoard, color, kingInCheck) == true) {
            kingTarget = initialKingPosition + 2;
            legalMoves.addMoveFor(initialKingPosition, new Move(initialKingPosition,
                    kingTarget, Move.Type.CASTLING_kING_SIDE));
        }
        if (canCastleQueenSide(chessBoard, color, kingInCheck) == true) {
            kingTarget = initialKingPosition - 2;
            legalMoves.addMoveFor(initialKingPosition, new Move(initialKingPosition,
                    kingTarget, Move.Type.CASTLING_QUEEN_SIDE));

        }

    }

    public static int getInitialRookKingSide(ChessBoard chessBoard, Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 7;
        } else {
            return 63;
        }

    }

    public static int getInitialRookQueenSide(ChessBoard chessBoard, Piece.Color rookColor) {
        if (rookColor == Piece.Color.WHITE) {
            return 0;
        } else {
            return 56;
        }
    }

    public static int getInitialKingPosition(ChessBoard chessBoard, Piece.Color kingColor) {
        if (kingColor == Piece.Color.WHITE) {
            return 4;
        } else {
            return 60;
        }
    }


    private static boolean isSquareAttacked(ChessBoard chessBoard, int square, Piece.Color opponentColor) {
        Piece.Color color = opponentColor.getOpposite();

        // attacked by a pawn
        int opposingToRightPawnSquare = 0;
        int opposingToLeftPawnSquare = 0;
        if (color == Piece.Color.WHITE) {
            opposingToRightPawnSquare = ChessBoard.offset(square, 1, 1);
            opposingToLeftPawnSquare = ChessBoard.offset(square, -1, 1);

        } else {
            opposingToRightPawnSquare = ChessBoard.offset(square, -1, -1);
            opposingToLeftPawnSquare = ChessBoard.offset(square, 1, -1);

        }
        if (chessBoard.isPieceAt(opposingToLeftPawnSquare, Piece.Type.PAWN, opponentColor)) {
            return true;
        }
        if (chessBoard.isPieceAt(opposingToRightPawnSquare, Piece.Type.PAWN, opponentColor)) {
            return true;
        }
        //--------------------------


        int attackingSquare = ChessBoard.OUT_OF_BOARD;


        // attacked by a bishop or diagonal queen
        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                fileOffset++;
                rankOffset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }



        }


        //check the diagonal line along the upper left direction
        fileOffset = -1;
        rankOffset = 1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                fileOffset--;
                rankOffset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the diagonal line along the lower right direction
        fileOffset = 1;
        rankOffset = -1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                fileOffset++;
                rankOffset--;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the diagonal line along the lower left direction
        fileOffset = -1;
        rankOffset = -1;
        while (true) {
            attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                fileOffset--;
                rankOffset--;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.BISHOP ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }
        //--------------------------


        //attacked by a rook or queen
        //check the rank
        //to the right of the rook
        int offset = 1;
        while (true) {
            attackingSquare = ChessBoard.offsetFile(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                offset++;
                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the rank
        //to the left of the rook
        offset = -1;
        while (true) {
            attackingSquare = ChessBoard.offsetFile(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                offset--;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the file
        //to the top of the rook
        offset = 1;
        while (true) {
            attackingSquare = ChessBoard.offsetRank(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                offset++;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }


        //check the file
        //to the bottom of the rook
        offset = -1;
        while (true) {
            attackingSquare = ChessBoard.offsetRank(square, offset);
            if (attackingSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(attackingSquare)){
                offset--;

                continue;
            }
            if (chessBoard.getPieceColor(attackingSquare) == color) break;
            if (chessBoard.getPieceType(attackingSquare) == Piece.Type.ROOK ||
                    chessBoard.getPieceType(attackingSquare) == Piece.Type.QUEEN) {
                return true;
            } else {
                break;
            }
        }

        //------------------------------

        //attacked by king
        fileOffset = 0;
        rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KING) {
                        return true;
                    } else {
                        continue;
                    }
                }

            }
        }
        //--------------------------

        //attacked by knight
        fileOffset = 0;
        rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KNIGHT) {
                        return true;
                    } else {
                        continue;
                    }
                }

            }
        }

        for (rankOffset = -2; rankOffset <= 2; rankOffset += 4) {
            for (fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                attackingSquare = ChessBoard.offset(square, fileOffset, rankOffset);
                if (attackingSquare == ChessBoard.OUT_OF_BOARD) continue;
                if (chessBoard.isSquareEmpty(attackingSquare)) continue;

                if (chessBoard.getPieceColor(attackingSquare) == opponentColor) {
                    if (chessBoard.getPieceType(attackingSquare) == Piece.Type.KNIGHT) {
                        return true;
                    } else {
                        continue;
                    }
                }
            }
        }
        //--------------------------


        return false;
    }


    private static void removeMovesThatExposeKing(ChessBoard chessBoard, ArrayList<Move> pieceLegalMoves, Piece piece) {

        Iterator<Move> itr = pieceLegalMoves.iterator();
        while (itr.hasNext()) {
            ChessBoard chessBoardAfterMove = new ChessBoard(chessBoard);

            Move move = itr.next();
            chessBoardAfterMove.movePiece(move);
            if (isSquareAttacked(chessBoardAfterMove,
                    chessBoardAfterMove.getKingPosition(piece.color),piece.color.getOpposite() )) {
                itr.remove();
            }
        }

    }


    public static ArrayList<Move> getPieceMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> pieceLegalMoves = new ArrayList<Move>();

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


    private static boolean canCastleKingSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }

        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialKingPosition)) {
            return false;
        }

        if (chessBoard.moves.hasPieceMoved(initialKingPosition)) {
            return false;
        }


        int initialRookKingSidePosition = getInitialRookKingSide(chessBoard, color);

        if (chessBoard.isSquareEmpty(initialRookKingSidePosition)) {
            return false;
        }

        if (chessBoard.moves.hasPieceMoved(initialRookKingSidePosition)) {
            return false;
        }

        if (!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition + 2)) {
            return false;
        }


        if (isSquareAttacked(chessBoard, initialKingPosition + 1, color.getOpposite()) ||
                isSquareAttacked(chessBoard, initialKingPosition + 2,color.getOpposite() )) {
            return false;
        }

        return true;
    }

    private static boolean canCastleQueenSide(ChessBoard chessBoard, Piece.Color color, boolean kingInCheck) {
        if (kingInCheck) {
            return false;
        }
        int initialKingPosition = getInitialKingPosition(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialKingPosition)) {
            return false;
        }
        if (chessBoard.moves.hasPieceMoved(initialKingPosition)) {
            return false;
        }

        int initialRookQueenSidePosition = getInitialRookQueenSide(chessBoard, color);
        if (chessBoard.isSquareEmpty(initialRookQueenSidePosition)) {
            return false;
        }
        if (chessBoard.moves.hasPieceMoved(initialRookQueenSidePosition)) {
            return false;
        }


        if (!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
                !chessBoard.isSquareEmpty(initialKingPosition - 3)) {
            return false;
        }

        if (isSquareAttacked(chessBoard, initialKingPosition - 1,color.getOpposite() ) ||
                isSquareAttacked(chessBoard, initialKingPosition - 2,color.getOpposite() )) {
            return false;
        }


        return true;
    }

    private static ArrayList<Move> getKnightMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> knightLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -2; fileOffset <= 2; fileOffset += 4) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset += 2) {
                int targetSquare = piece.offset(fileOffset, rankOffset);
                if (targetSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(targetSquare)) {
                    knightLegalMoves.add(new Move(piece.position, targetSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(new Move(piece.position, targetSquare, true));
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
                    knightLegalMoves.add(new Move(piece.position, targetSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(targetSquare).color) {
                        knightLegalMoves.add(new Move(piece.position, targetSquare, true));
                    }
                }
            }
        }

        return knightLegalMoves;

    }

    private static ArrayList<Move> getKingMoves(ChessBoard chessBoard, Piece piece, boolean kingInCheck) {
        ArrayList<Move> kingLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        for (fileOffset = -1; fileOffset <= 1; fileOffset++) {
            for (rankOffset = -1; rankOffset <= 1; rankOffset++) {
                int adjacentSquare = piece.offset(fileOffset, rankOffset);
                if (adjacentSquare == ChessBoard.OUT_OF_BOARD) {
                    continue;
                }
                if (chessBoard.isSquareEmpty(adjacentSquare)) {
                    kingLegalMoves.add(new Move(piece.position, adjacentSquare));
                } else {
                    //check take
                    if (piece.color != chessBoard.getPieceAt(adjacentSquare).color) {
                        kingLegalMoves.add(new Move(piece.position, adjacentSquare, true));
                    }
                }
            }
        }


        return kingLegalMoves;
    }

    private static ArrayList<Move> getQueenMoves(ChessBoard chessBoard, Piece piece,
                                                 boolean kingInCheck) {
        ArrayList<Move> queenLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                queenLegalMoves.add(new Move(piece.position, upperRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, upperRightSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, upperLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, upperLeftSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, lowerRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, lowerRightSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, lowerLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, lowerLeftSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, toRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, toRightSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, toLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, toLeftSquare, true));

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
                queenLegalMoves.add(new Move(piece.position, toTopSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, toTopSquare, true));
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
                queenLegalMoves.add(new Move(piece.position, toBottomSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    queenLegalMoves.add(new Move(piece.position, toBottomSquare, true));
                }

                break;

            }
            offset--;
        }
        //-----------------

        return queenLegalMoves;
    }


    private static ArrayList<Move> getPawnMoves(ChessBoard chessBoard, Piece piece,
                                                boolean kingInCheck) {
        ArrayList<Move> pawnLegalMoves = new ArrayList<Move>();

        //the player at the bottom of chess board
        if (piece.color == Piece.Color.WHITE) {
            if (piece.getRank() == ChessBoard.RANK_8) return pawnLegalMoves;

            //check if the upper square is empty
            int oneSquareUp = piece.offsetRank(1);
            if (chessBoard.isSquareEmpty(oneSquareUp)) {
                pawnLegalMoves.add(new Move(piece.position, oneSquareUp));
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_2) {
                    int twoSquaresUp = piece.offsetRank(2);
                    if (chessBoard.isSquareEmpty(twoSquaresUp)) {
                        pawnLegalMoves.add(new Move(piece.position, twoSquaresUp));
                    }
                }
            }


            //check for takes
            int upperRightSquare = piece.offset(1, 1);
            if (upperRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperRightSquare)) {
                if (chessBoard.getPieceAt(upperRightSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, upperRightSquare));
                }
            }
            int upperLeftSquare = piece.offset(-1, 1);
            if (upperLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(upperLeftSquare)) {
                if (chessBoard.getPieceAt(upperLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, upperLeftSquare));
                }
            }
        }
        //--------------------------


        //the player at the top of chess board
        if (piece.color == Piece.Color.BLACK) {
            if (piece.getRank() == ChessBoard.RANK_1) return pawnLegalMoves;

            //check if the lower square is empty
            int oneSquareDown = piece.offsetRank(-1);
            if (chessBoard.isSquareEmpty(oneSquareDown)) {
                pawnLegalMoves.add(new Move(piece.position, oneSquareDown));
                //first move for the pawn can be tow squares up
                if (piece.getRank() == ChessBoard.RANK_7) {
                    int twoSquaresDown = piece.offsetRank(-2);
                    if (chessBoard.isSquareEmpty(twoSquaresDown)) {
                        pawnLegalMoves.add(new Move(piece.position, twoSquaresDown));
                    }
                }
            }


            //check for takes
            int lowerRightSquare = piece.offset(1, -1);
            if (lowerRightSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerRightSquare)) {
                if (chessBoard.getPieceAt(lowerRightSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, lowerRightSquare, true));
                }
            }
            int lowerLeftSquare = piece.offset(-1, -1);
            if (lowerLeftSquare != ChessBoard.OUT_OF_BOARD && !chessBoard.isSquareEmpty(lowerLeftSquare)) {
                if (chessBoard.getPieceAt(lowerLeftSquare).color != piece.color) {
                    pawnLegalMoves.add(new Move(piece.position, lowerLeftSquare, true));
                }
            }
        }


        return pawnLegalMoves;

    }

    private static ArrayList<Move> getRookMoves(ChessBoard chessBoard, Piece piece,
                                                boolean kingInCheck) {
        ArrayList<Move> rookLegalMoves = new ArrayList<Move>();
        int offset = 0;

        //check the rank
        //to the right of the rook
        offset = 1;
        while (true) {
            int toRightSquare = piece.offsetFile(offset);
            if (toRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(toRightSquare)) {
                rookLegalMoves.add(new Move(piece.position, toRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toRightSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toRightSquare, true));
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
                rookLegalMoves.add(new Move(piece.position, toLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toLeftSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toLeftSquare, true));

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
                rookLegalMoves.add(new Move(piece.position, toTopSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toTopSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toTopSquare, true));
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
                rookLegalMoves.add(new Move(piece.position, toBottomSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(toBottomSquare).color) {
                    rookLegalMoves.add(new Move(piece.position, toBottomSquare, true));
                }

                break;

            }
            offset--;
        }
        //-----------------


        return rookLegalMoves;

    }

    private static ArrayList<Move> getBishopMoves(ChessBoard chessBoard, Piece piece,
                                                  boolean kingInCheck) {
        ArrayList<Move> bishopLegalMoves = new ArrayList<Move>();

        int fileOffset = 0;
        int rankOffset = 0;

        //check the diagonal line along the upper right direction
        fileOffset = 1;
        rankOffset = 1;

        while (true) {
            int upperRightSquare = piece.offset(fileOffset, rankOffset);
            if (upperRightSquare == ChessBoard.OUT_OF_BOARD) break;
            if (chessBoard.isSquareEmpty(upperRightSquare)) {
                bishopLegalMoves.add(new Move(piece.position, upperRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperRightSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, upperRightSquare, true));
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
                bishopLegalMoves.add(new Move(piece.position, upperLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(upperLeftSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, upperLeftSquare, true));
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
                bishopLegalMoves.add(new Move(piece.position, lowerRightSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerRightSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, lowerRightSquare, true));
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
                bishopLegalMoves.add(new Move(piece.position, lowerLeftSquare));
            } else {
                //check take
                if (piece.color != chessBoard.getPieceAt(lowerLeftSquare).color) {
                    bishopLegalMoves.add(new Move(piece.position, lowerLeftSquare, true));
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
