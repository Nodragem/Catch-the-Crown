package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Player;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.InputHandler.InputProfile;

/**
 * Created by Geoffrey on 30/03/2015.
 */
public class GUILayer {

    private final BitmapFont font_reverse;
    public PauseWindow pauseWindow;
    private boolean wasPressedPause;
    private Viewport GUIViewport;
    private Constants.GUI_STATE GUIstate;
    private final NinePatch progressBarEmpty;
    private final NinePatch progressBarFull;
    private GameScreenTournament gameScreen;
    private ArrayMap<String, Player> players;
    private String timerFormatted;
    private BitmapFont font;
    private BitmapFont fontTimer;
    private OrthographicCamera cameraUI;
    private Array<Vector2> playersUIBox;
    private float ySizePlayerBox = 150;
    private float xSizePlayerBox = 520;
    private NinePatch panelBoxTexture;
    private TextureRegion crownGUI;
    private TextureRegion playerChestGUI;
    private TextureRegion groupChestGUI;
    private TextureRegion clockGUI;
    private TextureRegion emptyHeart;
    private TextureRegion halfHeart;
    private TextureRegion fullHeart;
    private Array<TextureRegion> playersHeadGUI;
    private Player currentPlayer;
    private float middlePanelPosX;
    private float middlePanelPosY;
    private float offset_clock_y;
    private float offset_clock_x;
    private float progress_ratio;
    public String debugText;
    private ScoreTableWindow scoreTableWindow;
    private Constants.GUI_STATE previousGUIState;
    private SpriteBatch batch;


    public GUILayer(GameScreenTournament gameScreen){
        batch = gameScreen.getBatch();
        GUIstate = Constants.GUI_STATE.DISPLAY_GUI;
        this.gameScreen = gameScreen;
        players = new ArrayMap <String, Player>(4); //  empty
        cameraUI = new OrthographicCamera();
        cameraUI.setToOrtho(false, Constants.VIEWPORT_GUI_WIDTH, Constants.VIEWPORT_GUI_HEIGHT);
        cameraUI.update();
        GUIViewport = new FitViewport(Constants.VIEWPORT_GUI_WIDTH, Constants.VIEWPORT_GUI_HEIGHT, cameraUI);
        GUIViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font = new BitmapFont(Gdx.files.internal("fonts/try64x64.fnt"), false);
        font.getData().setScale(0.90f);
        font_reverse = new BitmapFont(Gdx.files.internal("fonts/try64x64.fnt"), false);
        font_reverse.getData().setScale(0.90f);
        fontTimer = new BitmapFont(Gdx.files.internal("fonts/fonttimer.fnt"), false);
        playersUIBox = new Array<Vector2>(4);
        middlePanelPosX =  Constants.VIEWPORT_GUI_WIDTH/2-xSizePlayerBox/2;
        middlePanelPosY =  Constants.VIEWPORT_GUI_HEIGHT-ySizePlayerBox+70;
        playersHeadGUI = new Array<TextureRegion>(4);
        crownGUI = GameObject.atlas.findRegion("crown_small");
        playerChestGUI = GameObject.atlas.findRegion("silver_chest");
        groupChestGUI = GameObject.atlas.findRegion("gold_chest");
        clockGUI = GameObject.atlas.findRegion("clock");
        emptyHeart = GameObject.atlas.findRegion("heart_empty");
        halfHeart = GameObject.atlas.findRegion("heart_half");
        fullHeart = GameObject.atlas.findRegion("heart_full");
        panelBoxTexture = GameObject.atlas.createPatch("wood");

        progressBarEmpty = GameObject.atlas.createPatch("progressbar_small_empty");
        progressBarEmpty.scale(4.0f, 4.0f);
        progressBarFull = GameObject.atlas.createPatch("progressbar_small_full");
        progressBarFull.scale(4.0f, 4.0f);
        //updatePlayerList(players);
        scoreTableWindow = null; // will be created when we know the score!
        pauseWindow = new PauseWindow(gameScreen, GUIViewport, font);
        scoreTableWindow = new ScoreTableWindow(gameScreen, GUIViewport, font);

    }

    public Viewport getGUIViewport() {
        return GUIViewport;
    }

    public void setGUIstate(Constants.GUI_STATE GUIstate) {
        if(this.GUIstate != GUIstate) {
            this.previousGUIState = this.GUIstate;
            this.GUIstate = GUIstate;
        }
    }


