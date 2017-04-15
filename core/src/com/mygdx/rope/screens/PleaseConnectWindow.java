package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by geoff on 05/04/2017.
 */
public class PleaseConnectWindow extends DefaultWindow {
    private MenuScreen menuScreen;
    private Texture controllerTexture;


    public PleaseConnectWindow(MenuScreen menuScreen, SpriteBatch batch, Viewport viewport, BitmapFont font) {
        super(batch, viewport, font);
        this.menuScreen = menuScreen;
        this.titleText = "WARNING MESSAGE!";
        this.messageText = "Press Escape to Quit ...";
        listActions.set(0, "Press SpaceBar to Exit");
        controllerTexture = new Texture(Gdx.files.internal("screens/xbox360_connect_controller.png"));
        setWinSize(new Vector2(viewport.getWorldWidth() - 300, viewport.getWorldHeight() - 200));
        centerWindow();
        ymargin = 850;
        xmargin = 380;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            requestClosing();
            menuScreen.dispose();
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        batch.draw(controllerTexture,
                winCenter.x - controllerTexture.getWidth(),
                winCenter.y - controllerTexture.getHeight() - 40,
                controllerTexture.getWidth() * 2,
                controllerTexture.getHeight() * 2);
    }

    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                requestClosing();
                menuScreen.dispose();
                break;
        }
        return false;
    }
}
