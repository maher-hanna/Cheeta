package com.maherhanna.cheeta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;

public class Drawing {
    private MainActivity activity;
    private ChessboardView chessboardView;
    public ChessBoard chessBoard;


    //pieces drawables
    private final Bitmap blackPawnBitmap;
    private final Bitmap whitePawnBitmap;
    private final Bitmap blackBishopBitmap;
    private final Bitmap whiteBishopBitmap;
    private final Bitmap blackKnightBitmap;
    private final Bitmap whiteKnightBitmap;
    private final Bitmap blackRookBitmap;
    private final Bitmap whiteRookBitmap;
    private final Bitmap blackQueenBitmap;
    private final Bitmap whiteQueenBitmap;
    private final Bitmap blackKingBitmap;
    private final Bitmap whiteKingBitmap;
    //pieces drawing dimensions
    private RectF pawnDrawingRect;
    private RectF bishopDrawingRect;
    private RectF knightDrawingRect;
    private RectF rookDrawingRect;
    private RectF queenDrawingRect;
    private RectF kingDrawingRect;
    //chess board dimensions
    private Rect chessBoardViewRect;
    public float squareSize;
    private static float SCALE_PIECES_DOWN = 0.8f;
    //----------------



    public Drawing(MainActivity activity) {
        this.activity = activity;
        this.chessboardView = this.activity.findViewById(R.id.chessboardView);
        this.chessboardView.drawing = this;


        blackPawnBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_pawn);
        whitePawnBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_pawn);
        blackBishopBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_bishop_transparent);
        whiteBishopBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_bishop);
        blackKnightBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_knight_transparent);
        whiteKnightBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_knight);
        blackRookBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_rook_transparent);
        whiteRookBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_rook);
        blackQueenBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_queen_transparent);
        whiteQueenBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_queen);
        blackKingBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.black_king_transparent);
        whiteKingBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.white_king);

        updateDrawingRects(new Rect(0,0,chessboardView.getWidth()
                ,chessboardView.getHeight()));


    }

    public void updateDrawingRects(Rect newChessBoardRect){
        //called when the size of chessboard view change
        chessBoardViewRect = newChessBoardRect;

        squareSize = chessBoardViewRect.height() / 8.0f;


        //----------
        pawnDrawingRect = calculateRect(blackPawnBitmap.getWidth(),blackPawnBitmap.getHeight());
        bishopDrawingRect = calculateRect(blackBishopBitmap.getWidth(),blackBishopBitmap.getHeight());
        knightDrawingRect = calculateRect(blackKnightBitmap.getWidth(),blackKnightBitmap.getHeight());
        rookDrawingRect = calculateRect(blackRookBitmap.getWidth(),blackRookBitmap.getHeight());
        queenDrawingRect = calculateRect(blackQueenBitmap.getWidth(),blackQueenBitmap.getHeight());
        kingDrawingRect = calculateRect(blackKingBitmap.getWidth(),blackKingBitmap.getHeight());
        //------------



    }

    private RectF calculateRect(float bitmapWidth,float bitmapHeight){
        RectF result;
        float squareScaleDownFactor = Math.max(bitmapWidth, bitmapHeight)
                / squareSize;
        float width = bitmapWidth / squareScaleDownFactor;
        float height = bitmapHeight / squareScaleDownFactor;
        width *= SCALE_PIECES_DOWN;
        height *= SCALE_PIECES_DOWN;
        float left = (squareSize - width) / 2;
        float top = (squareSize - height) / 2;
        result = new RectF(0,0,width,height);
        result.offset(left,top);
        return result;

    }


    public void dragPiece(int position,float xOffset,float yOffset){
        chessboardView.clearBoard();
        for(int i = ChessBoard.MIN_POSITION ; i <= ChessBoard.MAX_POSITION; i++)
        {
            Piece piece = chessBoard.getPieceAt(i);
            if(piece != null) {
                if(i == position) continue;
                drawPiece(piece,i,0f,0f);
            }
        }
        drawPiece(chessBoard.getPieceAt(position),position,xOffset,yOffset);
        chessboardView.invalidate();

    }

    public void drawAllPieces()
    {

        for(int i = ChessBoard.MIN_POSITION ; i <= ChessBoard.MAX_POSITION; i++)
        {
            Piece piece = chessBoard.getPieceAt(i);
            if(piece != null) {
                drawPiece(piece,i, 0,0);
            }
        }
        chessboardView.invalidate();

    }

    private void drawPiece(Piece piece, int position,float xOffset,float yOffset) {

        RectF pieceRect = getPieceDrawingRect(piece,position);
        pieceRect.offset(xOffset,yOffset);

        switch (piece.type){

            case PAWN:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whitePawnBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackPawnBitmap,pieceRect);
                break;
            case ROOK:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteRookBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackRookBitmap,pieceRect);
                break;
            case KNIGHT:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteKnightBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackKnightBitmap,pieceRect);
                break;
            case BISHOP:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteBishopBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackBishopBitmap,pieceRect);
                break;
            case QUEEN:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteQueenBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackQueenBitmap,pieceRect);
                break;
            case KING:
                if(piece.color == Piece.Color.WHITE)
                    chessboardView.drawPiece(whiteKingBitmap,pieceRect);
                else
                    chessboardView.drawPiece(blackKingBitmap,pieceRect);
                break;
        }
    }



    private RectF getPieceDrawingRect(Piece piece,int position) {
        RectF result = new RectF();
        float x = chessBoard.GetFile(position) * squareSize;
        float y =  chessBoard.GetRank(position) * squareSize;

        //convert the y to the canvas drawing coordinates
        //which has x and y starts at top left corner
        float yOnDrawingBoard = chessBoardViewRect.width() - y - squareSize;

        switch (piece.type){

            case PAWN:
                  result = new RectF(pawnDrawingRect);
                  result.offset(x,yOnDrawingBoard);
                break;
            case ROOK:
                result = new RectF(rookDrawingRect);
                result.offset(x,yOnDrawingBoard);
                break;
            case KNIGHT:
                result = new RectF(knightDrawingRect);
                result.offset(x,yOnDrawingBoard);
                break;
            case BISHOP:
                result = new RectF(bishopDrawingRect);
                result.offset(x,yOnDrawingBoard);
                break;
            case QUEEN:
                result = new RectF(queenDrawingRect);
                result.offset(x,yOnDrawingBoard);
                break;
            case KING:
                result = new RectF(kingDrawingRect);
                result.offset(x,yOnDrawingBoard);
                break;
        }
    return result;

    }



}
