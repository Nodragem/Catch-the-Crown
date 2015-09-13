package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.InputHandler.InputProfile;

/**
 * Created by Geoffrey on 31/08/2015.
 */
public class DefaultWindow implements Window {
    public final GlyphLayout gLayout = new GlyphLayout();
    protected final SpriteBatch batch;
    protected SpriteDrawable cursor;
    private float textHeight;
    protected float titleYOffset;
    protected Vector2 winCenter;
    protected int selectedAction;
    protected NinePatch winTexture;
    protected BitmapFont font;
    protected String titleText;
    protected Array<String> listActions;
    public int icontroller;
    protected Viewport viewport;
    protected Vector2 winSize;
    protected Vector2 winPos;
    protected Vector2 winTopLeft;
    protected TiledDrawable background;
    protected Animation pauser_anim;
    protected NinePatch columnNinePatch;
    protected Window previousWindow;
    protected boolean closed;
    protected float xmargin;
    protected String colorTitle;
    protected String colorSelected;
    protected float Xspread; // 0 or 1
    protected float Yspread;
    protected float Xspacing;
    private float Yspacing;
    private Array<Float> cumulatedTextWidth;
    private Vector2 currentMovingVector;
    private float selectionCoolDown;
    protected InputProfile inputProfile;
    protected GameScreenTournament gameScreen;
    protected boolean closingRequested;
    protected float ymargin;

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font) {
        this.batch = batch;
        gameScreen = null;
        inputProfile = null;
        previousWindow =null;
        this.font = font;
        this.viewport = viewport;
        this.titleText = "WindowTitle";
        colorTitle = "[#9E5D41]";
        colorSelected = "[#EE6655]";
        closingRequested = false;
        titleYOffset = 0;
        Xspread = 0;
        Yspread = 1;
        Xspacing = 10f;
        Yspacing = 10f;
        Pixmap pixmap  = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.2f, 0.2f, 0.95f);
        pixmap.fillRectangle(0, 0, 32, 32);
        background = new TiledDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.2f, 0.2f, 0.95f);
        pixmap.fillTriangle(0,0,0,16,16,8);
        cursor = new SpriteDrawable(new Sprite(new Texture(pixmap)) );
        columnNinePatch = GameObject.atlas.createPatch("column");
        columnNinePatch.scale(4.0f, 4.0f);
        winSize = new Vector2(viewport.getWorldWidth() / 2.5f, viewport.getWorldHeight() / 2.7f);
        winPos = new Vector2((viewport.getWorldWidth() - winSize.x) / 2f, (viewport.getWorldHeight() - winSize.y) / 2f);
        winTopLeft = new Vector2(winPos.x+110, winPos.y + winSize.y);
        winCenter = new Vector2(winPos.x+ winSize.x/2.0f, winPos.y + winSize.y/2.0f);
        winTexture = GameObject.atlas.createPatch("paper_lance");
        winTexture.scale(4.0f, 4.0f);
        xmargin = 30;
        ymargin = 0;
        this.listActions = new Array<String>(1);
        this.listActions.add("Exit");
        this.selectedAction = 0;
        gLayout.setText(font, listActions.get(0));
        cumulatedTextWidth = new Array<Float>(2);
        cumulatedTextWidth.add(0f);
        this.textHeight = gLayout.height;
    }

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font, String[] listActions){
        this(batch, viewport, font);
        this.listActions = new Array<String>(listActions);
        updateYPositions();
    }

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font, Vector2 winSize){
        this(batch, viewport, font);
        this.winSize = winSize;
    }

    public void setWinSize(Vector2 winSize){
        this.winSize = winSize;

    }

    public void updateYPositions(){ // to call when using YSpread of the text, after any change of the text,
        float cumulated = 0;
        cumulatedTextWidth = new Array<Float>(listActions.size);
        for (String listAction : listActions) {
            cumulatedTextWidth.add(cumulated);
            gLayout.setText(font, listAction);
            cumulated += (gLayout.width + Xspacing);
        }
        gLayout.setText(font, "");
        Gdx.app.debug("Cumul", ""+listActions);
        Gdx.app.debug("Cumul", ""+cumulatedTextWidth);
    }

    public void centerWindow(){
        winPos = new Vector2( (viewport.getWorldWidth()-winSize.x)/2f, (viewport.getWorldHeight()-winSize.y)/2f);
        winTopLeft = new Vector2(winPos.x+110, winPos.y + winSize.y);
        winCenter = new Vector2(winPos.x+ winSize.x/2.0f, winPos.y + winSize.y/2.0f);
    }

    public void setListActions(Array<String> listActions) {
        this.listActions = listActions;
        updateYPositions();
    }

    public Array<String> getListActions() {
        return listActions;
    }

    @Override
    public void update(float deltaTime) {
        if (selectionCoolDown > 0)
            selectionCoolDown -= 5.0*deltaTime;
        else
            selectionCoolDown = 0;
        if (inputProfile != null && !isClosed()){
            currentMovingVector = inputProfile.getMovingVector();
            //gameScreen.setDebugText("7887"+currentMovingVector);
            processWindowMovingInput();
            processSelectInput(inputProfile.getButtonState("Select"));
            processBackInput(inputProfile.getButtonState("Back"));
            processPauseInput(inputProfile.getButtonState("Start"));

        }
        if (closingRequested == true){
            closeWindow();
            closingRequested = false;
        }

    }

    @Override
    public void render(float delta) {
        update(delta);
        background.draw(batch, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        winTexture.draw(batch, winPos.x, winPos.y, winSize.x, winSize.y);
        font.getData().markupEnabled = true;
        gLayout.setText(font, colorTitle+titleText+"[]");
        font.draw(batch, gLayout, winCenter.x - gLayout.width/2.0f, winTopLeft.y-100+titleYOffset ); // 110 if the border size
        for (int i = 0; i < listActions.size; i++) {
            if (i == selectedAction) {
                gLayout.setText(font, colorSelected+listActions.get(i)+"[]");
                font.draw(batch, gLayout,
                        winTopLeft.x + 0.5f + xmargin + cumulatedTextWidth.get(i) * Xspread,
                        winTopLeft.y -150 + ymargin - ((textHeight + Yspacing) * i * Yspread));
                cursor.draw(batch, winTopLeft.x - 5f - textHeight + xmargin + cumulatedTextWidth.get(i) * Xspread,
                        winTopLeft.y -150 + ymargin - ((textHeight + Yspacing) * i * Yspread) - textHeight, textHeight, textHeight);
            } else {
                gLayout.setText(font, "[WHITE]"+listActions.get(i)+"[]");
                font.draw(batch, gLayout,
                        winTopLeft.x +  0.5f + xmargin + cumulatedTextWidth.get(i) * Xspread,
                        winTopLeft.y -150 + ymargin - ((textHeight + Yspacing) * i * Yspread));
            }

        }
        font.getData().markupEnabled = false;

    }



    @Override
    public boolean executeSelectedAction() {
        return false;
    }

    @Override
    public void selectNextAction() {
        selectedAction = (selectedAction - 1) % listActions.size;
        if(selectedAction == -1)
            selectedAction = listActions.size - 1;
    }

    @Override
    public void selectPreviousAction() {
        selectedAction = (selectedAction + 1) % listActions.size;
    }

    @Override
    public void selectAction(int index) {

    }

    public void openWindow(InputProfile inputProfile, GameScreenTournament gameScreen) {
        this.openWindow(inputProfile, (Window) null);
        this.gameScreen = gameScreen;
        if (this.gameScreen.getStateGame() == Constants.GAME_STATE.PLAYED) {
            this.gameScreen.setStateGame(Constants.GAME_STATE.PAUSED);
        }

    }

    @Override
    public void openWindow(InputProfile inputProfile, Window previousWindow) {
        selectionCoolDown = 1;
        this.previousWindow = previousWindow;
        this.inputProfile = inputProfile;
        this.inputProfile.setContext("Menu");
        this.closed = false;
    }
    @Override
    public void callBackWindow(){
        this.closed = false;
    }
    @Override
    public int getActivePlayer() {
        return icontroller;
    }

    @Override
    public void setPreviousWindow(Window window) {
        this.previousWindow = window;
    }

    @Override
    public Window getPreviousWindow() {
        return previousWindow;
    }


    protected void closeWindow() {
        if (previousWindow == null) { // if there is no previous window:
            //inputProfile.setContext("Game");
            if(gameScreen!=null){ // that was the gamescreen which opens it
                gameScreen.setStateGame(gameScreen.getPreviousStateGame());
            }
        }
        else {
            previousWindow.callBackWindow();
        }
        inputProfile = null;
        this.closed = true;
    }

    public void requestClosing(){
        closingRequested = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void processWindowMovingInput() {
        if (currentMovingVector.y > 0.5 && selectionCoolDown == 0) {
            selectionCoolDown = 1;
            this.selectNextAction();
            //gameScreen.setDebugText("UP");
        }
        else if (currentMovingVector.y < -0.5 && selectionCoolDown == 0){
            selectionCoolDown = 1;
            this.selectPreviousAction();
            //gameScreen.setDebugText("DOWN");
        }

    }

    protected void processSelectInput(boolean isPressed){
        if(isPressed && selectionCoolDown == 0) {
            this.executeSelectedAction();
            selectionCoolDown=1;
        }
    }

    protected void processBackInput(boolean isPressed) {
        if (isPressed && selectionCoolDown == 0) {
            this.closeWindow();
            selectionCoolDown=1;
        }
    }
    protected void processPauseInput(boolean isPressed) {
        if (isPressed && selectionCoolDown == 0) {
            selectionCoolDown = 1;
            requestClosing();
        }
    }


}
