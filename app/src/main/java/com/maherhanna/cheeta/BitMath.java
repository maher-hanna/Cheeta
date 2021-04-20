package com.maherhanna.cheeta;

public class BitMath {
    //-----------------------------------------------------------------------------------
    //magic table for bit scanning (finding least and most significant set bit)

    private static final long Magic = 0x37E84A99DAE458FL;

    private static final int[] MagicTable =
            {
                    0, 1, 17, 2, 18, 50, 3, 57,
                    47, 19, 22, 51, 29, 4, 33, 58,
                    15, 48, 20, 27, 25, 23, 52, 41,
                    54, 30, 38, 5, 43, 34, 59, 8,
                    63, 16, 49, 56, 46, 21, 28, 32,
                    14, 26, 24, 40, 53, 37, 42, 7,
                    62, 55, 45, 31, 13, 39, 36, 6,
                    61, 44, 12, 35, 60, 11, 10, 9,
            };
    //********************************************************************************8888

    public static long setBitsValue(long number, long startingBit, long bitMask, int value){
        long result = 0;
        value = value << startingBit;
        number = number &(~bitMask);
        bitMask = bitMask & value;
        result = number | bitMask;
        return  result;
    }

    public static int setBitsValue(int number, int startingBit, int bitMask, int value){
        int result = 0;
        value = value << startingBit;
        number = number &(~bitMask);
        bitMask = bitMask & value;
        result = number | bitMask;
        return  result;
    }
    public static int getBitsValue(int number,int startingBit,int bitMask){
        int value = 0;
        value = bitMask & number;
        value = value >>> startingBit;
        return  value;

    }
    public static long getBitsValue(long number,long startingBit,long bitMask){
        long value = 0;
        value = bitMask & number;
        value = value >>> startingBit;
        return  value;

    }

    public static boolean isBitSet(long bitboard,long index){
        return (bitboard & (1L << index)) != 0;
    }

    public static int getBit(long bitboard,int index){
        return (int)((bitboard & (1L << index)) >>> index);
    }
    public static long setBit(long bitboard,int index){
        bitboard |= 1L << index;
        return bitboard;
    }
    public static long popBit(long bitboard,int index){
        return ~(1L << index) & bitboard;
    }

    public static int countSetBits(long bitboard){
        int count = 0;
        while (bitboard != 0){
            bitboard &= bitboard -1;
            count++;
        }
        return count;
    }

    //get least significant bit index
    public static int getLSBitIndex(long bitboard){
        if(bitboard != 0){
            return MagicTable[(int)(((bitboard & -bitboard) * Magic) >>> 58)];
        } else {
            return ChessBoard.OUT;
        }
    }

    //get most significant bit index
    public static int getMSBitIndex(long bitboard){
        if(bitboard != 0){
            bitboard |= bitboard >>> 1;
            bitboard |= bitboard >>> 2;
            bitboard |= bitboard >>> 4;
            bitboard |= bitboard >>> 8;
            bitboard |= bitboard >>> 16;
            bitboard |= bitboard >>> 32;
            bitboard = bitboard & ~(bitboard >>> 1);
            return MagicTable[(int)(bitboard * Magic >>> 58)];
        } else {
            return ChessBoard.OUT;
        }
    }
}
