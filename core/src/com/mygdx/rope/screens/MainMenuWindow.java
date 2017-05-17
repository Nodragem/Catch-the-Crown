package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
//    private Vector2 posGameTitle;

    public MainMenuWindow(MenuScreen menuScreen, SpriteBatch batch, Viewport viewport, BitmapFont font) {
        super(batch, viewport, font);
        this.menuScreen = menuScreen;
        setListActions(new Array(new String[]
                {"Quick Start", "Tournament Mode", "Adventure Mode", "Option","Control", "Quit"}));
        //this.messageText = "\"Let's have \n a break ...\"";
        this.titleText = "MENU";
        additionalTexts = new Array<String>(3);
        additionalTexts.add("[#EE6655]CATCH THE CROWN![]");
        additionalTexts.add("[WHITE]Alpha Version[]");
        additionalTexts.add("Twitter: [#EEEE55]@Nodragem[] , itch.io: [#EEEE55]Nodragem[]");



//        this.posGameTitle = new Vector2(winCenter.x - gLayout.width/2.0f, winTopLeft.y-titleYOffset)
//        setWinSize(new Vector2(800, 500));
        //updateWinSize();
        setXYspread(0, 1, true);
        centerXPositions();

        // add Game Title and Subtitle (need to take into account the markup)
        font.getData().markupEnabled = true;
        posAddTexts = new Array<Vector3>(2);
        font.getData().setScale(2.0f);
        gLayout.setText(font, additionalTexts.get(0));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y-gLayout.height/2+150, 2.0f));
        font.getData().setScale(0.9f);
        gLayout.setText(font, additionalTexts.get(1));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y+50, 0.9f));
        font.getData().setScale(.9f);
        gLayout.setText(font, additionalTexts.get(2));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y-winSize.y - 150, 0.9f));
        font.getData().markupEnabled = false;
        font.getData().setScale(1.0f);

    }
    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                addChild(new TutorialWindow(this.menuScreen, this.batch, this.viewport, this.font));

                //menuScreen.startTournament();
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
