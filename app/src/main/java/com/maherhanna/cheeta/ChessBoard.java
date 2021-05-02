package com.maherhanna.cheeta;

import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;

public class ChessBoard {
    //public constants
    //-------------------------------------------------------------------------------
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 63;
    public static final int OUT = -1;
    public static final int NO_SQUARE = -1;
    public static final int EMPTY = -1;

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


    public static final int NO_CASTLING = 0;
    public static final int CASTLING_KING_SIDE = 1;
    public static final int CASTLING_QUEEN_SIDE = 2;
    public static final int CASTLING_BOTH_SIDES = 3;


    //----------------------------------------------------------------------------------


    //data
    //-----------------------------------------------------------------------------------
    public ChessboardMoves moves;
    public ArrayList<State> states;

    LegalMoves blackLegalMoves;
    LegalMoves whiteLegalMoves;

    long whitePawns = 0;
    long whiteRooks = 0;
    long whiteBishops = 0;
    long whiteKnights = 0;
    long whiteQueens = 0;
    long whiteKing = 0;

    long blackPawns = 0;
    long blackRooks = 0;
    long blackBishops = 0;
    long blackKnights = 0;
    long blackQueens = 0;
    long blackKing = 0;

    long allWhitePieces = 0;
    long allBlackPieces = 0;

    long emptySquares = 0;
    long allPieces = 0;

    //state
    int toPlayColor = Piece.WHITE;
    int fiftyMovesDrawCount = 0;
    int fullMovesCount = 1;


    //---------------------------------------------------------------------------------


    public ChessBoard(ChessBoard copy) {
        this.moves = new ChessboardMoves(copy.moves);
        this.states = new ArrayList<>(copy.states);

        whitePawns = copy.whitePawns;
        whiteRooks = copy.whiteRooks;
        whiteBishops = copy.whiteBishops;
        whiteKnights = copy.whiteKnights;
        whiteQueens = copy.whiteQueens;
        whiteKing = copy.whiteKing;

        blackPawns = copy.blackPawns;
        blackRooks = copy.blackRooks;
        blackBishops = copy.blackBishops;
        blackKnights = copy.blackKnights;
        blackQueens = copy.blackQueens;
        blackKing = copy.blackKing;

        allWhitePieces = copy.allWhitePieces;
        allBlackPieces = copy.allBlackPieces;

        emptySquares = copy.emptySquares;
        allPieces = copy.allPieces;

        toPlayColor = copy.toPlayColor;

        fiftyMovesDrawCount = copy.fiftyMovesDrawCount;
        fullMovesCount = copy.fullMovesCount;
    }


    public ChessBoard() {
        moves = new ChessboardMoves();
        states = new ArrayList<>();

        blackLegalMoves = new LegalMoves();
        whiteLegalMoves = new LegalMoves();

    }

    public void setUpBoard(String fenString) {
        setupFromFen(fenString);

    }

