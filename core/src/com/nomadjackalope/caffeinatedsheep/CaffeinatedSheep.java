package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Benjamin on 6/19/2015.
 *
 */
public class CaffeinatedSheep extends Game {

    CaffeinatedSheep game;

    OrthographicCamera camera;
    Viewport viewport;

    //Drawing
    public SpriteBatch batch;
    public ShaderProgram fontShader;
    public ShapeRenderer shape;
    public BitmapFont font;
    public BitmapFont lightFont;
    public BitmapFont titleFont;
    public BitmapFont stageFont;
    public BitmapFont stageFontLarge;
    public BitmapFontCache fontCache;

    //Grid
    GridPiece[] gridPieces;
    ArrayList<ArrayList<GridPiece>> gridMap;

    // Grid Visuals
    final int STATE_NONE = 0;
    final int STATE_TEMP = 1;
    final int STATE_TEMP_NOTOUCH = 2;
    final int STATE_FINAL = 3;

    ArrayList<Integer> states = new ArrayList<Integer>();
    int integers;
    Vector3 quads = new Vector3();
    Vector2 tempVector = new Vector2();

    float[][] perlinNoise;
    float range = 5;

    // Visuals have changed
    boolean grassHasChanged = true;
    boolean wallsHaveChanged = true;

    // Grid Dimensions
    final int playWidth = 1920;
    final int playHeight = 1080;

    final float playWidthFloat = (float) playWidth;
    final float playHeightFloat = (float) playHeight;

    float gridPieceWidth;
    float gridPieceHeight;

    float backgroundGridPieceWidth;
    float backgroundGridPieceHeight;

    int gridSizeWidth;
    int gridSizeHeight;

    // Movers
    ArrayList<Mover> movers;

    // Target
    Vector3 target = new Vector3();
    Vector3 antiTarget = new Vector3();
    Vector2 target2D = new Vector2();

    // Time
    float frame;
    boolean resumed = false;
    long originTime;
    long deltaTime;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();
    final long hugeNum = 9999999900000L;

    // Random
    Random randomSeed = new Random(1);
    Random random = new Random(0);
    long seed = 0;

    Slope randomSlope;


    // Textures
    TextureAtlas textureAtlas;
    TextureRegion groundTempTexture;
    TextureRegion sheep;
    TextureRegion wolfTex;
    TextureRegion wolfTexOn;
    TextureRegion foodTex;
    TextureRegion foodTexOn;
    TextureRegion gardenTex;

    int numWalls = 10;

    ArrayList<TextureRegion> grounds = new ArrayList<TextureRegion>(20);
    ArrayList<TextureRegion> wallsTopsFinal = new ArrayList<TextureRegion>(numWalls);
    ArrayList<TextureRegion> wallsFinal = new ArrayList<TextureRegion>(numWalls);
    ArrayList<TextureRegion> wallsTemp = new ArrayList<TextureRegion>(numWalls);
    ArrayList<TextureRegion> wallsHighlight = new ArrayList<TextureRegion>(numWalls);

    //Scores
//    Scores scores;
    TreeMap <Integer, ArrayList<String>> scoreDist;

    //Users
    User activeUser;
    ArrayList<User> users = new ArrayList<User>();


    // Mover movement
    MoverMovement movement;

    int currentPiece1;
    int currentPiece2;

    boolean[] triangleFlips; // Tells a piece to have a triangle topleft/bottomright or bottomleft\topright


    @Override
    public void create() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        //scores = new Scores();
        scoreDist = new TreeMap<Integer, ArrayList<String>>();

        TreeMap<Integer, ArrayList<String>> tempMap =  SaveHelper.loadScores();
        if(tempMap != null) {
            scoreDist = tempMap;
        }

        //users
        users = new ArrayList<User>();

        ArrayList<User> tempUsers = SaveHelper.loadUsers();
        if(tempUsers != null) {
            if(tempUsers.size() > 0) {
                users = tempUsers;

                // Get last active user
                for (int i = 0; i < users.size(); i++) {
                    if(users.get(i).isActive) {
                        activeUser = users.get(i);
                        i = users.size();
                    } else {
                        activeUser = users.get(0);
                    }
                }

            } else {
                users.add(new User("Player", 0,0,0,0,0));
                activeUser = users.get(0);
            }
        }


        //System.out.println("D| names, scores: " + scores.names + "," + scores.scores);


        textureAtlas = new TextureAtlas("packed/textures.pack");

