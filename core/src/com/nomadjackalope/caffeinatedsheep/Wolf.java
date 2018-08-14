package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Benjamin on 8/12/2015.
 *
 */
public class Wolf {

    Rectangle rect;
    Float radius;

    Long endTime;

    Wolf(float x, float y, float r) {
        radius = r;
        rect = new Rectangle(x, y, 80, 120);


    }

}
