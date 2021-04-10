package com.maherhanna.cheeta;

public class BitMath {
    public static int setBitsValue(int number,int startingBit,int endingBit,int value){
        int result = 0;
        int mask = (int)Math.pow(2,endingBit - startingBit + 1) - 1;
        mask = mask << (startingBit - 1);
        value = value << (startingBit - 1);
        number = number &(~mask);
        mask = mask & value;
        result = number | mask;
        return  result;

    }

    public static int getBitsValue(int number,int startingBit,int endingBit){
        int value = 0;
        int mask = (int)Math.pow(2,endingBit - startingBit + 1) - 1;
        mask = mask << (startingBit - 1) ;
        value = mask & number;
        value = value >> (startingBit - 1);
        return  value;

    }
}
