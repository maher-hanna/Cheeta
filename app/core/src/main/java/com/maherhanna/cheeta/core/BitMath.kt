package com.maherhanna.cheeta.core

object BitMath {
    //-----------------------------------------------------------------------------------
    //magic table for bit scanning (finding least and most significant set bit)
    private const val Magic = 0x37E84A99DAE458FL
    private val MagicTable = intArrayOf(
        0, 1, 17, 2, 18, 50, 3, 57,
        47, 19, 22, 51, 29, 4, 33, 58,
        15, 48, 20, 27, 25, 23, 52, 41,
        54, 30, 38, 5, 43, 34, 59, 8,
        63, 16, 49, 56, 46, 21, 28, 32,
        14, 26, 24, 40, 53, 37, 42, 7,
        62, 55, 45, 31, 13, 39, 36, 6,
        61, 44, 12, 35, 60, 11, 10, 9
    )

    //********************************************************************************8888
    fun setBitsValue(number: Long, startingBit: Long, bitMask: Long, value: Int): Long {
        var number = number
        var bitMask = bitMask
        var result: Long = 0
        var valueMask = value.toLong()
        valueMask = valueMask shl startingBit.toInt()
        number = number and bitMask.inv()
        bitMask = bitMask and valueMask
        result = number or bitMask
        return result
    }

    fun getBitsValue(number: Long, startingBit: Long, bitMask: Long): Long {
        var value: Long = 0
        value = bitMask and number
        value = value ushr startingBit.toInt()
        return value
    }

    fun isBitSet(bitboard: Long, index: Long): Boolean {
        return bitboard and (1L shl index.toInt()) != 0L
    }

    fun getBit(bitboard: Long, index: Int): Int {
        return (bitboard and (1L shl index) ushr index).toInt()
    }

    fun setBit(bitboard: Long, index: Int): Long {
        var bitboard = bitboard
        bitboard = bitboard or (1L shl index)
        return bitboard
    }

    fun unSetBit(value: Int, index: Int): Int {
        var value = value
        value = value and (1 shl index).inv()
        return value
    }

    fun popBit(bitboard: Long, index: Int): Long {
        return (1L shl index).inv() and bitboard
    }

    // the java.lang.Long bit functions are compiler intrinsics (single CPU instructions)
    fun countSetBits(bitboard: Long): Int {
        return java.lang.Long.bitCount(bitboard)
    }

    //get least significant bit index
    fun getLSBitIndex(bitboard: Long): Int {
        return if (bitboard != 0L) {
            java.lang.Long.numberOfTrailingZeros(bitboard)
        } else {
            //ChessBoard.OUT
            -1
        }
    }

    fun getPositionsOf(bitboard: Long): ArrayList<Int> {
        var bitboard = bitboard
        val positions = ArrayList<Int>()
        val setBitsCount = countSetBits(bitboard)
        var currentPiecePosition = 0
        for (i in 0 until setBitsCount) {
            currentPiecePosition = getLSBitIndex(bitboard)
            positions.add(currentPiecePosition)
            bitboard = popBit(bitboard, currentPiecePosition)
        }
        return positions
    }

    //get most significant bit index
    fun getMSBitIndex(bitboard: Long): Int {
        return if (bitboard != 0L) {
            63 - java.lang.Long.numberOfLeadingZeros(bitboard)
        } else {
            //ChessBoard.OUT
            -1
        }
    }

    fun getBitIndex(mask:Long,rayDirection:Int): Int{
        return if (rayDirection == MoveGenerator.NORTH_EAST || rayDirection == MoveGenerator.NORTH_WEST ||
            rayDirection == MoveGenerator.EAST) {
            getLSBitIndex(mask)
        } else {
            getMSBitIndex(mask)
        }
    }

    fun print(bitboard: Long):String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(" \n")
        for (rank in 7 downTo 0) {
            for (file in 0..7) {
               if(isBitSet(bitboard,ChessBoard.Square(file,rank).toLong())) {
                   stringBuilder.append('1')
                   stringBuilder.append(' ')

               } else {
                   stringBuilder.append('0')
                   stringBuilder.append(' ')

               }
            }
            stringBuilder.append('\n')
        }
        return stringBuilder.toString()
    }

}
