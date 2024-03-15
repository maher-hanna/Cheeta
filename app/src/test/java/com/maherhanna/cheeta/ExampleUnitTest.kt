package com.maherhanna.cheeta

import com.maherhanna.cheeta.core.ChessBoard
import com.maherhanna.cheeta.core.ChessEngine
import com.maherhanna.cheeta.core.MoveGenerator
import com.maherhanna.cheeta.core.Piece
import com.maherhanna.cheeta.core.Uci
import com.maherhanna.cheeta.core.util.Log
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Random
import kotlin.system.exitProcess


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    val chessBoard = ChessBoard(ChessBoard.positionInUse)
    val chessBoard2 = ChessBoard(ChessBoard.trickyPosition)
    val chessBoard3 = ChessBoard("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -")
    val moveGenerator = MoveGenerator()

    @Before
    fun setup() {
        chessBoard.toPlayColor = Piece.WHITE
        moveGenerator.updateWhiteLegalMoves(chessBoard)
        moveGenerator.updateBlackLegalMoves(chessBoard)
    }

    @Test(expected = Test.None::class)
    fun getMove() {
        val uci = Uci()
        uci.parseInput("position fen  ${ChessBoard.killerPosition}")
        val move = uci.parseInput("go depth 5")

    }
    @Test(expected = Test.None::class)
    fun testUciProtocol() {
        val numberOfGames = 30
        var currentGameNumber = 1
        var isCurrentGameFinished: Boolean
        var movesList: String
        var firstEngineWins = 0
        var firstEngineDraws = 0
        var firstEngineLoses = 0
        val uci = Uci()
        val classLoader = ClassLoader.getSystemClassLoader()

        val carlsenGamesStream =
            classLoader.getResourceAsStream("carlsen.txt")
        val carlsenGamesBufferReader = carlsenGamesStream.bufferedReader()

        val positions = mutableListOf<String>()
        try {
            var line: String?
            while (carlsenGamesBufferReader.readLine().also { line = it } != null) {
                // Process each line
                if (line != "\u0000") {
                    line?.let { positions.add(it.trim().replace("\n".toRegex(), replacement = "")) }
                }
            }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            exitProcess(-1)
        } finally {
            try {
                carlsenGamesBufferReader.close()
            } catch (e: Exception) {
                println("An error occurred while closing the file: ${e.message}")
            }
        }
        try {
            while (currentGameNumber <= numberOfGames) {
                isCurrentGameFinished = false
                movesList = ""
                val random = Random(System.currentTimeMillis())
                val randomPlayerIndex = random.nextInt(2)
                var currentPlayerIndex = randomPlayerIndex
                val randomPosition =
                    positions[random.nextInt(positions.size)] ?: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 "
                if(currentPlayerIndex == 0){
                        uci.parseInput("position fen  $randomPosition")
                    val gameStatusResponse = uci.parseInput("check_status")
                    // game status : 0 not finished, 1 draw, 2 white winds, 3 black wins
                    val statusCode = gameStatusResponse.toInt()
                    if (statusCode != 0) {
                        break
                    }
                    val move = uci.parseInput("go infinite")
                    val splits = move.trim().split("\\s+".toRegex())
                    if(splits.size > 1){
                        movesList += splits[1] + " "
                    }
                } else {
                    uci.parseInput("position fen  $randomPosition")
                    val gameStatusResponse = uci.parseInput("check_status")
                    // game status : 0 not finished, 1 draw, 2 white winds, 3 black wins
                    val statusCode = gameStatusResponse.toInt()
                    if (statusCode != 0) {
                        break
                    }
                    val move = uci.parseInput("go infinite")

                    val splits = move.trim().split("\\s+".toRegex())
                    if(splits.size > 1){
                        movesList += splits[1] + " "
                    }
                }
                while (!isCurrentGameFinished){
                    // flip current player index from 0 to 1 and vice versa
                    currentPlayerIndex = currentPlayerIndex xor 1
                    if(randomPosition.contains("moves")){
                        uci.parseInput("position fen $randomPosition $movesList")
                    }else {
                        uci.parseInput("position fen $randomPosition moves $movesList")
                    }
                    val gameStatusResponse = uci.parseInput("check_status")
                    // game status : 0 not finished, 1 draw, 2 white winds, 3 black wins
                    val statusCode = gameStatusResponse.toInt()
                    isCurrentGameFinished = statusCode != 0
                    if(isCurrentGameFinished){
                        when(statusCode){
                            1 -> firstEngineDraws += 1
                            2 -> {
                                if(randomPlayerIndex == 0){
                                    firstEngineWins += 1
                                }else {
                                    firstEngineLoses += 1
                                }
                            }
                            3 -> {
                                if(randomPlayerIndex == 0){
                                    firstEngineLoses += 1
                                }else {
                                    firstEngineWins += 1
                                }
                            }
                        }
                        break
                    }

                    if(currentPlayerIndex == 0){

                        val move = uci.parseInput("go infinite")
                        val splits = move.trim().split("\\s+".toRegex())
                        if(splits.size > 1){
                            movesList += splits[1] + " "
                        }
                    } else{

                        val move = uci.parseInput("go infinite")
                        val splits = move.trim().split("\\s+".toRegex())
                        if(splits.size > 1){
                            movesList += splits[1] + " "
                        }
                    }
                    println("position fen $randomPosition moves $movesList")

                }
                currentGameNumber += 1
            }
            println("First engine results compared to second engine are:")
            println("wins: $firstEngineWins draws: $firstEngineDraws loses: $firstEngineLoses")
        } catch (e: IllegalStateException) {
            println("System.in was closed; exiting")
        } catch (e: NoSuchElementException) {
            println("System.in was closed; exiting")

        }
    }

    @Test
    fun moveGeneratorMoves() {

        val perftResults = listOf<ULong>(
            1UL,
            20UL,
            400UL,
            8902UL,
            197281UL,
            4865609UL,
            119060324UL,
            3195901860UL,
            84998978956UL,
            2439530234167UL
        )

        for (i in 1 until 7) {
            val perftResult = Perft(i, chessBoard, moveGenerator)
            if(perftResults[i] != perftResult){
                Log.d(ChessEngine.DEBUG_TAG,"depth: ${i}")
            }
            assertEquals(perftResults[i], perftResult)

        }
    }

    @Test
    fun moveGeneratorMoves2() {

        val perftResults = listOf<ULong>(
            48UL,
            2039UL,
            97862UL,
            4085603UL,
            193690690UL,
            8031647685UL,
        )

        for (i in 1 until 7) {
            val perftResult = Perft(i, chessBoard2, moveGenerator)
            if(perftResults[i - 1] != perftResult){
                Log.d(ChessEngine.DEBUG_TAG,"depth: ${i}")
            }
            assertEquals(perftResults[i - 1], perftResult)

        }
    }

    @Test
    fun moveGeneratorMoves3() {
        val perftResults = listOf<ULong>(
            14UL,
            191UL,
            2812UL,
            43238UL,
            674624UL,
            11030083UL,
        )

        for (i in 1 until 7) {
            val perftResult = Perft(i, chessBoard3, moveGenerator)
            if(perftResults[i - 1] != perftResult){
                Log.d(ChessEngine.DEBUG_TAG,"depth: ${i}")
            }
            assertEquals(perftResults[i - 1], perftResult)

        }
    }
    @Test
    fun moveGeneratorCaptures() {
        val perftCaptureResults = listOf<ULong>(
            0UL,
            0UL,
            0UL,
            34UL,
            1576UL,
            82719UL,
            2812008UL,
            108329926UL,
            3523740106UL
        )
        for (i in 1 until 7) {
            val perftResult = PerftCaptures(i, chessBoard, moveGenerator, false)
            assertEquals(perftCaptureResults[i], perftResult)

        }
    }

    @Test
    fun moveGeneratorCaptures2() {
        val perftCaptureResults = listOf<ULong>(
            8UL,
            351UL,
            17102UL,
            757163UL,
            35043416UL,
            1558445089UL,

        )
        for (i in 1 until 7) {
            val perftResult = PerftCaptures(i, chessBoard2, moveGenerator, false)
            assertEquals(perftCaptureResults[i - 1], perftResult)

        }
    }

    @Test
    fun moveGeneratorChecks() {
        val perftChecksResults = listOf<ULong>(
            0UL,
            0UL,
            0UL,
            12UL,
            469UL,
            27351UL,
            809099UL,
            33103848UL,
            968981593UL
        )
        for (i in 1 until 7) {
            val perftResult = PerftChecks(i, chessBoard, moveGenerator, false)
            assertEquals(perftChecksResults[i], perftResult)

        }
    }

    fun Perft(depth: Int, chessBoard: ChessBoard, moveGenerator: MoveGenerator): ULong {
        var nodes = 0UL;

        if (depth == 0)
            return 1UL;

        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, chessBoard.toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += Perft(depth - 1, chessBoard, moveGenerator);
            chessBoard.unMakeMove();
        }
        return nodes;
    }

    fun PerftCaptures(
        depth: Int,
        chessBoard: ChessBoard,
        moveGenerator: MoveGenerator,
        isCapture: Boolean
    ): ULong {
        var nodes = 0UL;

        if (depth == 0) {
            return if (isCapture) 1UL else 0UL

        }

        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, chessBoard.toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += PerftCaptures(depth - 1, chessBoard, moveGenerator, move_list[i].isCapture);
            chessBoard.unMakeMove();
        }
        return nodes;
    }

    fun PerftChecks(
        depth: Int,
        chessBoard: ChessBoard,
        moveGenerator: MoveGenerator,
        isCheck: Boolean
    ): ULong {
        var nodes = 0UL;

        if (depth == 0) {
            return if (isCheck) 1UL else 0UL
        }
        val toPlayColor = chessBoard.toPlayColor
        val move_list = moveGenerator.generateLegalMovesFor(chessBoard, toPlayColor);
        val n_moves = move_list.size()
        for (i in 0 until n_moves) {
            chessBoard.makeMove(move_list[i]);
            nodes += PerftChecks(
                depth - 1,
                chessBoard,
                moveGenerator,
                moveGenerator.isKingAttacked(chessBoard,chessBoard.toPlayColor)
            )
            chessBoard.unMakeMove()
        }
        return nodes;
    }
}