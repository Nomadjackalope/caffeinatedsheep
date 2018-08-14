package com.nomadjackalope.caffeinatedsheep;

import java.util.ArrayList;

/**
 * Created by Benjamin on 7/22/2015.
 *
 *
 */
public class Scores {
    public String names;
    public Integer score;

    public Scores(String s, Integer i) {
        names = s;
        score = i;
    }

    @Override
    public String toString() {
        return names + " | "  + score;
    }
}
