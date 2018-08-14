package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 7/1/2015.
 *
 */
public class FailScreen implements Screen {

    CaffeinatedSheep game;

    int numMovers;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();

    int screen;

    String youMadeIt;

    private Animate anim;


    public FailScreen(final CaffeinatedSheep game, int timeLeft, final int numMovers) {
        this.game = game;

        this.numMovers = numMovers;


        //this.game.resetGrid();

        youMadeIt = "You made it to " + numMovers + " sheep!";

        animTimes.clear();

        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));


        game.resetTimes(TimeUtils.millis());

    }

    @Override
    public void render(float delta) {

        game.frame++;
        //Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);


        animator();


    }

    private void animator() {
        for(int i = 0; i < animTimes.size(); i++) {
            anim = animTimes.get(i);
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
        game.batchDrawing();
        game.batch.end();
    }

    private void outro() {
        game.batch.begin();
        game.drawGrass();
        game.outroWalls();
        game.outroMovers();
        game.batch.end();
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
        game.grassHasChanged = true;

        switch (screen) {
            case 0:
                game.setScreen(new MainMenuScreen(game));
                break;
            case 1:
                game.setScreen(new GameScreen(game, 2, 15000)); // 15 seconds
                break;
        }
    }

    private void main() {
        game.batch.begin();
        game.batchDrawing();

        game.batch.setShader(game.fontShader);
        game.titleFont.getData().setScale(2);
        game.titleFont.draw(game.batch, "Main Menu", 130, 300);
        //game.titleFont.draw(game.batch, "Play again", 130, 300);
        game.titleFont.draw(game.batch, youMadeIt, 130, 400);
        game.batch.setShader(null);

        game.batch.end();
    }


    private void handleClicks() {
        if(Gdx.input.justTouched()) {
//            MyTextInputListener listener = new MyTextInputListener();
//            Gdx.input.getTextInput(listener, "Dialog Title", "", "name");
            game.addAScore(numMovers, game.activeUser.name);
            SaveHelper.saveHighScores(game.scoreDist);

            setEnd(0);
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

    public class MyTextInputListener implements Input.TextInputListener {
        @Override
        public void input (String text) {
            if(game.scoreDist != null) {
                System.out.println("GS| Entered");
                game.addAScore(numMovers, text);
                SaveHelper.saveHighScores(game.scoreDist);

                setEnd(0);
            }
        }

        @Override
        public void canceled () {

        }
    }
}