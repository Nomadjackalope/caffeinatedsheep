package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.utils.TimeUtils;


/**
 * Created by Benjamin on 8/12/2015.
 *
 *
 */
public class Animate {

    Object obj;
    long beginTime;
    long endTime;
    long length;
    private long timeLeft;
    private long timeElapsed;

    Integer id;


    public Animate(Integer i, Long begin, Long length) {
        //obj = o;
        id = i;
        beginTime = begin;
        this.length = length;
        endTime = begin + length;
        timeLeft = endTime;

    }


    public void resetBeginTime(Long newTime) {
        beginTime = newTime;
        endTime = beginTime + length;
        getTimeLeft();
    }

    public long getTimeLeft() {
        timeLeft = endTime - TimeUtils.millis();
        return timeLeft;
    }

    private long getTimeElapsed() {
        timeElapsed = TimeUtils.millis() - beginTime;
        return timeElapsed;
    }

    public void pause() {
        getTimeElapsed();
    }

    public void resume() {
        resetBeginTime(TimeUtils.millis() - timeElapsed);
    }


    @Override
    public String toString() {
        return id.toString();
    }
}
