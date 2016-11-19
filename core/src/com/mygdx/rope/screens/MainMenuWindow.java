package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.RopeGame;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 29/08/2015.
 */
public class MainMenuWindow extends DefaultWindow {

    private final MenuScreen menuScreen;

    public MainMenuWindow(MenuScreen menuScreen, SpriteBatch batch, Viewport viewport, BitmapFont font) {
        super(batch, viewport, font);
        this.menuScreen = menuScreen;
        setListActions(new Array(new String[]
                {"Quick Start", "Tournament Mode", "Adventure Mode", "Option","Control", "Quit"}));
        //this.messageText = "\"Let's have \n a break ...\"";
        this.titleText = "MENU";
        //this.timer = 0;
    }
    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                menuScreen.startTournament();
                break;
            case 1:
                addChild(new LevelSelectionWindow(this.menuScreen, this.batch, this.viewport, this.font));

                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                requestClosing();
                menuScreen.dispose();
                break;
        }
        return false;
    }


    @Override
    public void render(float delta) {
        super.render(delta);
    }


}
