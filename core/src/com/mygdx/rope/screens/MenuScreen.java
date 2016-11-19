package com.mygdx.rope.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.rope.RopeGame;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 06/09/2015.
 */
public class MenuScreen implements Screen {
    public RopeGame game;
    private Constants.MENU_STATE menuState;
    private final SpriteBatch batch;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private MainMenuWindow mainMenuWindow;
    private BitmapFont font;

    public MenuScreen(RopeGame game){
        this.menuState = Constants.MENU_STATE.MAIN_MENU;
        this.game = game;
        this.batch = game.getBatch();
        this.camera = game.camera;
    }

    @Override
    public void show() {
        camera.setToOrtho(false, Constants.VIEWPORT_GUI_WIDTH, Constants.VIEWPORT_GUI_HEIGHT);
        camera.update();
        viewport = new FitViewport(Constants.VIEWPORT_GUI_WIDTH, Constants.VIEWPORT_GUI_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font = new BitmapFont(Gdx.files.internal("fonts/try64x64.fnt"), false);
        font.getData().setScale(0.90f);
        mainMenuWindow = new MainMenuWindow(this, batch, viewport, font);
        mainMenuWindow.openWindow(game.getInputProfiles().getValueAt(0), (Window) null);
    }
    public void startTournament() {
        game.startTournament();
        mainMenuWindow.requestClosing();
    }

    public void updateLevelSelectionForTournament(BooleanArray levelSelected, boolean isRandom) {
        game.updateLevelSelection(Constants.TOURNAMENT_LEVEL_PATH, levelSelected, isRandom);
    }

    public void prepareLevelForStory() {
        game.updateLevelSelection(Constants.TOURNAMENT_LEVEL_PATH, null, false); //FIXME: should be a STORY LEVEL PATH
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        Gdx.gl.glClearColor(0x64 / 255.0f, 0x95 / 255.0f, 0xed / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined); // what is the change?
        batch.begin();
        switch(menuState){
            case MAIN_MENU:
                mainMenuWindow.render(delta);
                break;
            case STORY_MENU:
                break;
            case TOURNAMENT_MENU:
                break;
            case OPTION_MENU:
                break;
            case CONTROL_MENU:
                break;
            case QUIT_REQUEST:
                break;
        }
        batch.end();

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
        font.dispose();
        //mainMenuWindow.requestClosing();
        game.requestExit();

    }


}
