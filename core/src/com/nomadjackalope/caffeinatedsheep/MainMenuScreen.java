package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 6/19/2015.
 *
 */
public class MainMenuScreen implements Screen {

    CaffeinatedSheep game;

    private boolean clickedLastTime;

    Vector3 touchPos = new Vector3();

    GlyphLayout title;
    GlyphLayout play;
    GlyphLayout highScores;
    GlyphLayout user;

    int screen;

    final int GAME = 0;
    final int HIGHSCORES = 1;
    final int USERS = 2;

    Integer high = 0;
    String name = "";

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();



    public MainMenuScreen(final CaffeinatedSheep game) {
        this.game = game;

        game.frame = 0;

        game.resetGrid();



        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));


        game.resetTimes(TimeUtils.millis());


        int numSheep = 7; //
        //Sheep
        if(game.movers.size() != numSheep) {
            // Remove sheep down to numSheep
            for (int i = game.movers.size(); i > numSheep; i--) {
                game.movers.remove(game.movers.size() - 1);
            }


            // Add sheep up to numsheep
            for (int i = game.movers.size(); i < numSheep; i++) {
                game.spawnMover();
            }
        }


        game.fontCache.getFont().getData().setScale(5);
        game.fontCache.addText("Caffeinated Sheep!", 130, 350);

        game.titleFont.getData().setScale(2);

        if(!game.scoreDist.isEmpty()) {
            high = game.getHighestScore();
            name = game.scoreDist.get(high).get(0);
        } else {
            high = 0;
            name = "TBD";
        }

        highScores = new GlyphLayout(game.titleFont, "High Score: " + name + " | " + high);
        play = new GlyphLayout(game.titleFont, "Play");
        user = new GlyphLayout(game.titleFont, game.activeUser.toString() + " | " + String.valueOf(game.activeUser.money));

        game.titleFont.getData().setScale(1);

    }

    @Override
    public void render(float delta) {

        game.frame++;
        //Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        animator();

        //game.testDrawing();

    }

    private Animate anim;

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
        game.drawGrass();
        game.introWalls();
        game.introMovers();
        introLabels();
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
        game.titleFont.draw(game.batch, highScores, 1760 - highScores.width, 920);
        game.titleFont.draw(game.batch, play, 130, 200);
        game.titleFont.draw(game.batch, user, 130, 920);

        game.fontCache.getFont().getData().setScale(5);
        game.fontCache.draw(game.batch);
        game.batch.setShader(null);


        // ----------- High Scores ------------

        game.batch.end();
    }


    private void handleClicks() {
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);

            if(game.target != null) {
                game.target.x = touchPos.x;
                game.target.y = touchPos.y;
                game.target.z = 300;
            } else {
                game.target = new Vector3(touchPos.x, touchPos.y, 300); //radius
            }

            //System.out.println("MMS| touchPos: " + touchPos.x + ", " + touchPos.y);
            //if(touchPos.y > 120 && touchPos.y < 200 && touchPos.x > 112 && touchPos.x < 248) {

            // Get half
            if(touchPos.y > game.playHeightFloat / 2) {
                if (touchPos.y < 940 && touchPos.y > 900 - user.height
                        && touchPos.x > 100 && touchPos.x < 160 + user.width) {
                    game.target = null;
                    setEnd(USERS);
                } else if(game.scoreDist != null) {
                    if(game.scoreDist.size() > 0) {
                        if(touchPos.y > 845 && touchPos.y < 905 && touchPos.x > 995 && touchPos.x < 1645) {
                            game.target = null;
                            setEnd(HIGHSCORES);
                        }
                    }
                }
            } else {
                if(touchPos.y > 80 && touchPos.y < 300
                        && touchPos.x > 80 && touchPos.x < 300) {
                    game.target = null;
                    setEnd(GAME);
                }
            }

        } else {
            game.target = null;
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
            case GAME:
                game.setScreen(new GameScreen(game, 2, 15000)); //15 seconds
                dispose();
                break;
            case HIGHSCORES:
                game.setScreen(new HighScoreScreen(game));
                dispose();
                break;
            case USERS:
                game.setScreen(new UserScreen(game));
                dispose();
                break;
        }
    }

    private void introLabels() {

        long scale = TimeUtils.millis() - animTimes.get(INTRO).beginTime;

        game.titleFont.getData().setScale(game.introFunc(scale));
        game.titleFont.draw(game.batch, "Play", 130, 200);
        game.batch.setShader(game.fontShader);
        game.titleFont.getData().setScale(5 * game.introFunc(scale));
        game.titleFont.setColor(1, 1, 1, 0.93f);
        title = game.titleFont.draw(game.batch, "Caffeinated Sheep!", 130, 350);
        game.titleFont.getData().setScale(3 * game.introFunc(scale));
        game.titleFont.draw(game.batch, "High score: " + name + " | " + high,
                1760 - highScores.width, 920);
        game.batch.setShader(null);

    }


    @Override
    public void dispose() {
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
}
