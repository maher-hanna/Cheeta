package com.maherhanna.cheeta.core

class Uci {
    val engine = ChessEngine()
    fun parseInput(input : String):String{
        return when(input){
            "uci" ->{
                "uciok"
            }
            else -> "error"
        }
    }
}