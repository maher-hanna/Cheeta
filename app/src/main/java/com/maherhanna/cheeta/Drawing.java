package com.maherhanna.cheeta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;

public class Drawing {
    private MainActivity activity;
    private ChessboardView chessboardView;
    public ChessBoard chessBoard;


    //pieces drawables and corresponding drawing dimensions
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
    //drawing dimensions
    private RectF pawnDrawingRect;
    private RectF bishopDrawingRect;
    private RectF knightDrawingRect;
    private RectF rookDrawingRect;
    private RectF queenDrawingRect;
    private RectF kingDrawingRect;
    //
    private Rect chessBoardViewRect;
    private float squareSize;
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
        chessBoardViewRect = newChessBoardRect;

        squareSize = chessBoardViewRect.height() / 8.0f;
        float pieceScaleDownFactor = 0.8f;


        //----------
        float pawnWidth = blackPawnBitmap.getWidth();
        float pawnHeight = blackPawnBitmap.getHeight();
        float pawnScaleDownFactor = Math.max(pawnWidth, pawnHeight)
                / squareSize;
        pawnWidth = pawnWidth / pawnScaleDownFactor;
        pawnHeight = pawnHeight / pawnScaleDownFactor;
        pawnWidth *= pieceScaleDownFactor;
        pawnHeight *= pieceScaleDownFactor;
        float pawnLeft = (squareSize - pawnWidth) / 2;
        float pawnTop = (squareSize - pawnHeight) / 2;
        pawnDrawingRect = new RectF(0,0,pawnWidth,pawnHeight);
        pawnDrawingRect.offset(pawnLeft,pawnTop);
        //------------

        //----------
        float bishopWidth = blackBishopBitmap.getWidth();
        float bishopHeight = blackBishopBitmap.getHeight();
        float bishopScaleDownFactor = Math.max(bishopWidth, bishopHeight)
                / squareSize;
        bishopWidth = bishopWidth / bishopScaleDownFactor;
        bishopHeight = bishopHeight / bishopScaleDownFactor;
        bishopWidth *= pieceScaleDownFactor;
        bishopHeight *= pieceScaleDownFactor;
        float bishopLeft = (squareSize - bishopWidth) / 2;
        float bishopTop = (squareSize - bishopHeight) / 2;
        bishopDrawingRect = new RectF(0,0,bishopWidth,bishopHeight);
        bishopDrawingRect.offset(bishopLeft,bishopTop);
        //------------


        //----------
        float knightWidth = blackKnightBitmap.getWidth();
        float knightHeight = blackKnightBitmap.getHeight();
        float knightScaleDownFactor = Math.max(knightWidth, knightHeight)
                / squareSize;
        knightWidth = knightWidth / knightScaleDownFactor;
        knightHeight = knightHeight / knightScaleDownFactor;
        knightWidth *= pieceScaleDownFactor;
        knightHeight *= pieceScaleDownFactor;
        float knightLeft = (squareSize - knightWidth) / 2;
        float knightTop = (squareSize - knightHeight) / 2;
        knightDrawingRect = new RectF(0,0,knightWidth,knightHeight);
        knightDrawingRect.offset(knightLeft,knightTop);
        //------------


        //----------
        float rookWidth = blackRookBitmap.getWidth();
        float rookHeight = blackRookBitmap.getHeight();
        float rookScaleDownFactor = Math.max(rookWidth, rookHeight)
                / squareSize;
        rookWidth = rookWidth / rookScaleDownFactor;
        rookHeight = rookHeight / rookScaleDownFactor;
        rookWidth *= pieceScaleDownFactor;
        rookHeight *= pieceScaleDownFactor;
        float rookLeft = (squareSize - rookWidth) / 2;
        float rookTop = (squareSize - rookHeight) / 2;
        rookDrawingRect = new RectF(0,0,rookWidth,rookHeight);
        rookDrawingRect.offset(rookLeft,rookTop);
        //------------

        //----------
        float queenWidth = blackQueenBitmap.getWidth();
        float queenHeight = blackQueenBitmap.getHeight();
        float queenScaleDownFactor = Math.max(queenWidth, queenHeight)
                / squareSize;
        queenWidth = queenWidth / queenScaleDownFactor;
        queenHeight = queenHeight / queenScaleDownFactor;
        queenWidth *= pieceScaleDownFactor;
        queenHeight *= pieceScaleDownFactor;
        float queenLeft = (squareSize - queenWidth) / 2;
        float queenTop = (squareSize - queenHeight) / 2;
        queenDrawingRect = new RectF(0,0,queenWidth,queenHeight);
        queenDrawingRect.offset(queenLeft,queenTop);
        //------------


        //----------
        float kingWidth = blackKingBitmap.getWidth();
        float kingHeight = blackKingBitmap.getHeight();
        float kingScaleDownFactor = Math.max(kingWidth, kingHeight)
                / squareSize;
        kingWidth = kingWidth / kingScaleDownFactor;
        kingHeight = kingHeight / kingScaleDownFactor;
        kingWidth *= pieceScaleDownFactor;
        kingHeight *= pieceScaleDownFactor;
        float kingLeft = (squareSize - kingWidth) / 2;
        float kingTop = (squareSize - kingHeight) / 2;
        kingDrawingRect = new RectF(0,0,kingWidth,kingHeight);
        kingDrawingRect.offset(kingLeft,kingTop);
        //------------


    }

    public void drawAllPieces()
    {
        for(int i = ChessBoard.MIN_POSITION ; i <= ChessBoard.MAX_POSITION; i++)
        {
            Piece piece = chessBoard.getPieceAt(i);
            if(piece != null) {
                drawPiece(piece,i);
            }
        }

    }

    private void drawPiece(Piece piece,int position) {
        RectF pieceRect = getPieceDrawingRect(piece,position);
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
