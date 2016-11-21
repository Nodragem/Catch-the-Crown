package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
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
                    {"Resume Level", "Restart Level", "Option", "Back to Main Menu"}));
        this.gameScreen = gameScreen;
        this.messageText = "\"Let's have \n a break ...\"";
        this.titleText = "PAUSE";
        this.timer = 0;


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
        posMessage.set(winCenter.x - gLayout.width/2.0f, winTopLeft.y + 90);
        animePauser(batch, region);

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
