package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor

class MoveGenerator(
    var blackPlayerLegalMoves: PlayerLegalMoves,
    var whitePlayerLegalMoves: PlayerLegalMoves
) {

    //*****************************************************************************
    //calculated at start
    //-----------------------------------------------------------------------------
    //knight attacks
    var knightAttacksMask = LongArray(64)
    var rookAttacksMask = Array(4) { LongArray(64) }
    var bishopAttacksMask = Array(4) { LongArray(64) }

    //*****************************************************************************
    init {
        //initialize knight attacks table
        var knight: Long = 0
        for (square in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {
            knight = 0
            knight = BitMath.setBit(knight, square)
            val northNorthEast = knight shl 17 and notAFile
            val northEastEast = knight shl 10 and notABFile
            val southEastEast = knight ushr 6 and notABFile
            val southSouthEast = knight ushr 15 and notAFile
            val northNorthWest = knight shl 15 and notHFile
            val northWestWest = knight shl 6 and notGHFile
            val southWestWest = knight ushr 10 and notGHFile
            val southSouthWest = knight ushr 17 and notHFile
            val attacks = northNorthEast or northEastEast or southEastEast or southSouthEast or
                    northNorthWest or northWestWest or southWestWest or southSouthWest
            knightAttacksMask[square] = attacks
        }

        //initialize rook attacks table
        var rook: Long = 0
        for (square in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {
            rook = 0
            rook = BitMath.setBit(knight, square)
            var westAttackMask: Long = 0
            var eastAttackMask: Long = 0
            var northAttackMask: Long = 0
            var southAttackMask: Long = 0

            //west ray
            val currentRank = ChessBoard.GetRank(square)
            for (file in ChessBoard.GetFile(square) - 1 downTo ChessBoard.FILE_A) {
                westAttackMask =
                    BitMath.setBit(westAttackMask, ChessBoard.Square(file, currentRank))
            }
            rookAttacksMask[WEST][square] = westAttackMask

            //east ray
            for (file in ChessBoard.GetFile(square) + 1..ChessBoard.FILE_H) {
                eastAttackMask =
                    BitMath.setBit(eastAttackMask, ChessBoard.Square(file, currentRank))
            }
            rookAttacksMask[EAST][square] = eastAttackMask

            //north ray
            val currentFile = ChessBoard.GetFile(square)
            for (rank in ChessBoard.GetRank(square) + 1..ChessBoard.RANK_8) {
                northAttackMask =
                    BitMath.setBit(northAttackMask, ChessBoard.Square(currentFile, rank))
            }
            rookAttacksMask[NORTH][square] = northAttackMask

            //south ray
            for (rank in ChessBoard.GetRank(square) - 1 downTo ChessBoard.RANK_1) {
                southAttackMask =
                    BitMath.setBit(southAttackMask, ChessBoard.Square(currentFile, rank))
            }
            rookAttacksMask[SOUTH][square] = southAttackMask
        }

        //initialize bishop attacks table
        var bishop: Long = 0
        for (square in ChessBoard.MIN_POSITION..ChessBoard.MAX_POSITION) {
            bishop = 0
            bishop = BitMath.setBit(bishop, square)
            var northWestAttackMask: Long = 0
            var northEastAttackMask: Long = 0
            var southWestAttackMask: Long = 0
            var southEastAttackMask: Long = 0

            //north west ray
            var rank = ChessBoard.GetRank(square) + 1
            run {
                var file = ChessBoard.GetFile(square) - 1
                while (file >= ChessBoard.FILE_A && rank <= ChessBoard.RANK_8) {
                    northWestAttackMask =
                        BitMath.setBit(northWestAttackMask, ChessBoard.Square(file, rank))
                    file--
                    rank++
                }
            }
            bishopAttacksMask[NORTH_WEST][square] = northWestAttackMask

            //north east ray
            rank = ChessBoard.GetRank(square) + 1
            run {
                var file = ChessBoard.GetFile(square) + 1
                while (file <= ChessBoard.FILE_H && rank <= ChessBoard.RANK_8) {
                    northEastAttackMask =
                        BitMath.setBit(northEastAttackMask, ChessBoard.Square(file, rank))
                    file++
                    rank++
                }
            }
            bishopAttacksMask[NORTH_EAST][square] = northEastAttackMask

            //south west ray
            rank = ChessBoard.GetRank(square) - 1
            run {
                var file = ChessBoard.GetFile(square) - 1
                while (file >= ChessBoard.FILE_A && rank >= ChessBoard.RANK_1) {
                    southWestAttackMask =
                        BitMath.setBit(southWestAttackMask, ChessBoard.Square(file, rank))
                    file--
                    rank--
                }
            }
            bishopAttacksMask[SOUTH_WEST][square] = southWestAttackMask

            //south east ray
            rank = ChessBoard.GetRank(square) - 1
            var file = ChessBoard.GetFile(square) + 1
            while (file <= ChessBoard.FILE_H && rank >= ChessBoard.RANK_1) {
                southEastAttackMask =
                    BitMath.setBit(southEastAttackMask, ChessBoard.Square(file, rank))
                file++
                rank--
            }
            bishopAttacksMask[SOUTH_EAST][square] = southEastAttackMask
        }
    }
    fun getLegalMovesFor(color: Int): PlayerLegalMoves {
        return if (color == Piece.WHITE) {
            whitePlayerLegalMoves
        } else {
            blackPlayerLegalMoves
        }
    }
    fun getLegalTargetsFor(chessBoard: ChessBoard,position: Int): ArrayList<Int> {
        return if (chessBoard.isPieceWhiteAt(position)) {
            whitePlayerLegalMoves.getLegalTargetsFor(position)
        } else {
            blackPlayerLegalMoves.getLegalTargetsFor(position)
        }
    }
    fun canMove(chessBoard: ChessBoard,fromSquare: Int, toSquare: Int): Boolean {
        var isLegal = false
        isLegal = if (chessBoard.isPieceBlackAt(fromSquare)) {
            blackPlayerLegalMoves.canMove(fromSquare, toSquare)
        } else {
            whitePlayerLegalMoves.canMove(fromSquare, toSquare)
        }
        return isLegal
    }
    fun updateBlackLegalMoves(chessBoard: ChessBoard) {
        blackPlayerLegalMoves = getBlackLegalMoves(chessBoard)
    }

    fun updateWhiteLegalMoves(chessBoard: ChessBoard) {
        whitePlayerLegalMoves = getWhiteLegalMoves(chessBoard)
    }

    fun updateLegalMovesFor(chessBoard: ChessBoard,playerColor: Int, kingInCheck: Boolean) {
        if (playerColor == Piece.WHITE) {
            updateWhiteLegalMoves(chessBoard)
        } else {
            updateBlackLegalMoves(chessBoard)
        }
    }
    fun getWhitePawnsPushes(whitePawns: Long, empty: Long): ArrayList<Move> {
        val moves = ArrayList<Move>()
        val singlePushes = whitePawnsSinglePush(whitePawns, empty)
        val doublePushes = whitePawnsDoublePush(whitePawns, empty)
        var count = BitMath.countSetBits(singlePushes)
        var singlePushesCopy = singlePushes
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(singlePushesCopy)
            singlePushesCopy = BitMath.popBit(singlePushesCopy, index)
            val move = Move(
                Piece.PAWN, Piece.WHITE,
                index - 8, index
            )
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                move.setPromotes(Piece.QUEEN)
            }
            moves.add(move)
        }
        count = BitMath.countSetBits(doublePushes)
        var doublePushesCopy = doublePushes
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(doublePushesCopy)
            doublePushesCopy = BitMath.popBit(doublePushesCopy, index)
            val move = Move(
                Piece.PAWN, Piece.WHITE,
                index - 16, index
            )
            move.setPawnDoublePush()
            moves.add(move)
        }
        return moves
    }

    fun getWhitePawnsCaptures(chessBoard: ChessBoard): ArrayList<Move> {
        val attacks = ArrayList<Move>()
        val attacksWest = whitePawnsAttackWest(chessBoard.whitePawns, chessBoard.allBlackPieces)
        val attacksEast = whitePawnsAttackEast(chessBoard.whitePawns, chessBoard.allBlackPieces)
        var count = BitMath.countSetBits(attacksWest)
        var attacksWestCopy = attacksWest
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(attacksWestCopy)
            attacksWestCopy = BitMath.popBit(attacksWestCopy, index)
            val attack = Move(
                Piece.PAWN, Piece.WHITE,
                index - 7, index
            )
            attack.setTakes(chessBoard.pieceType(index))
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                attack.setPromotes(Piece.QUEEN)
            }
            attacks.add(attack)
        }
        count = BitMath.countSetBits(attacksEast)
        var attacksEastCopy = attacksEast
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(attacksEastCopy)
            attacksEastCopy = BitMath.popBit(attacksEastCopy, index)
            val attack = Move(
                Piece.PAWN, Piece.WHITE,
                index - 9, index
            )
            attack.setTakes(chessBoard.pieceType(index))
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_8) {
                attack.setPromotes(Piece.QUEEN)
            }
            attacks.add(attack)
        }
        if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE &&
            ChessBoard.GetRank(chessBoard.enPassantTarget) == ChessBoard.RANK_6
        ) {
            val enPassantWest =
                whitePawnsAttackWest(chessBoard.whitePawns, 1L shl chessBoard.enPassantTarget)
            val enPassantEast =
                whitePawnsAttackEast(chessBoard.whitePawns, 1L shl chessBoard.enPassantTarget)

            //en passant west
            if (enPassantWest != 0L) {
                val index = chessBoard.enPassantTarget
                val enPassantWestMove = Move(
                    Piece.PAWN, Piece.WHITE,
                    index - 7, index
                )
                enPassantWestMove.setTakes(Piece.PAWN)
                enPassantWestMove.setEnPasant()
                attacks.add(enPassantWestMove)
            }
            if (enPassantEast != 0L) {
                //en passant east
                val index = chessBoard.enPassantTarget
                val enPassantEastMove = Move(
                    Piece.PAWN, Piece.WHITE,
                    index - 9, index
                )
                enPassantEastMove.setTakes(Piece.PAWN)
                enPassantEastMove.setEnPasant()
                attacks.add(enPassantEastMove)
            }
        }
        return attacks
    }

    fun getBlackPawnsPushes(blackPawns: Long, empty: Long): ArrayList<Move> {
        val moves = ArrayList<Move>()
        val singlePushes = blackPawnsSinglePush(blackPawns, empty)
        val doublePushes = blackPawnsDoublePush(blackPawns, empty)
        var count = BitMath.countSetBits(singlePushes)
        var singlePushesCopy = singlePushes
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(singlePushesCopy)
            singlePushesCopy = BitMath.popBit(singlePushesCopy, index)
            val move = Move(
                Piece.PAWN, Piece.BLACK,
                index + 8, index
            )
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                move.setPromotes(Piece.QUEEN)
            }
            moves.add(move)
        }
        count = BitMath.countSetBits(doublePushes)
        var doublePushesCopy = doublePushes
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(doublePushesCopy)
            doublePushesCopy = BitMath.popBit(doublePushesCopy, index)
            val move = Move(
                Piece.PAWN, Piece.BLACK,
                index + 16, index
            )
            move.setPawnDoublePush()
            moves.add(move)
        }
        return moves
    }

    fun getBlackPawnsCaptures(chessBoard: ChessBoard): ArrayList<Move> {
        val attacks = ArrayList<Move>()
        val attacksWest = blackPawnsAttackWest(chessBoard.blackPawns, chessBoard.allWhitePieces)
        val attacksEast = blackPawnsAttackEast(chessBoard.blackPawns, chessBoard.allWhitePieces)
        var count = BitMath.countSetBits(attacksWest)
        var attacksWestCopy = attacksWest
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(attacksWestCopy)
            attacksWestCopy = BitMath.popBit(attacksWestCopy, index)
            val attack = Move(
                Piece.PAWN, Piece.BLACK,
                index + 9, index
            )
            attack.setTakes(chessBoard.pieceType(index))
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                attack.setPromotes(Piece.QUEEN)
            }
            attacks.add(attack)
        }
        count = BitMath.countSetBits(attacksEast)
        var attacksEastCopy = attacksEast
        for (i in 0 until count) {
            val index = BitMath.getLSBitIndex(attacksEastCopy)
            attacksEastCopy = BitMath.popBit(attacksEastCopy, index)
            val attack = Move(
                Piece.PAWN, Piece.BLACK,
                index + 7, index
            )
            attack.setTakes(chessBoard.pieceType(index))
            if (ChessBoard.GetRank(index) == ChessBoard.RANK_1) {
                attack.setPromotes(Piece.QUEEN)
            }
            attacks.add(attack)
        }
        if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE &&
            ChessBoard.GetRank(chessBoard.enPassantTarget) == ChessBoard.RANK_3
        ) {
            val enPassantWest =
                blackPawnsAttackWest(chessBoard.blackPawns, 1L shl chessBoard.enPassantTarget)
            val enPassantEast =
                blackPawnsAttackEast(chessBoard.blackPawns, 1L shl chessBoard.enPassantTarget)
            if (enPassantWest != 0L) {

                //en passant west
                val index = chessBoard.enPassantTarget
                val enPassantWestMove = Move(
                    Piece.PAWN, Piece.BLACK,
                    index + 9, index
                )
                enPassantWestMove.setTakes(Piece.PAWN)
                enPassantWestMove.setEnPasant()
                attacks.add(enPassantWestMove)
            }
            if (enPassantEast != 0L) {

                //en passant east
                val index = chessBoard.enPassantTarget
                val enPassantEastMove = Move(
                    Piece.PAWN, Piece.BLACK,
                    index + 7, index
                )
                enPassantEastMove.setTakes(Piece.PAWN)
                enPassantEastMove.setEnPasant()
                attacks.add(enPassantEastMove)
            }
        }
        return attacks
    }

    //*******************************************************************************
    //king moves
    //-------------------------------------------------------------------------------
    private fun kingMovesQuite(king: Long, emptySquares: Long): Long {
        var quietTargets: Long = 0
        quietTargets = quietTargets or (northWest(king) and emptySquares)
        quietTargets = quietTargets or (north(king) and emptySquares)
        quietTargets = quietTargets or (northEast(king) and emptySquares)
        quietTargets = quietTargets or (west(king) and emptySquares)
        quietTargets = quietTargets or (east(king) and emptySquares)
        quietTargets = quietTargets or (southWest(king) and emptySquares)
        quietTargets = quietTargets or (south(king) and emptySquares)
        quietTargets = quietTargets or (southEast(king) and emptySquares)
        return quietTargets
    }

    private fun kingMovesCapture(king: Long, enemyPieces: Long): Long {
        var captureTargets: Long = 0
        captureTargets = captureTargets or (northWest(king) and enemyPieces)
        captureTargets = captureTargets or (north(king) and enemyPieces)
        captureTargets = captureTargets or (northEast(king) and enemyPieces)
        captureTargets = captureTargets or (west(king) and enemyPieces)
        captureTargets = captureTargets or (east(king) and enemyPieces)
        captureTargets = captureTargets or (southWest(king) and enemyPieces)
        captureTargets = captureTargets or (south(king) and enemyPieces)
        captureTargets = captureTargets or (southEast(king) and enemyPieces)
        return captureTargets
    }

    fun getKingMoves(chessBoard: ChessBoard, color: Int): ArrayList<Move> {
        var quietTargets: Long = 0
        var captureTargets: Long = 0
        var kingPosition = ChessBoard.OUT
        val moves = ArrayList<Move>()
        if (color == Piece.BLACK) {
            quietTargets = kingMovesQuite(chessBoard.blackKing, chessBoard.emptySquares)
            captureTargets = kingMovesCapture(chessBoard.blackKing, chessBoard.allWhitePieces)
            kingPosition = BitMath.getLSBitIndex(chessBoard.blackKing)
        } else {
            quietTargets = kingMovesQuite(chessBoard.whiteKing, chessBoard.emptySquares)
            captureTargets = kingMovesCapture(chessBoard.whiteKing, chessBoard.allBlackPieces)
            kingPosition = BitMath.getLSBitIndex(chessBoard.whiteKing)
        }
        var count = BitMath.countSetBits(quietTargets)
        for (i in 0 until count) {
            val target = BitMath.getLSBitIndex(quietTargets)
            quietTargets = BitMath.popBit(quietTargets, target)
            val move = Move(Piece.KING, color, kingPosition, target)
            moves.add(move)
        }
        count = BitMath.countSetBits(captureTargets)
        for (i in 0 until count) {
            val captureTarget = BitMath.getLSBitIndex(captureTargets)
            captureTargets = BitMath.popBit(captureTargets, captureTarget)
            val move = Move(Piece.KING, color, kingPosition, captureTarget)
            move.setTakes(chessBoard.pieceType(captureTarget))
            moves.add(move)
        }
        return moves
    }

    //*******************************************************************************
    //knight moves
    //-------------------------------------------------------------------------------
    private fun knightMovesQuite(knightIndex: Int, emptySquares: Long): Long {
        return knightAttacksMask[knightIndex] and emptySquares
    }

    private fun knightMovesCapture(knightIndex: Int, enemyPieces: Long): Long {
        return knightAttacksMask[knightIndex] and enemyPieces
    }

    fun getKnightMoves(chessBoard: ChessBoard, color: Int): ArrayList<Move> {
        var quietTargets: Long = 0
        var captureTargets: Long = 0
        var knights: Long = 0
        var currentKnightPosition = ChessBoard.OUT
        val moves = ArrayList<Move>()
        knights = if (color == Piece.BLACK) {
            chessBoard.blackKnights
        } else {
            chessBoard.whiteKnights
        }
        val knightsCount = BitMath.countSetBits(knights)
        var knightsCopy = knights
        for (i in 0 until knightsCount) {
            currentKnightPosition = BitMath.getLSBitIndex(knightsCopy)
            knightsCopy = BitMath.popBit(knightsCopy, currentKnightPosition)
            quietTargets = knightMovesQuite(currentKnightPosition, chessBoard.emptySquares)
            captureTargets = if (color == Piece.BLACK) {
                knightMovesCapture(currentKnightPosition, chessBoard.allWhitePieces)
            } else {
                knightMovesCapture(currentKnightPosition, chessBoard.allBlackPieces)
            }
            var count = BitMath.countSetBits(quietTargets)
            var quietTargetsCopy = quietTargets
            for (index in 0 until count) {
                val target = BitMath.getLSBitIndex(quietTargetsCopy)
                quietTargetsCopy = BitMath.popBit(quietTargetsCopy, target)
                val move = Move(Piece.KNIGHT, color, currentKnightPosition, target)
                moves.add(move)
            }
            count = BitMath.countSetBits(captureTargets)
            var captureTargetsCopy = captureTargets
            for (index in 0 until count) {
                val captureTarget = BitMath.getLSBitIndex(captureTargetsCopy)
                captureTargetsCopy = BitMath.popBit(captureTargetsCopy, captureTarget)
                val move = Move(Piece.KNIGHT, color, currentKnightPosition, captureTarget)
                move.setTakes(chessBoard.pieceType(captureTarget))
                moves.add(move)
            }
        }
        return moves
    }

    //*******************************************************************************
    //rook moves
    //-------------------------------------------------------------------------------
    private fun rookAttacks(rayDirection: Int, square: Int, occupied: Long): Long {
        var attacks = rookAttacksMask[rayDirection][square]
        val blocker = rookAttacksMask[rayDirection][square] and occupied
        if (blocker != 0L) {
            var blockerIndex = ChessBoard.OUT
            blockerIndex =
                if (rayDirection == EAST || rayDirection == NORTH) {
                    BitMath.getLSBitIndex(blocker)
                } else {
                    BitMath.getMSBitIndex(blocker)
                }
            attacks = attacks xor rookAttacksMask[rayDirection][blockerIndex]
        }
        return attacks
    }

    fun getRooksMoves(
        chessBoard: ChessBoard,
        rooks: Long,
        color: Int,
        pieceType: Int
    ): ArrayList<Move> {
        var rooks = rooks
        val moves = ArrayList<Move>()
        var quietTargets: Long = 0
        var captureTarget: Long = 0
        var northTargets: Long = 0
        var southTargets: Long = 0
        var eastTargets: Long = 0
        var westTargets: Long = 0
        var rookPosition = ChessBoard.OUT
        val rooksCount = BitMath.countSetBits(rooks)
        for (rook in 0 until rooksCount) {
            rookPosition = BitMath.getLSBitIndex(rooks)
            rooks = BitMath.popBit(rooks, rookPosition)
            northTargets = rookAttacks(NORTH, rookPosition, chessBoard.allPieces)
            southTargets = rookAttacks(SOUTH, rookPosition, chessBoard.allPieces)
            eastTargets = rookAttacks(EAST, rookPosition, chessBoard.allPieces)
            westTargets = rookAttacks(WEST, rookPosition, chessBoard.allPieces)
            var move: Move
            var captureTargetIndex = ChessBoard.OUT

            //north moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                northTargets and chessBoard.allWhitePieces
            } else {
                northTargets and chessBoard.allBlackPieces
            }
            quietTargets = northTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, rookPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            var quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, rookPosition, target)
                moves.add(move)
            }


            //south moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                southTargets and chessBoard.allWhitePieces
            } else {
                southTargets and chessBoard.allBlackPieces
            }
            quietTargets = southTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, rookPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, rookPosition, target)
                moves.add(move)
            }

            //west moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                westTargets and chessBoard.allWhitePieces
            } else {
                westTargets and chessBoard.allBlackPieces
            }
            quietTargets = westTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, rookPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, rookPosition, target)
                moves.add(move)
            }

            //east moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                eastTargets and chessBoard.allWhitePieces
            } else {
                eastTargets and chessBoard.allBlackPieces
            }
            quietTargets = eastTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, rookPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, rookPosition, target)
                moves.add(move)
            }
        }
        return moves
    }

    //*******************************************************************************
    //-------------------------------------------------------------------------------
    //bishop moves
    private fun bishopAttacks(rayDirection: Int, square: Int, occupied: Long): Long {
        var attacks = bishopAttacksMask[rayDirection][square]
        val blocker = bishopAttacksMask[rayDirection][square] and occupied
        if (blocker != 0L) {
            var blockerIndex = ChessBoard.OUT
            blockerIndex =
                if (rayDirection == NORTH_EAST || rayDirection == NORTH_WEST) {
                    BitMath.getLSBitIndex(blocker)
                } else {
                    BitMath.getMSBitIndex(blocker)
                }
            attacks = attacks xor bishopAttacksMask[rayDirection][blockerIndex]
        }
        return attacks
    }

    fun getBishopsMoves(
        chessBoard: ChessBoard,
        bishops: Long,
        color: Int,
        pieceType: Int
    ): ArrayList<Move> {
        var bishops = bishops
        val moves = ArrayList<Move>()
        var quietTargets: Long = 0
        var captureTarget: Long = 0
        var northWestTargets: Long = 0
        var southWestTargets: Long = 0
        var northEastTargets: Long = 0
        var southEastTargets: Long = 0
        var bishopPosition = ChessBoard.OUT
        val bishopsCount = BitMath.countSetBits(bishops)
        for (bishop in 0 until bishopsCount) {
            bishopPosition = BitMath.getLSBitIndex(bishops)
            bishops = BitMath.popBit(bishops, bishopPosition)
            northWestTargets = bishopAttacks(NORTH_WEST, bishopPosition, chessBoard.allPieces)
            southWestTargets = bishopAttacks(SOUTH_WEST, bishopPosition, chessBoard.allPieces)
            northEastTargets = bishopAttacks(NORTH_EAST, bishopPosition, chessBoard.allPieces)
            southEastTargets = bishopAttacks(SOUTH_EAST, bishopPosition, chessBoard.allPieces)
            var move: Move
            var captureTargetIndex = ChessBoard.OUT

            //north west moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                northWestTargets and chessBoard.allWhitePieces
            } else {
                northWestTargets and chessBoard.allBlackPieces
            }
            quietTargets = northWestTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, bishopPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            var quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, bishopPosition, target)
                moves.add(move)
            }


            //south west moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                southWestTargets and chessBoard.allWhitePieces
            } else {
                southWestTargets and chessBoard.allBlackPieces
            }
            quietTargets = southWestTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, bishopPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, bishopPosition, target)
                moves.add(move)
            }

            //south east moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                southEastTargets and chessBoard.allWhitePieces
            } else {
                southEastTargets and chessBoard.allBlackPieces
            }
            quietTargets = southEastTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, bishopPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, bishopPosition, target)
                moves.add(move)
            }

            //north east moves
            quietTargets = 0
            captureTarget = 0
            captureTargetIndex = ChessBoard.OUT
            captureTarget = if (color == Piece.BLACK) {
                northEastTargets and chessBoard.allWhitePieces
            } else {
                northEastTargets and chessBoard.allBlackPieces
            }
            quietTargets = northEastTargets and chessBoard.emptySquares
            if (captureTarget != 0L) {
                captureTargetIndex = BitMath.getLSBitIndex(captureTarget)
                move = Move(pieceType, color, bishopPosition, captureTargetIndex)
                move.setTakes(chessBoard.pieceType(captureTargetIndex))
                moves.add(move)
            }
            quietTargetsCount = BitMath.countSetBits(quietTargets)
            for (i in 0 until quietTargetsCount) {
                val target = BitMath.getLSBitIndex(quietTargets)
                quietTargets = BitMath.popBit(quietTargets, target)
                move = Move(pieceType, color, bishopPosition, target)
                moves.add(move)
            }
        }
        return moves
    }

    //*******************************************************************************
    //-------------------------------------------------------------------------------
    //queen moves
    fun getQueensMoves(
        chessBoard: ChessBoard,
        queens: Long,
        color: Int,
        pieceType: Int
    ): ArrayList<Move> {
        val moves = ArrayList<Move>()
        moves.addAll(getBishopsMoves(chessBoard, queens, color, pieceType))
        moves.addAll(getRooksMoves(chessBoard, queens, color, pieceType))
        return moves
    }

    //*******************************************************************************
    fun getAllAttackedSquaresFor(chessBoard: ChessBoard, color: Int): Long {
        var attackedSquares: Long = 0
        var pawns: Long = 0
        var rooks: Long = 0
        var bishops: Long = 0
        var knights: Long = 0
        var queens: Long = 0
        var king: Long = 0
        if (color == Piece.WHITE) {
            pawns = chessBoard.whitePawns
            rooks = chessBoard.whiteRooks
            bishops = chessBoard.whiteBishops
            knights = chessBoard.whiteKnights
            queens = chessBoard.whiteQueens
            king = chessBoard.whiteKing
            attackedSquares =
                attackedSquares or (northWest(pawns) or northEast(pawns) and chessBoard.emptySquares)
        } else {
            pawns = chessBoard.blackPawns
            rooks = chessBoard.blackRooks
            bishops = chessBoard.blackBishops
            knights = chessBoard.blackKnights
            queens = chessBoard.blackQueens
            king = chessBoard.blackKing
            attackedSquares =
                attackedSquares or (southWest(pawns) or southEast(pawns) and chessBoard.emptySquares)
        }

        //check rooks and queens
        var rooksAndQueens = rooks or queens
        val rooksAndQueensCount = BitMath.countSetBits(rooksAndQueens)
        var rookPosition = 0
        for (rook in 0 until rooksAndQueensCount) {
            rookPosition = BitMath.getLSBitIndex(rooksAndQueens)
            rooksAndQueens = BitMath.popBit(rooksAndQueens, rookPosition)
            val northTargets =
                rookAttacks(NORTH, rookPosition, chessBoard.allPieces) and chessBoard.emptySquares
            val southTargets =
                rookAttacks(SOUTH, rookPosition, chessBoard.allPieces) and chessBoard.emptySquares
            val eastTargets =
                rookAttacks(EAST, rookPosition, chessBoard.allPieces) and chessBoard.emptySquares
            val westTargets =
                rookAttacks(WEST, rookPosition, chessBoard.allPieces) and chessBoard.emptySquares
            attackedSquares =
                attackedSquares or (northTargets or southTargets or eastTargets or westTargets)
        }

        //check bishops and queens
        var bishopsAndQueens = bishops or queens
        val bishopsAndQueensCount = BitMath.countSetBits(bishopsAndQueens)
        var bishopPosition = 0
        for (bishop in 0 until bishopsAndQueensCount) {
            bishopPosition = BitMath.getLSBitIndex(bishopsAndQueens)
            bishopsAndQueens = BitMath.popBit(bishopsAndQueens, bishopPosition)
            val northWestTargets = bishopAttacks(
                NORTH_WEST,
                bishopPosition,
                chessBoard.allPieces
            ) and chessBoard.emptySquares
            val southWestTargets = bishopAttacks(
                SOUTH_WEST,
                bishopPosition,
                chessBoard.allPieces
            ) and chessBoard.emptySquares
            val northEastTargets = bishopAttacks(
                NORTH_EAST,
                bishopPosition,
                chessBoard.allPieces
            ) and chessBoard.emptySquares
            val southEastTargets = bishopAttacks(
                SOUTH_EAST,
                bishopPosition,
                chessBoard.allPieces
            ) and chessBoard.emptySquares
            attackedSquares =
                attackedSquares or (northWestTargets or southWestTargets or northEastTargets or southEastTargets)
        }

        //check knights
        val knightsCount = BitMath.countSetBits(knights)
        var knightPosition = 0
        for (knight in 0 until knightsCount) {
            knightPosition = BitMath.getLSBitIndex(knights)
            knights = BitMath.popBit(knights, knightPosition)
            attackedSquares =
                attackedSquares or (knightAttacksMask[knightPosition] and chessBoard.emptySquares)
        }

        //check king
        attackedSquares = attackedSquares or (northWest(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (north(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (northEast(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (west(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (east(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (southWest(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (south(king) and chessBoard.emptySquares)
        attackedSquares = attackedSquares or (southEast(king) and chessBoard.emptySquares)
        return attackedSquares
    }

    fun getWhitePseudoLegalMoves(chessBoard: ChessBoard): ArrayList<Move> {
        val moves = ArrayList<Move>()

        //add pawns moves
        moves.addAll(getWhitePawnsPushes(chessBoard.whitePawns, chessBoard.emptySquares))
        moves.addAll(getWhitePawnsCaptures(chessBoard))

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.WHITE))

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.WHITE))

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.whiteRooks, Piece.WHITE, Piece.ROOK))

        //add bishops moves
        moves.addAll(
            getBishopsMoves(
                chessBoard,
                chessBoard.whiteBishops,
                Piece.WHITE,
                Piece.BISHOP
            )
        )

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.whiteQueens, Piece.WHITE, Piece.QUEEN))
        return moves
    }

    fun getBlackPseudoLegalMoves(chessBoard: ChessBoard): ArrayList<Move> {
        val moves = ArrayList<Move>()

        //add pawns moves
        moves.addAll(getBlackPawnsPushes(chessBoard.blackPawns, chessBoard.emptySquares))
        moves.addAll(getBlackPawnsCaptures(chessBoard))

        //add king moves
        moves.addAll(getKingMoves(chessBoard, Piece.BLACK))

        //add knights moves
        moves.addAll(getKnightMoves(chessBoard, Piece.BLACK))

        //add rooks moves
        moves.addAll(getRooksMoves(chessBoard, chessBoard.blackRooks, Piece.BLACK, Piece.ROOK))

        //add bishops moves
        moves.addAll(
            getBishopsMoves(
                chessBoard,
                chessBoard.blackBishops,
                Piece.BLACK,
                Piece.BISHOP
            )
        )

        //add queens moves
        moves.addAll(getQueensMoves(chessBoard, chessBoard.blackQueens, Piece.BLACK, Piece.QUEEN))
        return moves
    }

    fun isSquareAttacked(chessBoard: ChessBoard, square: Int, attackingColor: Int): Boolean {
        return if (attackingColor == Piece.WHITE) {
            getAllAttackedSquaresFor(chessBoard, Piece.WHITE) and (1L shl square) != 0L
        } else {
            getAllAttackedSquaresFor(chessBoard, Piece.BLACK) and (1L shl square) != 0L
        }
    }

    fun isKingInCheck(chessBoard: ChessBoard,kingColor: Int): Boolean {
        return isKingAttacked(chessBoard, kingColor)

    }
    fun isKingAttacked(chessBoard: ChessBoard, kingColor: Int): Boolean {
        //val startTime = System.nanoTime()

        val chessBoardWithoutKing = ChessBoard(chessBoard)
        val kingPosition = getKingPosition(chessBoard, kingColor)
        chessBoardWithoutKing.removeKing(kingColor)
        val isAttacked =
            (getAllAttackedSquaresFor(chessBoardWithoutKing, GetOppositeColor(kingColor))
                    and (1L shl kingPosition)) != 0L
//        Log.d(
//            Game.DEBUG,
//            "isKingAttacked elapsedTme in nano seconds: ${System.nanoTime() - startTime}"
//        )
        return isAttacked


    }

    fun getKingPosition(chessBoard: ChessBoard, kingColor: Int): Int {
        var kingPosition = ChessBoard.OUT
        kingPosition = if (kingColor == Piece.WHITE) {
            BitMath.getLSBitIndex(chessBoard.whiteKing)
        } else {
            BitMath.getLSBitIndex(chessBoard.blackKing)
        }
        return kingPosition
    }

    fun getBlackPositions(chessBoard: ChessBoard): ArrayList<Int> {
        val blackPositions = ArrayList<Int>()
        var blackPieces = chessBoard.allBlackPieces
        val blackPiecesCount = BitMath.countSetBits(blackPieces)
        var currentPiecePosition = 0
        for (i in 0 until blackPiecesCount) {
            currentPiecePosition = BitMath.getLSBitIndex(blackPieces)
            blackPositions.add(currentPiecePosition)
            blackPieces = BitMath.popBit(blackPieces, currentPiecePosition)
        }
        return blackPositions
    }

    fun getWhitePositions(chessBoard: ChessBoard): ArrayList<Int> {
        val whitePositions = ArrayList<Int>()
        var whitePieces = chessBoard.allWhitePieces
        val whitePiecesCount = BitMath.countSetBits(whitePieces)
        var currentPiecePosition = 0
        for (i in 0 until whitePiecesCount) {
            currentPiecePosition = BitMath.getLSBitIndex(whitePieces)
            whitePositions.add(currentPiecePosition)
            whitePieces = BitMath.popBit(whitePieces, currentPiecePosition)
        }
        return whitePositions
    }

    fun movePiece(chessBoard: ChessBoard, move: Move): ChessBoard {
        val chessBoardAfterMove = ChessBoard(chessBoard)
        val fromSquare = move.from
        val toSquare = move.to
        chessBoardAfterMove.setPieceAt(
            toSquare, chessBoardAfterMove.pieceType(fromSquare),
            chessBoardAfterMove.pieceColor(fromSquare)
        )
        chessBoardAfterMove.removePiece(fromSquare)
        if (move.isEnPasant) {
            if (move.color == Piece.WHITE) {
                chessBoardAfterMove.removePiece(toSquare - 8)
            } else {
                chessBoardAfterMove.removePiece(toSquare + 8)
            }
        }
        return chessBoardAfterMove
    }

    private fun removeMovesThatExposeKing(
        chessBoard: ChessBoard,
        pieceLegalMoves: ArrayList<Move>,
        kingColor: Int
    ) {
        val itr = pieceLegalMoves.iterator()
        var chessBoardAfterMove: ChessBoard
        while (itr.hasNext()) {
            val move = itr.next()
            chessBoardAfterMove = movePiece(chessBoard, move)
            if (isKingAttacked(chessBoardAfterMove, kingColor)) {
                itr.remove()
            }
        }
    }

    fun getWhiteLegalMoves(chessBoard: ChessBoard): PlayerLegalMoves {
        val moves = getWhitePseudoLegalMoves(chessBoard)
        removeMovesThatExposeKing(chessBoard, moves, Piece.WHITE)
        val playerLegalMoves = PlayerLegalMoves()
        playerLegalMoves.addAll(moves)
        checkCastling(chessBoard, playerLegalMoves, Piece.WHITE)
        return playerLegalMoves
    }

    fun getBlackLegalMoves(chessBoard: ChessBoard): PlayerLegalMoves {
        val moves = getBlackPseudoLegalMoves(chessBoard)
        removeMovesThatExposeKing(chessBoard, moves, Piece.BLACK)
        val playerLegalMoves = PlayerLegalMoves()
        playerLegalMoves.addAll(moves)
        checkCastling(chessBoard, playerLegalMoves, Piece.BLACK)
        return playerLegalMoves
    }

    fun getLegalMovesFor(chessBoard: ChessBoard, color: Int): PlayerLegalMoves {
        return if (color == Piece.WHITE) {
            getWhiteLegalMoves(chessBoard)
        } else {
            getBlackLegalMoves(chessBoard)
        }
    }

    private fun checkCastling(
        chessBoard: ChessBoard, playerLegalMoves: PlayerLegalMoves,
        color: Int
    ) {
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        var kingTarget = 0
        if (canCastleKingSide(chessBoard, color, isKingInCheck(chessBoard,color))) {
            kingTarget = initialKingPosition + 2
            val move = Move(Piece.KING, color, initialKingPosition, kingTarget)
            move.setCastling(Move.CastlingType.CASTLING_kING_SIDE)
            playerLegalMoves.add(move)
        }
        if (canCastleQueenSide(chessBoard, color, isKingInCheck(chessBoard,color))) {
            kingTarget = initialKingPosition - 2
            val move = Move(Piece.KING, color, initialKingPosition, kingTarget)
            move.setCastling(Move.CastlingType.CASTLING_QUEEN_SIDE)
            playerLegalMoves.add(move)
        }
    }

    private fun canCastleKingSide(
        chessBoard: ChessBoard,
        color: Int,
        kingInCheck: Boolean
    ): Boolean {
        if (kingInCheck) {
            return false
        }
        if (color == Piece.WHITE) {
            if (chessBoard.whiteCastlingRights == ChessBoard.NO_CASTLING) {
                return false
            }
            if (chessBoard.whiteCastlingRights == ChessBoard.CASTLING_QUEEN_SIDE) {
                return false
            }
        } else {
            if (chessBoard.blackCastlingRights == ChessBoard.NO_CASTLING) {
                return false
            }
            if (chessBoard.blackCastlingRights == ChessBoard.CASTLING_QUEEN_SIDE) {
                return false
            }
        }
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        return if (!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
            !chessBoard.isSquareEmpty(initialKingPosition + 2)
        ) {
            false
        } else !isSquareAttacked(
            chessBoard,
            initialKingPosition + 1,
            GetOppositeColor(color)
        ) &&
                !isSquareAttacked(chessBoard, initialKingPosition + 2, GetOppositeColor(color))
    }

    private fun canCastleQueenSide(
        chessBoard: ChessBoard,
        color: Int,
        kingInCheck: Boolean
    ): Boolean {
        if (kingInCheck) {
            return false
        }
        if (color == Piece.WHITE) {
            if (chessBoard.whiteCastlingRights == ChessBoard.NO_CASTLING) {
                return false
            }
            if (chessBoard.whiteCastlingRights == ChessBoard.CASTLING_KING_SIDE) {
                return false
            }
        } else {
            if (chessBoard.blackCastlingRights == ChessBoard.NO_CASTLING) {
                return false
            }
            if (chessBoard.blackCastlingRights == ChessBoard.CASTLING_KING_SIDE) {
                return false
            }
        }
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        return if (!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
            !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
            !chessBoard.isSquareEmpty(initialKingPosition - 3)
        ) {
            false
        } else !isSquareAttacked(
            chessBoard,
            initialKingPosition - 1,
            GetOppositeColor(color)
        ) &&
                !isSquareAttacked(chessBoard, initialKingPosition - 2, GetOppositeColor(color))
    }

    fun getInitialKingPosition(chessBoard: ChessBoard?, kingColor: Int): Int {
        return if (kingColor == Piece.WHITE) {
            4
        } else {
            60
        }
    }

    companion object {
        //move operations
        //----------------------------------------------------------------------------
        private const val notAFile = -0x101010101010102L
        private const val notHFile = 0x7f7f7f7f7f7f7f7fL
        private const val notABFile = -0x303030303030304L
        private const val notGHFile = 0x3f3f3f3f3f3f3f3fL
        private const val rank4 = 0xff000000L
        private const val rank5 = 0xff00000000L
        private const val rank8 = -0x100000000000000L
        private const val rank1 = 0xffL
        private fun south(square: Long): Long {
            return square ushr 8
        }

        private fun north(square: Long): Long {
            return square shl 8
        }

        private fun east(square: Long): Long {
            return square and notHFile shl 1
        }

        private fun west(square: Long): Long {
            return square and notAFile ushr 1
        }

        private fun southWest(square: Long): Long {
            return square and notAFile ushr 9
        }

        private fun southEast(square: Long): Long {
            return square and notHFile ushr 7
        }

        private fun northWest(square: Long): Long {
            return square and notAFile shl 7
        }

        private fun northEast(square: Long): Long {
            return square and notHFile shl 9
        }

        //rook and bishop tables
        //bishop rays constants
        private const val NORTH_WEST = 0
        private const val NORTH_EAST = 1
        private const val SOUTH_WEST = 2
        private const val SOUTH_EAST = 3

        //rook rays constants
        private const val SOUTH = 0
        private const val NORTH = 1
        private const val EAST = 2
        private const val WEST = 3

        //pawn moves
        //-------------------------------------------------------------------------------
        private fun whitePawnsSinglePush(whitePawns: Long, empty: Long): Long {
            return north(whitePawns) and empty
        }

        private fun whitePawnsDoublePush(whitePawns: Long, empty: Long): Long {
            val singlePush = north(whitePawns) and empty
            return north(singlePush) and rank4 and empty
        }

        private fun whitePawnsAttackWest(whitePawns: Long, blackPieces: Long): Long {
            return northWest(whitePawns) and blackPieces
        }

        private fun whitePawnsAttackEast(whitePawns: Long, blackPieces: Long): Long {
            return northEast(whitePawns) and blackPieces
        }

        private fun blackPawnsSinglePush(blackPawns: Long, empty: Long): Long {
            return south(blackPawns) and empty
        }

        private fun blackPawnsDoublePush(blackPawns: Long, empty: Long): Long {
            val singlePush = south(blackPawns) and empty
            return south(singlePush) and rank5 and empty
        }

        private fun blackPawnsAttackWest(blackPawns: Long, whitePieces: Long): Long {
            return southWest(blackPawns) and whitePieces
        }

        private fun blackPawnsAttackEast(blackPawns: Long, whitePieces: Long): Long {
            return southEast(blackPawns) and whitePieces
        }

        @JvmStatic
        fun getInitialRookKingSide(rookColor: Int): Int {
            return if (rookColor == Piece.WHITE) {
                7
            } else {
                63
            }
        }

        @JvmStatic
        fun getInitialRookQueenSide(rookColor: Int): Int {
            return if (rookColor == Piece.WHITE) {
                0
            } else {
                56
            }
        }
    }
}
