package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 6/21/2015.
 *
 * The screen that shows when you are done with the game
 */
public class EndScreen implements Screen{
    CaffeinatedSheep game;

    int timeLeft;
    int timeUsed;
    int numMovers;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();
    int animI;
    Animate anim;

    String youCaptured;

    int screen;

    public EndScreen(final CaffeinatedSheep game, int timeLeft, long totalTime, final int numMovers) {
        this.game = game;
        this.numMovers = numMovers;
        this.timeLeft = timeLeft;

        //this.game.resetGrid();

        timeUsed = (int) totalTime / 1000 - timeLeft;

        giveMoney();


        animTimes.clear();

        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));



        if(timeUsed == 1) {
            youCaptured = "You captured the required garden space in " + (timeUsed) + " second!";
        } else {
            youCaptured = "You captured the required garden space in " + (timeUsed) + " seconds!";
        }


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
        for (animI = 0; animI < animTimes.size(); animI++) {
            anim = animTimes.get(animI);
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

    private void main() {
        game.batch.begin();
        game.batchDrawing();

        // ----------- Labels ----------

        game.batch.setShader(game.fontShader);
        game.titleFont.getData().setScale(2);
        game.titleFont.draw(game.batch, "Continue", 130, 200);

        game.titleFont.draw(game.batch, youCaptured, 130, 300);
        game.batch.setShader(null);

        game.batch.end();
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
        game.grassHasChanged = true;
        switch (screen) {
            case 0:
                game.setScreen(new GameScreen(game, numMovers + 1, (timeLeft * 1000) + 15000)); // timeleft to milliseconds + 15 seconds
                break;
            case 1:
                game.setScreen(new MainMenuScreen(game));
                break;
        }
    }

    private void giveMoney(){
        game.activeUser.money += numMovers * (28 /(timeUsed + 1) + 1);

        SaveHelper.saveUsers(game.users);
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
