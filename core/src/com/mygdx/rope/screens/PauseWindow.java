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

/**
 * Created by Geoffrey on 29/08/2015.
 */
public class PauseWindow implements Window  {
    private final NinePatch winTexture;
    private final BitmapFont font;
    private final GameScreenTournament gameScreen;
    private final Array<String> selectionText;
    private final String mainText;
    public int ipauser;
    private int selectedText;
    private Viewport viewport;
    private final Vector2 winSize;
    private final Vector2 winPos;
    private final Vector2 winTopLeft;
    private TiledDrawable background;
    private ParticleEffect[] effects;
    private float timer;
    private Animation pauser_anim;
    private NinePatch columnNinePatch;

    public PauseWindow(GameScreenTournament gameScreen, Viewport viewport, BitmapFont font, ArrayMap<String, Player> players) {
        this.font = font;
        this.ipauser = -1;
        this.gameScreen = gameScreen;
        this.viewport = viewport;
        this.mainText = "\"Let's have \n a break ...\"";
        Pixmap pixmap  = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.2f, 0.2f, 0.95f);
        pixmap.fillRectangle(0, 0, 32, 32);
        background = new TiledDrawable(new TextureRegion(new Texture(pixmap)));
        columnNinePatch = GameObject.atlas.createPatch("column");
        columnNinePatch.scale(4.0f, 4.0f);
        winSize = new Vector2(viewport.getWorldWidth()/2.5f, viewport.getWorldHeight()/2.7f);
        winPos = new Vector2( (viewport.getWorldWidth()-winSize.x)/2f, (viewport.getWorldHeight()-winSize.y)/2f);
        winTopLeft = new Vector2(winPos.x+110, winPos.y + winSize.y);
        winTexture = GameObject.atlas.createPatch("paper_lance");
        winTexture.scale(4.0f, 4.0f);
        timer = 0;

        selectionText = new Array<String>(4);
        selectionText.add("Resume Level"); // default 0
        selectionText.add("Restart Level"); // 1
        selectionText.add("Option");// 2
        selectionText.add("Back to Main Menu"); // 3
        this.selectedText = 0;

    }

    public int getIndexPauser() {
        return ipauser;
    }

    public void openWindow(int ipauser) {
        this.ipauser = ipauser;
        //Gdx.app.debug("PAUSE", "PAUSE");
        gameScreen.setDebugText("");
        Array<TextureAtlas.AtlasRegion> regions = null;
        regions = GameObject.atlas.findRegions("Piaf_" +
                gameScreen.getPlayersList().getValueAt(ipauser).getCharacter().color_texture + "_loosing");
        pauser_anim = new Animation(1.0f / 2.0f, regions, Animation.PlayMode.LOOP);
        Player p = gameScreen.getPlayersList().getValueAt(ipauser);
        p.getInputProfile().setContext("Menu");
        p.setCurrentWindow(this);
        if (gameScreen.getStateGame() == Constants.GAME_STATE.PLAYED) {
            gameScreen.setStateGame(Constants.GAME_STATE.PAUSED);
            //gameScreen.setDebugText("PAUSE");
        }
    }

    @Override
    public void closeWindow() {
        Player p = gameScreen.getPlayersList().getValueAt(ipauser);
        if (!p.toPreviousWindow()) // if there is no previous window:
            p.getInputProfile().setContext("Game");
        else
            p.getInputProfile().setContext("Menu");
        if(gameScreen.getStateGame() == Constants.GAME_STATE.PAUSED)
            gameScreen.setStateGame(gameScreen.getPreviousStateGame());
        this.ipauser = -1;
    }

    @Override
    public void update(float deltaTime) {

    }

    public void render(SpriteBatch batch){
        update(0);
        timer += Gdx.graphics.getDeltaTime();
        background.draw(batch, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        winTexture.draw(batch, winPos.x, winPos.y, winSize.x, winSize.y);
        gameScreen.glayout.setText(font, "Score Table");
        //BitmapFont.TextBounds bound = font.getBounds();
        font.setColor(1, 0, 0, 1);
        //font.draw(batch, "PAUSE", winTopLeft.x +200, winTopLeft.y);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, mainText, winTopLeft.x + 50, winTopLeft.y + 90); // 110 if the border size
        font.getData().markupEnabled = true;

        TextureRegion region = null;
        float margin = animePauser(batch, region);
        for (int i = 0; i < selectionText.size; i++) {
            if (i == selectedText) {
                font.draw(batch, "[RED]"+selectionText.get(i)+"[]",
                        winTopLeft.x + 0.5f + margin,
                        winTopLeft.y -150 - (gameScreen.glayout.height + 10f) * i);
            } else {
                font.draw(batch, "[WHITE]"+selectionText.get(i)+"[]",
                        winTopLeft.x + 0.5f + margin,
                        winTopLeft.y -150 - (gameScreen.glayout.height + 10f) * i);
            }

        }
        font.getData().markupEnabled = false;

    }

    @Override
    public boolean executeSelectedAction() {
        switch(selectedText){
            case 0:
                closeWindow();
                break;
            case 1:
                gameScreen.startNewLevel(gameScreen.getCurrentLevel());
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return false;
    }

    @Override
    public void selectNextAction() {
        selectedText = (selectedText - 1) % selectionText.size;
        if(selectedText == -1)
            selectedText = selectionText.size - 1;
    }

    @Override
    public void selectPreviousAction() {
        selectedText = (selectedText + 1) % selectionText.size;
    }

    @Override
    public int getActivePlayer() {
        return 0;
    }

    @Override
    public void selectAction(int index) {

    }

    private float animePauser(SpriteBatch batch, TextureRegion region) {
        region=pauser_anim.getKeyFrame(timer);
        batch.draw(region, winTopLeft.x - region.getRegionWidth() - 50f,
                winTopLeft.y - region.getRegionHeight() - 0.5f, 150, 150);
        return region.getRegionWidth();
    }

}
