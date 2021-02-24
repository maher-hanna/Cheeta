package com.maherhanna.cheeta;

class PlayerPiece {
    //position in chessboard array with values from 0 to 63
    private int position;
    public PlayerPiece(int position){

            if(position < 0 || position > 63)
                throw new IndexOutOfBoundsException("Given position of piece is out of board range");
            else
                this.position = position;
    }


}
