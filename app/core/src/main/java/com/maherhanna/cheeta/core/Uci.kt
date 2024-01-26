package com.maherhanna.cheeta.core

class Uci {
    var engine = ChessEngine()
    var chessBoard = ChessBoard(ChessBoard.startPosition)
    fun parseInput(input : String):String{
        var response = ""
        val splitWords = input.split("\\s+".toRegex())

        when(splitWords[0]){
            "uci" ->{
                engine = ChessEngine()
                engine.isUciMode = true
                engine.reset()
                chessBoard = ChessBoard(ChessBoard.startPosition)
                response = "id name Cheeta\n"
                response += "id auther Maher Hanna\n"
                response += "uciok\n"
            }
            "isready" ->{
                response = "readyok\n"
            }
            "ucinewgame" -> {
                engine.reset()
                engine.isUciMode = true
                chessBoard = ChessBoard(ChessBoard.startPosition)

            }
            "position" ->{
                engine.reset()
                engine.isUciMode = true
                chessBoard = ChessBoard(ChessBoard.startPosition)
                if(splitWords[1] == "fen"){
                    chessBoard = ChessBoard(splitWords[2])
                }
                if(splitWords[3] == "moves"){
                    handleMoves(engine,chessBoard,splitWords.subList(4,splitWords.size))
                }
            }
            "go" -> {
                val move = engine.getMove(chessBoard)
                response = "bestmove ${move.notation}\n"
            }
            "check_status" -> {
                val status = engine.checkStatus(chessBoard)
                response = status.ordinal.toString()
            }
            else -> {
                response = ""
            }
        }
        return response
    }

    private fun handleMoves(engine: ChessEngine,chessBoard: ChessBoard, movesList: List<String>) {
        for(moveNotation in movesList){
            engine.makeMove(chessBoard = chessBoard,moveNotation = moveNotation)

        }
    }


}