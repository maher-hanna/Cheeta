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


    public static int Piece(int type, int color){return type | (color << 3) ;}
    //----------------------------------------------------------------------------------



    //data
    //-----------------------------------------------------------------------------------
    private final Piece[] pieces;
    public ChessboardMoves moves;

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

    int activeColor = Piece.WHITE;
    int whiteCastlingRights = NO_CASTLING;
    int blackCastlingRights = NO_CASTLING;
    int enPassantTarget = NO_SQUARE;
    int fiftyMovesDrawCount = 0;
    int fullMovesCount = 0;
    //---------------------------------------------------------------------------------



    public ChessBoard(ChessBoard copy) {
        this.pieces = copy.pieces.clone();
        this.moves = new ChessboardMoves(copy.moves);

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
        allBlackPieces = copy.blackPawns;

        emptySquares = copy.emptySquares;
        allPieces = copy.allPieces;

        activeColor = copy.activeColor;
        whiteCastlingRights = copy.whiteCastlingRights;
        blackCastlingRights = copy.blackCastlingRights;
        enPassantTarget = copy.enPassantTarget;
        fiftyMovesDrawCount = copy.fiftyMovesDrawCount;
        fullMovesCount = copy.fullMovesCount;
    }


    public ChessBoard(Drawing drawing) {
        pieces = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            pieces[i] = null;
        }

        moves = new ChessboardMoves();
        blackLegalMoves = new LegalMoves();
        whiteLegalMoves = new LegalMoves();


    }

    public void setUpBoard() {
        setupFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\n");

        for(int i = MIN_POSITION; i <= MAX_POSITION;i++){
            if(pieceType(i) == Piece.NONE) continue;

            setPieceAt(i,Piece.Type.values()[pieceType(i) - 1],Piece.Color.values()[pieceColor(i)]);
        }
        updateWhiteLegalMoves(false);
        updateBlackLegalMoves(false);

        ChessBoard test = new ChessBoard(this);
        MoveGenerator moveGenerator = new MoveGenerator();
        ArrayList<Move> moves = moveGenerator.getWhitepseudoLegalMoves(test);
        for(Move move : moves){
            test.move(move);
            test.print();
            test.unMove(move);
        }


    }

    public void setupFromFen(String fenString){
        int currentFile = FILE_A;
        int currentRank = RANK_8;
        int currentChar = 0;

        //parse piece placement
        //------------------------------------------------------------------------------
        while(currentRank >= RANK_1){

            switch (fenString.charAt(currentChar)){
                case 'r':
                    addBlackRook(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'p':
                    addBlackPawn(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'b':
                    addBlackBishop(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'n':
                    addBlackKnight(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'q':
                    addBlackQueen(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'k':
                    addBlackKing(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'R':
                    addWhiteRook(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'P':
                    addWhitePawn(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'B':
                    addWhiteBishop(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'N':
                    addWhiteKnight(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'Q':
                    addWhiteQueen(GetPosition(currentFile,currentRank));
                    currentFile++;
                    break;
                case 'K':
                    addWhiteKing(GetPosition(currentFile,currentRank));
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

            if(currentFile > FILE_H){
                currentFile = FILE_A;
                currentRank--;
            }
        }
        //***************************************************************************

        //skip space
        currentChar++;


        //parse active color
        if(fenString.charAt(currentChar) == 'w') activeColor = Piece.WHITE;
        else activeColor = Piece.BLACK;
        currentChar++;


        //skip space
        currentChar++;

        //parse castling rights
        //-----------------------------------------------------------------------------------
        while(fenString.charAt(currentChar) != ' '){
            switch (fenString.charAt(currentChar)){
                case '-':
                    whiteCastlingRights = NO_CASTLING;
                    blackCastlingRights = NO_CASTLING;
                    currentChar++;
                    break;
                case 'K':
                    whiteCastlingRights |= CASTLING_KING_SIDE;
                    currentChar++;
                    break;
                case 'Q':
                    whiteCastlingRights |= CASTLING_QUEEN_SIDE;
                    currentChar++;
                    break;
                case 'k':
                    blackCastlingRights |= CASTLING_KING_SIDE;
                    currentChar++;
                    break;
                case 'q':
                    blackCastlingRights |= CASTLING_QUEEN_SIDE;
                    currentChar++;
                    break;
            }
        }
        //--------------------------------------------------------------------------------

        //skip space
        currentChar++;


        //parse En passant target square
        if(fenString.charAt(currentChar) == '-') enPassantTarget = NO_SQUARE;
        enPassantTarget = Square(fenString.substring(currentChar,currentChar + 2));
        currentChar++;

        //skip space
        currentChar++;


        Scanner scanner = new Scanner(fenString.substring(currentChar));

        //parse number of half moves since last pawn or capture occured
        fiftyMovesDrawCount = scanner.nextInt();

        //parse full move count since start of game
        fullMovesCount = scanner.nextInt();

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
        emptySquares = ~ allPieces;
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
    private void removePiece(int square){
        whitePawns = BitMath.popBit(whitePawns,square);
        whiteRooks = BitMath.popBit(whiteRooks,square);
        whiteKnights = BitMath.popBit(whiteKnights,square);
        whiteBishops = BitMath.popBit(whiteBishops,square);
        whiteQueens = BitMath.popBit(whiteQueens,square);
        whiteKing = BitMath.popBit(whiteKing,square);

        blackPawns = BitMath.popBit(blackPawns,square);
        blackRooks = BitMath.popBit(blackRooks,square);
        blackKnights = BitMath.popBit(blackKnights,square);
        blackBishops = BitMath.popBit(blackBishops,square);
        blackQueens = BitMath.popBit(blackQueens,square);
        blackKing = BitMath.popBit(blackKing,square);


        allWhitePieces = BitMath.popBit(allWhitePieces,square);
        allBlackPieces = BitMath.popBit(allBlackPieces,square);

        allPieces = BitMath.popBit(allPieces,square);

        emptySquares = ~allPieces;
    }



    public void updateBlackLegalMoves(boolean kingInCheck) {
        blackLegalMoves = LegalMovesChecker.getBlackLegalMoves(this);
    }


    public void updateWhiteLegalMoves(boolean kingInCheck) {
        whiteLegalMoves = LegalMovesChecker.getWhiteLegalMoves(this);

    }

    public void updateLegalMovesFor(Piece.Color playerColor, boolean kingInCheck) {
        if (playerColor == Piece.Color.WHITE) {
            updateWhiteLegalMoves(kingInCheck);
        } else {
            updateBlackLegalMoves(kingInCheck);
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

    public ArrayList<Integer> getPositionsFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return getWhitePositions();
        } else {
            return getBlackPositions();
        }
    }

    public LegalMoves getLegalMovesFor(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            return whiteLegalMoves;
        } else {
            return blackLegalMoves;
        }
    }


    public ArrayList<Integer> getLegalTargetsFor(int position) {
        if (getPieceAt(position).getColor() == Piece.Color.WHITE) {
            return whiteLegalMoves.getLegalTargetsFor(position);
        } else {
            return blackLegalMoves.getLegalTargetsFor(position);
        }
    }




    public boolean isKingInCheck(Piece.Color kingColor) {
        return LegalMovesChecker.isSquareAttacked(this, getKingPosition(kingColor), kingColor.getOpposite());
    }



    public int getKingPosition(Piece.Color kingColor) {
        int kingPosition = OUT;
        for (int i = MIN_POSITION; i <= MAX_POSITION; i++) {
            Piece piece = getPieceAt(i);
            if (piece != null && piece.getType() == Piece.Type.KING && piece.getColor() == kingColor) {
                kingPosition = i;
                break;
            }
        }
        return kingPosition;
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
        setPieceAt(toSquare, getPieceAt(fromSquare));
        setPieceAt(fromSquare, null);
        getPieceAt(toSquare).setPosition(toSquare);


        if (move.isCastling()) {
            int rookPosition;
            int rookCastlingTarget;
            Piece.Color moveColor = move.getColor();

            if (move.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = LegalMovesChecker.getInitialRookKingSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() + 1;

            } else {
                rookPosition = LegalMovesChecker.getInitialRookQueenSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() -1;
            }
            setPieceAt(rookCastlingTarget, getPieceAt(rookPosition) );
            setPieceAt(rookPosition, null);
            getPieceAt(rookCastlingTarget).setPosition(rookCastlingTarget);
        }
        if(move.isPromote()){
            getPieceAt(toSquare).setType(move.getPromotionPieceType());
        }
        if(move.isEnPasant()){
            if(move.getColor() == Piece.Color.WHITE){
                setPieceAt(ChessBoard.offsetRank(move.getTo(),-1),null);
            }
            else {
                setPieceAt(ChessBoard.offsetRank(move.getTo(),1),null);
            }
        }

        moves.add(move);

    }

    public void unMove(Move move) {
        int fromSquare = move.getFrom();
        int toSquare = move.getTo();
        setPieceAt(fromSquare, getPieceAt(toSquare));
        getPieceAt(fromSquare).setPosition(fromSquare);
        setPieceAt(toSquare, null);

        if(move.isTake()){
            setPieceAt(toSquare,move.getTakenPieceType(),move.getColor().getOpposite());
            getPieceAt(toSquare).setPosition(toSquare);

        }



        if (move.isCastling()) {
            int rookPosition;
            int rookCastlingTarget;
            Piece.Color moveColor = move.getColor();

            if (move.getCastlingType() == Move.CastlingType.CASTLING_kING_SIDE) {
                rookPosition = LegalMovesChecker.getInitialRookKingSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() + 1;

            } else {
                rookPosition = LegalMovesChecker.getInitialRookQueenSide(this,
                        moveColor);
                rookCastlingTarget = move.getFrom() -1;
            }
            setPieceAt(rookCastlingTarget, getPieceAt(rookPosition) );
            setPieceAt(rookPosition, null);
            getPieceAt(rookCastlingTarget).setPosition(rookCastlingTarget);
        }
        if(move.isPromote()){
            getPieceAt(toSquare).setType(move.getPromotionPieceType());
        }
        if(move.isEnPasant()){
            if(move.getColor() == Piece.Color.WHITE){
                setPieceAt(ChessBoard.offsetRank(move.getTo(),-1),null);
            }
            else {
                setPieceAt(ChessBoard.offsetRank(move.getTo(),1),null);
            }
        }

        moves.removeLastMove();

    }


    //get and set a square info
    public Piece getPieceAt(int position) {

        return pieces[position];
    }

    public Piece getPieceAt(int file, int rank) {
        return pieces[GetPosition(file, rank)];
    }

    public void setPieceAt(int position, Piece.Type pieceType, Piece.Color pieceColor) {
        pieces[position] = new Piece(pieceType, pieceColor, position);
    }


    public Game.GameStatus checkStatus(){
        Piece.Color lastPlayed = moves.getLastPlayed();
        Game.GameStatus gameStatus = Game.GameStatus.NOT_FINISHED;
        Piece.Color currentToPlayColor = lastPlayed.getOpposite();
        boolean isKingInCheck = isKingInCheck(currentToPlayColor);

        LegalMoves currentToPlayLegalMoves = LegalMovesChecker.getLegalMovesFor(this,
                currentToPlayColor);

        if (isKingInCheck) {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {
                //win
                if (lastPlayed == Piece.Color.WHITE) {
                    gameStatus = Game.GameStatus.FINISHED_WIN_WHITE;
                } else {
                    gameStatus = Game.GameStatus.FINISHED_WIN_BLACK;

                }
            }
        } else {
            if (currentToPlayLegalMoves.getNumberOfMoves() == 0) {

                //draw stalemate
                gameStatus = Game.GameStatus.FINISHED_DRAW;
            }
            if (insufficientMaterial()) {
                gameStatus = Game.GameStatus.FINISHED_DRAW;
            }

        }

        return gameStatus;
    }

    public boolean checkGameFinished(){
        boolean finished = false;
        Game.GameStatus gameStatus = checkStatus();
        if(gameStatus == Game.GameStatus.FINISHED_DRAW || gameStatus == Game.GameStatus.FINISHED_WIN_WHITE
        || gameStatus == Game.GameStatus.FINISHED_WIN_BLACK){
            finished = true;
        }
        return finished;
    }

    private boolean insufficientMaterial() {
        ArrayList<Integer> whitePieces = getWhitePositions();
        ArrayList<Integer> blackPieces = getBlackPositions();
        int whitePiecesNumber = whitePieces.size();
        int blackPiecesNumber = blackPieces.size();

        // tow kings remaining
        if (whitePiecesNumber + blackPiecesNumber == 2) {
            return true;
        }

        if (whitePiecesNumber + blackPiecesNumber == 3) {

            Piece.Type remainingPieceType = Piece.Type.PAWN;
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (getPieceAt(i) != null && getPieceAt(i).getType() != Piece.Type.KING) {
                    remainingPieceType = getPieceType(i);
                }
            }

            // tow kings and a bishop or knight
            if(remainingPieceType == Piece.Type.BISHOP || remainingPieceType == Piece.Type.KNIGHT){
                return true;
            }

        }

        if (whitePiecesNumber + blackPiecesNumber == 4) {

            ArrayList<Piece> remainingPieces = new ArrayList<>();
            for (int i = ChessBoard.MIN_POSITION; i <= ChessBoard.MAX_POSITION; i++) {
                if (getPieceAt(i) != null && getPieceAt(i).getType() != Piece.Type.KING) {
                    remainingPieces.add(getPieceAt(i));
                }
            }
            Piece firstPiece = remainingPieces.get(0);
            Piece secondPiece = remainingPieces.get(1);

            // tow king and tow bishops of the same square color
            if(firstPiece.getType() == Piece.Type.BISHOP && secondPiece.getType() == Piece.Type.BISHOP){
                if(firstPiece.getColor() != secondPiece.getColor()){
                    return ChessBoard.GetSquareColor(firstPiece.getPosition()) ==
                            ChessBoard.GetSquareColor(secondPiece.getPosition());
                }
            }



        }



        return false;
    }


    public void setPieceAt(int position, Piece piece) {
        if(piece == null){
            pieces[position] = null;
            removePiece(position);
            return;
        }
        pieces[position] = new Piece(piece);
        if(piece.getColor() == Piece.Color.WHITE){
            switch (piece.getType()){
                case PAWN:
                    addWhitePawn(position);
                    break;
                case ROOK:
                    addWhiteRook(position);
                    break;
                case KNIGHT:
                    addWhiteKnight(position);
                    break;
                case BISHOP:
                    addWhiteBishop(position);
                    break;
                case QUEEN:
                    addWhiteQueen(position);
                    break;
                case KING:
                    addWhiteKing(position);
                    break;
            }
        } else {
            switch (piece.getType()){
                case PAWN:
                    addBlackPawn(position);
                    break;
                case ROOK:
                    addBlackRook(position);
                    break;
                case KNIGHT:
                    addBlackKnight(position);
                    break;
                case BISHOP:
                    addBlackBishop(position);
                    break;
                case QUEEN:
                    addBlackQueen(position);
                    break;
                case KING:
                    addBlackKing(position);
                    break;
            }

        }

    }

    public boolean isSquareEmpty(int position) {
        return getPieceAt(position) == null;
    }

    public boolean isPieceBlackAt(int position) {
        return pieces[position].getColor() == Piece.Color.BLACK;
    }

    public boolean isPieceWhiteAt(int position) {
        return pieces[position].getColor() == Piece.Color.WHITE;
    }

    public int pieceColor(int position){return (int) ((allBlackPieces & (1L <<position)) >>> position) ;}
    public int pieceType(int position){
        long  positionBit = 1L << position;
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

    public Piece.Color getPieceColor(int position){
        return pieces[position].getColor();
    }
    public Piece.Type getPieceType(int position){
        return pieces[position].getType();
    }

    public boolean isPieceAt(int square, Piece.Type type, Piece.Color color){
        if(square == OUT) return false;
        if(isSquareEmpty(square)) return false;
        return getPieceColor(square) == color && getPieceType(square) == type;
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
        switch (fileChar){
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

        switch (rankChar){
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
        return GetPosition(file,rank);
    }

    public static int GetFile(int position) {
        return position % 8;
    }

    public static int GetRank(int position) {
        return position / 8;
    }

    public static int offset(int square, int file, int rank){
        int newFile = GetFile(square) + file;
        int newRank = GetRank(square) + rank;
        if(newFile < ChessBoard.FILE_A || newFile > ChessBoard.FILE_H) return ChessBoard.OUT;
        if(newRank < ChessBoard.RANK_1 || newRank > ChessBoard.RANK_8) return ChessBoard.OUT;

        return (newRank * 8 )  + newFile;
    }
    public static int offsetFile(int square,int file){
        return offset(square,file,0);
    }
    public static int offsetRank(int square,int rank){
        return offset(square,0,rank);
    }
    public static Piece.Color GetSquareColor(int square){
        if((square % 2) == 0){
            return Piece.Color.BLACK;
        } else {
            return Piece.Color.WHITE;
        }
    }

    //------------------------



    public void print() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" \n");
        char[] blackPiecesSymbols = {'0','p','r','n','b','q','k'};
        char[] whitePiecesSymbols = {'0','P','R','N','B','Q','K'};

        char currentSymbol = '0';
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int pieceType = pieceType(Square(file,rank));
                int pieceColor = pieceColor(Square(file,rank));
                if(pieceColor == Piece.WHITE) currentSymbol = whitePiecesSymbols[pieceType];
                if(pieceColor == Piece.BLACK) currentSymbol = blackPiecesSymbols[pieceType];
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
                if(BitMath.getBit(bitboard,Square(file,rank)) == 1) currentSymbol = '1';
                stringBuilder.append(currentSymbol);
                stringBuilder.append(' ');

            }
            stringBuilder.append('\n');

        }
        Log.d(Game.DEBUG, stringBuilder.toString());
    }



}
