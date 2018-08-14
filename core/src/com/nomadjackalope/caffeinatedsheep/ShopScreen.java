package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 7/30/2015.
 *
 */
public class ShopScreen implements Screen {
    CaffeinatedSheep game;

    int screen;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();


    public ShopScreen(final CaffeinatedSheep game) {
        this.game = game;


        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));


        game.resetTimes(TimeUtils.millis());
    }


    @Override
    public void render(float delta) {

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        animator();

    }

    private void animator() {
        for( Animate anim : animTimes) {
            if (anim.beginTime < TimeUtils.millis() &&
                    anim.getTimeLeft() > 0) {
                functions(anim.id);
            }
        }
    }

    private void functions(Integer x) {
        switch (x) {
            case INTRO:
                intro();
                break;
            case OUTRO:
                outro();
                break;
            case HANDLECLICKS:
                handleClicks();
                break;
            case END:
                end();
                break;
            case MAIN:
                main();
        }
    }

    private void intro() {
        game.batch.begin();
        game.drawGrass();
        game.introWalls();
        game.introMovers();
        game.batch.end();
    }

    private void outro() {
        game.batch.begin();
        game.drawGrass();
        game.outroWalls();
        game.outroMovers();
        game.batch.end();
    }

    private void main() {

        game.batch.begin();

        //---------- Move Sheep ---------------
        game.calculateMoverMovement();
        game.batchDrawing();

        // ----------- Labels ----------

        game.batch.setShader(game.fontShader);
        game.titleFont.setColor(1, 1, 1, 0.93f);

        game.titleFont.getData().setScale(2);
        game.titleFont.draw(game.batch, "Buy food", 130, 820);
        game.titleFont.draw(game.batch, "Buy wolf", 130, 720);


        game.batch.setShader(null);


        game.batch.end();
    }


    private void handleClicks() {
        if(Gdx.input.isTouched()) {
            setEnd(0);
        }
    }

    private void setEnd(int num) {

        screen = num;

        animTimes.get(MAIN).endTime = TimeUtils.millis();
        animTimes.get(HANDLECLICKS).endTime = TimeUtils.millis();
        animTimes.get(OUTRO).resetBeginTime(TimeUtils.millis());
        animTimes.get(END).resetBeginTime(animTimes.get(OUTRO).endTime);

        game.resetTimes(animTimes.get(END).beginTime);
    }

    private void end() {
        switch (screen) {
            case 0:
                game.setScreen(new MainMenuScreen(game));
                dispose();
                break;
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        for(Animate anim: animTimes) {
            anim.pause();
        }
    }

    @Override
    public void resume() {
        for(Animate anim: animTimes) {
            anim.resume();
        }

        game.resetTimes(TimeUtils.millis());


    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
