package com.maherhanna.cheeta.core

import com.maherhanna.cheeta.core.Piece.Companion.GetOppositeColor
import com.maherhanna.cheeta.core.Piece.Companion.WHITE

class MoveGenerator(

) {
    // king pinned pieces
    var whiteKingPinnedPieces = LongArray(64)
    var blackKingPinnedPieces = LongArray(64)

    var whiteKingCheckLine: Long = 0L.inv()
    var blackKingCheckLine: Long = 0L.inv()

    var whiteAttackedPieces: Long = 0L
    var blackAttackedPieces: Long = 0L

    var isWhiteKingChecked: Boolean = false
    var isBlackKingChecked: Boolean = false

    // ANDed into every quiet-move target set: all ones normally, zero while generating
    // captures only (see generateLegalCapturesFor)
    private var quietMask = 0L.inv()

    var blackPlayerLegalMoves: PlayerLegalMoves = PlayerLegalMoves()
    var whitePlayerLegalMoves: PlayerLegalMoves = PlayerLegalMoves()


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

    fun reset() {
        whitePlayerLegalMoves = PlayerLegalMoves()
        blackPlayerLegalMoves = PlayerLegalMoves()

        whiteKingPinnedPieces = LongArray(64)
        blackKingPinnedPieces = LongArray(64)

        whiteKingCheckLine = 0L.inv()
        blackKingCheckLine = 0L.inv()

        whiteAttackedPieces = 0L
        blackAttackedPieces = 0L

        isWhiteKingChecked = false
        isBlackKingChecked = false
    }

    fun getLegalMovesFor(color: Int): PlayerLegalMoves {
        return if (color == Piece.WHITE) {
            whitePlayerLegalMoves
        } else {
            blackPlayerLegalMoves
        }
    }


    fun getLegalTargetsFor(chessBoard: ChessBoard, position: Int): ArrayList<Int> {
        return if (chessBoard.isPieceWhiteAt(position)) {
            whitePlayerLegalMoves.getLegalTargetsFor(position)
        } else {
            blackPlayerLegalMoves.getLegalTargetsFor(position)
        }
    }

    fun canMove(chessBoard: ChessBoard, fromSquare: Int, toSquare: Int): Boolean {
        var isLegal = false
        isLegal = if (chessBoard.isPieceBlackAt(fromSquare)) {
            blackPlayerLegalMoves.canMove(fromSquare, toSquare)
        } else {
            whitePlayerLegalMoves.canMove(fromSquare, toSquare)
        }
        return isLegal
    }

    fun updateBlackLegalMoves(chessBoard: ChessBoard) {
        blackPlayerLegalMoves = generateBlackLegalMoves(chessBoard)
    }

    fun updateWhiteLegalMoves(chessBoard: ChessBoard) {
        whitePlayerLegalMoves = generateWhiteLegalMoves(chessBoard)
    }

    fun updateLegalMovesFor(chessBoard: ChessBoard, playerColor: Int) {
        if (playerColor == Piece.WHITE) {
            updateWhiteLegalMoves(chessBoard)
        } else {
            updateBlackLegalMoves(chessBoard)
        }
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

    //*******************************************************************************
    //knight moves
    //-------------------------------------------------------------------------------
    private fun knightMovesQuite(knightIndex: Int, emptySquares: Long): Long {
        return knightAttacksMask[knightIndex] and emptySquares
    }

    private fun knightMovesCapture(knightIndex: Int, enemyPieces: Long): Long {
        return knightAttacksMask[knightIndex] and enemyPieces
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

    //*******************************************************************************
    //-------------------------------------------------------------------------------
    //queen moves
    //*******************************************************************************
    fun getAttacks(chessBoard: ChessBoard, color: Int): Long {
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
                (northWest(pawns) or northEast(pawns) and chessBoard.emptySquares)
        } else {
            pawns = chessBoard.blackPawns
            rooks = chessBoard.blackRooks
            bishops = chessBoard.blackBishops
            knights = chessBoard.blackKnights
            queens = chessBoard.blackQueens
            king = chessBoard.blackKing
            attackedSquares =
                (southWest(pawns) or southEast(pawns) and chessBoard.emptySquares)
        }

        // sliders: one magic bitboard lookup per piece
        var rooksAndQueens = rooks or queens
        while (rooksAndQueens != 0L) {
            val square = java.lang.Long.numberOfTrailingZeros(rooksAndQueens)
            rooksAndQueens = rooksAndQueens and (rooksAndQueens - 1)
            attackedSquares = attackedSquares or
                    (SliderAttacks.rook(square, chessBoard.allPieces) and chessBoard.emptySquares)
        }
        var bishopsAndQueens = bishops or queens
        while (bishopsAndQueens != 0L) {
            val square = java.lang.Long.numberOfTrailingZeros(bishopsAndQueens)
            bishopsAndQueens = bishopsAndQueens and (bishopsAndQueens - 1)
            attackedSquares = attackedSquares or
                    (SliderAttacks.bishop(square, chessBoard.allPieces) and chessBoard.emptySquares)
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

    fun getAttacksWithCaptures(chessBoard: ChessBoard, color: Int, occupied: Long): Long {
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
                (northWest(pawns) or northEast(pawns) and (chessBoard.emptySquares or occupied))
        } else {
            pawns = chessBoard.blackPawns
            rooks = chessBoard.blackRooks
            bishops = chessBoard.blackBishops
            knights = chessBoard.blackKnights
            queens = chessBoard.blackQueens
            king = chessBoard.blackKing
            attackedSquares =
                (southWest(pawns) or southEast(pawns) and (chessBoard.emptySquares or occupied))
        }

        // sliders: one magic bitboard lookup per piece
        var rooksAndQueens = rooks or queens
        while (rooksAndQueens != 0L) {
            val square = java.lang.Long.numberOfTrailingZeros(rooksAndQueens)
            rooksAndQueens = rooksAndQueens and (rooksAndQueens - 1)
            attackedSquares = attackedSquares or
                    (SliderAttacks.rook(square, chessBoard.allPieces) and (chessBoard.emptySquares or occupied))
        }
        var bishopsAndQueens = bishops or queens
        while (bishopsAndQueens != 0L) {
            val square = java.lang.Long.numberOfTrailingZeros(bishopsAndQueens)
            bishopsAndQueens = bishopsAndQueens and (bishopsAndQueens - 1)
            attackedSquares = attackedSquares or
                    (SliderAttacks.bishop(square, chessBoard.allPieces) and (chessBoard.emptySquares or occupied))
        }

        //check knights
        val knightsCount = BitMath.countSetBits(knights)
        var knightPosition = 0
        for (knight in 0 until knightsCount) {
            knightPosition = BitMath.getLSBitIndex(knights)
            knights = BitMath.popBit(knights, knightPosition)
            attackedSquares =
                attackedSquares or (knightAttacksMask[knightPosition] and (chessBoard.emptySquares or occupied))
        }

        //check king
        attackedSquares =
            attackedSquares or (northWest(king) and (chessBoard.emptySquares or occupied))
        attackedSquares = attackedSquares or (north(king) and (chessBoard.emptySquares or occupied))
        attackedSquares =
            attackedSquares or (northEast(king) and (chessBoard.emptySquares or occupied))
        attackedSquares = attackedSquares or (west(king) and (chessBoard.emptySquares or occupied))
        attackedSquares = attackedSquares or (east(king) and (chessBoard.emptySquares or occupied))
        attackedSquares =
            attackedSquares or (southWest(king) and (chessBoard.emptySquares or occupied))
        attackedSquares = attackedSquares or (south(king) and (chessBoard.emptySquares or occupied))
        attackedSquares =
            attackedSquares or (southEast(king) and (chessBoard.emptySquares or occupied))
        return attackedSquares
    }

    fun isSquareAttacked(chessBoard: ChessBoard, square: Int, attackingColor: Int): Boolean {
        return if (attackingColor == Piece.WHITE) {
            getAttacks(chessBoard, Piece.WHITE) and (1L shl square) != 0L
        } else {
            getAttacks(chessBoard, Piece.BLACK) and (1L shl square) != 0L
        }
    }


    fun isKingAttacked(chessBoard: ChessBoard, kingColor: Int): Boolean {
        val kingPosition = getKingPosition(chessBoard, kingColor)
        // lift the king off the occupancy in place (getAttacks only reads the attacker's
        // pieces and the occupancy), instead of copying the board
        val savedAllPieces = chessBoard.allPieces
        val savedEmptySquares = chessBoard.emptySquares
        chessBoard.allPieces = savedAllPieces and (1L shl kingPosition).inv()
        chessBoard.emptySquares = chessBoard.allPieces.inv()
        val isAttacked =
            (getAttacks(chessBoard, GetOppositeColor(kingColor))
                    and (1L shl kingPosition)) != 0L
        chessBoard.allPieces = savedAllPieces
        chessBoard.emptySquares = savedEmptySquares
        return isAttacked
    }

    fun isKingChecked(color: Int): Boolean {
        return if (color == WHITE) {
            isWhiteKingChecked
        } else {
            isBlackKingChecked
        }
    }

    fun getAttackedPieces(color: Int): Long {
        return if (color == WHITE) {
            whiteAttackedPieces
        } else {
            blackAttackedPieces
        }
    }

    fun isPieceAttacked(color: Int, position: Int): Boolean {
        return if (color == WHITE) {
            (whiteAttackedPieces and (1L shl position)) != 0L
        } else {
            (blackAttackedPieces and (1L shl position)) != 0L
        }
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
        if (move.isEnPasant) {
            if (move.color == Piece.WHITE) {
                chessBoardAfterMove.removePiece(toSquare - 8)
            } else {
                chessBoardAfterMove.removePiece(toSquare + 8)
            }
        } else {
            chessBoardAfterMove.removePiece(fromSquare)

        }
        return chessBoardAfterMove
    }

    fun generateWhiteLegalMoves(chessBoard: ChessBoard): PlayerLegalMoves =
        generateLegalMovesFor(chessBoard, Piece.WHITE)

    fun generateBlackLegalMoves(chessBoard: ChessBoard): PlayerLegalMoves =
        generateLegalMovesFor(chessBoard, Piece.BLACK)

    fun generateLegalMovesFor(chessBoard: ChessBoard, color: Int): PlayerLegalMoves =
        generateLegalMovesFor(chessBoard, color, PlayerLegalMoves())

    /**
     * Generates the legal moves of [color] into [into] (cleared first) and returns it.
     * The search passes a reusable list per ply, so no objects are allocated.
     * Also updates isKingChecked for [color].
     */
    fun generateLegalMovesFor(
        chessBoard: ChessBoard,
        color: Int,
        into: PlayerLegalMoves
    ): PlayerLegalMoves {
        into.clear()
        updateKingPinnedPieces(chessBoard, color)
        addLegalMoves(chessBoard, color, into)
        if (quietMask != 0L) checkCastling(chessBoard, into, color)
        return into
    }

    /**
     * Legal captures only (including en passant and capturing promotions), much cheaper than
     * generating every move when only captures are needed (quiescence search). Like
     * generateLegalMovesFor, it updates isKingChecked for [color].
     */
    fun generateLegalCapturesFor(
        chessBoard: ChessBoard,
        color: Int,
        into: PlayerLegalMoves = PlayerLegalMoves()
    ): PlayerLegalMoves {
        quietMask = 0L
        try {
            return generateLegalMovesFor(chessBoard, color, into)
        } finally {
            quietMask = 0L.inv()
        }
    }

    // Emits all legal moves except castling. Legality comes from updateKingPinnedPieces:
    // every non-king move must land on the check line (all squares when not in check)
    // and inside the piece's pin mask (all squares when not pinned).
    private fun addLegalMoves(chessBoard: ChessBoard, color: Int, moves: PlayerLegalMoves) {
        val white = color == Piece.WHITE
        val ownPieces = if (white) chessBoard.allWhitePieces else chessBoard.allBlackPieces
        val enemyPieces = if (white) chessBoard.allBlackPieces else chessBoard.allWhitePieces
        val quietSquares = chessBoard.emptySquares and quietMask
        val checkLine = if (white) whiteKingCheckLine else blackKingCheckLine
        val pinned = if (white) whiteKingPinnedPieces else blackKingPinnedPieces
        val notOwn = ownPieces.inv()

        addPawnMoves(chessBoard, color, moves, enemyPieces, quietSquares, checkLine, pinned)

        var knights = if (white) chessBoard.whiteKnights else chessBoard.blackKnights
        while (knights != 0L) {
            val from = java.lang.Long.numberOfTrailingZeros(knights)
            knights = knights and (knights - 1)
            addTargets(
                chessBoard, moves, Piece.KNIGHT, color, from,
                knightAttacksMask[from] and notOwn and checkLine and pinned[from],
                enemyPieces, quietSquares
            )
        }
        var bishops = if (white) chessBoard.whiteBishops else chessBoard.blackBishops
        while (bishops != 0L) {
            val from = java.lang.Long.numberOfTrailingZeros(bishops)
            bishops = bishops and (bishops - 1)
            addTargets(
                chessBoard, moves, Piece.BISHOP, color, from,
                SliderAttacks.bishop(from, chessBoard.allPieces) and notOwn and checkLine and pinned[from],
                enemyPieces, quietSquares
            )
        }
        var rooks = if (white) chessBoard.whiteRooks else chessBoard.blackRooks
        while (rooks != 0L) {
            val from = java.lang.Long.numberOfTrailingZeros(rooks)
            rooks = rooks and (rooks - 1)
            addTargets(
                chessBoard, moves, Piece.ROOK, color, from,
                SliderAttacks.rook(from, chessBoard.allPieces) and notOwn and checkLine and pinned[from],
                enemyPieces, quietSquares
            )
        }
        var queens = if (white) chessBoard.whiteQueens else chessBoard.blackQueens
        while (queens != 0L) {
            val from = java.lang.Long.numberOfTrailingZeros(queens)
            queens = queens and (queens - 1)
            addTargets(
                chessBoard, moves, Piece.QUEEN, color, from,
                SliderAttacks.queen(from, chessBoard.allPieces) and notOwn and checkLine and pinned[from],
                enemyPieces, quietSquares
            )
        }
        addKingMoves(chessBoard, color, moves, enemyPieces, quietSquares)
    }

    private fun addTargets(
        chessBoard: ChessBoard,
        moves: PlayerLegalMoves,
        pieceType: Int,
        color: Int,
        from: Int,
        targets: Long,
        enemyPieces: Long,
        quietSquares: Long
    ) {
        var captures = targets and enemyPieces
        while (captures != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(captures)
            captures = captures and (captures - 1)
            moves.addBits(
                Move.withCapture(Move.encode(pieceType, color, from, to), chessBoard.pieceType(to))
            )
        }
        var quiets = targets and quietSquares
        while (quiets != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(quiets)
            quiets = quiets and (quiets - 1)
            moves.addBits(Move.encode(pieceType, color, from, to))
        }
    }

    private fun addKingMoves(
        chessBoard: ChessBoard,
        color: Int,
        moves: PlayerLegalMoves,
        enemyPieces: Long,
        quietSquares: Long
    ) {
        val king = if (color == Piece.WHITE) chessBoard.whiteKing else chessBoard.blackKing
        val kingPosition = java.lang.Long.numberOfTrailingZeros(king)
        // squares attacked with the king lifted off the board, so sliders "see through"
        // it. The attack calculation only reads the attacker's pieces and the occupancy,
        // so the king is removed from the occupancy in place instead of copying the board
        val savedAllPieces = chessBoard.allPieces
        val savedEmptySquares = chessBoard.emptySquares
        chessBoard.allPieces = savedAllPieces and king.inv()
        chessBoard.emptySquares = chessBoard.allPieces.inv()
        val attacked = getAttacksWithCaptures(chessBoard, Piece.GetOppositeColor(color), enemyPieces)
        chessBoard.allPieces = savedAllPieces
        chessBoard.emptySquares = savedEmptySquares
        addTargets(
            chessBoard, moves, Piece.KING, color, kingPosition,
            (kingMovesCapture(king, enemyPieces) or kingMovesQuite(king, quietSquares)) and attacked.inv(),
            enemyPieces, quietSquares
        )
    }

    // adds a pawn move, or the four promotions (queen first) when it reaches the last rank
    private fun addPawnMove(moves: PlayerLegalMoves, bits: Long, to: Int) {
        val rank = ChessBoard.GetRank(to)
        if (rank == ChessBoard.RANK_8 || rank == ChessBoard.RANK_1) {
            moves.addBits(Move.withPromotion(bits, Piece.QUEEN))
            moves.addBits(Move.withPromotion(bits, Piece.KNIGHT))
            moves.addBits(Move.withPromotion(bits, Piece.ROOK))
            moves.addBits(Move.withPromotion(bits, Piece.BISHOP))
        } else {
            moves.addBits(bits)
        }
    }

    private fun addPawnMoves(
        chessBoard: ChessBoard,
        color: Int,
        moves: PlayerLegalMoves,
        enemyPieces: Long,
        quietSquares: Long,
        checkLine: Long,
        pinned: LongArray
    ) {
        val white = color == Piece.WHITE
        val pawns = if (white) chessBoard.whitePawns else chessBoard.blackPawns
        // target minus origin for each kind of pawn move
        val push = if (white) 8 else -8
        val westCapture = if (white) 7 else -9
        val eastCapture = if (white) 9 else -7

        var singles = (if (white) whitePawnsSinglePush(pawns, quietSquares)
        else blackPawnsSinglePush(pawns, quietSquares)) and checkLine
        while (singles != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(singles)
            singles = singles and (singles - 1)
            val from = to - push
            if (pinned[from] and (1L shl to) != 0L) {
                addPawnMove(moves, Move.encode(Piece.PAWN, color, from, to), to)
            }
        }
        var doubles = (if (white) whitePawnsDoublePush(pawns, quietSquares)
        else blackPawnsDoublePush(pawns, quietSquares)) and checkLine
        while (doubles != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(doubles)
            doubles = doubles and (doubles - 1)
            val from = to - 2 * push
            if (pinned[from] and (1L shl to) != 0L) {
                moves.addBits(Move.withPawnDoublePush(Move.encode(Piece.PAWN, color, from, to)))
            }
        }
        var west = (if (white) whitePawnsAttackWest(pawns, enemyPieces)
        else blackPawnsAttackWest(pawns, enemyPieces)) and checkLine
        while (west != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(west)
            west = west and (west - 1)
            val from = to - westCapture
            if (pinned[from] and (1L shl to) != 0L) {
                addPawnMove(
                    moves,
                    Move.withCapture(Move.encode(Piece.PAWN, color, from, to), chessBoard.pieceType(to)),
                    to
                )
            }
        }
        var east = (if (white) whitePawnsAttackEast(pawns, enemyPieces)
        else blackPawnsAttackEast(pawns, enemyPieces)) and checkLine
        while (east != 0L) {
            val to = java.lang.Long.numberOfTrailingZeros(east)
            east = east and (east - 1)
            val from = to - eastCapture
            if (pinned[from] and (1L shl to) != 0L) {
                addPawnMove(
                    moves,
                    Move.withCapture(Move.encode(Piece.PAWN, color, from, to), chessBoard.pieceType(to)),
                    to
                )
            }
        }

        // en passant
        val target = chessBoard.enPassantTarget
        if (target == ChessBoard.NO_SQUARE) return
        if (ChessBoard.GetRank(target) != (if (white) ChessBoard.RANK_6 else ChessBoard.RANK_3)) return
        val targetMask = 1L shl target
        val capturedPawn = target - push
        // en passant resolves a check if it blocks the check line or captures the
        // checking pawn (the pawn that just double pushed)
        if (checkLine and (targetMask or (1L shl capturedPawn)) == 0L) return
        addEnPassant(
            moves, color, target, capturedPawn, westCapture, pinned,
            if (white) whitePawnsAttackWest(pawns, targetMask) else blackPawnsAttackWest(pawns, targetMask)
        )
        addEnPassant(
            moves, color, target, capturedPawn, eastCapture, pinned,
            if (white) whitePawnsAttackEast(pawns, targetMask) else blackPawnsAttackEast(pawns, targetMask)
        )
    }

    private fun addEnPassant(
        moves: PlayerLegalMoves,
        color: Int,
        target: Int,
        capturedPawn: Int,
        offset: Int,
        pinned: LongArray,
        hit: Long
    ) {
        if (hit == 0L) return
        val from = target - offset
        // the capturing pawn's pin mask, and the "en passant reveal" restriction stored on
        // the captured pawn's square (both pawns leave the rank at once)
        if (pinned[from] and (1L shl target) != 0L &&
            pinned[capturedPawn] and (1L shl capturedPawn) != 0L
        ) {
            moves.addBits(Move.withEnPassant(Move.encode(Piece.PAWN, color, from, target)))
        }
    }

    fun checkStatus(chessBoard: ChessBoard,legalMoves: PlayerLegalMoves? = null): GameStatus {
        var gameStatus = GameStatus.NOT_FINISHED
        val toPlayPlayerLegalMoves = legalMoves ?: generateLegalMovesFor(chessBoard, chessBoard.toPlayColor)
        if (toPlayPlayerLegalMoves.size() == 0) {
            return if (isKingChecked(chessBoard.toPlayColor)) {
                //win for the side that is not to move (works without move history, e.g. from FEN)
                if (chessBoard.toPlayColor == Piece.BLACK) {
                    GameStatus.FINISHED_WIN_WHITE
                } else {
                    GameStatus.FINISHED_WIN_BLACK
                }
            } else {
                //draw stalemate
                GameStatus.FINISHED_DRAW
            }
        }
        if (chessBoard.insufficientMaterial()) {
            gameStatus = GameStatus.FINISHED_DRAW
            return gameStatus
        }
        // fifty full moves = 100 half moves without a capture or pawn move
        if (chessBoard.fiftyMovesDrawCount >= 100) {
            return GameStatus.FINISHED_DRAW
        }

        if (isThreefoldRepetition(chessBoard)) {
            return GameStatus.FINISHED_DRAW
        }
        return gameStatus
    }

    /**
     * True if the current position has occurred three times. A position can only repeat
     * since the last capture or pawn move, and only with the same side to move, so only
     * every second state in that window is checked.
     */
    fun isThreefoldRepetition(chessBoard: ChessBoard): Boolean {
        var repeatedPositionCount = 1
        val lastIndex = chessBoard.states.size - 1
        val lastState = chessBoard.states[lastIndex]
        val firstIndex = maxOf(0, lastIndex - chessBoard.fiftyMovesDrawCount)
        for (i in lastIndex - 2 downTo firstIndex step 2) {
            if (lastState == chessBoard.states[i]) {
                repeatedPositionCount++
                if (repeatedPositionCount == 3) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkCastling(
        chessBoard: ChessBoard, playerLegalMoves: PlayerLegalMoves,
        color: Int
    ) {
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        if (canCastleKingSide(chessBoard, color, isKingChecked(color))) {
            playerLegalMoves.addBits(
                Move.encodeCastling(color, initialKingPosition, initialKingPosition + 2, false)
            )
        }
        if (canCastleQueenSide(chessBoard, color, isKingChecked(color))) {
            playerLegalMoves.addBits(
                Move.encodeCastling(color, initialKingPosition, initialKingPosition - 2, true)
            )
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
            if (chessBoard.whiteCastlingRights and ChessBoard.CASTLING_KING_SIDE == 0) {
                return false
            }

        } else {
            if (chessBoard.blackCastlingRights and ChessBoard.CASTLING_KING_SIDE == 0) {
                return false
            }

        }
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        val initialRookPosition = getInitialRookKingSide(color)

        if (chessBoard.pieceType(initialKingPosition) != Piece.KING || chessBoard.pieceType(
                initialRookPosition
            ) != Piece.ROOK || chessBoard.pieceColor(initialKingPosition) != color || chessBoard.pieceColor(
                initialRookPosition
            ) != color
        ) {
            return false
        }

        return if (!chessBoard.isSquareEmpty(initialKingPosition + 1) ||
            !chessBoard.isSquareEmpty(initialKingPosition + 2)
        ) {
            false
        } else !isPieceAttacked(color, initialKingPosition + 1) &&
                !isPieceAttacked(color, initialKingPosition + 2)
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
            if (chessBoard.whiteCastlingRights and ChessBoard.CASTLING_QUEEN_SIDE == 0) {
                return false
            }

        } else {
            if (chessBoard.blackCastlingRights and ChessBoard.CASTLING_QUEEN_SIDE == 0) {
                return false
            }

        }
        val initialKingPosition = getInitialKingPosition(chessBoard, color)
        val initialRookPosition = getInitialRookQueenSide(color)
        if (chessBoard.pieceType(initialKingPosition) != Piece.KING || chessBoard.pieceType(
                initialRookPosition
            ) != Piece.ROOK || chessBoard.pieceColor(initialKingPosition) != color || chessBoard.pieceColor(
                initialRookPosition
            ) != color
        ) {
            return false
        }
        // the b-file square must be empty but may be attacked; only the squares the king
        // crosses (d and c files) must be safe
        return if (!chessBoard.isSquareEmpty(initialKingPosition - 1) ||
            !chessBoard.isSquareEmpty(initialKingPosition - 2) ||
            !chessBoard.isSquareEmpty(initialKingPosition - 3)
        ) {
            false
        } else !isPieceAttacked(color, initialKingPosition - 1) &&
                !isPieceAttacked(color, initialKingPosition - 2)
    }

    fun getInitialKingPosition(chessBoard: ChessBoard?, kingColor: Int): Int {
        return if (kingColor == Piece.WHITE) {
            4
        } else {
            60
        }
    }


    fun updateKingPinnedPieces(chessBoard: ChessBoard, color: Int) {
        // calculate king connected pieces
        // -------------------------------------------------------------------
        //white
        whiteKingPinnedPieces.fill(0L.inv())
        whiteKingCheckLine = 0L.inv()
        whiteAttackedPieces = 0L
        var whiteKingNorthConnectedPieces = 0L
        var whiteKingNorthWestConnectedPieces = 0L
        var whiteKingWestConnectedPieces = 0L
        var whiteKingSouthWestConnectedPieces = 0L
        var whiteKingSouthConnectedPieces = 0L
        var whiteKingSouthEastConnectedPieces = 0L
        var whiteKingEastConnectedPieces = 0L
        var whiteKingNorthEastConnectedPieces = 0L
        //black
        blackKingPinnedPieces.fill(0L.inv())
        blackKingCheckLine = 0L.inv()
        blackAttackedPieces = 0L
        var blackKingNorthConnectedPieces = 0L
        var blackKingNorthWestConnectedPieces = 0L
        var blackKingWestConnectedPieces = 0L
        var blackKingSouthWestConnectedPieces = 0L
        var blackKingSouthConnectedPieces = 0L
        var blackKingSouthEastConnectedPieces = 0L
        var blackKingEastConnectedPieces = 0L
        var blackKingNorthEastConnectedPieces = 0L

        if (color == Piece.WHITE) {
            whiteAttackedPieces =
                getAttacksWithCaptures(chessBoard, Piece.BLACK, chessBoard.allWhitePieces)
        } else {
            blackAttackedPieces =
                getAttacksWithCaptures(chessBoard, Piece.WHITE, chessBoard.allBlackPieces)
        }

        val kingPosition = getKingPosition(chessBoard, color)
        val northTargets =
            rookAttacks(NORTH, kingPosition, chessBoard.allPieces)
        val southTargets =
            rookAttacks(SOUTH, kingPosition, chessBoard.allPieces)
        val eastTargets =
            rookAttacks(EAST, kingPosition, chessBoard.allPieces)
        val westTargets =
            rookAttacks(WEST, kingPosition, chessBoard.allPieces)

        val kingMask = if (color == Piece.WHITE) chessBoard.whiteKing else chessBoard.blackKing
        val enemyPieces =
            if (color == Piece.WHITE) chessBoard.allBlackPieces else chessBoard.allWhitePieces
        val enemyKnights =
            if (color == Piece.WHITE) chessBoard.blackKnights else chessBoard.whiteKnights
        val enemyPawns = if (color == Piece.WHITE) chessBoard.blackPawns else chessBoard.whitePawns
        val enemyRooksAndQueens =
            if (color == Piece.WHITE) (chessBoard.blackRooks or chessBoard.blackQueens) else (chessBoard.whiteRooks or chessBoard.whiteQueens)
        val enemyBishopsAndQueens =
            if (color == Piece.WHITE) (chessBoard.blackBishops or chessBoard.blackQueens) else (chessBoard.whiteBishops or chessBoard.whiteQueens)

        val straitTargets = (northTargets or southTargets or eastTargets or westTargets)
        val straitEnemyCheckPieces = straitTargets and enemyRooksAndQueens
        if ((northTargets and straitEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = northTargets
            } else {
                blackKingCheckLine = northTargets
            }
        }
        if ((southTargets and straitEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and southTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and southTargets
            }
        }
        if ((eastTargets and straitEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and eastTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and eastTargets
            }
        }
        if ((westTargets and straitEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and westTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and westTargets
            }
        }

        val northWestTargets = bishopAttacks(
            NORTH_WEST,
            kingPosition,
            chessBoard.allPieces
        )
        val southWestTargets = bishopAttacks(
            SOUTH_WEST,
            kingPosition,
            chessBoard.allPieces
        )
        val northEastTargets = bishopAttacks(
            NORTH_EAST,
            kingPosition,
            chessBoard.allPieces
        )
        val southEastTargets = bishopAttacks(
            SOUTH_EAST,
            kingPosition,
            chessBoard.allPieces
        )

        val diagonalTargets =
            (northWestTargets or southWestTargets or northEastTargets or southEastTargets)
        val diagonalEnemyCheckPieces = diagonalTargets and enemyPieces and enemyBishopsAndQueens
        if ((northWestTargets and diagonalEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and northWestTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and northWestTargets
            }
        } else if ((southWestTargets and diagonalEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and southWestTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and southWestTargets
            }
        } else if ((northEastTargets and diagonalEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and northEastTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and northEastTargets
            }
        } else if ((southEastTargets and diagonalEnemyCheckPieces) != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and southEastTargets
            } else {
                blackKingCheckLine = blackKingCheckLine and southEastTargets
            }
        }

        // add attacking knights to check
        var knightAndPawnsAttacks =
            knightAttacksMask[kingPosition] and enemyKnights


        // add attacking pawns to check
        if (color == Piece.WHITE) {
            knightAndPawnsAttacks = knightAndPawnsAttacks or (northWest(kingMask) and enemyPawns)
            knightAndPawnsAttacks = knightAndPawnsAttacks or (northEast(kingMask) and enemyPawns)
        } else {
            knightAndPawnsAttacks = knightAndPawnsAttacks or (southWest(kingMask) and enemyPawns)
            knightAndPawnsAttacks = knightAndPawnsAttacks or (southEast(kingMask) and enemyPawns)
        }

        if (knightAndPawnsAttacks != 0L) {
            if (color == Piece.WHITE) {
                whiteKingCheckLine = whiteKingCheckLine and knightAndPawnsAttacks

            } else {
                blackKingCheckLine = blackKingCheckLine and knightAndPawnsAttacks

            }
        }

        //Log.d(ChessEngine.DEBUG_TAG, BitMath.print(whiteKingCheckLine))

        if (color == Piece.WHITE) {
            val myPieces =
                if (chessBoard.enPassantTarget == ChessBoard.NO_SQUARE) chessBoard.allWhitePieces else (chessBoard.allWhitePieces or (1L shl (chessBoard.enPassantTarget - 8)))
            whiteKingNorthConnectedPieces = northTargets and myPieces
            whiteKingNorthWestConnectedPieces = northWestTargets and myPieces
            whiteKingWestConnectedPieces = westTargets and myPieces
            whiteKingSouthWestConnectedPieces = southWestTargets and myPieces
            whiteKingSouthConnectedPieces = southTargets and myPieces
            whiteKingSouthEastConnectedPieces = southEastTargets and myPieces
            whiteKingEastConnectedPieces = eastTargets and myPieces
            whiteKingNorthEastConnectedPieces = northEastTargets and myPieces

        } else {
            val myPieces =
                if (chessBoard.enPassantTarget == ChessBoard.NO_SQUARE) chessBoard.allBlackPieces else (chessBoard.allBlackPieces or (1L shl (chessBoard.enPassantTarget + 8)))
            blackKingNorthConnectedPieces = northTargets and myPieces
            blackKingNorthWestConnectedPieces = northWestTargets and myPieces
            blackKingWestConnectedPieces = westTargets and myPieces
            blackKingSouthWestConnectedPieces = southWestTargets and myPieces
            blackKingSouthConnectedPieces = southTargets and myPieces
            blackKingSouthEastConnectedPieces = southEastTargets and myPieces
            blackKingEastConnectedPieces = eastTargets and myPieces
            blackKingNorthEastConnectedPieces = northEastTargets and myPieces
        }
        //-------------------------------------------------------------

        val opponentColor = Piece.GetOppositeColor(color)
        var rooks: Long = 0
        var bishops: Long = 0
        var queens: Long = 0
        if (opponentColor == Piece.WHITE) {
            rooks = chessBoard.whiteRooks
            bishops = chessBoard.whiteBishops
            queens = chessBoard.whiteQueens
        } else {
            rooks = chessBoard.blackRooks
            bishops = chessBoard.blackBishops
            queens = chessBoard.blackQueens

        }
        //check rooks and queens
        var rooksAndQueens = rooks or queens
        val rooksAndQueensCount = BitMath.countSetBits(rooksAndQueens)
        var rookPosition = 0
        for (rook in 0 until rooksAndQueensCount) {
            rookPosition = BitMath.getLSBitIndex(rooksAndQueens)
            rooksAndQueens = BitMath.popBit(rooksAndQueens, rookPosition)
            val southAttacks =
                rookAttacks(NORTH, rookPosition, chessBoard.allPieces)
            val northAttacks =
                rookAttacks(SOUTH, rookPosition, chessBoard.allPieces)
            val westAttacks =
                rookAttacks(EAST, rookPosition, chessBoard.allPieces)
            val eastAttacks =
                rookAttacks(WEST, rookPosition, chessBoard.allPieces)

            //update king pinned pieces
            if (color == Piece.WHITE) {
                val northPinnedPiece = northAttacks and whiteKingNorthConnectedPieces
                if (northPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(northPinnedPiece, SOUTH)] =
                        northAttacks or (1L shl rookPosition) or northTargets // include squares between king and pinned piece
                }
                val southPinnedPiece = southAttacks and whiteKingSouthConnectedPieces
                if (southPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(southPinnedPiece, NORTH)] =
                        southAttacks or (1L shl rookPosition) or southTargets // include squares between king and pinned piece
                }
                val westPinnedPiece = westAttacks and whiteKingWestConnectedPieces
                if (westPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(westPinnedPiece, EAST)] =
                        westAttacks or (1L shl rookPosition) or westTargets // include squares between king and pinned piece

                }

                // check EnPassant capture reveal
                if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE) {
                    val enPassantPawn = chessBoard.enPassantTarget - 8
                    if(westAttacks and (1L shl (enPassantPawn - 1)) and chessBoard.whitePawns != 0L && whiteKingWestConnectedPieces and (1L shl enPassantPawn) != 0L){
                        whiteKingPinnedPieces[enPassantPawn - 1] =
                            whiteKingPinnedPieces[enPassantPawn - 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }
                    if(westAttacks and (1L shl (enPassantPawn)) != 0L && whiteKingWestConnectedPieces and (1L shl (enPassantPawn + 1)) and chessBoard.whitePawns != 0L){
                        whiteKingPinnedPieces[enPassantPawn + 1] =
                            whiteKingPinnedPieces[enPassantPawn + 1] and (1L shl chessBoard.enPassantTarget).inv()

                    }

                }

                val eastPinnedPiece = eastAttacks and whiteKingEastConnectedPieces

                if (eastPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(eastPinnedPiece, WEST)] =
                        eastAttacks or (1L shl rookPosition) or eastTargets // include squares between king and pinned piece
                }

                // check EnPassant capture reveal
                if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE) {
                    val enPassantPawn = chessBoard.enPassantTarget - 8
                    if(eastAttacks and (1L shl (enPassantPawn + 1)) and chessBoard.whitePawns != 0L && whiteKingEastConnectedPieces and (1L shl enPassantPawn) != 0L){
                        whiteKingPinnedPieces[enPassantPawn + 1] =
                            whiteKingPinnedPieces[enPassantPawn + 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }
                    if(eastAttacks and (1L shl (enPassantPawn)) != 0L && whiteKingEastConnectedPieces and (1L shl (enPassantPawn - 1)) and chessBoard.whitePawns != 0L){
                        whiteKingPinnedPieces[enPassantPawn - 1] =
                            whiteKingPinnedPieces[enPassantPawn - 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }

                }

            } else {
                val northPinnedPiece = northAttacks and blackKingNorthConnectedPieces
                if (northPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(northPinnedPiece, SOUTH)] =
                        northAttacks or (1L shl rookPosition) or northTargets // include squares between king and pinned piece
                }
                val southPinnedPiece = southAttacks and blackKingSouthConnectedPieces
                if (southPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(southPinnedPiece, NORTH)] =
                        southAttacks or (1L shl rookPosition) or southTargets // include squares between king and pinned piece
                }
                val westPinnedPiece = westAttacks and blackKingWestConnectedPieces
                if (westPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(westPinnedPiece, EAST)] =
                        westAttacks or (1L shl rookPosition) or westTargets // include squares between king and pinned piece

                }

                // check EnPassant capture reveal
                if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE) {
                    val enPassantPawn = chessBoard.enPassantTarget + 8
                    if(westAttacks and (1L shl (enPassantPawn -1)) and chessBoard.blackPawns != 0L && blackKingWestConnectedPieces and (1L shl enPassantPawn) != 0L){
                        blackKingPinnedPieces[enPassantPawn - 1] =
                            blackKingPinnedPieces[enPassantPawn - 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }
                    if(westAttacks and (1L shl (enPassantPawn)) != 0L && blackKingWestConnectedPieces and (1L shl (enPassantPawn + 1)) and chessBoard.blackPawns != 0L){
                        blackKingPinnedPieces[enPassantPawn + 1] =
                            blackKingPinnedPieces[enPassantPawn + 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }

                }


                val eastPinnedPiece = eastAttacks and blackKingEastConnectedPieces
                if (eastPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(eastPinnedPiece, WEST)] =
                        eastAttacks or (1L shl rookPosition) or eastTargets // include squares between king and pinned piece

                }

                // check EnPassant capture reveal
                if (chessBoard.enPassantTarget != ChessBoard.NO_SQUARE) {
                    val enPassantPawn = chessBoard.enPassantTarget + 8
                    if(eastAttacks and (1L shl (enPassantPawn + 1)) and chessBoard.blackPawns != 0L && blackKingEastConnectedPieces and (1L shl enPassantPawn) != 0L){
                        blackKingPinnedPieces[enPassantPawn + 1] =
                            blackKingPinnedPieces[enPassantPawn + 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }
                    if(eastAttacks and (1L shl (enPassantPawn)) != 0L && blackKingEastConnectedPieces and (1L shl (enPassantPawn - 1)) and chessBoard.blackPawns != 0L){
                        blackKingPinnedPieces[enPassantPawn - 1] =
                            blackKingPinnedPieces[enPassantPawn - 1] and (1L shl chessBoard.enPassantTarget).inv()
                    }

                }

            }

        }

        //check bishops and queens
        var bishopsAndQueens = bishops or queens
        val bishopsAndQueensCount = BitMath.countSetBits(bishopsAndQueens)
        var bishopPosition = 0
        for (bishop in 0 until bishopsAndQueensCount) {
            bishopPosition = BitMath.getLSBitIndex(bishopsAndQueens)
            bishopsAndQueens = BitMath.popBit(bishopsAndQueens, bishopPosition)
            val southEastAttacks = bishopAttacks(
                NORTH_WEST,
                bishopPosition,
                chessBoard.allPieces
            )
            val northEastAttacks = bishopAttacks(
                SOUTH_WEST,
                bishopPosition,
                chessBoard.allPieces
            )
            val southWestAttacks = bishopAttacks(
                NORTH_EAST,
                bishopPosition,
                chessBoard.allPieces
            )
            val northWestAttacks = bishopAttacks(
                SOUTH_EAST,
                bishopPosition,
                chessBoard.allPieces
            )

            //update king pinned pieces
            if (color == Piece.WHITE) {
                val southEastPinnedPiece = southEastAttacks and whiteKingSouthEastConnectedPieces
                if (southEastPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(southEastPinnedPiece, NORTH_WEST)] =
                        southEastAttacks or (1L shl bishopPosition) or southEastTargets // include squares between king and pinned piece
                }
                val northEastPinnedPiece = northEastAttacks and whiteKingNorthEastConnectedPieces
                if (northEastPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(northEastPinnedPiece, SOUTH_WEST)] =
                        northEastAttacks or (1L shl bishopPosition) or northEastTargets // include squares between king and pinned piece
                }
                val southWestPinnedPiece = southWestAttacks and whiteKingSouthWestConnectedPieces
                if (southWestPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(southWestPinnedPiece, NORTH_EAST)] =
                        southWestAttacks or (1L shl bishopPosition) or southWestTargets // include squares between king and pinned piece
                }
                val northWestPinnedPiece = northWestAttacks and whiteKingNorthWestConnectedPieces
                if (northWestPinnedPiece != 0L) {
                    whiteKingPinnedPieces[BitMath.getBitIndex(northWestPinnedPiece, SOUTH_EAST)] =
                        northWestAttacks or (1L shl bishopPosition) or northWestTargets // include squares between king and pinned piece
                }
            } else {
                val southEastPinnedPiece = southEastAttacks and blackKingSouthEastConnectedPieces
                if (southEastPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(southEastPinnedPiece, NORTH_WEST)] =
                        southEastAttacks or (1L shl bishopPosition) or southEastTargets // include squares between king and pinned piece
                }
                val northEastPinnedPiece = northEastAttacks and blackKingNorthEastConnectedPieces
                if (northEastPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(northEastPinnedPiece, SOUTH_WEST)] =
                        northEastAttacks or (1L shl bishopPosition) or northEastTargets // include squares between king and pinned piece
                }
                val southWestPinnedPiece = southWestAttacks and blackKingSouthWestConnectedPieces
                if (southWestPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(southWestPinnedPiece, NORTH_EAST)] =
                        southWestAttacks or (1L shl bishopPosition) or southWestTargets // include squares between king and pinned piece
                }
                val northWestPinnedPiece = northWestAttacks and blackKingNorthWestConnectedPieces
                if (northWestPinnedPiece != 0L) {
                    blackKingPinnedPieces[BitMath.getBitIndex(northWestPinnedPiece, SOUTH_EAST)] =
                        northWestAttacks or (1L shl bishopPosition) or northWestTargets // include squares between king and pinned piece
                }
            }
        }

        isWhiteKingChecked = whiteKingCheckLine != 0L.inv()
        isBlackKingChecked = blackKingCheckLine != 0L.inv()
    }


    companion object {
        // initial capacity for a full legal move list
        private const val MAX_MOVES_HINT = 64
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
        const val NORTH_WEST = 0
        const val NORTH_EAST = 1
        private const val SOUTH_WEST = 2
        private const val SOUTH_EAST = 3

        //rook rays constants
        private const val SOUTH = 0
        const val NORTH = 1
        const val EAST = 2
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
