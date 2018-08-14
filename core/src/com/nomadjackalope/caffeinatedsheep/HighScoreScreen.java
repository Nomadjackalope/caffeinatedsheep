package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 8/13/2015.
 *
 *
 */
public class HighScoreScreen implements Screen {

    CaffeinatedSheep game;

    int screen;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();

    ArrayList<Scores> scores = new ArrayList<Scores>();


    public HighScoreScreen(final CaffeinatedSheep game) {
        this.game = game;

        this.game.resetGrid();

        animTimes.clear();

        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));


        getHighScores();

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
                break;
        }
    }

    private void main() {
        game.calculateMoverMovement();

        game.batch.begin();
        game.batchDrawing();
        drawHighScores();
        game.batch.end();

    }

    private void getHighScores() {
        //System.out.println("HSS| scoresdist: " + game.scoreDist.descendingKeySet());
        //System.out.println("HSS| scoresdist: " + game.scoreDist);
        if(game.scoreDist != null) {
            if(!game.scoreDist.isEmpty() ) {

                int numHS = 6;

                for(Integer key : game.scoreDist.descendingKeySet()) {

                    for (int i = 0; i < game.scoreDist.get(key).size(); i++) {
                        scores.add(new Scores(game.scoreDist.get(key).get(i), key));

                        numHS--;

                        if(numHS <= 0) {
                            break;
                        }
                    }

                    if(numHS <= 0) {
                        break;
                    }
                }


            }
        }
        //System.out.println("HSS| scores: " + scores);
    }

    private void drawHighScores() {
        game.batch.setShader(game.fontShader);
        game.titleFont.getData().setScale(3);
        for (int i = 0; i < scores.size(); i++) {
            game.titleFont.draw(game.batch, scores.get(i).toString(), 130, 900 - (i* 130));
        }
        game.batch.setShader(null);
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}

