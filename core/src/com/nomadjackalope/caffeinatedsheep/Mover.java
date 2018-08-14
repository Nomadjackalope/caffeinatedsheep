package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Benjamin on 6/24/2015.
 *
 *
 */
public class Mover {
    public Vector2 vector = new Vector2();

    static float idCount = 0;
    float id = 0;

    ArrayList<Mover> overlap = new ArrayList<Mover>();

    // The perimeter of the object, depending on visuals this could be a circle
    public Rectangle rect;

    public TextureRegion image;

    Random random = new Random();

    // Animation
    long startTime = TimeUtils.millis();


    private float speedAdjust = 1f;


    public Mover(float x, float y, TextureRegion image) {
        rect = new Rectangle(x, y, 40, 60);

        vector.set(0, 0);

        vector.set(randPosNeg()  * speedAdjust,
                randPosNeg() * speedAdjust);

        this.image = image;
    }

    public float randPosNeg() {
        float rand = Math.round(random.nextFloat());
        if(rand == 1f) {
            return rand;
        } else {
            return rand - 1f;
        }

    }

}
