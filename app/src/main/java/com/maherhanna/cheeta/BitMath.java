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
        value = value >> startingBit;
        return  value;

    }
}
