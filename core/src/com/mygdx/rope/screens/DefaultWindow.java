package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.TimeUtils;
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
    private float maxTextWidth;
    private float textWidth;
    public Array<DefaultWindow> children;
    protected SpriteDrawable cursor;
    BooleanArray toggled;
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
    private float Xspread; // 0 or 1
    private float Yspread;
    protected float Xspacing;
    private float Yspacing;
    private Array<Float> cumulatedTextWidth;
    private Array<Float> cumulatedTextHeight;
    private Vector2 currentMovingVector;
    protected float selectionCoolDown;
    protected InputProfile inputProfile;
    protected GameScreenTournament gameScreen;
    protected boolean closingRequested;
    protected float ymargin;
    private String colorToggled;
    private String selectButton;
    public String messageText;
    public Vector2 posMessage;
    private float cursor_offset;
    protected boolean isYAxisSelection;

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font) {
        isYAxisSelection = true;
        this.batch = batch;
        gameScreen = null;
        inputProfile = null;
        previousWindow =null;
        this.font = font;
        this.viewport = viewport;
        children = new Array<DefaultWindow>(1);
        selectButton = "Select";
        messageText = "";
        posMessage = new Vector2(0, 0);

        Array <String> temp = new Array<String>(1);
        temp.add("Exit");
        setListActions(temp);
        this.selectedAction = 0;
        gLayout.setText(font, listActions.get(0));
        this.textHeight = gLayout.height;
        this.textWidth = gLayout.width;

        this.titleText = "WindowTitle";
        colorTitle = "[#9E5D41]";
        colorSelected = "[#EE6655]";
        colorToggled = "[#EEEE55]";
        closingRequested = false;
        titleYOffset = 85;
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
        winTopLeft = new Vector2(winPos.x, winPos.y + winSize.y);
        winCenter = new Vector2(winPos.x+ winSize.x/2.0f, winPos.y + winSize.y/2.0f);
        winTexture = GameObject.atlas.createPatch("paper_lance");
        winTexture.scale(4.0f, 4.0f);
        xmargin = 110+30.5f;
        ymargin = titleYOffset + 60;
        cursor_offset = -5;
        // by default there is only the action 'Exit'
        cumulatedTextWidth = new Array<Float>(2);
        cumulatedTextWidth.add(0f);
        cumulatedTextHeight = new Array<Float>(2);
        cumulatedTextHeight.add(0f);
        maxTextWidth = textWidth;



    }

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font, String[] listActions){
        this(batch, viewport, font);
        setListActions(new Array<String>(listActions) );
        updateYPositions();
        updateXPositions();
    }

    public DefaultWindow(SpriteBatch batch, Viewport viewport, BitmapFont font, Vector2 winSize){
        this(batch, viewport, font);
        this.winSize = winSize;
    }

    public void setWinSize(Vector2 winSize){
        this.winSize = winSize;

    }

    public void setSelectButton(String selectButton) {
        this.selectButton = selectButton;
    }

    public void toggleAction(int index){
        toggled.set(index, !toggled.get(index));
    }

    public void setXYspread(float xspread, float yspread, boolean updatePosition) {
        Xspread = xspread;
        Yspread = yspread;
        if(updatePosition) {
            updateXPositions();
            updateYPositions();
            updateWinSize();
        }
    }



    public void updateXPositions(){ // to call when using XSpread of the text, after any change of the text,
        // this will cancel centerXPosition
        float cumulated = 0;
        maxTextWidth = 0;
        cumulatedTextWidth = new Array<Float>(listActions.size);
        for (String listAction : listActions) {
            cumulatedTextWidth.add(cumulated);
            gLayout.setText(font, listAction);
            cumulated += (gLayout.width + Xspacing)*Xspread;
            if (maxTextWidth < gLayout.width) maxTextWidth = gLayout.width;
        }
        gLayout.setText(font, listActions.get(0));
    }

    public void centerXPositions(){ // make the text centred, call updateXPositions() to cancel
        for (int i = 0; i < listActions.size ; i++) {
            gLayout.setText(font, listActions.get(i));
            cumulatedTextWidth.set(i, cumulatedTextWidth.get(i) - gLayout.width/2 + maxTextWidth/2 );
        }
        gLayout.setText(font, listActions.get(0));
    }

    public void updateYPositions(){ // to call when using YSpread of the text, after any change of the text,
        float cumulated = 0;
        cumulatedTextHeight = new Array<Float>(listActions.size);
        for (String listAction : listActions) {
            cumulatedTextHeight.add(cumulated);
            gLayout.setText(font, listAction);
            cumulated += (gLayout.height + Yspacing)*Yspread;
        }
        //cumulatedTextHeight.reverse(); // remember the y axis is reversed
        gLayout.setText(font, listActions.get(0));
    }

    public void updateWinSize(){ // to call when using changing Yspread or Xspread,
        // minimum one column, then add one column by action if we want to spread on X axis
        winSize.x = ((listActions.size - 1) * Xspread + 1) * (maxTextWidth + Xspacing) + 2*xmargin;
        gLayout.setText(font, titleText);
        float titleHeight = gLayout.height;
        winSize.y = ((listActions.size - 1) * Yspread + 1) * (textHeight + Yspacing) + 2.1f*ymargin - titleYOffset + titleHeight;
        gLayout.setText(font, listActions.get(0));
        winTopLeft = new Vector2(winPos.x, winPos.y + winSize.y);
        winCenter = new Vector2(winPos.x+ winSize.x/2.0f, winPos.y + winSize.y/2.0f);
    }

    public void centerWindow(){
        winPos = new Vector2( (viewport.getWorldWidth()-winSize.x)/2f, (viewport.getWorldHeight()-winSize.y)/2f);
        winTopLeft = new Vector2(winPos.x+110, winPos.y + winSize.y);
        winCenter = new Vector2(winPos.x+ winSize.x/2.0f, winPos.y + winSize.y/2.0f);
    }

    public void setListActions(Array<String> listActions) {
        this.listActions = new Array<String>(listActions);
        updateYPositions();
        updateXPositions();
        resizeToogleList();
    }

    public void addActionToList(String string){
        this.listActions.add(string);
        updateYPositions();
        updateXPositions();
        resizeToogleList();
    }

    public void resizeToogleList(){
        this.toggled = new BooleanArray(this.listActions.size);
        for (int i = 0; i < listActions.size; i++) {
            this.toggled.add(false);
        }

    }

    public Array<String> getListActions() {
        return listActions;
    }

    @Override
    public void update(float deltaTime) {
         if (selectionCoolDown > 0)
            selectionCoolDown -= 5.0 * deltaTime;
        else
            selectionCoolDown = 0;
        if (inputProfile != null && !isClosed()) {
            currentMovingVector = inputProfile.getMovingVector();
            //gameScreen.setDebugText("7887"+currentMovingVector);
            processWindowMovingInput();
            processSelectInput(inputProfile.getButtonState(selectButton));
            processBackInput(inputProfile.getButtonState("Back"));
            processPauseInput(inputProfile.getButtonState("Start"));

        }
        if (closingRequested == true) {
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
        font.draw(batch, gLayout, winCenter.x - gLayout.width/2.0f, winTopLeft.y-titleYOffset ); // 110 if the border size
        for (int i = 0; i < listActions.size; i++) {
            if (i == selectedAction) {
                gLayout.setText(font, colorSelected+listActions.get(i)+"[]");
                cursor.draw(batch,
                        winTopLeft.x + cursor_offset - textHeight + xmargin + cumulatedTextWidth.get(i) * Xspread,
                        winTopLeft.y - ymargin - cumulatedTextHeight.get(i) - textHeight, textHeight, textHeight);
            } else if(toggled.get(i) == true){
                gLayout.setText(font, colorToggled+listActions.get(i)+"[]");
            } else {
                gLayout.setText(font, "[WHITE]"+listActions.get(i)+"[]");
            }
            font.draw(batch, gLayout,
                    winTopLeft.x + xmargin + cumulatedTextWidth.get(i),
                    winTopLeft.y - ymargin - cumulatedTextHeight.get(i) ); // y goes down

        }
        gLayout.setText(font, messageText);
        font.draw(batch, gLayout, posMessage.x, posMessage.y); // 110 if the border size
        font.getData().markupEnabled = false;
        for (DefaultWindow child:children) {
            child.render(delta);
        }

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
        Gdx.app.debug("DefaultWindow", "select "+selectedAction);
    }

    @Override
    public void selectPreviousAction() {
        selectedAction = (selectedAction + 1) % listActions.size;
        Gdx.app.debug("DefaultWindow", "select "+selectedAction);
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
        if (this.inputProfile != null) this.inputProfile.setContext("Menu");
        this.closed = false;
    }
    @Override
    public void callBackWindow(){
        this.closed = false;
        this.selectionCoolDown = 1;
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
                inputProfile = null;
                this.closed = true;
            }
        }
        else {
            previousWindow.removeChild(this);
        }

    }

    public void requestClosing(){
        closingRequested = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void processWindowMovingInput() {
        float direction = (isYAxisSelection) ? currentMovingVector.y : currentMovingVector.x;
        if (direction > 0.5 && selectionCoolDown == 0) {
            selectionCoolDown = 1;
            this.selectNextAction();
            //gameScreen.setDebugText("UP");
        }
        else if (direction < -0.5 && selectionCoolDown == 0){
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
            selectionCoolDown=1;
            requestClosing();
        }
    }
    protected void processPauseInput(boolean isPressed) {
        if (isPressed && selectionCoolDown == 0) {
            selectionCoolDown = 1;
            requestClosing();
        }
    }


    public void addChild(DefaultWindow window){
        children.add(window);
        window.openWindow(this.inputProfile, this);
        closed = true;
    }

    public void removeChild(DefaultWindow window){
        window.inputProfile = null;
        window.closed = true;
        children.removeValue(window, true);
        if (children.size < 1){
            this.callBackWindow();
        }
    }

    public void removeLastChild(){
        DefaultWindow window = children.pop();
        window.inputProfile = null;
        window.closed = true;
        if (children.size < 1){
            this.callBackWindow();
        }
    }


}
