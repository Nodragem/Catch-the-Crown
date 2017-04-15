package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by geoff on 08/04/2017.
 */
public class TutorialWindow extends DefaultWindow {
    private int currentTutorial;
    private MenuScreen menuScreen;
    private Array<Texture> tutoTextures;
//    public boolean fromMainMenu;


    public TutorialWindow(MenuScreen menuScreen, SpriteBatch batch, Viewport viewport, BitmapFont font) {
        super(batch, viewport, font);
//        fromMainMenu = false;
        this.menuScreen = menuScreen;
        this.titleText = "";
        if (menuScreen != null)
            this.messageText = "(A) Start \t (B) Exit";
        else
            this.messageText = "(A) or (B) to Exit";
        posMessage.x = 680;
        posMessage.y= 75;
        listActions.set(0, "");
        currentTutorial = 0;
        tutoTextures = new Array<Texture>(true, 5);
        tutoTextures.add(new Texture(Gdx.files.internal("screens/tuto_controls.png")));
        tutoTextures.add(new Texture(Gdx.files.internal("screens/tuto_1.png")));
        tutoTextures.add(new Texture(Gdx.files.internal("screens/tuto_2.png")));
        tutoTextures.add(new Texture(Gdx.files.internal("screens/tuto_3.png")));
        tutoTextures.add(new Texture(Gdx.files.internal("screens/tuto_4.png")));
        setWinSize(new Vector2(viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 100));
        centerWindow();
        ymargin = 1500; // off screen
        xmargin = 380;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (inputProfile != null && !isClosed()) {
            processNextInput(inputProfile.getButtonState("Next"));
            processPreviousInput(inputProfile.getButtonState("Previous"));

        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Texture todraw = tutoTextures.get(currentTutorial);
        float resize;
        if (currentTutorial == 0)
            resize = 1.5f;
        else
            resize = 1.5f;
        batch.draw(todraw,
                winCenter.x - todraw.getWidth() * resize / 2,
                winCenter.y - todraw.getHeight() * resize / 2,
                todraw.getWidth() * resize,
                todraw.getHeight() * resize);
    }

    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                if(menuScreen != null)
                    menuScreen.startTournament();
                requestClosing();
                break;
        }
        return false;
    }

    protected void processNextInput(boolean isPressed) {
        if (isPressed && selectionCoolDown == 0) {
            selectionCoolDown=1;
            if (currentTutorial < (tutoTextures.size - 1)) {
                currentTutorial += 1;
            }
        }
    }

    protected void processPreviousInput(boolean isPressed) {
        if (isPressed && selectionCoolDown == 0) {
            selectionCoolDown=1;
            if (currentTutorial > 0)
                currentTutorial -= 1;
        }
    }
}