    public void setupFromFen(String fenString) {
        int currentFile = FILE_A;
        int currentRank = RANK_8;
        int currentChar = 0;
        int blackCastlingRights = 0;
        int whiteCastlingRights = 0;
        int enPassantTarget = NO_SQUARE;


        //parse piece placement
        //------------------------------------------------------------------------------
        while (currentRank >= RANK_1) {

            switch (fenString.charAt(currentChar)) {
                case 'r':
                    addBlackRook(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'p':
                    addBlackPawn(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'b':
                    addBlackBishop(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'n':
                    addBlackKnight(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'q':
                    addBlackQueen(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'k':
                    addBlackKing(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'R':
                    addWhiteRook(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'P':
                    addWhitePawn(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'B':
                    addWhiteBishop(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'N':
                    addWhiteKnight(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'Q':
                    addWhiteQueen(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case 'K':
                    addWhiteKing(GetPosition(currentFile, currentRank));
                    currentFile++;
                    break;
                case '1':
                    currentFile += 1;
                    break;
                case '2':
                    currentFile += 2;
                    break;
                case '3':
                    currentFile += 3;
                    break;
                case '4':
                    currentFile += 4;
                    break;
                case '5':
                    currentFile += 5;
                    break;
                case '6':
                    currentFile += 6;
                    break;
                case '7':
                    currentFile += 7;
                    break;
                case '8':
                    currentFile += 8;
                    break;
            }
            currentChar++;

            if (currentFile > FILE_H) {
                currentFile = FILE_A;
                currentRank--;
            }
        }
        //***************************************************************************

        //skip space
        currentChar++;


        //parse active color
        if (fenString.charAt(currentChar) == 'w') toPlayColor = Piece.WHITE;
        else toPlayColor = Piece.BLACK;
        currentChar++;


        //skip space
        currentChar++;

        //parse castling rights
        //-----------------------------------------------------------------------------------
        while (fenString.charAt(currentChar) != ' ') {
            switch (fenString.charAt(currentChar)) {
                case '-':
                    whiteCastlingRights = NO_CASTLING;
                    blackCastlingRights = NO_CASTLING;
                    currentChar++;
                    break;
                case 'K':
                    whiteCastlingRights = whiteCastlingRights | CASTLING_KING_SIDE;
                    currentChar++;
                    break;
                case 'Q':
                    whiteCastlingRights = whiteCastlingRights | CASTLING_QUEEN_SIDE;
                    currentChar++;
                    break;
                case 'k':
                    blackCastlingRights = blackCastlingRights | CASTLING_KING_SIDE;
                    currentChar++;
                    break;
                case 'q':
                    blackCastlingRights = blackCastlingRights | CASTLING_QUEEN_SIDE;
                    currentChar++;
                    break;
            }
        }
        //--------------------------------------------------------------------------------

        //skip space
        currentChar++;


        //parse En passant target square
        if (fenString.charAt(currentChar) == '-') enPassantTarget = NO_SQUARE;
        enPassantTarget = Square(fenString.substring(currentChar, currentChar + 2));
        currentChar++;

        //skip space
        currentChar++;


        Scanner scanner = new Scanner(fenString.substring(currentChar));

        //parse number of half moves since last pawn or capture occured
        fiftyMovesDrawCount = scanner.nextInt();

        //parse full move count since start of game
        fullMovesCount = scanner.nextInt();


        State startState = new State(allPieces, enPassantTarget, blackCastlingRights, whiteCastlingRights);
        states.add(startState);
        updateWhiteLegalMoves();
        updateBlackLegalMoves();

    }

    private void addBlackPawn(int square) {
        blackPawns |= 1L << square;
        allBlackPieces |= blackPawns;
        allPieces |= blackPawns;
        emptySquares = ~allPieces;
    }

    private void addBlackRook(int square) {
        blackRooks |= 1L << square;
        allBlackPieces |= blackRooks;
        allPieces |= blackRooks;
        emptySquares = ~allPieces;
    }

    private void addBlackBishop(int square) {
        blackBishops |= 1L << square;
        allBlackPieces |= blackBishops;
        allPieces |= blackBishops;
        emptySquares = ~allPieces;
    }

    private void addBlackKnight(int square) {
        blackKnights |= 1L << square;
        allBlackPieces |= blackKnights;
        allPieces |= blackKnights;
        emptySquares = ~allPieces;
    }

    private void addBlackQueen(int square) {
        blackQueens |= 1L << square;
        allBlackPieces |= blackQueens;
        allPieces |= blackQueens;
        emptySquares = ~allPieces;

    }

    private void addBlackKing(int square) {
        blackKing |= 1L << square;
        allBlackPieces |= blackKing;
        allPieces |= blackKing;
        emptySquares = ~allPieces;

    }

    private void addWhitePawn(int square) {
        whitePawns |= 1L << square;
        allWhitePieces |= whitePawns;
        allPieces |= whitePawns;
        emptySquares = ~allPieces;
    }

    private void addWhiteRook(int square) {
        whiteRooks |= 1L << square;
        allWhitePieces |= whiteRooks;
        allPieces |= whiteRooks;
        emptySquares = ~allPieces;

    }

    private void addWhiteBishop(int square) {
        whiteBishops |= 1L << square;
        allWhitePieces |= whiteBishops;
        allPieces |= whiteBishops;
        emptySquares = ~allPieces;

    }

    private void addWhiteKnight(int square) {
        whiteKnights |= 1L << square;
        allWhitePieces |= whiteKnights;
        allPieces |= whiteKnights;
        emptySquares = ~allPieces;

    }

    private void addWhiteQueen(int square) {
        whiteQueens |= 1L << square;
        allWhitePieces |= whiteQueens;
        allPieces |= whiteQueens;
        emptySquares = ~allPieces;

    }

    private void addWhiteKing(int square) {
        whiteKing |= 1L << square;
        allWhitePieces |= whiteKing;
        allPieces |= whiteKing;
        emptySquares = ~allPieces;

    }

    public void removePiece(int square) {

        allPieces = BitMath.popBit(allPieces, square);
        allWhitePieces = allPieces & allWhitePieces;
        allBlackPieces = allPieces & allBlackPieces;

        whitePawns = allPieces & whitePawns;
        whiteRooks = allPieces & whiteRooks;
        whiteKnights = allPieces & whiteKnights;
        whiteBishops = allPieces & whiteBishops;
        whiteQueens = allPieces & whiteQueens;
        whiteKing = allPieces & whiteKing;

        blackPawns = allPieces & blackPawns;
        blackRooks = allPieces & blackRooks;
        blackKnights = allPieces & blackKnights;
        blackBishops = allPieces & blackBishops;
        blackQueens = allPieces & blackQueens;
        blackKing = allPieces & blackKing;

        emptySquares = ~allPieces;
    }


    public void updateBlackLegalMoves() {
        blackLegalMoves = Game.moveGenerator.getBlackLegalMoves(this);
    }


    public void updateWhiteLegalMoves() {
        whiteLegalMoves = Game.moveGenerator.getWhiteLegalMoves(this);

    }

    public void updateLegalMovesFor(int playerColor, boolean kingInCheck) {
        if (playerColor == Piece.WHITE) {
            updateWhiteLegalMoves();
        } else {
            updateBlackLegalMoves();
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


    public LegalMoves getLegalMovesFor(int color) {
        if (color == Piece.WHITE) {
            return whiteLegalMoves;
        } else {
            return blackLegalMoves;
        }
    }


    public ArrayList<Integer> getLegalTargetsFor(int position) {
        if (isPieceWhiteAt(position)) {
            return whiteLegalMoves.getLegalTargetsFor(position);
        } else {
            return blackLegalMoves.getLegalTargetsFor(position);
        }
    }


    public boolean isKingInCheck(int kingColor) {
        return Game.moveGenerator.isKingAttacked(this,kingColor);
    }


    public boolean canMove(int fromSquare, int toSquare) {
        boolean isLegal = false;
        if (isPieceBlackAt(fromSquare)) {
            isLegal = blackLegalMoves.canMove(fromSquare, toSquare);

        } else {
            isLegal = whiteLegalMoves.canMove(fromSquare, toSquare);
        }
        return isLegal;

    }


    public void move(Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        int moveColor = move.getColor();
        int movedPieceType = move.getPieceType();


        setPieceAt(toSquare, move.getPieceType(),moveColor);
        removePiece(fromSquare);

        if (moveColor == Piece.WHITE) fullMovesCount++;


        //set enPassant target
        int enPassantTarget = NO_SQUARE;

        if (move.isPawnDoubleMove()) {
            if (moveColor == Piece.WHITE) {
                enPassantTarget = move.getTo() - 8;
            } else {
                enPassantTarget = move.getTo() + 8;
            }
        }


        int blackCastlingRights = getBlackCastlingRights();
        int whiteCastlingRights = getWhiteCastlingRights();
        if (move.isCastling()) {
            int rookPosition;
            int rookCastlingTarget;
            if (moveColor == Piece.WHITE) {
                whiteCastlingRights = NO_CASTLING;
            } else {
                blackCastlingRights = NO_CASTLING;
            }

            if (move.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = Game.moveGenerator.getInitialRookKingSide(
                        moveColor);
                rookCastlingTarget = move.getFrom() + 1;

            } else {
                rookPosition = Game.moveGenerator.getInitialRookQueenSide(
                        moveColor);
                rookCastlingTarget = move.getFrom() - 1;
            }
            setPieceAt(rookCastlingTarget, Piece.ROOK,moveColor);
            removePiece(rookPosition);
        } else {
            if (movedPieceType == Piece.ROOK) {
                if (moveColor == Piece.WHITE) {
                    if (fromSquare == Game.moveGenerator.getInitialRookKingSide(Piece.WHITE)) {
                        //king side
                        whiteCastlingRights = BitMath.unSetBit(whiteCastlingRights, 0);

                    } else {
                        //queen side
                        whiteCastlingRights = BitMath.unSetBit(whiteCastlingRights, 1);

                    }

                } else {
                    if (fromSquare == Game.moveGenerator.getInitialRookKingSide(Piece.BLACK)) {
                        //king side
                        blackCastlingRights = BitMath.unSetBit(blackCastlingRights, 0);

                    } else {
                        //queen side
                        blackCastlingRights = BitMath.unSetBit(blackCastlingRights, 1);

                    }

                }
            }
            if (movedPieceType == Piece.KING) {
                if (moveColor == Piece.WHITE) {
                    whiteCastlingRights = NO_CASTLING;
                } else {
                    blackCastlingRights = NO_CASTLING;
                }
            }
        }


        if (move.isPromote()) {
            setPieceAt(toSquare, move.getPromotionPieceType(), moveColor);
        }
        if (move.isEnPasant()) {
            if (moveColor == Piece.WHITE) {
                removePiece(toSquare - 8);
            } else {
                removePiece(toSquare + 8);

            }
        }

        move.setPreviousFiftyMoves(fiftyMovesDrawCount);
        //increase fifty moves if no capture or pawn push
        fiftyMovesDrawCount++;
        if (move.isTake() || movedPieceType == Piece.PAWN) {
            fiftyMovesDrawCount = 0;
        }

        State state = new State(allPieces, enPassantTarget, blackCastlingRights, whiteCastlingRights);
        states.add(state);
        moves.add(move);
        if (toPlayColor == Piece.WHITE) toPlayColor = Piece.BLACK;
        else toPlayColor = Piece.WHITE;

    }

    public void unMove() {
        Move lastMove = moves.getLastMove();
        if (lastMove == null) return;

        int fromSquare = lastMove.getFrom();
        int toSquare = lastMove.getTo();
        int moveColor = lastMove.getColor();
        int movedPieceType = lastMove.getPieceType();

        setPieceAt(fromSquare, movedPieceType,moveColor);
        removePiece(toSquare);


        if (lastMove.isTake()) {
            setPieceAt(toSquare, lastMove.getTakenPieceType(), Piece.GetOppositeColor(moveColor));

        }

        if (lastMove.isPromote()) {
            setPieceAt(fromSquare, Piece.PAWN, moveColor);
        }

        if (moveColor == Piece.WHITE) {
            fullMovesCount--;
        }


        if (lastMove.isCastling()) {
            int rookPosition;
            int currentRookPosition;


            if (lastMove.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = Game.moveGenerator.getInitialRookKingSide(
                        moveColor);
                currentRookPosition = lastMove.getFrom() + 1;

            } else {
                rookPosition = Game.moveGenerator.getInitialRookQueenSide(
                        moveColor);
                currentRookPosition = lastMove.getFrom() - 1;

            }
            setPieceAt(rookPosition, Piece.ROOK, moveColor);
            removePiece(currentRookPosition);
        }


        if (lastMove.isEnPasant()) {
            if (moveColor == Piece.WHITE) {
                setPieceAt(toSquare - 8,Piece.PAWN,Piece.BLACK);
            } else {
                setPieceAt(toSquare + 8,Piece.PAWN,Piece.WHITE);

            }
        }


        if (toPlayColor == Piece.WHITE) toPlayColor = Piece.BLACK;
        else toPlayColor = Piece.WHITE;


        //restore previous fifty moves count
        fiftyMovesDrawCount = lastMove.getPreviousFiftyMoves();


        states.remove(states.size() - 1);
        moves.removeLastMove();

    }

    public void unMove(int numberOfSteps) {
        for (int i = 0; i < numberOfSteps; i++) {
            unMove();
        }
    }

    //get and set a square info



    public void setPieceAt(int position, int pieceType,int pieceColor) {
        removePiece(position);

        if (pieceColor == Piece.WHITE) {
            switch (pieceType) {
                case Piece.PAWN:
                    addWhitePawn(position);
                    break;
                case Piece.ROOK:
                    addWhiteRook(position);
                    break;
                case Piece.KNIGHT:
                    addWhiteKnight(position);
                    break;
                case Piece.BISHOP:
                    addWhiteBishop(position);
                    break;
                case Piece.QUEEN:
                    addWhiteQueen(position);
                    break;
                case Piece.KING:
                    addWhiteKing(position);
                    break;
            }
        } else {
            switch (pieceType) {
                case Piece.PAWN:
                    addBlackPawn(position);
                    break;
                case Piece.ROOK:
                    addBlackRook(position);
                    break;
                case Piece.KNIGHT:
                    addBlackKnight(position);
                    break;
                case Piece.BISHOP:
                    addBlackBishop(position);
                    break;
                case Piece.QUEEN:
                    addBlackQueen(position);
                    break;
                case Piece.KING:
                    addBlackKing(position);
                    break;
            }

        }

    }


    public Game.GameStatus checkStatus(LegalMoves toPlayLegalMoves) {
        int lastPlayed = moves.getLastPlayed();
        Game.GameStatus gameStatus = Game.GameStatus.NOT_FINISHED;
        int currentToPlayColor = Piece.GetOppositeColor(lastPlayed);

        if (toPlayLegalMoves.size() == 0) {
            boolean isKingInCheck = Game.moveGenerator.isKingAttacked(this, currentToPlayColor);
            if (isKingInCheck) {
                //win
                if (lastPlayed == Piece.WHITE) {
                    gameStatus = Game.GameStatus.FINISHED_WIN_WHITE;
                } else {
                    gameStatus = Game.GameStatus.FINISHED_WIN_BLACK;

                }
            } else {
                //draw stalemate
                gameStatus = Game.GameStatus.FINISHED_DRAW;

            }

        }
        if (insufficientMaterial()) {
            gameStatus = Game.GameStatus.FINISHED_DRAW;
            return gameStatus;
        }
        if (fiftyMovesDrawCount == 50) {
            gameStatus = Game.GameStatus.FINISHED_DRAW;
            return gameStatus;
        }

        //check for third repetition draw
        int repeatedPositionCount = 1;
        State lastState = states.get(states.size() - 1);
        for (int i = 0; i < (states.size() - 1); i++) {
            if (lastState.equals(states.get(i))) {
                repeatedPositionCount++;
                if (repeatedPositionCount == 3) {
                    gameStatus = Game.GameStatus.FINISHED_DRAW;
                    break;
                }
            }
        }


        return gameStatus;
    }

    public int getAllPiecesCount() {
        return BitMath.countSetBits(allPieces);
    }

    public int getWhitePiecesCount() {
        return BitMath.countSetBits(allWhitePieces);
    }

    public int getBlackPiecesCount() {
        return BitMath.countSetBits(allBlackPieces);
    }

    public int getWhiteBishopsCount() {
        return BitMath.countSetBits(whiteBishops);
    }

    public int getBlackBishopsCount() {
        return BitMath.countSetBits(blackBishops);

    }


    private boolean insufficientMaterial() {
        int allPiecesCount = getAllPiecesCount();


        // tow kings remaining
        if (allPiecesCount == 2) {
            return true;
        }

        if (allPiecesCount == 3) {

            // tow kings and a bishop or knight

            if ((whiteBishops | blackBishops | whiteKnights | blackKnights) != 0) {
                return true;
            }

        }

        if (allPiecesCount == 4) {

            if (getWhiteBishopsCount() == 1 && getBlackBishopsCount() == 1) {
                int whiteBishopPosition = BitMath.getPositionsOf(whiteBishops).get(0);
                int blackBishopPosition = BitMath.getPositionsOf(blackBishops).get(0);
                // tow king and tow bishops of the same square color
                return ChessBoard.GetSquareColor(whiteBishopPosition) ==
                        ChessBoard.GetSquareColor(blackBishopPosition);
            }



        }


        return false;
    }

    public void removeKing(int kingToRemoveColor){
        if(kingToRemoveColor == Piece.WHITE){
            long whiteKingMask = ~whiteKing;
            allWhitePieces &= whiteKingMask;
            allPieces &= whiteKingMask;
            emptySquares = ~allPieces;
            whiteKing = 0;

        } else {
            long blackKingMask = ~blackKing;
            allBlackPieces &= blackKingMask;
            allPieces &= blackKingMask;
            emptySquares = ~allPieces;
            blackKing = 0;
        }
    }

    public boolean isSquareEmpty(int position) {
        return BitMath.isBitSet(emptySquares,position);
    }

    public boolean isPieceBlackAt(int position) {
        return BitMath.isBitSet(allBlackPieces,position);
    }

    public boolean isPieceWhiteAt(int position) {
        return BitMath.isBitSet(allWhitePieces,position);
    }

    public int pieceColor(int position) {
        return (int) ((allBlackPieces & (1L << position)) >>> position);
    }

    public int pieceType(int position) {
        long positionBit = 1L << position;
        long isPawn = (((whitePawns | blackPawns) & positionBit) >>> position);
        long isRook = (((whiteRooks | blackRooks) & positionBit) >>> position);
        long isKnight = (((whiteKnights | blackKnights) & positionBit) >>> position);
        long isBishop = (((whiteBishops | blackBishops) & positionBit) >>> position);
        long isQueen = (((whiteQueens | blackQueens) & positionBit) >>> position);
        long isKing = (((whiteKing | blackKing) & positionBit) >>> position);
        return (int) (
                (isPawn * Piece.PAWN) + (isRook * Piece.ROOK) + (isKnight * Piece.KNIGHT) +
                        (isBishop * Piece.BISHOP) + (isQueen * Piece.QUEEN) + (isKing * Piece.KING)
        );
    }




    public static int GetPosition(int file, int rank) {
        if (file < FILE_A || file > FILE_H) return OUT;
        if (rank < RANK_1 || rank > RANK_8) return OUT;

        return (rank * 8) + file;
    }


    public static int Square(int file, int rank) {
        return (rank * 8) + file;
    }

    public static int Square(String square) {
        char fileChar = square.charAt(0);
        char rankChar = square.charAt(1);
        int file = NO_SQUARE;
        int rank = NO_SQUARE;
        switch (fileChar) {
            case 'a':
                file = FILE_A;
                break;
            case 'b':
                file = FILE_B;
                break;
            case 'c':
                file = FILE_C;
                break;
            case 'd':
                file = FILE_D;
                break;
            case 'e':
                file = FILE_E;
                break;
            case 'f':
                file = FILE_F;
                break;
            case 'g':
                file = FILE_G;
                break;
            case 'h':
                file = FILE_H;
                break;
        }

        switch (rankChar) {
            case '1':
                rank = RANK_1;
                break;
            case '2':
                rank = RANK_2;
                break;
            case '3':
                rank = RANK_3;
                break;
            case '4':
                rank = RANK_4;
                break;
            case '5':
                rank = RANK_5;
                break;
            case '6':
                rank = RANK_6;
                break;
            case '7':
                rank = RANK_7;
                break;
            case '8':
                rank = RANK_8;
                break;
        }
        return GetPosition(file, rank);
    }

    public static int GetFile(int position) {
        return position % 8;
    }

    public static int GetRank(int position) {
        return position / 8;
    }

    public static int offset(int square, int file, int rank) {
        int newFile = GetFile(square) + file;
        int newRank = GetRank(square) + rank;
        if (newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT;
        if (newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) return ChessBoard.OUT;

        return (newRank * 8) + newFile;
    }

    public static int offsetFile(int square, int file) {
        return offset(square, file, 0);
    }

    public static int offsetRank(int square, int rank) {
        return offset(square, 0, rank);
    }

    public static int GetSquareColor(int square) {
        if ((square % 2) == 0) {
            return Piece.BLACK;
        } else {
            return Piece.WHITE;
        }
    }

    //------------------------


    public void print() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" \n");
        char[] blackPiecesSymbols = {'0', 'p', 'r', 'n', 'b', 'q', 'k'};
        char[] whitePiecesSymbols = {'0', 'P', 'R', 'N', 'B', 'Q', 'K'};

        char currentSymbol = '0';
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int pieceType = pieceType(Square(file, rank));
                int pieceColor = pieceColor(Square(file, rank));
                if (pieceColor == Piece.WHITE) currentSymbol = whitePiecesSymbols[pieceType];
                if (pieceColor == Piece.BLACK) currentSymbol = blackPiecesSymbols[pieceType];
                stringBuilder.append(currentSymbol);
                stringBuilder.append(' ');

            }
            stringBuilder.append('\n');

        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }

    public static void printBitboard(long bitboard) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" \n");


        char currentSymbol = '0';
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                currentSymbol = '0';
                if (BitMath.getBit(bitboard, Square(file, rank)) == 1) currentSymbol = '1';
                stringBuilder.append(currentSymbol);
                stringBuilder.append(' ');

            }
            stringBuilder.append('\n');

        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }


    public int getWhiteCastlingRights() {
        return states.get(states.size() - 1).whiteCastlingRights;
    }

    public int getBlackCastlingRights() {
        return states.get(states.size() - 1).blackCastlingRights;
    }

    public int getEnPassantTarget() {
        return states.get(states.size() - 1).enPassantTarget;
    }
}
