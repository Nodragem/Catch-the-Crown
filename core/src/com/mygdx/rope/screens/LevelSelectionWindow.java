package com.mygdx.rope.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 29/08/2015.
 */
public class LevelSelectionWindow extends DefaultWindow {

    private final MenuScreen menuScreen;
    private BooleanArray levelBlocked;
    private BooleanArray levelSelected;
    private boolean isRandom;
    private Game game;
    private Array<String> listLevels;

    public LevelSelectionWindow(MenuScreen menuScreen, SpriteBatch batch, Viewport viewport, BitmapFont font) {
        super(batch, viewport, font);
        this.menuScreen = menuScreen;
        JsonReader reader = new JsonReader();
        JsonValue levelInfo = reader.parse(Gdx.files.internal(Constants.TOURNAMENT_LEVEL_PATH + "/Levels.json") );
        listLevels = new Array<String>(levelInfo.get("levels").asStringArray());
        levelBlocked = new BooleanArray(levelInfo.get("unblocked").asBooleanArray());
        levelSelected = new BooleanArray(levelInfo.get("selected").asBooleanArray());
        isRandom = levelInfo.get("random").asBoolean();
        setListActions(listLevels);
        addActionToList("-- Random Order --");
        addActionToList("-- Start Tournament --");
        addActionToList("- Back to Menu -");
        for (int i = 0; i < levelSelected.size; i++) {
            toggled.set(i, levelSelected.get(i));
        }
        toggled.set(levelSelected.size, isRandom);
        //this.messageText = "\"Let's have \n a break ...\"";
        this.titleText = "LEVEL SELECTION: ";

        //this.timer = 0;
    }
    @Override
    public boolean executeSelectedAction() {
        Gdx.app.debug("LevelSelectionWindow", "action "+selectedAction + ", nb. of level: "+listLevels.size);
        if (selectedAction < listLevels.size)
            toggleAction(selectedAction);
        else if(selectedAction == listLevels.size)
            toggleAction(selectedAction);
        else if(selectedAction == listLevels.size + 1) {
            for (int i = 0; i < listLevels.size; i++) {
                levelSelected.set(i, toggled.get(i));
            }
            isRandom = toggled.get(listLevels.size);
            menuScreen.updateLevelSelectionForTournament(levelSelected, isRandom);
            menuScreen.startTournament();
        }
        else if(selectedAction == listLevels.size + 2){
            requestClosing();
        }

        return false;
    }


    @Override
    public void render(float delta) {
        super.render(delta);
    }


}
