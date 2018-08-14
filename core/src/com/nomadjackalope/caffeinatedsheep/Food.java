package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Benjamin on 8/13/2015.
 *
 */
public class Food {

    Rectangle rect;
    Float radius;

    Long endTime;

    Food(float x, float y, float r) {
        radius = r;
        rect = new Rectangle(x, y, 66, 54);

    }
}
