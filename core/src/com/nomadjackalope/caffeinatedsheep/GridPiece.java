package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by Benjamin on 6/26/2015.
 *
 *
 */
public class GridPiece {

    Integer id;
    int row;
    int column;

    ArrayList<GridPiece> neighbors = new ArrayList<GridPiece>(8);

    boolean isOrigin = false;

    // space = the space it's part of
    Integer space = 0;

    Rectangle rectangle;
    Rectangle wallRectangle;

    float gridPieceWidth = 60f;
    float gridPieceHeight = 60f;

    float offsetX = (gridPieceWidth / 241 * 50);
    float offsetY = (gridPieceHeight / 241 * 50);

    // State 0 = off, state 1 = temp, state 2 = temp-non-interactive, state 3 = final
    int state = 0;
    int previousState = 0;

    TextureRegion groundTex1;
    TextureRegion groundTex2;
    int wallTex;

    int firstAngle;
    int secondAngle;

    // Animation
    long startTime = TimeUtils.millis();
    float deltaTime = 0;

    public GridPiece() {
        rectangle = new Rectangle(0,0,gridPieceWidth,gridPieceHeight);
    }

    public GridPiece(float x, float y) {
        rectangle = new Rectangle(x, y, gridPieceWidth, gridPieceHeight);

    }

    public GridPiece(float x, float y, float width, float height) {
        gridPieceWidth = width;
        gridPieceHeight = height;
        rectangle = new Rectangle(x, y, gridPieceWidth, gridPieceHeight);

        offsetX = (gridPieceWidth / 241 * 50);
        offsetY = (gridPieceHeight / 241 * 50);

        wallRectangle = new Rectangle(x - offsetX, y - offsetY, width + offsetX, height + offsetY);
    }

    public GridPiece(float x, float y, Integer id) {
        rectangle = new Rectangle(x, y, gridPieceWidth, gridPieceHeight);
        wallRectangle = new Rectangle(x - offsetX, y - offsetY,
                gridPieceWidth + offsetX, gridPieceHeight + offsetY);
        this.id = id;
    }

    @Override
    public String toString() {
        if(id != null) {
            return id.toString();
        } else {
            return "No id";
        }
    }


    public Vector2 getCenter() {
        Vector2 vector2 = new Vector2();

        vector2.x = rectangle.x + (rectangle.width / 2);
        vector2.y = rectangle.y + (rectangle.height / 2);

        return vector2;

    }

    public void setState(int state) {
        if(state != this.state) {
            previousState = this.state;
            this.state = state;
        }
    }

    public void restoreState() {
        state = previousState;
    }



}
