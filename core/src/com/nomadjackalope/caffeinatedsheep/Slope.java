package com.nomadjackalope.caffeinatedsheep;

/**
 * Created by Benjamin on 8/6/2015.
 */
public class Slope {

    CaffeinatedSheep game;

    float right;
    float top;

    float randInt;
    float groundSize;

    int chanceDif;

    float x;
    float y;

    //derivative of Sin function
    //adjusting x and y
    float xHat;
    float yHat;

    float m = 5;

    float dz;

    float sinInt;

    float max;

    float progress;

    float lightInt;

    float rand;

    float finalInt;


    float fin;

    public Slope(CaffeinatedSheep game) {
        this.game = game;

        max = game.playWidthFloat + game.playHeightFloat;
    }


    public float randomFloat(GridPiece piece, float variance, boolean flip, boolean first) {
        //random.setSeed(randomSeed.nextLong());

        right = 0;
        top = 0;

        // Gets center of triangle
        if (flip) {
            if(first) {
                right = 0.25f * piece.rectangle.width;
                top = 0.75f * piece.rectangle.height;
            } else {
                right = 0.75f * piece.rectangle.width;
                top = 0.25f * piece.rectangle.height;
            }

        } else {
            if(first) {
                right = 0.25f * piece.rectangle.width;
                top = 0.25f * piece.rectangle.height;
            } else {
                right = 0.75f * piece.rectangle.width;
                top = 0.75f * piece.rectangle.height;
            }
        }

        randInt = game.random.nextInt(game.grounds.size());
        groundSize = (float) game.grounds.size();

        chanceDif = 0;
        x = piece.rectangle.x + right;
        y = piece.rectangle.y + top;

        //derivative of Sin function
        //adjusting x and y
        xHat = x * (3.14159265f * 4) / game.playWidthFloat;
        yHat = y * (3.14159265f * 4) / game.playHeightFloat;

        m = 5;

        dz = m * game.cos(m * xHat) + m * game.cos(m * yHat);

        sinInt = (dz + (m * 2)) * (20 / (m * 4));


        max = game.playWidthFloat + game.playHeightFloat;

        progress = (x + y) / max;

        lightInt = ((groundSize) * progress) + 1;

        // Gets a random value from 0 to 1
        rand = game.random.nextFloat();
        // adjusts the value to 0 to range
        rand = rand * variance;

        // For example: the range y = 15/20x to y = 15/20x + 5
        lightInt = lightInt * (groundSize - variance) / groundSize + rand;

        finalInt = (lightInt + 3 * sinInt) / 4;

        if (!first) {
            right = 1;
        } else {
            right = 0;
        }

        fin = game.perlinNoise[(int) (piece.column + right) * 2][(piece.row) * 2]; // * 20

        //fin = sin(piece.rectangle.x/playWidthFloat * 2 * 3.1415926f); (fin + 1) * 9;

        //fin = (Math.abs(piece.rectangle.x/playWidthFloat) + Math.abs(piece.rectangle.y/playHeightFloat)) / 2.1f; // * 20

        return fin * 20;
    }


}
