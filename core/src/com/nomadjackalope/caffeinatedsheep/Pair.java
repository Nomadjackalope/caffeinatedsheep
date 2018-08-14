package com.nomadjackalope.caffeinatedsheep;

/**
 * Created by Benjamin on 6/24/2015.
 *
 *
 */
public class Pair {
    Integer x;
    Integer y;

    public Pair() {
        x = 0;
        y = 0;
    }

    public Pair(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public void setY(Integer y) {
        this.y = y;
    }

}