    public void renderUI(float deltaTime, boolean DEBUG_MODE) {
        GUIViewport.apply();
        batch.setProjectionMatrix(cameraUI.combined);
        batch.begin();
        displayHUD(batch);
//        if (previousGUIState != GUIstate){ // on change
//            switch (GUIstate) {
//                case DISPLAY_PAUSE:
//                    pauseWindow.openWindow(gameScreen.getIndexPauser());
//                    break;
//                case DISPLAY_END:
//                    if(previousGUIState == Constants.GUI_STATE.DISPLAY_PAUSE)
//                        pauseWindow.closeWindow();
//                    break;
//                case DISPLAY_GUI:
//                    if(previousGUIState == Constants.GUI_STATE.DISPLAY_PAUSE)
//                        pauseWindow.closeWindow();
//                    break;
//            }
//        }
        switch (GUIstate) {
            case DISPLAY_PAUSE:
                if (pauseWindow != null) {
                    //pauseWindow.update(deltaTime);
                    pauseWindow.render(deltaTime);
                }
                break;
            case DISPLAY_END:
                if (scoreTableWindow != null) {
                    //scoreTableWindow.update(deltaTime);
                    scoreTableWindow.render(deltaTime);}
                break;
            case DISPLAY_GUI:
                break;
        }
        if(DEBUG_MODE)
            renderDebug(batch);
        batch.end();
    }

    public Window openPauseWindow(InputProfile inputProfile, int icontroller){
        pauseWindow.openWindow(inputProfile, gameScreen, icontroller);
        return pauseWindow;
    }

    private void displayHUD(SpriteBatch batch){
        timerFormatted = getFormatedTime(gameScreen.timer);
        //font.drawMultiLine(batch, gameScreen.getDebugText(), Constants.VIEWPORT_GUI_WIDTH / 2, Constants.VIEWPORT_GUI_HEIGHT / 2);
        batch.setColor(0.7f,0.7f,0.7f,1f);
        // ##  The Middle Top and Middle Bottom Panel
        // # The Top Panel
        panelBoxTexture.draw(batch, middlePanelPosX, middlePanelPosY, xSizePlayerBox, ySizePlayerBox);
        batch.setColor(1, 1, 1, 1);
        // # The Clock
        offset_clock_x = 0;
        offset_clock_y = 0;
        if (gameScreen.timer < 30){
            //font.setColor(1, 0, 0, 1);
            offset_clock_x = MathUtils.cos(gameScreen.timer*20);
            offset_clock_y = MathUtils.sin(gameScreen.timer*10);
            font.setColor(1, (offset_clock_y+1)/2, (offset_clock_y+1)/2, 1);
        }

        if(gameScreen.timer < 15){
            //font.setColor(1, seconds%2, seconds%2, 1);
            offset_clock_x = MathUtils.cos(gameScreen.timer*40);
            offset_clock_y = MathUtils.sin(gameScreen.timer*20);
            font.setColor(1, (offset_clock_y+1)/2, (offset_clock_y+1)/2, 1);
        }
        batch.draw(clockGUI, middlePanelPosX+20+3*offset_clock_x, middlePanelPosY + 18-3*offset_clock_y, 55, 55);

        font.draw(batch, timerFormatted, middlePanelPosX+85, middlePanelPosY + 60);
        font.setColor(1, 1, 1, 1);
        // # The Crown
        batch.draw(crownGUI, middlePanelPosX+xSizePlayerBox/2-20, middlePanelPosY + 18, 55, 55);
        font.draw(batch, String.format("%,06d", (int)gameScreen.getCrownGoldValue()), middlePanelPosX+xSizePlayerBox/2 -20 +55+10, middlePanelPosY + 60);

        // ## The Bottom Panel
        batch.setColor(0.7f,0.7f,0.7f,1f);
        panelBoxTexture.draw(batch, middlePanelPosX, -70, xSizePlayerBox, ySizePlayerBox);
        batch.setColor(1, 1, 1, 1);
        batch.draw(groupChestGUI, middlePanelPosX + 22, 4, 55, 55);
        progress_ratio = MathUtils.clamp(gameScreen.getGroupScore() / Constants.GOLDENDLEVEL, 0, 1);
        if (progress_ratio == 1)
            batch.setColor(0f, 1f, 1f, 1f);
        else
            batch.setColor(1f, 0.5f, 0.5f, 1f);
        if ((xSizePlayerBox-130)*progress_ratio > 40)
            progressBarFull.draw(batch, middlePanelPosX + 105, 15, (xSizePlayerBox - 130) * progress_ratio, 30);
        batch.setColor(1, 1, 1, 1);
        progressBarEmpty.draw(batch, middlePanelPosX+105, 15, xSizePlayerBox-130, 30);

        // # Player Boxes
        for (int i = 0; i < players.size; i++) {
            currentPlayer = players.getValueAt(i);
            batch.setColor(0,0,0,0.5f);
            if(i<2)
                panelBoxTexture.draw(batch, playersUIBox.get(i).x, playersUIBox.get(i).y, xSizePlayerBox, ySizePlayerBox);
            else
                panelBoxTexture.draw(batch, playersUIBox.get(i).x, playersUIBox.get(i).y-52, xSizePlayerBox, ySizePlayerBox);
            batch.setColor(0, 0, 0, 0.5f);
            batch.draw(playersHeadGUI.get(i), playersUIBox.get(i).x + 5, playersUIBox.get(i).y + 30, 128, 64);
            batch.setColor(1, 1, 1, 1);
            batch.draw(playersHeadGUI.get(i), playersUIBox.get(i).x, playersUIBox.get(i).y + 22, 128, 64);
            batch.draw(playerChestGUI, playersUIBox.get(i).x+245, playersUIBox.get(i).y+22, 55, 55);
            //String score = customFormat("$###,###.###", 12345.67);
            String score = String.format("%,06d", (int)currentPlayer.getScore());
            if (currentPlayer.hasTheCrown())
                font.setColor(0, 1, 1, 1);
            else if (gameScreen.getWinnerName().equals(currentPlayer.getName()))
                font.setColor(1, 1, 0, 1);
            font.draw(batch, score, playersUIBox.get(i).x + 245 + 55 + 5, playersUIBox.get(i).y + 60);
            font.setColor(1, 1, 1, 1);
            if(currentPlayer.getCharacterLife() > 0) {
                drawHeartLife(batch, currentPlayer, playersUIBox.get(i).x, playersUIBox.get(i).y);
            }
            else{
                font.draw(batch, getFormatedTime(currentPlayer.getCharacter().getRespawnTime()),
                        playersUIBox.get(i).x + 105, playersUIBox.get(i).y + 60);
            }
        }
    }

