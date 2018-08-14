package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 8/25/2015.
 *
 *
 */
public class MoverMovement {

    CaffeinatedSheep game;


    float prevX;
    float prevY;

    float speed;
    float multiplier;

    float time;
    float speedTimeConst;

    float idOffset;

    float myAngleRad;
    float angleRad;

    float speedAdj;

    float x;

    Mover mover;
    int moverI;

    Vector2 sheepVector = new Vector2();

    public MoverMovement(CaffeinatedSheep g) {
        game = g;
    }

    public void calculateMovement() {
        for (moverI = 0; moverI < game.movers.size(); moverI++) {

            mover = game.movers.get(moverI);

            game.states.clear();
            game.states.add(game.STATE_TEMP);
            // Assuming we'll never get into this unless we're in the GameScreen
            if (game.moverOverlapsState(mover, game.states) != 0) {
                //System.out.println("GS| overlaps: " + lastTimeTouched);

                if(game.getScreen().getClass() == GameScreen.class) {
                    GameScreen screen = (GameScreen) game.getScreen();
                    screen.cancelLine();
                    screen.lastTimeTouched = true;
                }

            }

            // Bounces the sheep off walls
            // 0 = no overlap, 1 = botleft, 2 = botright, 4 = topright, 8 = topleft // Sheep hitbox
            // All following references to direction are in reference to screen
            game.states.add(game.STATE_FINAL);
            game.states.add(game.STATE_TEMP_NOTOUCH);
            int integers = game.moverOverlapsState(mover, game.states);
            if (integers != 0) {
                //bottom
                if (integers == 3) {
                    if (mover.vector.y < 0) {
//                        if(Math.abs(mover.vector.x) > 0.8f) {
//                            mover.vector.setAngle(360 - mover.vector.angle() + 90);
//                        }
                        mover.vector.setAngle(360 - mover.vector.angle());
                        //mover.vector.y = -mover.vector.y;
                    }
                }
                //right
                if (integers == 6) {
                    if (mover.vector.x > 0) {
//                        if(Math.abs(mover.vector.y) > 0.8f) {
//                            mover.vector.setAngle(360 - mover.vector.angle() + 270);
//                        }
                        mover.vector.setAngle(360 - mover.vector.angle() + 180);
                        //mover.vector.x = -mover.vector.x;
                    }
                }
                //top
                if (integers == 12) {
                    if (mover.vector.y > 0) {
//                        if(Math.abs(mover.vector.x) > 0.8f) {
//                            mover.vector.setAngle(360 - mover.vector.angle() + 90);
//                        }
                        mover.vector.setAngle(360 - mover.vector.angle());
                        //mover.vector.y = -mover.vector.y;
                    }
                }
                //left
                if (integers == 9) {
                    if (mover.vector.x < 0) {
//                        if(Math.abs(mover.vector.y) > 0.8f) {
//                            mover.vector.setAngle(360 - mover.vector.angle() + 270);
//                        }
                        mover.vector.setAngle(360 - mover.vector.angle() + 180);
                        //mover.vector.x = -mover.vector.x;
                    }
                }
                //bottom left
                if (integers == 11) {
                    if (mover.vector.y < 0) {
                        mover.vector.setAngle(45);
                    }
                }
                //bottom right
                if (integers == 7) {
                    if (mover.vector.x > 0) {
                        mover.vector.setAngle(135);
                    }
                }
                //top right
                if (integers == 14) {
                    if (mover.vector.y > 0) {
                        mover.vector.setAngle(225);
                    }
                }
                //top left
                if (integers == 13) {
                    if (mover.vector.x < 0) {
                        mover.vector.setAngle(315);
                    }
                }

            }

            int moverIndex = game.movers.indexOf(mover);
            int moverSize = game.movers.size();

            // Last mover is not checked
            if (moverIndex < moverSize) {

                // For all movers past the current in the list
                for (int i = moverIndex + 1; i < moverSize; i++) {

                    // If the two were not overlapped last frame
                    if (!mover.overlap.contains(game.movers.get(i))) {

                        // If they intersect
                        //System.out.println("GS| : ");
                        if (mover.rect.overlaps(game.movers.get(i).rect)) {

                            mover.overlap.add(game.movers.get(i));

                            prevX = mover.vector.x;
                            prevY = mover.vector.y;

                            if ((mover.vector.x > 0 && game.movers.get(i).vector.x < 0) ||
                                    (mover.vector.x < 0 && game.movers.get(i).vector.x > 0)) {

                                mover.vector.x = game.movers.get(i).vector.x;
                                game.movers.get(i).vector.x = prevX;


                            }

                            if ((mover.vector.y > 0 && game.movers.get(i).vector.y < 0) ||
                                    (mover.vector.y < 0 && game.movers.get(i).vector.y > 0)) {
                                mover.vector.y = game.movers.get(i).vector.y;
                                game.movers.get(i).vector.y = prevY;


                            }
                        }
                    } else {
                        if (!mover.rect.overlaps(game.movers.get(i).rect)
                                && !game.movers.get(i).rect.overlaps(mover.rect)) {
                            mover.overlap.remove(game.movers.get(i));
                        }
                    }
                }
            }


            //--------------------------------Movers / Random movement ----------------------

            // Higher speed will produce straighter paths
            speed = 5; // higher #'s = slower
            multiplier = 1/speed;

            game.tempVector.x = mover.rect.x;
            game.tempVector.y = mover.rect.y;

            x++;

            time = (float) (TimeUtils.millis() - game.originTime);

            speedTimeConst = ((TimeUtils.millis() - game.deltaTime)/6.28318f)/speed;

            //Groups sheep up. The closer this gets to zero the more grouping up happens
            idOffset = mover.id/1000;

            // Makes a vector placed roughly in the scale of the unit circle
            sheepVector.x = game.sin(x/mover.vector.x);
            sheepVector.y = game.cos(x/mover.vector.y);

            // Vector2 myVector = new Vector2(game.sin(time / (speedTimeConst + 3.14f * mover.vector.x / game.playWidthFloat + idOffset)),
                   // game.cos(time / (speedTimeConst + 3.14f * mover.vector.y / game.playHeightFloat + idOffset)));

            myAngleRad = sheepVector.angleRad();

            sheepVector.x = game.cos(myAngleRad);
            sheepVector.y = game.sin(myAngleRad);

            //Aim to target
            if(game.target != null) {
                //target.z is used for radius
                float deltaX = mover.rect.x - game.target.x;
                float deltaY = mover.rect.y - game.target.y;
                if(deltaX * deltaX + deltaY * deltaY < game.target.z * game.target.z) {

                    game.target2D.x = game.target.x;
                    game.target2D.y = game.target.y;
                    sheepVector.setAngle(game.getAngle(game.tempVector, game.target2D));

                }
            }

            if(game.antiTarget != null) {
                float deltaX = mover.rect.x - game.antiTarget.x;
                float deltaY = mover.rect.y - game.antiTarget.y;
                if(deltaX * deltaX + deltaY * deltaY < game.antiTarget.z * game.antiTarget.z) {

                    game.target2D.x = game.antiTarget.x;
                    game.target2D.y = game.antiTarget.y;
                    sheepVector.setAngle(game.getAngle(game.tempVector, game.target2D) + 180);

                }
            }

            mover.vector.x += multiplier * sheepVector.x;
            mover.vector.y += multiplier * sheepVector.y;

            //Rescales vectors down approximately to unit circle size
            angleRad = mover.vector.angleRad();
            mover.vector.x = game.cos(angleRad);
            mover.vector.y = game.sin(angleRad);


            if (game.resumed) {
                game.resumed = false;
            } else {
                // These make the movers move
                speedAdj = 400; //Expecting a value of -1 to 1 from vectors. Change this if that range changes
                mover.rect.x += mover.vector.x * speedAdj * Gdx.graphics.getDeltaTime(); //mover.vector.x *
                mover.rect.y += mover.vector.y * speedAdj * Gdx.graphics.getDeltaTime(); //mover.vector.y *
            }
        }

        game.deltaTime = TimeUtils.millis();
    }

}