        stageFont = getStageFont();
        stageFontLarge = getStageFontLarge();
        font = getFont();
        lightFont = getLightFont();
        titleFont = getBoldFont(); //120

        fontCache = new BitmapFontCache(titleFont);


        //Screen dimensions
        gridPieceWidth = 60f;
        gridPieceHeight = 60f;

        backgroundGridPieceWidth = 120f;
        backgroundGridPieceHeight = 120f;

        gridSizeWidth = (int) (playWidthFloat / gridPieceWidth);
        gridSizeHeight = (int) (playHeightFloat / gridPieceHeight);


        //Camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(playWidth, playHeight, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();


        //Sprites
        initGridPieces();


        movers = new ArrayList<Mover>();

        sheep = new TextureRegion(textureAtlas.findRegion("sheep"));
        wolfTex = new TextureRegion(textureAtlas.findRegion("wolf"));
        wolfTexOn = new TextureRegion(textureAtlas.findRegion("wolfOn"));
        foodTex = new TextureRegion(textureAtlas.findRegion("food"));
        foodTexOn = new TextureRegion(textureAtlas.findRegion("foodOn"));
        gardenTex = new TextureRegion(textureAtlas.findRegion("garden"));


        String name = "grass";

        for (int numPics = 1; numPics <= 20; numPics++) {
            String formatted = String.format("%04d", numPics);
            formatted = name + formatted;
            grounds.add(new TextureRegion(textureAtlas.findRegion(formatted)));
        }

        name = "wallsTops";

        for (int numPics = 1; numPics <= numWalls; numPics++) {
            String formatted = String.format("%04d", numPics);
            formatted = name + formatted;
            wallsTopsFinal.add(new TextureRegion(textureAtlas.findRegion(formatted)));
        }

        name = "walls";

        for (int numPics = 1; numPics <= numWalls; numPics++) {
            String formatted = String.format("%04d", numPics);
            formatted = name + formatted;
            wallsFinal.add(new TextureRegion(textureAtlas.findRegion(formatted)));
        }

        name = "wallsTemps";

        for (int numPics = 1; numPics <= numWalls; numPics++) {
            String formatted = String.format("%04d", numPics);
            formatted = name + formatted;
            wallsTemp.add(new TextureRegion(textureAtlas.findRegion(formatted)));
        }

        name = "wallsHighlights";

        for (int numPics = 1; numPics <= numWalls; numPics++) {
            String formatted = String.format("%04d", numPics);
            formatted = name + formatted;
            wallsHighlight.add(new TextureRegion(textureAtlas.findRegion(formatted)));
        }


        //highlight = getColorFromRGB(5, 142, 217, 255);

        perlinNoise = generateSmoothNoise(generateWhiteNoise(66, 35), 3);

        originTime = TimeUtils.millis();
        deltaTime = TimeUtils.millis();

        fontShader = new ShaderProgram(Gdx.files.internal("font.vert"), Gdx.files.internal("font.frag"));
        if(!fontShader.isCompiled()) {
            Gdx.app.error("fontShader", "compilation failed: \n" + fontShader.getLog());
        }

        movement = new MoverMovement(this);

        randomSlope = new Slope(this);
        if(users.isEmpty()) {
            this.setScreen(new TutorialScreen(this));
        } else {
            this.setScreen(new MainMenuScreen(this));
        }

        // Initialize grass triangles
        triangleFlips = new boolean[gridSizeHeight * gridSizeWidth];
        for (int i = 0; i < triangleFlips.length; i++) {
            triangleFlips[i] = random.nextBoolean();

            if(!triangleFlips[i]) {
                gridPieces[i].firstAngle = 0;
                gridPieces[i].secondAngle = 180;
            } else {
                gridPieces[i].firstAngle = 270;
                gridPieces[i].secondAngle = 90;
            }
        }

    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        lightFont.dispose();
        titleFont.dispose();
        textureAtlas.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void pause() {
        super.pause();

    }

    @Override
    public void resume() {
        super.resume();
        resumed = true;
    }

    //--------------------- Fonts ---------------------

    public BitmapFont getStageFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Comfortaa-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        BitmapFont font32 = generator.generateFont(parameter); // font size 12 pixels
        generator.dispose();

