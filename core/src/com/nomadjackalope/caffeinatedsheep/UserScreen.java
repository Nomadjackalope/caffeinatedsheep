package com.nomadjackalope.caffeinatedsheep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by Benjamin on 7/30/2015.
 *
 */
public class UserScreen implements Screen {
    CaffeinatedSheep game;

    int screen;

    final int INTRO = 0;
    final int MAIN = 1;
    final int HANDLECLICKS = 2;
    final int OUTRO = 3;
    final int END = 4;

    ArrayList<Animate> animTimes = new ArrayList<Animate>();

    User enteringUser;
    User lastUser;


    Stage stage;
    Table table;
    List list;
    Skin skin;
    Array<TextButton> buttons = new Array<TextButton>();

    Label food;
    Label dogs;
    TextButton name;
    Label money;
    TextButton pick;
    TextButton deleteUser;

    ShapeRenderer shapeRenderer = new ShapeRenderer();

    boolean doDeleteUser = false;


    public UserScreen(final CaffeinatedSheep game) {
        this.game = game;


        animTimes.add(INTRO, new Animate(INTRO, TimeUtils.millis(), 300l));
        animTimes.add(MAIN, new Animate(MAIN, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(HANDLECLICKS, new Animate(HANDLECLICKS, animTimes.get(0).endTime, game.hugeNum));
        animTimes.add(OUTRO, new Animate(OUTRO, game.hugeNum, 300l));
        animTimes.add(END, new Animate(END, game.hugeNum, 300l));


        //Stage
        stage = new Stage();


        Label.LabelStyle labelStyle = new Label.LabelStyle(game.stageFontLarge, new Color(1,1,1,1));

        //name = new Label(game.activeUser.name, labelStyle);
        //name.setFontScale(3);

        TextButton.TextButtonStyle nameStyle = new TextButton.TextButtonStyle();
        nameStyle.font = game.stageFontLarge;

        name = new TextButton(game.activeUser.name, nameStyle);
        name.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                editUser();
                return true;
            }
        });

        labelStyle.font = game.stageFont;
        money = new Label("Money: " + String.valueOf(game.activeUser.money), labelStyle);
        food = new Label("Food: " + String.valueOf(game.activeUser.food), labelStyle);
        dogs = new Label("Dogs: " + String.valueOf(game.activeUser.dogs), labelStyle);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = game.stageFont;


        // ??
//        if(game.users != null) {
//            for (int i = 0; i < game.users.size(); i++) {
//
//                buttons.add(new TextButton(game.users.get(i).name, style));
//                buttons.get(i).addListener(new ClickListener() {
//                    @Override
//                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                        System.out.println("US| event: " + event.getRelatedActor());
//                        return true;
//                    }
//                });
//
//            }
//        }


        //
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("atlas.pack"));
        skin = new Skin(Gdx.files.internal("uiskin.json"), atlas);

        list = new List(skin);
        list.setColor(0,0,0,0);
        list.getStyle().font = game.stageFont;

        list.setItems(getUserArr());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        //game.stageFont.getData().setScale(0.5f);
        textButtonStyle.font = game.stageFont;


        // Back button
        pick = new TextButton("Back", textButtonStyle);
        //textButton.getStyle().font = game.stageFont;
        pick.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                SaveHelper.saveUsers(game.users);  //TODO don't do this because the game will crash?
                setEnd(0);
                return true;
            }
        });


        // Buy food button
        TextButton buyFood = new TextButton("Buy food @ 300", textButtonStyle);
        buyFood.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(game.activeUser.money > 300) {
                    game.activeUser.money -= 300;
                    game.activeUser.food++;
                    updateUserLabels();
                }
                return true;
            }
        });

        // Buy dogs button
        TextButton buyDogs = new TextButton("Buy dogs @ 150", textButtonStyle);
        buyDogs.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(game.activeUser.money > 150) {
                    game.activeUser.money -= 150;
                    game.activeUser.dogs++;
                    updateUserLabels();
                }
                return true;
            }
        });



        ScrollPane scrollPane = new ScrollPane(list);


        // New User
        TextButton addUser = new TextButton("New User", textButtonStyle);
        addUser.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                newUser();
                return true;
            }
        });


        // Delete user and double check
        deleteUser = new TextButton("Delete User", textButtonStyle);
        deleteUser.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!doDeleteUser) {
                    deleteUser.setText("Really? Delete?");
                    doDeleteUser = true;
                } else {
                    System.out.println("US| users: " + game.users);
                    game.users.remove(game.activeUser);
                    System.out.println("US| users: " + game.users);
                    if (game.users.size() == 0) {
                        game.activeUser = new User("Guest", 0,0,0,0,0);
                        game.users.add(game.activeUser);
                    } else {
                        game.activeUser = game.users.get(0);
                    }
                    list.setItems(getUserArr());
                    System.out.println("US| users: " + game.users);
                    updateUserLabels();
                    deleteUser.setText("Delete User");
                    doDeleteUser = false;
                }
                return true;
            }
        });



        // Layout page and fill in with previously created buttons
        table = new Table();
        table.setFillParent(true);
        table.align(Align.left | Align.top);
        table.pad(90, 80, 90, 90);


        Table left = new Table();
        left.add(scrollPane).align(Align.top).expandY();
        left.row();
        left.add(pick).align(Align.bottom | Align.left);


        Table center = new Table();
        center.add(name).align(Align.top | Align.left).colspan(2).expandX();
        center.row();
        center.add(money).align(Align.left);
        center.row();
        center.add(food).align(Align.left);
        center.add(buyFood).align(Align.right);
        center.row();
        center.add(dogs).align(Align.left);
        center.add(buyDogs).align(Align.right);
        center.row();
        center.add().expandY();



        Table right = new Table();
        right.add(deleteUser).expandY().align(Align.top | Align.right);
        right.row();
        right.add(addUser).align(Align.right);



        table.add(left).align(Align.left | Align.top).expandY().fill();
        table.add(center).fill().expandX().align(Align.left | Align.top).padLeft(20).padRight(20).padTop(-15);
        table.add(right).align(Align.top).fill();