    private String getFormatedTime(float timeInSecond){
        int seconds = (int) timeInSecond % 60 ;
        int minutes = (int) timeInSecond / 60 ;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void drawHeartLife(SpriteBatch batch, Player p, float x, float y){
        int liferound = Math.round(p.getCharacterLife() / 3.33f);
         // life between 0 and 30,
         // this round up is useful in order to get one heart removed for 34.0f of damage for example (we are kind of a buffer)
        for (int j = 0; j < 3 ; j++) {
            if (liferound >= j*10 + 10){
                batch.draw(fullHeart, x+95 + j*45, y+22, 45, 45);
            }
            else if(liferound >= j*10 + 5){
                batch.draw(halfHeart, x+95 + j*45, y+22, 45, 45);
            }
            else{
                batch.draw(emptyHeart, x+95 + j*45, y+22, 45, 45);
            }
        }
    }

    public void addPlayer(Player player, int cornerIndex){
        float x = 10+(Constants.VIEWPORT_GUI_WIDTH-xSizePlayerBox-15)*(cornerIndex%2);
        float y = cornerIndex>=2?-18:(Constants.VIEWPORT_GUI_HEIGHT-ySizePlayerBox+70);
        playersUIBox.add(new Vector2(x,y));
        playersHeadGUI.add(new TextureRegion(
                player.getCharacter().
                        getAnimation("Standing").
                        getKeyFrame(1),
                0, 0, 32,14));
    }

    public boolean removePlayer(String name){
        for (int i = 0; i < players.size; i++) {
            if (name.equals(players.getKeyAt(i))){
                playersUIBox.removeIndex(i);
                //players.removeIndex(i); // it is not our list, it is not our power to touch this list
                return true;
            }
        }
        return false;
    }

    public void resize(int width, int height){
        GUIViewport.update(width, height);
    }

    public void dispose(){
        font.dispose();
        font_reverse.dispose();
        fontTimer.dispose();
        if (scoreTableWindow != null)
            scoreTableWindow.dispose();
    }

    public void updatePlayerList(ArrayMap<String, Player> playersList) {
        this.players = playersList;
        for (int j = 0; j < players.size; j++) {
            addPlayer(players.getValueAt(j), j);
        }
        this.scoreTableWindow.setPlayerList(players);

    }

    public Window openScoreWindow(ArrayMap<String, Integer> victoryTable, ArrayMap<String, Integer> scoreTable, ArrayMap<String, Integer> rankTable) {
        scoreTableWindow.updateScore(victoryTable, scoreTable, rankTable);
        scoreTableWindow.openWindow((InputProfile) gameScreen.getWinner().getInputProfile(), gameScreen);
        return scoreTableWindow;
    }

    public void renderDebug(SpriteBatch batch) {
        font.draw(batch, debugText, Constants.VIEWPORT_GUI_WIDTH / 2, Constants.VIEWPORT_GUI_HEIGHT / 2);
    }
}
