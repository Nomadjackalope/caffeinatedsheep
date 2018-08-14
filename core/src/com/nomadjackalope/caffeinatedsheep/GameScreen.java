package com.nomadjackalope.caffeinatedsheep;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class GameScreen implements Screen {

    CaffeinatedSheep game;

    Rectangle lineBounds;

    Vector3 touchPos = new Vector3();
    Vector3 lineVector = new Vector3();

    boolean lastTimeTouched = true;

    int dropsGathered;
    int percentNeeded = 70;

    int screen;
    long TOTAL_TIME;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    final int FOOD = 5;
    final int WOLF = 6;
    final int COUNT = 7;
    final int NULLTARGET = 8;
    final int NULLANTITARGET = 9;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();
    int animI;
    Animate anim;

    GridPiece falsePiece;
    TreeMap<Integer, Rectangle> spaces = new TreeMap<Integer, Rectangle>();
    ArrayList<GridPiece> neighbors = new ArrayList<GridPiece>(4);


    GridPiece origin;
    ArrayList<GridPiece> originOpposite;
    GridPiece orOpActive;
    GridPiece tempPiece = falsePiece;

    boolean noLine = false;

    int numMovers;
    int timeLeft;


    int fps = 0;

    Texture wallTempTexture;

    Wolf theWolf;
    Food theFood;
    boolean placeFood = false;
    boolean placeWolf = false;
    boolean foodOrWolfActive = false;

    // FingerFling
    Vector3 fingerLastPosition;
    boolean returnable;
    float a;
    float b;
    float c;
    float speed;
    float distance;
    float distanceTotal;
    float tempX;
    float tempY;

    // Strings
    String userFood;
    String userDogs;
    String percentContained;
    String timeLeftString;


    public GameScreen(CaffeinatedSheep theGame, int numMovers, long time) {
        this.game = theGame;

        this.numMovers = numMovers;

        this.TOTAL_TIME = time;

        game.seed = (long) (numMovers * game.randomSeed.nextInt());

        game.resetGrid();

        falsePiece = new GridPiece();
        falsePiece.id = -1;
        origin = falsePiece;
        originOpposite = new ArrayList<GridPiece>();
        originOpposite.add(falsePiece);
        orOpActive = falsePiece;

        game.movers.clear();
        for (int i = 0; i < this.numMovers; i++) {
            game.spawnMover();
        }

        game.frame = 0;

        animTimes.clear();

        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300L));
        animTimes.add(MAIN, new Animate(MAIN, 0L, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, 0L, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300L));
        animTimes.add(END, new Animate(END, game.hugeNum, 300L));

        animTimes.add(FOOD, new Animate(FOOD, -game.hugeNum, 5000L));
        animTimes.add(WOLF, new Animate(WOLF, -game.hugeNum, 5000L));
        animTimes.add(COUNT, new Animate(COUNT, animTimes.get(INTRO).endTime, 3000L));
        animTimes.add(NULLTARGET, new Animate(NULLTARGET, game.hugeNum, 50L));
        animTimes.add(NULLANTITARGET, new Animate(NULLANTITARGET, game.hugeNum, 50L));


        animTimes.get(MAIN).resetBeginTime(animTimes.get(COUNT).endTime);
        animTimes.get(HANDLECLICKS).resetBeginTime(animTimes.get(COUNT).endTime);


        game.resetTimes(TimeUtils.millis());

    }

    @Override
    public void render(float delta) {
        //____________________________________________________________
        game.frame++;
        fps = Gdx.graphics.getFramesPerSecond();
        if (Math.abs(fps - Gdx.graphics.getFramesPerSecond()) > 2) {
            System.out.println("GS| fps: " + fps);
        }
        //____________________________________________________________


        //Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        animator();

    }

    // Tracks animation times
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
                break;
            case FOOD:
                food();
                break;
            case WOLF:
                wolf();
                break;
            case COUNT:
                count();
                break;
            case NULLTARGET:
                nullTarget();
                break;
            case NULLANTITARGET:
                nullAntiTarget();
                break;
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
        game.batchDrawing();
        game.batch.end();
    }

    private void count() {
        game.batch.begin();

        game.batchDrawing();

        game.titleFont.getData().setScale(5);
        if(animTimes.get(COUNT).getTimeLeft() > 0) {
            game.batch.setShader(game.fontShader);
            game.titleFont.draw(game.batch, String.valueOf(animTimes.get(COUNT).getTimeLeft() / 1000 + 1),
                    game.playWidthFloat / 2 - 50, game.playHeightFloat / 2 + 50);
            game.batch.setShader(null);
        }

        game.batch.end();
    }

    private void end() {

        for (int i = 0; i < game.gridPieces.length; i++) {
            GridPiece piece = game.gridPieces[i];
            if(piece.state == game.STATE_TEMP_NOTOUCH) {
                piece.state = game.STATE_FINAL;
            } else if(piece.state == game.STATE_TEMP) {
                piece.state = game.STATE_NONE;
            }
        }

        System.out.println("GS| percent gathered: " + dropsGathered);
        System.out.println("GS| percent needed: " + percentNeeded);
        switch (screen) {
            case 0:
                game.setScreen(new EndScreen(game, timeLeft, TOTAL_TIME, numMovers));
                dispose();
                break;
            case 1:
                game.setScreen(new FailScreen(game, timeLeft , numMovers));
                dispose();
                break;
        }
    }

    private void main() {

        Gdx.graphics.getGL20().glClearColor(0.8f,0.8f,0.8f,1);
        Gdx.graphics.getGL20().glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        //--------------Ends Game-----------

        if (dropsGathered > percentNeeded && TimeUtils.millis() - animTimes.get(MAIN).beginTime <= TOTAL_TIME) {
            setEnd(0);
        } else if ((TOTAL_TIME - (TimeUtils.millis() - animTimes.get(MAIN).beginTime)) < 0) {
            setEnd(1);
        }

        //-----------------------------------Movers Movement-------------------------------

        game.calculateMoverMovement();

        //-----------------------------------Batch / Drawing-------------------------------


        game.random.setSeed(0);

        game.batch.begin();

        game.batchDrawing();

        // Draw food button
        if(placeFood) {
            game.batch.draw(game.foodTexOn, 0, 0, 66 * 2.5f, 54 * 2.5f);
        } else {
            game.batch.draw(game.foodTex, 0, 0, 66 * 1.8f, 54 * 1.8f);
        }

        // Draw wolf button
        if(placeWolf) {
            game.batch.draw(game.wolfTexOn, 66 * 2.5f, 0, 66 * 2.5f, 54 * 2.5f);
        } else {
            game.batch.draw(game.wolfTex, 66*1.8f, 0, 66 * 1.8f, 54 * 1.8f);
        }

        if(TimeUtils.millis() - animTimes.get(MAIN).beginTime > TOTAL_TIME - 3000) {
            float ratio = ((float) ((TimeUtils.millis() - animTimes.get(MAIN).beginTime) - (TOTAL_TIME - 3000))) / 3000 * 0.5f + 0.5f;
            //System.out.println("GS| ratio: " + ratio);
            game.titleFont.setColor(ratio + 0.25f, 1 - ratio, 1 - ratio, 1);
        } else {
            game.font.setColor(1, 1, 1, 1);
        }

        timeLeft = (int) (TOTAL_TIME - (TimeUtils.millis() - animTimes.get(MAIN).beginTime)) / 1000 + 1;

        userFood = String.valueOf(game.activeUser.food);
        userDogs = String.valueOf(game.activeUser.dogs);
        percentContained = "Percent contained: " + dropsGathered + "%";
        timeLeftString = "Time left: " + timeLeft;

        game.batch.setShader(game.fontShader);
        game.titleFont.getData().setScale(2);
        game.titleFont.draw(game.batch, userFood, 60, 80);
        game.titleFont.draw(game.batch, userDogs, 140, 80);
        game.titleFont.draw(game.batch, percentContained, 20, game.playHeight - 20);
        game.titleFont.draw(game.batch, timeLeftString, game.playWidth - 400, game.playHeight - 20);
        game.titleFont.draw(game.batch, "FPS: " + fps, 1690, 80);
        game.titleFont.setColor(1, 1, 1, 1);
        game.batch.setShader(null);

        game.batch.end();
        score();

    }

    private void food() {
        if(theFood != null) {
            game.batch.begin();
            game.batch.draw(game.foodTex, theFood.rect.x, theFood.rect.y, theFood.rect.width, theFood.rect.height);
            game.batch.end();
        }
    }

    private void wolf() {
        if(theWolf != null) {
            game.batch.begin();
            game.batch.draw(game.wolfTex, theWolf.rect.x, theWolf.rect.y, theWolf.rect.width, theWolf.rect.height);
            game.batch.end();
        }
    }

    private void nullTarget() {
        game.target = null;
    }
    private void nullAntiTarget() { game.antiTarget = null; }

    // Handle Clicks
    private void handleClicks() {
        if(fingerLastPosition == null) {
            fingerLastPosition = new Vector3();
        }


        // Just clicked ------------------------
        if (Gdx.input.isTouched() && !lastTimeTouched) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);

            if(fingerLastPosition.x == 0 && fingerLastPosition.y == 0) {
                fingerLastPosition.x = touchPos.x;
                fingerLastPosition.y = touchPos.y;
            }

            tempPiece = game.gridPieces[game.getGridPiece(touchPos)];

            // If user touches food icon else if user touches wolf icon
            if(touchPos.x > 0 && touchPos.x < 165 &&
                    touchPos.y > 0 && touchPos.y < 134 && game.activeUser.food > 0) {
                isFoodOrWolfActive();
                toggleFoodPlacementOn();

            } else if(touchPos.x > 165 && touchPos.x < 330 &&
                        touchPos.y > 0 && touchPos.y < 134 && game.activeUser.food > 0 ) {
                isFoodOrWolfActive();
                toggleWolfPlacementOn();
            }




            // If touchPos is not a fence piece
            if (tempPiece.state == game.STATE_FINAL) {
                origin = tempPiece;
                origin.setState(game.STATE_TEMP_NOTOUCH);
                getOpposite(origin);
                for (GridPiece piece : originOpposite) {
                    piece.setState(game.STATE_TEMP_NOTOUCH);
                }
                lastTimeTouched = true;
            } else {
                getDirectNeighbors(tempPiece);
                Boolean neiEmpt = true;
                for(GridPiece neighbor : neighbors) {
                    if(neighbor.state == game.STATE_FINAL) {
                        neiEmpt = false;
                    }
                }
                if(neiEmpt) {
                    isFoodOrWolfActive();
                    if(!foodOrWolfActive) {
                        if (placeFood) {
                            if(game.activeUser.food > 0) {
                                placeFood();
                                game.activeUser.food--;
                            }
                        } else if(placeWolf) {
                            if(game.activeUser.dogs > 0) {
                                placeWolf();
                                game.activeUser.dogs--;
                            }
                        }
                    }
                } else {
                    for (GridPiece neiPiece : neighbors) {
                        if (neiPiece.state == game.STATE_FINAL) {
                            // Get origin and set its state
                            origin = neiPiece;
                            origin.setState(game.STATE_TEMP_NOTOUCH);
                            //setState(origin, STATE_TEMP_NOTOUCH);


                            // Get originOpposites and changes their state
                            getOpposite(origin);
                            for (GridPiece piece : originOpposite) {
                                piece.setState(game.STATE_TEMP_NOTOUCH);
                            }
                            lastTimeTouched = true;

                            game.grassHasChanged = true;
                            System.out.println("GS| hit here");

                            break;
                        }
                    }
                }
            }


            // Clicked	------------------------------
        } else if (Gdx.input.isTouched() && origin.id != -1) {

            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);
            //System.out.println("GS| touchPos: " + touchPos);

            //Reset progress of the line
            for (int i = 0; i < game.gridPieces.length; i++) {
                GridPiece piece = game.gridPieces[i];
                if (piece.state == game.STATE_TEMP) {
                    piece.restoreState();
                }
            }

            // Determine what orOp touch is leaning toward
            orOpActive = getOppositeActive();

            // If the orOpActive does not return origin hide all but the one and create line toward it
            if (orOpActive != origin) {
                for (GridPiece gridPiece : originOpposite) {
                    gridPiece.setState(game.STATE_FINAL);
                }

                orOpActive.setState(game.STATE_TEMP_NOTOUCH);

                // If orOp has been passed create line from origin to orOp
                if (createLine()) {
                    if (finalizeLine()) { // Makes sure a mover is not in the last space
                        createFinalLine();
                        fillGrid();
                        origin.restoreState();
                        origin = falsePiece;
                    }

                }

                game.grassHasChanged = true;


            }
            // Else keep all shown

            lastTimeTouched = true;


            // Just not clicked	= not successful line---------------------
        } else if (!Gdx.input.isTouched() && lastTimeTouched) {
            //System.out.println("GS| notTouched but lasttimeTouched: " + lastTimeTouched);

            if (origin.id != -1) {
                cancelLine();
            }

            lastTimeTouched = false;

            // Not clicked	--------------------------
        } else if (!Gdx.input.isTouched()) {
            //System.out.println("GS| else: " + lastTimeTouched);

            clearStragglers();

            lastTimeTouched = false;
        }
    }

    private void setEnd(int num) {
        game.target = null;

        screen = num;
        animTimes.get(MAIN).endTime = TimeUtils.millis();
        animTimes.get(HANDLECLICKS).endTime = TimeUtils.millis();
        animTimes.get(OUTRO).resetBeginTime(TimeUtils.millis());
        animTimes.get(END).resetBeginTime(animTimes.get(OUTRO).endTime);

        game.resetTimes(animTimes.get(END).beginTime);

    }

    // Draws a line on path from origin to orOpActive using touchPos and returns if line has been finished
    private boolean createLine() {
        boolean lineFinished = false;

        //tempPiece = gridPieces.get(getGridPiece(touchPos));
        lineVector = touchPos;


        if (origin.row == orOpActive.row) {
            if (Math.abs(lineVector.x - origin.rectangle.x) > Math.abs(orOpActive.rectangle.x - origin.rectangle.x)) {
                tempPiece = orOpActive;
            } else {
                lineVector.y = origin.rectangle.y;
                tempPiece = game.gridPieces[game.getGridPiece(lineVector)];
            }
            // Right
            if (origin.column < tempPiece.column) {
                for (int i = origin.column + 1; i <= tempPiece.column; i++) {
                    game.gridMap.get(origin.row).get(i).setState(game.STATE_TEMP);
                }
                // Left
            } else {
                for (int i = tempPiece.column; i < origin.column; i++) {
                    game.gridMap.get(origin.row).get(i).setState(game.STATE_TEMP);
                }
            }
        } else {
            if (Math.abs(lineVector.y - origin.rectangle.y) > Math.abs(orOpActive.rectangle.y - origin.rectangle.y)) {
                tempPiece = orOpActive;
            } else {
                lineVector.x = origin.rectangle.x;
                tempPiece = game.gridPieces[game.getGridPiece(lineVector)];
            }
            // Up
            if (origin.row < tempPiece.row) {
                for (int i = origin.row + 1; i <= tempPiece.row; i++) {
                    game.gridMap.get(i).get(origin.column).setState(game.STATE_TEMP);
                }
                // Down
            } else {
                for (int i = tempPiece.row; i < origin.row; i++) {
                    game.gridMap.get(i).get(origin.column).setState(game.STATE_TEMP);
                }
            }
        }

        if (getDirectNeighbors(orOpActive).contains(game.gridPieces[game.getGridPiece(lineVector)])) {
            lineFinished = true;
        }

        if(checkFingerFling()) {
            lineFinished = true;
        }

        return lineFinished;
    }

    // Draws a line between origin and orOpActive (non-inclusive)
    private void createFinalLine() {
        //Up
        if (orOpActive.row > origin.row) {
            for (int i = origin.row + 1; i <= orOpActive.row; i++) {
                game.gridMap.get(i).get(origin.column).setState(game.STATE_FINAL);
                //setState(gridMap.get(i).get(origin.column), STATE_FINAL);
            }
        }

        //Down
        if (orOpActive.row < origin.row) {
            for (int i = origin.row - 1; i >= orOpActive.row; i--) {
                game.gridMap.get(i).get(origin.column).setState(game.STATE_FINAL);
                //setState(gridMap.get(i).get(origin.column), STATE_FINAL);
            }
        }
        //Right
        if (orOpActive.column > origin.column) {
            for (int i = origin.column + 1; i <= orOpActive.column; i++) {
                game.gridMap.get(origin.row).get(i).setState(game.STATE_FINAL);
                //setState(gridMap.get(origin.row).get(i), STATE_FINAL);
            }
        }
        //Left
        if (orOpActive.column < origin.column) {
            for (int i = origin.column - 1; i >= orOpActive.column; i--) {
                game.gridMap.get(origin.row).get(i).setState(game.STATE_FINAL);
                //setState(gridMap.get(origin.row).get(i), STATE_FINAL);
            }
        }
    }

    // Returns a list of grid pieces that the user can aim toward to finish a fence
    private ArrayList<GridPiece> getOpposite(GridPiece origin) {
        originOpposite.clear();

        //Check for corners
        if (origin.column == 0 && (origin.row == 0 || origin.row == game.gridSizeHeight - 1)) {
            originOpposite.add(falsePiece);
            return originOpposite;
        } else if (origin.column == game.gridSizeWidth - 1 && (origin.row == 0 || origin.row == game.gridSizeHeight - 1)) {
            originOpposite.add(falsePiece);
            return originOpposite;
        }

        if (originOpposite.size() > 0) {
            return originOpposite;
        }

        // Search Around

        boolean canCreateOp;

        // Search up
        if (game.gridMap.size() > origin.row + 1) {
            canCreateOp = false;

            for (int i = origin.row + 1; i < game.gridSizeHeight; i++) {
                if (game.gridMap.get(i).get(origin.column).state == game.STATE_NONE) {
                    canCreateOp = true;
                } else if (game.gridMap.get(i).get(origin.column).state == game.STATE_FINAL && canCreateOp) {
                    originOpposite.add(game.gridMap.get(i).get(origin.column));
                    break;
                }
            }


            // Search down
        }
        if (origin.row > 0) {
            canCreateOp = false;

            for (int i = origin.row - 1; i >= 0; i--) {
                if (game.gridMap.get(i).get(origin.column).state == game.STATE_NONE) {
                    canCreateOp = true;
                } else if (game.gridMap.get(i).get(origin.column).state == game.STATE_FINAL && canCreateOp) {
                    originOpposite.add(game.gridMap.get(i).get(origin.column));
                    break;
                }
            }


            // Search right
        }
        if (game.gridMap.get(0).size() > origin.column + 1) {
            canCreateOp = false;

            for (int i = origin.column + 1; i < game.gridSizeWidth; i++) {
                if (game.gridMap.get(origin.row).get(i).state == game.STATE_NONE) {
                    canCreateOp = true;
                } else if (game.gridMap.get(origin.row).get(i).state == game.STATE_FINAL && canCreateOp) {
                    originOpposite.add(game.gridMap.get(origin.row).get(i));
                    break;
                }
            }

            // Search left
        }
        if (origin.column > 0) {
            canCreateOp = false;

            for (int i = origin.column - 1; i >= 0; i--) {
                if (game.gridMap.get(origin.row).get(i).state == game.STATE_NONE) {
                    canCreateOp = true;
                } else if (game.gridMap.get(origin.row).get(i).state == game.STATE_FINAL && canCreateOp) {
                    originOpposite.add(game.gridMap.get(origin.row).get(i));
                    break;
                }
            }
        }

        noLine = true;

        if (originOpposite.size() == 0) {
            originOpposite.add(falsePiece);
        }

        return originOpposite;
    }

    // Returns the originOpposite that the pointer is headed towards, origin if nothing
    private GridPiece getOppositeActive() {

		/*System.out.println("GS| origin row: " + origin.row);
		System.out.println("GS| origin col: " + origin.column);
		System.out.println("GS| origin center x,y: " + origin.getCenter());*/


        if (game.gridPieces[game.getGridPiece(touchPos)] == origin) {
            return origin;
        }
        float degrees;

        // Get degrees
        degrees = MathUtils.atan2(touchPos.y - origin.getCenter().y, touchPos.x - origin.getCenter().x);

        float quarterPi = 0.78539816339f;

        if (!originOpposite.contains(falsePiece)) {
            for (GridPiece gridPiece : originOpposite) {

                //System.out.println("GS| degrees: " + degrees);
			/*System.out.println("GS| gridpiece row: " + gridPiece.row);
			System.out.println("GS| gridpiece col: " + gridPiece.column);
			System.out.println("GS| gridpiece center x,y: " + gridPiece.getCenter());*/

                // Right
                if (degrees >= 0 && degrees < quarterPi || degrees < 0 && degrees >= -quarterPi) {
                    if (gridPiece.row == origin.row && gridPiece.getCenter().x > origin.getCenter().x) {
                        return gridPiece;
                    }
                }

                // Down
                else if (degrees <= -quarterPi && degrees > -quarterPi * 3) {
                    if (gridPiece.column == origin.column && gridPiece.getCenter().y < origin.getCenter().y) {
                        return gridPiece;
                    }
                }

                // Left
                else if (degrees > quarterPi * 3 && degrees <= quarterPi * 4
                        || degrees < -quarterPi * 3 && degrees >= -quarterPi * 4) {
                    if (gridPiece.row == origin.row && gridPiece.getCenter().x < origin.getCenter().x) {
                        return gridPiece;
                    }
                }

                // Up
                else if (degrees >= quarterPi && degrees < quarterPi * 3) {
                    if (gridPiece.column == origin.column && gridPiece.getCenter().y > origin.getCenter().y) {
                        return gridPiece;
                    }
                }

            }
        }
        return origin;
    }

    private void fillGrid() {

        for (int i = 0; i < game.gridPieces.length; i++) {
            GridPiece piece = game.gridPieces[i];
            if (piece.state == game.STATE_NONE && piece.space == 0) {
                buildASpace(piece);
            }
        }

        // Resets spaces for next time
        for (int i = 0; i < game.gridPieces.length; i++) {
            GridPiece piece = game.gridPieces[i];
            piece.space = 0;
        }
    }

    private void buildASpace(GridPiece piece) {
        Integer currentSpace = spaces.size() + 1;
        boolean keepGoing = true;
        boolean skipJ = false;
        boolean skipI = false;
        boolean skipJI = false;
        int i = piece.column;
        int j = piece.row;
        Rectangle space = new Rectangle((float) piece.column, (float) piece.row, 0f, 0f);
        boolean hasMovers = false;


        // Get far right corner gridPiece
        while (keepGoing) {

            if (j + 1 < game.gridMap.size() && i + 1 < game.gridMap.get(j).size() && !skipJI) {
                // Go up diagonal
                if (game.gridMap.get(j + 1).get(i + 1).state == game.STATE_NONE && game.gridMap.get(j + 1).get(i + 1).space == 0) {
                    j++;
                    i++;
                } else {
                    skipJI = true;
                }

            } else if (j + 1 < game.gridMap.size() && !skipJ) {
                // Else go up
                if (game.gridMap.get(j + 1).get(i).state == game.STATE_NONE && game.gridMap.get(j + 1).get(i).space == 0) {
                    j++;
                } else {
                    skipJ = true;
                }

            } else if (i + 1 < game.gridMap.get(j).size() && !skipI) {
                // Else go right
                if (game.gridMap.get(j).get(i + 1).state == game.STATE_NONE && game.gridMap.get(j).get(i + 1).space == 0) {
                    i++;
                } else {
                    skipI = true;
                }

                // Cannot go further
            } else {
                keepGoing = false;
                space.width = ((float) i) - space.x + 1;
                space.height = ((float) j) - space.y + 1;
                spaces.put(currentSpace, space);

				/*System.out.println("GS| i: " + i);
				System.out.println("GS| j: " + j);
				System.out.println("GS| spaces: " + spaces);*/


            }
        }


        for (Mover mover : game.movers) {
            Mover tempMover = new Mover(mover.rect.x / game.gridPieceWidth, mover.rect.y / game.gridPieceHeight, mover.image);
            tempMover.rect.width = mover.rect.width / game.gridPieceWidth;
            tempMover.rect.height = mover.rect.height / game.gridPieceHeight;

            if (space.overlaps(tempMover.rect)) {
                hasMovers = true;
                break;
            }
        }


        // Set all spaces' states
        int xStart = (int) spaces.get(currentSpace).x;
        int yStart = (int) spaces.get(currentSpace).y;
        int xDelta = (int) (spaces.get(currentSpace).width + spaces.get(currentSpace).x);
        int yDelta = (int) (spaces.get(currentSpace).height + spaces.get(currentSpace).y);

		/*System.out.println("GS| hasMovers: " + hasMovers);
		System.out.println("GS| xStart: " + xStart);
		System.out.println("GS| yStart: " + yStart);
		System.out.println("GS| xDelta: " + xDelta);
		System.out.println("GS| yDelta: " + yDelta);
		System.out.println("GS| -------------------- " );*/

        // Iterate through x, y
        for (int a = yStart; a < yDelta; a++) {
            for (int b = xStart; b < xDelta; b++) {
                if (!hasMovers) {
                    game.gridMap.get(a).get(b).setState(game.STATE_FINAL);
                    //setState(game.gridMap.get(a).get(b), game.STATE_FINAL);
                }
                game.gridMap.get(a).get(b).space = currentSpace;
            }

        }

    }

    private void score() {
        int drops = 0;

        for (int i = 0; i < game.gridPieces.length; i++) {
            GridPiece piece = game.gridPieces[i];

            if (piece.row > 0 && piece.row < game.gridSizeHeight - 1
                    && piece.column > 0 && piece.column < game.gridSizeWidth - 1) {


                if (piece.state == game.STATE_FINAL || piece.state == game.STATE_TEMP_NOTOUCH) {
                    drops++;
                }
            }

        }

        float availableGrid = (float) (game.gridPieces.length - (game.gridMap.get(0).size() * 2
                + (game.gridMap.size() - 2) * 2));

        dropsGathered = Math.round((drops / availableGrid) * 100);
    }

    // gets the 4 adjacent gridpieces to a gridpiece if they exist
    private ArrayList<GridPiece> getDirectNeighbors(GridPiece piece) {
        neighbors.clear();

        //Top
        if (game.gridMap.size() > piece.row + 1) {
            neighbors.add(game.gridMap.get(piece.row + 1).get(piece.column));
        }
        //Bottom
        if (piece.row > 0) {
            neighbors.add(game.gridMap.get(piece.row - 1).get(piece.column));
        }

        //Right
        if (game.gridMap.get(0).size() > piece.column + 1) {
            neighbors.add(game.gridMap.get(piece.row).get(piece.column + 1));
        }

        //Left
        if (piece.column > 0) {
            neighbors.add(game.gridMap.get(piece.row).get(piece.column - 1));
        }

        return neighbors;
    }

    public void cancelLine() {
        origin.restoreState();
        //restoreState(origin);
        origin = falsePiece;
        for (GridPiece opposites : originOpposite) {
            opposites.restoreState();
            //restoreState(opposites);
        }

        clearStragglers();
    }

    // If a piece is temp or noTouch this sets them to be what they should
    private void clearStragglers() {
        for (int i = 0; i < game.gridPieces.length; i++) {
            GridPiece piece = game.gridPieces[i];

            if (piece.state == game.STATE_TEMP) {
                piece.restoreState();
                //restoreState(piece);
            } else if (piece.state == game.STATE_TEMP_NOTOUCH) {
                piece.setState(game.STATE_FINAL);
            }
        }
    }

    private boolean checkFingerFling() {
        returnable = false;
        a = (fingerLastPosition.x - touchPos.x);
        b = (fingerLastPosition.y - touchPos.y);
        c = a * a + b * b;
        c = (float) Math.sqrt((double) c);

        System.out.println("x " + fingerLastPosition.x + ", " + touchPos.x);
        System.out.println("y " + fingerLastPosition.y + ", " + touchPos.y);
        System.out.println("c " + c);
        speed = c/Gdx.graphics.getDeltaTime();


        // Get distance left & Get total distance
        if(origin.column == orOpActive.column) {
            distance = Math.abs(orOpActive.getCenter().y - touchPos.y);
            distanceTotal = Math.abs(orOpActive.getCenter().y - origin.getCenter().y);
        } else if (origin.row == orOpActive.row) {
            distance = Math.abs(orOpActive.getCenter().x - touchPos.x);
            distanceTotal = Math.abs(orOpActive.getCenter().x - origin.getCenter().x);
        } else {
            distance = 0;
            distanceTotal = 0;
        }

        System.out.println("s " + speed);
        System.out.println("d " + distance);

        if(speed * Gdx.graphics.getDeltaTime() > distance
                && speed * Gdx.graphics.getDeltaTime() < 0.5f * distanceTotal) {
            returnable = true;
            System.out.println("GS| returned true");

        }
        System.out.println("----------------");
        tempX = touchPos.x;
        tempY = touchPos.y;

        fingerLastPosition.x = tempX;
        fingerLastPosition.y = tempY;
        return returnable;
    }

    private boolean finalizeLine() {
        boolean finalizeLine = true;
        game.states.clear();
        game.states.add(game.STATE_TEMP);

        lineBounds = new Rectangle();

        if(origin.row == orOpActive.row) {
            if(origin.rectangle.x < orOpActive.rectangle.x) {
                lineBounds.x = origin.rectangle.x;
            } else {
                lineBounds.x = orOpActive.rectangle.x;
            }
            lineBounds.y = origin.rectangle.y;
            lineBounds.width = Math.abs(orOpActive.getCenter().x - origin.getCenter().x);
            lineBounds.height = game.gridPieceHeight;
        } else {
            lineBounds.x = origin.rectangle.x;
            if(origin.rectangle.y < orOpActive.rectangle.y) {
                lineBounds.y = origin.rectangle.y;
            } else {
                lineBounds.y = orOpActive.rectangle.y;
            }
            lineBounds.width = game.gridPieceWidth;
            lineBounds.height = Math.abs(orOpActive.getCenter().y - origin.getCenter().y);
        }


        float distance;
        for (Mover mover : game.movers) {
            distance = moverToGridpiece(mover, orOpActive);
            if(distance < game.gridPieceWidth * 3) {

                /*if (game.moverOverlapsState(mover, game.states).size() > 0) {
                *    System.out.println("GS| overlap");
                *    finalizeLine = false;
                *    break;
                *}
                */
                if(mover.rect.overlaps(lineBounds)) {
                    System.out.println("GS| overlap");
                    return false;
                }
            }
        }

        return true;
    }

    private void isFoodOrWolfActive () {
        if(animTimes.get(FOOD).endTime < TimeUtils.millis() &&
                animTimes.get(WOLF).endTime < TimeUtils.millis()) {
            foodOrWolfActive = false;
        }
    }

    private void toggleFoodPlacementOn() {
        if(!foodOrWolfActive) {
            placeFood = !placeFood;

            if (placeFood && placeWolf) {
                placeWolf = false;
            }
        }
    }

    private void toggleWolfPlacementOn() {
        if(!foodOrWolfActive) {
            placeWolf = !placeWolf;

            if (placeWolf && placeFood) {
                placeFood = false;
            }
        }
    }

    private void placeFood() {
        theFood = new Food(touchPos.x, touchPos.y, 400);
        if(game.target == null) {
            game.target = new Vector3(theFood.rect.x + theFood.rect.width / 2,
                    theFood.rect.y + theFood.rect.height / 2, theFood.radius);
        }
        animTimes.get(FOOD).resetBeginTime(TimeUtils.millis());
        animTimes.get(NULLTARGET).resetBeginTime(animTimes.get(FOOD).endTime);

        foodOrWolfActive = true;
    }

    private void placeWolf() {
        theWolf = new Wolf(touchPos.x, touchPos.y, 400);
        if(game.target == null) {
            game.target = new Vector3(theWolf.rect.x + theWolf.rect.width / 2,
                    theWolf.rect.y + theWolf.rect.height / 2, theWolf.radius);
        }
        animTimes.get(WOLF).resetBeginTime(TimeUtils.millis());
        animTimes.get(NULLTARGET).resetBeginTime(animTimes.get(WOLF).endTime);

        foodOrWolfActive = true;
    }

    private float moverToGridpiece(Mover mover, GridPiece gridPiece) {
        float a = (mover.rect.x + mover.rect.width - gridPiece.getCenter().x);
        float b = (mover.rect.y + mover.rect.height - gridPiece.getCenter().y);
        float c = a * a + b * b;
        c = (float) Math.sqrt((double) c);
        return c;
    }


    @Override
    public void pause() {
        fingerLastPosition = null;

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
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