//        left.debug();
//        center.debug();
//        right.debug();
//        table.debug();

        stage.addActor(table);

        // Add input processing
        Clicky clicky = new Clicky();

        InputMultiplexer im = new InputMultiplexer(stage, clicky);
        Gdx.input.setInputProcessor(im);



        list.setSelected(game.activeUser);


        game.resetTimes(TimeUtils.millis());
    }


    @Override
    public void render(float delta) {

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        animator();

        stage.act(Gdx.graphics.getDeltaTime());
        //stage.getBatch().setShader(game.fontShader);
        stage.draw();
        //stage.getBatch().setShader(null);


//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        table.drawDebug(shapeRenderer);
//        shapeRenderer.end();
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

    // Main function for changing user on list change and rendering
    private void main() {

        game.activeUser = (User) list.getSelected();
        updateUserLabels();


        if(lastUser != null && lastUser != list.getSelected()) {
            lastUser.isActive = false;
            game.activeUser.isActive = true;
            doDeleteUser = false;
        }

        lastUser = game.activeUser;


        game.batch.begin();

        //---------- Move Sheep ---------------
        game.calculateMoverMovement();
        game.batchDrawing();


        game.batch.end();
    }


    private void handleClicks() {

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

    public class Clicky implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            //setEnd(0);
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            return false;
        }
    }

    // Updates the active user info shown
    private void updateUserLabels() {
        if(game.activeUser != null) {
            name.setText(game.activeUser.name);
            food.setText("Food: " + String.valueOf(game.activeUser.food));
            dogs.setText("Dogs: " + String.valueOf(game.activeUser.dogs));
            money.setText("Money: " + String.valueOf(game.activeUser.money));
        }
        if (doDeleteUser) {
            deleteUser.setText("Really? Delete?");
        } else {
            deleteUser.setText("Delete User");
        }
    }

    // Puts user list into Array type
    private Array<User> getUserArr() {
        Array<User> usersArr = new Array<User>();

        for (int i = 0; i < game.users.size(); i++) {
            usersArr.add(game.users.get(i));
        }

        return usersArr;
    }

    // Pops up text box to allow user to add new user
    private void newUser() {
        AddUserInputListener listener = new AddUserInputListener();
        Gdx.input.getTextInput(listener, "Name", "", ""); // TODO make this pretty
    }

    private void editUser() {
        EditUserInputListener listener = new EditUserInputListener();
        Gdx.input.getTextInput(listener, "Name", "", "");
    }

    private void updateEverything() {
        game.activeUser = game.users.get(game.users.size() - 1);
        updateUserLabels();
        list.setItems(getUserArr());
        list.setSelectedIndex(game.users.size() - 1);
    }

    public class AddUserInputListener implements Input.TextInputListener {
        @Override
        public void input (String text) {
            game.users.add(new User(text,0,0,0,0,0));
            updateEverything();
        }

        @Override
        public void canceled () {
        }
    }

    public class EditUserInputListener implements Input.TextInputListener {
        @Override
        public void input (String text) {
            game.activeUser.name = text;
            updateEverything();
        }

        @Override
        public void canceled () {
        }
    }


    //--------------- All screens should have the following functions ----------------
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
