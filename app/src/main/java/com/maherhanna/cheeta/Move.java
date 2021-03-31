package com.maherhanna.cheeta;

import java.util.Objects;

public class Move {
    public int from;
    public int to;
    public Type type;
    public boolean takes ;


    public Move(int from, int to){
        this.from = from;
        this.to = to;
        type = Type.NORMAL;
        takes = false;
    }

    public Move(int from,int to ,boolean takes){
        this(from,to);
        this.takes = takes;
    }

    public Move(int from, int to, Type type){
        this(from,to);
        this.type = type;
    }

    public boolean isCastling(){
        if(type == Type.CASTLING_kING_SIDE || type == Type.CASTLING_QUEEN_SIDE) {
            return true;
        }
        else {
            return false;
        }
    }

    public Move(Move copy){
        this.from = copy.from;
        this.to = copy.to;
        this.type = copy.type;
        this.takes = copy.takes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from &&
                to == move.to &&
                takes == move.takes &&
                type == move.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type, takes);
    }

    public enum Type{NORMAL,CASTLING_kING_SIDE,CASTLING_QUEEN_SIDE};
}
