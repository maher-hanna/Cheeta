package com.maherhanna.cheeta;

public class ChessBoard {
    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 63;

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


    private Piece[] squares;
    Player firstPlayer;
    Player secondPlayer;
    private LegalMoves legalMoves;


    public ChessBoard() {
        squares = new Piece[64];

        for (int i = 0; i < 64; ++i) {
            squares[i] = null;
        }

        legalMoves = new LegalMoves(this);
    }

    public void setPlayers(Player firstPlayer, Player secondPlayer){
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;

    }

    public void setUpBoard() {
        setUpFirstPlayerPieces(firstPlayer.color);

        setUpSecondPlayerPieces(secondPlayer.color);



    }


    private void setUpFirstPlayerPieces(Piece.Color color) {

        Piece piece;
        PlayerPiece playerPiece;
        for (int i = 0; i < 8; ++i) {
            int position = GetPosition(i, 1);
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.PAWN,color,position));

        }

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,0));

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,1));

        firstPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,2));



        if (color == Piece.Color.WHITE) {

            firstPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,3));
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,4));

        } else {
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,3));
            firstPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,4));
        }

            firstPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,5));
        firstPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,6));
        firstPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,7));


    }

    private void setUpSecondPlayerPieces(Piece.Color color) {
        for (int i = 0; i < 8; ++i) {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.PAWN,color,GetPosition(i, 6)));
        }

        secondPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(0, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(1, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(2, 7)));

        if (color == Piece.Color.WHITE) {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(3, 7)));
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(4, 7)));

        } else {
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.QUEEN,color,GetPosition(3, 7)));
            secondPlayer.addPiece(new PlayerPiece(Piece.Type.KING,color,GetPosition(4, 7)));
        }

        secondPlayer.addPiece(new PlayerPiece(Piece.Type.BISHOP,color,GetPosition(5, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.KNIGHT,color,GetPosition(6, 7)));
        secondPlayer.addPiece(new PlayerPiece(Piece.Type.ROOK,color,GetPosition(7, 7)));

    }


    public Piece getPieceAt(int position){
        return squares[position];
    }
    public void setPieceAt(int position,Piece piece){
        squares[position] = piece;
    }
    public boolean isSquareEmpty(int position){return getPieceAt(position) == null;}

    public static int GetPosition(int file, int rank) {
        return (rank * 8 )+ file;
    }
    public static int GetFile(int position){return position % 8; }
    public static int GetRank(int position){return position / 8;}




}