        return font32;
    }
    public BitmapFont getStageFontLarge() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Comfortaa-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 64;
        BitmapFont font = generator.generateFont(parameter); // font size 12 pixels
        generator.dispose();

        return font;
    }
    public BitmapFont getFont() {

        Texture texture = new Texture(Gdx.files.internal("Comfortaa-DF.png"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new BitmapFont(Gdx.files.internal("Comfortaa-DF.fnt"));
    }
    public BitmapFont getLightFont() {
        Texture texture = new Texture(Gdx.files.internal("Comfortaa-Light-DF.png"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new BitmapFont(Gdx.files.internal("Comfortaa-Light-DF.fnt"));
    }
    public BitmapFont getBoldFont() {
        Texture texture = new Texture(Gdx.files.internal("Comfortaa-Bold-DF.png"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new BitmapFont(Gdx.files.internal("Comfortaa-Bold-DF.fnt"), new TextureRegion(texture), false);
    }

    //-------------------- Drawing --------------------

    // Draws grass, walls, and sheep
    public void batchDrawing() {

        random.setSeed(0);

        //--------------Grass /////^/^/^/^/^/^/^/^^/^^/^^^/^^^^/
        drawGrass();

        // ------------------------------ WALLS --------------------------------

        drawWalls();

        // ----------------------------------- SHEEP ----------------------------

        drawMovers();


    }

    public void drawGrass() {
        random.setSeed(0);

        GridPiece piece;

        int groundInt;

        int modifier;

        if(grassHasChanged) {
            //System.out.println("Grass has changed true");
            for (int i = 0; i < gridPieces.length; i++) {
                piece = gridPieces[i];

                if(piece.column % 2 == 0 && piece.row % 2 == 0) {

                    //if(piece.state == STATE_NONE) {

//				System.out.println("GS| finalInt: " + (int) finalInt);
//				System.out.println("GS| x, y: " + x + ", " + y);
//				System.out.println("GS| randInt: " + randInt);
//				System.out.println("GS| progress: " + progress);

                    // Only works because it is done to all pieces
                    groundInt = (int) randomSlope.randomFloat(piece, range, triangleFlips[i], true);

                    currentPiece1 = piece.id - 1;
                    currentPiece2 = piece.id - 2;

                    // Cast shadow based on walls to left
                    if (piece.id > 1) {

                        // If there is wall to the left or wall on this piece
                        if (gridPieces[currentPiece1].state > STATE_NONE
                                || piece.state > STATE_NONE) {
                            modifier = (int) (groundInt + 0.4f * (grounds.size() - groundInt));
                            groundTempTexture = grounds.get(modifier);

                            // If there is wall two spaces to the left
                        } else if (gridPieces[currentPiece2].state > STATE_NONE) {
                            modifier = (int) (groundInt + 0.2f * (grounds.size() - groundInt));
                            groundTempTexture = grounds.get(modifier);

                        } else {
                            groundTempTexture = grounds.get(groundInt);
                        }
                    } else {
                        groundTempTexture = grounds.get(groundInt);
                    }

                    piece.groundTex1 = groundTempTexture;

                    batch.draw(groundTempTexture, piece.rectangle.x, piece.rectangle.y,
                            1f * piece.rectangle.width,
                            1f * piece.rectangle.height,
                            2 * piece.rectangle.width,
                            2 * piece.rectangle.height,
                            1, 1, piece.firstAngle);

                    // Only works because it is done to all pieces
                    groundInt = (int) randomSlope.randomFloat(piece, range, triangleFlips[i], false);


                    if (piece.id > 1) {
                        if (gridPieces[currentPiece1].state > STATE_NONE
                                || piece.state > STATE_NONE) {
//                        if (triangleFlips[i]) {
//                            modifier = (int) (groundInt + 0.4f * (grounds.size() - groundInt));
//                            groundTempTexture = grounds.get(modifier);
//                        } else {
                            modifier = (int) (groundInt + 0.3f * (grounds.size() - groundInt));
                            groundTempTexture = grounds.get(modifier);
//                        }
                        } else if (gridPieces[currentPiece2].state > STATE_NONE) {
//                        if (triangleFlips[i]) {
//                            modifier = (int) (groundInt + 0.2f * (grounds.size() - groundInt));
//                            groundTempTexture = grounds.get(modifier);
//                        } else {
                            modifier = (int) (groundInt + 0.1f * (grounds.size() - groundInt));
                            groundTempTexture = grounds.get(modifier);
//                        }
                        } else {
                            groundTempTexture = grounds.get(groundInt);
                        }
                    } else {
                        groundTempTexture = grounds.get(groundInt);
                    }

                    piece.groundTex2 = groundTempTexture;

                    batch.draw(groundTempTexture, piece.rectangle.x, piece.rectangle.y,
                            1f * piece.rectangle.width,
                            1f * piece.rectangle.height,
                            2 * piece.rectangle.width,
                            2 * piece.rectangle.height,
                            1, 1, piece.secondAngle);

                    //boldFont.draw(batch, "" + (int) randomFloat(piece, range, false, false) , piece.getCenter().x, piece.getCenter().y);
                    //}

                    grassHasChanged = false;
                }

            }
        } else {
            for (int i = 0; i < gridPieces.length; i++) {
                piece = gridPieces[i];

                if (piece.column % 2 == 0 && piece.row % 2 == 0) {

                    groundTempTexture = piece.groundTex1;

                    batch.draw(groundTempTexture, piece.rectangle.x, piece.rectangle.y,
                            1f * piece.rectangle.width,
                            1f * piece.rectangle.height,
                            2 * piece.rectangle.width,
                            2 * piece.rectangle.height,
                            1, 1, piece.firstAngle);

                    groundTempTexture = piece.groundTex2;

                    batch.draw(groundTempTexture, piece.rectangle.x, piece.rectangle.y,
                            1f * piece.rectangle.width,
                            1f * piece.rectangle.height,
                            2 * piece.rectangle.width,
                            2 * piece.rectangle.height,
                            1, 1, piece.secondAngle);
                }
            }
        }


    }

    public void drawWalls() {
        float offsetX = (gridPieceWidth / 241 * 50);
        float offsetY = (gridPieceHeight / 241 * 50);

        int getter = 0;
        float pieceID = 0;
        int garden;
        boolean draw;

        GridPiece neighbor;
        GridPiece piece;

        for (int i = 0; i < gridPieces.length; i++) {
            piece = gridPieces[i];

            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

				/*case STATE_TEMP:
					batch.draw(fenceTexTemp, piece.rectangle.x + 10, piece.rectangle.y + 10);
					break;
				case STATE_TEMP_NOTOUCH:

					break;*/
                case STATE_FINAL:
                    draw = false;

                    for (int j = 0; j < piece.neighbors.size(); j++) {
                        neighbor = piece.neighbors.get(j);
                        if(neighbor.state == STATE_NONE) {
                            draw = true;
                            break;
                        }
                    }
                    if(draw) { //|| piece.neighbors.size() != 8
                        pieceID = piece.id;
                        getter = piece.wallTex;//(int) (9f * Math.abs(sin(piece.rectangle.x * pieceID + piece.rectangle.y * pieceID)));



                        batch.draw(wallsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                                , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
                    }
                    break;
                default:
                    break;
            }

        }

        offsetX = (gridPieceWidth / 219 * 25.5f);
        offsetY = (gridPieceHeight / 219 * 20);
        for (int i = 0; i < gridPieces.length; i++) {
            piece = gridPieces[i];

            pieceID = piece.id;

            getter = piece.wallTex;//(int) (9f * Math.abs(sin(piece.rectangle.x * pieceID + piece.rectangle.y * pieceID)));

            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

                case STATE_TEMP:
                    batch.draw(wallsTemp.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                            , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
                    break;
                case STATE_TEMP_NOTOUCH:
                    batch.draw(wallsHighlight.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                            , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
                    break;
                case STATE_FINAL:
                    garden = 0;

                    for (int j = 0; j < piece.neighbors.size(); j++) {
                        neighbor = piece.neighbors.get(j);

                        if(neighbor.state == STATE_FINAL || neighbor.state == STATE_TEMP_NOTOUCH) { //STATE_NONE
                            garden++;
                            //break;
                        }
                    }
                    if(garden < piece.neighbors.size()) {
                        batch.draw(wallsTopsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                                , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
                    } else {
                        // If the 4 grid pieces the garden plant will occupy are empty draw it in the center
                        if(piece.row % 2 == 0 && piece.column % 2 == 0) {
                            batch.draw(gardenTex, piece.rectangle.x + offsetX, piece.rectangle.y + offsetY
                                    , piece.rectangle.width - 35, piece.rectangle.height - 35);
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    }

    public void drawMovers() {
        Mover mover;
        for (int i = 0; i < movers.size(); i++) {
            mover = movers.get(i);
            batch.draw(sheep, mover.rect.x, mover.rect.y,                   // Image, x, y
                    mover.rect.width * 0.5f, mover.rect.height * 0.5f,      // Rotation origin
                    mover.rect.width , mover.rect.height,                   // Width, Height
                    1, 1,                                                   // Scale
                    mover.vector.angle() - 90);                             // Rotation
        }
    }

    // Tests rectangle sizes - Don't use in final
    public void testDrawing() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        // Walls
        shape.setColor(0,1,0,1);
        float offsetX = (gridPieceWidth / 241 * 50);
        float offsetY = (gridPieceHeight / 241 * 50);
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];
            shape.rect(piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                    , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
        }

        shape.setColor(1, 0, 0, 1);
        offsetX = (gridPieceWidth / 219 * 25.5f);
        offsetY = (gridPieceHeight / 219 * 20);
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];
            shape.rect(piece.rectangle.x - offsetX, piece.rectangle.y - offsetY
                    , piece.rectangle.width + offsetX, piece.rectangle.height + offsetY);
        }

        // Sheep



        // text?

        shape.end();
    }

    public void introWalls() {
        float offsetX = (gridPieceWidth / 241 * 50);
        float offsetY = (gridPieceHeight / 241 * 50);

        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];
            int getter = (int) (9f * Math.abs(sin(piece.rectangle.x * (float) piece.id + piece.rectangle.y * (float) piece.id)));
            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

				/*case STATE_TEMP:
					batch.draw(fenceTexTemp, piece.rectangle.x + 10, piece.rectangle.y + 10);
					break;
				case STATE_TEMP_NOTOUCH:

					break;*/
                case STATE_FINAL:
                    boolean draw = false;
                    GridPiece neighbor;
                    for(int j = 0; j < piece.neighbors.size(); j++) {
                        neighbor = piece.neighbors.get(j);

                        if(neighbor.state == STATE_NONE) {
                            draw = true;
                            break;
                        }
                    }
                    if(draw) {
                        batch.draw(wallsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                                piece.rectangle.width / 2,
                                piece.rectangle.height / 2,
                                piece.rectangle.width + offsetX,
                                piece.rectangle.height + offsetY,
                                introFunc(TimeUtils.millis() - piece.startTime),
                                introFunc(TimeUtils.millis() - piece.startTime),
                                0);
                    }
                    break;
                default:
                    break;
            }

        }
        offsetX = (gridPieceWidth / 219 * 25.5f);
        offsetY = (gridPieceHeight / 219 * 20);
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];

            int getter = (int) (9f * Math.abs(sin(piece.rectangle.x * (float) piece.id + piece.rectangle.y * (float) piece.id)));

            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

                case STATE_TEMP:
                    batch.draw(wallsTemp.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            introFunc(TimeUtils.millis() - piece.startTime),
                            introFunc(TimeUtils.millis() - piece.startTime),
                            0);
                    break;
                case STATE_TEMP_NOTOUCH:
                    batch.draw(wallsHighlight.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            introFunc(TimeUtils.millis() - piece.startTime),
                            introFunc(TimeUtils.millis() - piece.startTime),
                            0);
                    break;
                case STATE_FINAL:
                    batch.draw(wallsTopsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            introFunc(TimeUtils.millis() - piece.startTime),
                            introFunc(TimeUtils.millis() - piece.startTime),
                            0);
                    break;
                default:
                    break;
            }
        }
    }

    public void outroWalls() {
        float offsetX = (gridPieceWidth / 241 * 50);
        float offsetY = (gridPieceHeight / 241 * 50);

        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];
            int getter = (int) (9f * Math.abs(sin(piece.rectangle.x * (float) piece.id + piece.rectangle.y * (float) piece.id)));
            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

				/*case STATE_TEMP:
					batch.draw(fenceTexTemp, piece.rectangle.x + 10, piece.rectangle.y + 10);
					break;
				case STATE_TEMP_NOTOUCH:

					break;*/
                case STATE_FINAL:
                    boolean draw = false;
                    GridPiece neighbor;
                    for(int j = 0; j < piece.neighbors.size(); j++) {
                        neighbor = piece.neighbors.get(j);
                        if(neighbor.state == STATE_NONE) {
                            draw = true;
                            break;
                        }
                    }
                    if(draw) {
                        batch.draw(wallsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                                piece.rectangle.width / 2,
                                piece.rectangle.height / 2,
                                piece.rectangle.width + offsetX,
                                piece.rectangle.height + offsetY,
                                outroFunc(piece.startTime - TimeUtils.millis()),
                                outroFunc(piece.startTime - TimeUtils.millis()),
                                0);
                    }
                    break;
                default:
                    break;
            }

        }
        offsetX = (gridPieceWidth / 219 * 25.5f);
        offsetY = (gridPieceHeight / 219 * 20);
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];

            int getter = (int) (9f * Math.abs(sin(piece.rectangle.x * (float) piece.id + piece.rectangle.y * (float) piece.id)));

            switch (piece.state) {
                //case STATE_NONE:
                //Line
                //break;

                case STATE_TEMP:
                    batch.draw(wallsTemp.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            0);
                    break;
                case STATE_TEMP_NOTOUCH:
                    batch.draw(wallsHighlight.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            0);
                    break;
                case STATE_FINAL:
                    batch.draw(wallsTopsFinal.get(getter), piece.rectangle.x - offsetX, piece.rectangle.y - offsetY,
                            piece.rectangle.width / 2,
                            piece.rectangle.height / 2,
                            piece.rectangle.width + offsetX,
                            piece.rectangle.height + offsetY,
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            outroFunc(piece.startTime - TimeUtils.millis()),
                            0);
                    break;
                default:
                    break;
            }
        }
    }

    public void introMovers() {
        Mover mover;
        for (int i = 0; i < movers.size(); i++) {
            mover = movers.get(i);

            batch.draw(sheep, mover.rect.x, mover.rect.y,                   // Image, x, y
                    mover.rect.width * 0.5f, mover.rect.height * 0.5f,      // Rotation origin
                    mover.rect.width,                                       // Width
                    mover.rect.height,                                      // Height
                    introFunc(TimeUtils.millis() - mover.startTime),        // ScaleX
                    introFunc(TimeUtils.millis() - mover.startTime),        // ScaleY
                    mover.vector.angle() - 90);                             // Rotation
        }
    }

    public void outroMovers() {
        Mover mover;
        for (int i = 0; i < movers.size(); i++) {
            mover = movers.get(i);

            batch.draw(sheep, mover.rect.x, mover.rect.y,                   // Image, x, y
                    mover.rect.width * 0.5f, mover.rect.height * 0.5f,      // Rotation origin
                    mover.rect.width,                                       // Width
                    mover.rect.height,                                      // Height
                    outroFunc(mover.startTime - TimeUtils.millis()),        // ScaleX
                    outroFunc(mover.startTime - TimeUtils.millis()),        // ScaleY
                    mover.vector.angle() - 90);                             // Rotation
        }
    }



    //------------------- Initializations ---------------

    // Populates grid arrays with fresh new GridPieces
    public void initGridPieces() {

        gridPieces = new GridPiece[gridSizeHeight * gridSizeWidth];
        gridMap = new ArrayList<ArrayList<GridPiece>>();

        int totalGridPieces = 0;

        int wallInt = 0;
        for (float i = 0f; i < gridSizeHeight; i++) {
            gridMap.add(new ArrayList<GridPiece>());

            for (float j = 0f; j < gridSizeWidth; j++) {
                GridPiece gridPiece = new GridPiece(j * gridPieceWidth, i * gridPieceHeight,
                        gridPieceWidth, gridPieceHeight);
                gridPiece.id = totalGridPieces;

                int tempI = (int) i;
                int tempJ = (int) j;

                gridPiece.row = tempI;
                gridPiece.column = tempJ;

                //

                gridPieces[totalGridPieces] = gridPiece;
                gridMap.get((int) i).add(gridPieces[totalGridPieces]);

                // Creates fence on edge
                if (tempI == 0 || tempJ == 0 || tempJ == gridSizeWidth - 1
                        || tempI == gridSizeHeight - 1) {
                    gridPiece.setState(STATE_FINAL);
                    //setState(gridPiece, STATE_FINAL);
                }

                // Adds wall int to tell drawWalls which texture to use
                gridPiece.wallTex = wallInt++;
                if(wallInt > numWalls - 1) {
                    wallInt = 0;
                }

                // Adds neighbors to array list of neighbors in each grid piece
                GridPiece neighbor;

                //Left && Right
                if(gridPiece.row > 0) {
                    neighbor = gridMap.get(gridPiece.row - 1).get(gridPiece.column);
                    neighbor.neighbors.add(gridPiece);
                    gridPiece.neighbors.add(neighbor);
                }
                //Bottom && Top
                if(gridPiece.column > 0) {
                    neighbor = gridMap.get(gridPiece.row).get(gridPiece.column - 1);
                    neighbor.neighbors.add(gridPiece);
                    gridPiece.neighbors.add(neighbor);
                }
                // BotLeft && TopRight
                if(gridPiece.column > 0 && gridPiece.row > 0) {
                    neighbor = gridMap.get(gridPiece.row - 1).get(gridPiece.column - 1);
                    neighbor.neighbors.add(gridPiece);
                    gridPiece.neighbors.add(neighbor);
                }
                //BotRight && TopLeft
                if(gridPiece.row > 0 && gridPiece.column < gridSizeWidth - 1) {
                    neighbor = gridMap.get(gridPiece.row - 1).get(gridPiece.column + 1);
                    neighbor.neighbors.add(gridPiece);
                    gridPiece.neighbors.add(neighbor);
                }

                totalGridPieces++;
            }
        }

    }

    public void resetTimes(long time) {
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];

                piece.startTime = time;
            }

            for(Mover mover : movers) {
                mover.startTime = time;
            }
    }

    public void resetGrid() {
        for (int i = 0; i < gridPieces.length; i++) {
            GridPiece piece = gridPieces[i];
            if (piece.column > 0 && piece.column < gridSizeWidth - 1
                    && piece.row > 0 && piece.row < gridSizeHeight -1) {
                piece.setState(STATE_NONE);
            } else {
                piece.setState(STATE_FINAL);
            }
        }
    }

    public void spawnMover() {
        Mover mover = new Mover(MathUtils.random(gridPieceWidth, playWidthFloat - (2 * gridPieceWidth)),
                MathUtils.random(gridPieceHeight, playHeightFloat - (2 * gridPieceHeight)),
                sheep);
        mover.id = Mover.idCount;
        Mover.idCount++;
        movers.add(mover);
    }


    //-------------------- Movement ----------------------
    public void calculateMoverMovement() {
        movement.calculateMovement();
    }

    //Checks all four corners for overlapping a gridPiece
    // 0 = no overlap, 1 = botleft, 2 = botright, 4 = topright, 8 = topleft
    public Integer moverOverlapsState(Mover mover, ArrayList<Integer> states) {
        integers = 0;

        int pieceState;
        int state;

        for (int i = 0; i < states.size(); i++) {
            state = states.get(i);
            quads.x = mover.rect.x;
            quads.y = mover.rect.y;
            pieceState = gridPieces[getGridPiece(quads)].state;

            if (pieceState == state) {
                integers += 1;
            }

            quads.x += mover.rect.width;
            pieceState = gridPieces[getGridPiece(quads)].state;

            if (pieceState == state) {
                integers += 2;
            }

            quads.y += mover.rect.height;
            pieceState = gridPieces[getGridPiece(quads)].state;

            if (pieceState == state) {
                integers += 4;
            }

            quads.x -= mover.rect.width;
            pieceState = gridPieces[getGridPiece(quads)].state;

            if (pieceState == state) {
                integers += 8;
            }
        }

        return integers;
    }


