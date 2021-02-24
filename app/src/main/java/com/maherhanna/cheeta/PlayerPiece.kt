package com.maherhanna.cheeta

public class PlayerPiece(position:Int) {
    //position in chessboard array with values from 0 to 63
    private var position:Int = position
        set(value){
        if(value < 0 || value > 63)
            throw IndexOutOfBoundsException("Given position of piece is out of board range")
        else
            field = value
    }
}
