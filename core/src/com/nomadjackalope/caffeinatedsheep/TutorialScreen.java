package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 7/30/2015.
 *
 */
public class TutorialScreen implements Screen {
    CaffeinatedSheep game;

    int screen;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();


    Texture tutorial;


    public TutorialScreen(final CaffeinatedSheep game) {
        this.game = game;

        tutorial = new Texture(Gdx.files.internal("gameintrosmall.png"));

        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, TimeUtils.millis(), game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(INTRO).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));

        MyTextInputListener listener = new MyTextInputListener();
        Gdx.input.getTextInput(listener, "Name please", "", "");

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

        game.batch.draw(tutorial, 64.5f,0, 1791, 1080);
        game.batch.end();
    }


    private void handleClicks() {
        if(Gdx.input.justTouched()) {
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

    public class MyTextInputListener implements Input.TextInputListener {


        @Override
        public void input (String text) {
            game.users.add(0, new User(text, 0, 0, 0, 0, 0));
            game.activeUser = game.users.get(0);
        }

        @Override
        public void canceled () {
            game.users.add(new User("Guest", 0,0,0,0,0));
            game.activeUser = game.users.get(0);
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