//    public ArrayList<Integer> moverOverlapsWall(Mover mover, ArrayList<Integer> states) {
//        integers.clear();
//
//        int pieceState;
//        int state;
//
//        for (int i = 0; i < states.size(); i++) {
//            state = states.get(i);
//            quads.x = mover.rect.x;
//            quads.y = mover.rect.y;
//            pieceState = gridPieces.get(getGridPiece(quads)).state;
//
//            if (pieceState == state) {
//                integers.add(1);
//            }
//
//            quads.x += mover.rect.width;
//            pieceState = gridPieces.get(getGridPiece(quads)).state;
//
//            if (pieceState == state) {
//                integers.add(2);
//            }
//
//            quads.y += mover.rect.height;
//            pieceState = gridPieces.get(getGridPiece(quads)).state;
//
//            if (pieceState == state) {
//                integers.add(3);
//            }
//
//            quads.x -= mover.rect.width;
//            pieceState = gridPieces.get(getGridPiece(quads)).state;
//
//            if (pieceState == state) {
//                integers.add(4);
//            }
//        }
//
//        return integers;
//    }

    public int getGridPiece(Vector3 vector) {

        // Deals with clicks on edge or beyond
        if (vector.x >= (playWidthFloat)) {
            vector.x = playWidthFloat - (gridPieceWidth);

        } else if (vector.x < 0) {
            vector.x = 0;
        }
        if (vector.y >= (playHeightFloat)) {
            vector.y = playHeightFloat - (gridPieceWidth);

        } else if (vector.y < 0) {
            vector.y = 0;
        }

        // Gets scaled grid x,y
        int x = Math.round(vector.x / gridPieceWidth - 0.5f); //0.5f makes it round properly
        int y = Math.round(vector.y / gridPieceHeight - 0.5f);

        // GridPieceId
        return y * gridSizeWidth + x;

    }


    //------------------- Math and Processing ------------------
    //Radians
    public float sin(float input) {
        return (float) Math.sin((double) input);
    }

    public float cos(float input) {
        return (float) Math.cos((double) input);
    }

    public float tan(float input) {
        return (float) Math.tan((double) input);
    }

    public float slope(float x1, float y1, float x2, float y2) {
        return (y2 - y1)/(x2 - x1);
    }

    public float getAngle(Vector2 vector1, Vector2 vector2) {

        return new Vector2(vector2.x - vector1.x, vector2.y - vector1.y).angle();

    }

    public Color getColorFromRGB(float r, float g, float b, float a) {

        float rf = r / 255;
        float gf = g / 255;
        float bf = b / 255;
        float af = a / 255;

        return new Color(rf, gf, bf, af);
    }




    //------------------- Grid Slope Mapping ------------------

    private float[][] getEmptyArray(int width, int height) {
        float[][] arr = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                arr[i][j] = 0;
            }
        }
        return arr;
    }

    private float[][] generateWhiteNoise(int width, int height) {
        Random random = new Random(0); //Seed to 0 for testing
        float[][] noise = getEmptyArray(width, height);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                noise[i][j] = (float) random.nextDouble() % 1;
            }
        }

        return noise;
    }

    float[][] generateSmoothNoise(float[][] baseNoise, int octave) {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][] smoothNoise = getEmptyArray(width, height);

        int samplePeriod = 1 << octave; // calculates 2 ^ k
        float sampleFrequency = 1.0f / samplePeriod;

        for (int i = 0; i < width; i++) {
            //calculate the horizontal sampling indices
            int sample_i0 = (i / samplePeriod) * samplePeriod;
            int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
            float horizontal_blend = (i - sample_i0) * sampleFrequency;

            for (int j = 0; j < height; j++) {
                //calculate the vertical sampling indices
                int sample_j0 = (j / samplePeriod) * samplePeriod;
                int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
                float vertical_blend = (j - sample_j0) * sampleFrequency;

                //blend the top two corners
                float top = Interpolate(baseNoise[sample_i0][sample_j0],
                        baseNoise[sample_i1][sample_j0], horizontal_blend);

                //blend the bottom two corners
                float bottom = Interpolate(baseNoise[sample_i0][sample_j1],
                        baseNoise[sample_i1][sample_j1], horizontal_blend);

                //final blend
                smoothNoise[i][j] = Interpolate(top, bottom, vertical_blend);
            }
        }

        return smoothNoise;
    }

    float Interpolate(float x0, float x1, float alpha) {
        return x0 * (1 - alpha) + alpha * x1;
    }

    float[][] generateGordonNoise(float[][] baseNoise, int octaveCount) {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing

        float persistance = 0.5f;

        //generate smooth noise
        for (int i = 0; i < octaveCount; i++) {
            smoothNoise[i] = generateSmoothNoise(baseNoise, i);
        }

        float[][] perlinNoise = getEmptyArray(width, height);
        float amplitude = 1.0f;
        float totalAmplitude = 0.0f;

        //blend noise together
        for (int octave = octaveCount - 1; octave >= 0; octave--) {
            amplitude *= persistance;
            totalAmplitude += amplitude;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
                }
            }
        }

        //normalisation
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                perlinNoise[i][j] /= totalAmplitude;
            }
        }

        return perlinNoise;
    }



    //--------------------- High Scores ------------------------

    public void addAScore(Integer val, String name) {
        if(!scoreDist.containsKey(val)) {
            scoreDist.put(val, new ArrayList<String>());
        }

        scoreDist.get(val).add(name);
    }

    public Integer getHighestScore() {
        return scoreDist.lastKey();
    }

    //--------------------- Animation --------------------------

    // Function that takes deltaTime and returns a value between 0 and 1 to multiply with size
    public float introFunc(float deltaTime) {
        //System.out.println("D| deltaTime: " + deltaTime);
        deltaTime = deltaTime / 1000;
        float h = 1.7f;
        float g = 0.6f;

        return h * deltaTime / (deltaTime * deltaTime + g);
        //float w = -0.63f;
        //float b = 0.717f;
        //return (w * w)/(deltaTime * deltaTime + 2 * b * w * deltaTime + w * w) - 1f;
    }

    // Function that takes deltaTime and returns a value between 1 and 0 to multiply with size
    public float outroFunc(float deltaTime) {
        deltaTime = deltaTime / 1000 * 3;
        return deltaTime * deltaTime;
    }

    // Animator that adjusts values
    public void animator() {

    }

}
