package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Player;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.InputHandler.InputProfile;

/**
 * Created by Geoffrey on 29/08/2015.
 */
public class PauseWindow extends DefaultWindow {
    //private GameScreenTournament gameScreen;
    private float timer;

    public PauseWindow(GameScreenTournament gameScreen, Viewport viewport, BitmapFont font) {
        super(gameScreen.getBatch(), viewport,font);
        setListActions(new Array(new String[]
                    {"Resume Level", "Restart Level", "Controls", "Back to Main Menu"}));
        this.gameScreen = gameScreen;
        this.messageText = "[#666666]\"Let's have \n a break ...\"[]";
        this.titleText = "PAUSE";
        this.timer = 0;

        additionalTexts = new Array<String>(3);
        additionalTexts.add("[#EE6655]CATCH THE CROWN![]");
        additionalTexts.add("[WHITE]Alpha Version[]");
        additionalTexts.add("Twitter: [#EEEE55]@Nodragem[] , itch.io: [#EEEE55]Nodragem[]");



//        this.posGameTitle = new Vector2(winCenter.x - gLayout.width/2.0f, winTopLeft.y-titleYOffset)
//        setWinSize(new Vector2(800, 500));
        //updateWinSize();
//        setXYspread(0, 1, true);
//        centerXPositions();

        // add Game Title and Subtitle (need to take into account the markup)
        font.getData().markupEnabled = true;
        posAddTexts = new Array<Vector3>(2);
        font.getData().setScale(2.0f);
        gLayout.setText(font, additionalTexts.get(0));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y-gLayout.height/2+300, 2.0f));
        font.getData().setScale(0.9f);
        gLayout.setText(font, additionalTexts.get(1));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y+200, 0.9f));
        font.getData().setScale(.9f);
        gLayout.setText(font, additionalTexts.get(2));
        posAddTexts.add(new Vector3(winCenter.x - gLayout.width/2.0f, winTopLeft.y-winSize.y - 250, 0.9f));
        font.getData().markupEnabled = false;
        font.getData().setScale(1.0f);


    }

    public void openWindow(InputProfile inputProfile, GameScreenTournament gameScreen, int icontroller) {
        super.openWindow(inputProfile, gameScreen);
        Array<TextureAtlas.AtlasRegion> regions = null;
        regions = GameObject.atlas.findRegions("Piaf_" +
                gameScreen.getPlayersList().getValueAt(icontroller).getCharacter().color_texture + "_Loosing");
        pauser_anim = new Animation(1.0f / 2.0f, regions, Animation.PlayMode.LOOP);
    }

//    @Override
//    public void closeWindow() {
//
//    }

    @Override
    public void render(float delta){
        super.render(delta);
        TextureRegion region = null;
        if (children.size < 1) {
            gLayout.setText(font, messageText);
            posMessage.set(winCenter.x - gLayout.width / 2.0f, winTopLeft.y + 90);
            animePauser(batch, region);
        }

    }

    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                requestClosing();
                break;
            case 1:
                gameScreen.startNewLevel(gameScreen.getCurrentLevel());
                break;
            case 2:
                addChild(new TutorialWindow(null, this.batch, this.viewport, this.font));
                break;
            case 3:
                gameScreen.toMainMenu();
                gameScreen = null;
                requestClosing();
                break;
        }
        return false;
    }


    private float animePauser(SpriteBatch batch, TextureRegion region) {
        timer += Gdx.graphics.getDeltaTime();
        region=pauser_anim.getKeyFrame(timer);
        batch.draw(region, winTopLeft.x - region.getRegionWidth() - 50f,
                winTopLeft.y - region.getRegionHeight() - 0.5f, 150, 150);
        return region.getRegionWidth();
    }

}
