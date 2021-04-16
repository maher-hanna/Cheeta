package com.maherhanna.cheeta;

public class BitMath {
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

    public static boolean isBitSet(long bitboard,int index){
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

    public static int count1Bits(long bitboard){
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
            return count1Bits((bitboard & -bitboard) - 1);
        } else {
            return ChessBoard.OUT;
        }
    }
}
