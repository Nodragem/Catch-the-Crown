package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Player;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 12/04/2015.
 */
public class ScoreTableWindow implements Window {
    private final NinePatch winTexture;
    private final BitmapFont font;
    private final GameScreen gameScreen;
    private final Array<String> scoreText;
    private final float columnStepY;
    private final float columnDistance;
    private Viewport viewport;
    private final Vector2 winSize;
    private final Vector2 winPos;
    private final Vector2 winTopLeft;
    private Array<Float> columns_size;
    private TiledDrawable background;
    private Player currentPlayer;
    private ParticleEffect[] effects;
    private ArrayMap<String, Player> players;
    private Animation win_pre_anim;
    private Animation win_jump_anim;
    private Animation win_post_anim;
    private int winnerIndex;
    private float winner_column_size;
    private float numberOfWinners;
    private String resultAnnouncement;
    private float timer;
    private Array<Animation> loser_anim = new Array<Animation>(4);
    private NinePatch columnNinePatch;
    private Array<Label> scoreLabels = new Array<Label>(4);
    private float jumpY;

    public ScoreTableWindow(GameScreen gameScreen, Viewport viewport, BitmapFont font, ArrayMap<String, Player> players, ArrayMap<String, Integer> victoryTable, ArrayMap<String, Integer> scoreTable, ArrayMap<String, Integer> rankTable){
        this.font = font;
        this.players = players;
        this.gameScreen = gameScreen;
        this.viewport = viewport;
        Pixmap pixmap  = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.2f, 0.2f, 0.95f);
        pixmap.fillRectangle(0, 0, 32, 32);
        background = new TiledDrawable(new TextureRegion(new Texture(pixmap)));
        columnNinePatch = GameObject.atlas.createPatch("column");
        columnNinePatch.scale(4.0f, 4.0f);
        winSize = new Vector2(viewport.getWorldWidth()-300, viewport.getWorldHeight()-200);
        winPos = new Vector2( (viewport.getWorldWidth()-winSize.x)/2f, (viewport.getWorldHeight()-winSize.y)/2f);
        winTopLeft = new Vector2(winPos.x+110, winPos.y + winSize.y);
        winTexture = GameObject.atlas.createPatch("paper_lance");
        winTexture.scale(4.0f, 4.0f);
        columns_size = new Array<Float>();
        columnDistance = (1f/players.size)*(winSize.x - 2*110f);
        winnerIndex = -1;
        timer = 0;
        numberOfWinners = gameScreen.getNumberOfWinners(scoreTable); // we should just have one winner normally, but a draw between two or more players is possible!

        //debug:
       // winnerIndex = 2;
        //numberOfWinners =1;

        Array<TextureAtlas.AtlasRegion> regions = null;
        Gdx.app.debug("Table Of Score:", ""+numberOfWinners);
        resultAnnouncement = "Seems We got a Draw!";
        if(numberOfWinners == 1) {
            String winnerName = scoreTable.getKeyAt(0);
            winnerIndex = players.indexOfKey(winnerName);
            if(gameScreen.getStateGame() == Constants.GAME_STATE.TOURNAMENT_END)
                resultAnnouncement = winnerName + " is the KING OF THE TOURNAMENT !!";
            else
                resultAnnouncement = winnerName + " wins !!";
            regions =        GameObject.atlas.findRegions("Piaf_"+players.get(winnerName).getCharacter().color_texture+"_win_jump");
            win_jump_anim = new Animation(1.0f/4.0f, regions, Animation.PlayMode.NORMAL);
            regions =        GameObject.atlas.findRegions("Piaf_"+players.get(winnerName).getCharacter().color_texture+"_win_pre");
            win_pre_anim = new Animation(1.0f/6.0f, regions, Animation.PlayMode.LOOP_REVERSED);
            regions =        GameObject.atlas.findRegions("Piaf_"+players.get(winnerName).getCharacter().color_texture+"_win_post");
            win_post_anim = new Animation(1.0f/6.0f, regions, Animation.PlayMode.REVERSED);
        }
        for (int i = 0; i < players.size; i++) {
            regions = GameObject.atlas.findRegions("Piaf_" + players.getValueAt(i).getCharacter().color_texture + "_loosing");
            loser_anim.add(new Animation(1.0f / 2.0f, regions, Animation.PlayMode.LOOP));
        }
        scoreText = new Array<String>(4);
        columnStepY =  (400.0f / gameScreen.getVictoryThreshold());
        for (int i = 0; i < players.size; i++) {
            String playerName = players.getKeyAt(i);
            scoreText.add("[#9E5D41]" + rankTable.get(playerName) + "-" + String.format(playerName + "\n   [#C4B78F]" + scoreTable.get(playerName) + "\n[]"));
            columns_size.add(100 + victoryTable.getValueAt(i) * columnStepY);
        }
        if(winnerIndex != -1) {
            winner_column_size = 100 + ( (victoryTable.getValueAt(winnerIndex) ) / gameScreen.getVictoryThreshold()) * 400f;
        }

        effects = new ParticleEffect[2];
        boolean debug = false;
        if (gameScreen.getStateGame() == Constants.GAME_STATE.TOURNAMENT_END | debug){
            effects[0] = new ParticleEffect();
            effects[0].load(Gdx.files.internal("particles/confetti.particles"), GameObject.atlas);
            effects[0].setPosition(getCharacterPositionX(winnerIndex)+50, getCharacterPositionY(winnerIndex) + 200);
            effects[0].scaleEffect(4f);
            effects[0].start();
            //int indexGoldSmith = gameScreen.getMaxIndex(gameScreen.finalScoreTable);
            int indexGoldSmith = 3;
            if (indexGoldSmith != winnerIndex){
                effects[1] = new ParticleEffect();
                effects[1].load(Gdx.files.internal("particles/money.particles"), GameObject.atlas);
                effects[1].setPosition(getCharacterPositionX(indexGoldSmith)-0f, getCharacterPositionY(indexGoldSmith) + 1750);
                effects[1].scaleEffect(4f);
                effects[1].start();
            }
        }

    }
    
    public void render(SpriteBatch batch){
        timer += Gdx.graphics.getDeltaTime();
        background.draw(batch, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        winTexture.draw(batch, winPos.x, winPos.y, winSize.x, winSize.y);
        //BitmapFont.TextBounds bound = font.getBounds("Score Table");
        font.setColor(1, 0, 0, 1);
        font.draw(batch, "Score Table", (winSize.x) / 2, winTopLeft.y);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, resultAnnouncement, winTopLeft.x, winTopLeft.y - 110); // 110 if the border size
        font.getData().markupEnabled = true;

        for (int i = 0; i < effects.length; i++) {
          if (effects[i] != null) {
            effects[i].draw(batch, Gdx.graphics.getDeltaTime());
            if (effects[i].isComplete())
                effects[i].reset();
          }
        }

        TextureRegion region = null;
        for (int i = 0; i < players.size; i++) {
            if (i == winnerIndex)
                animeWinner(batch, region, i);
            else
                animeLooser(batch, region, i);
            gameScreen.glayout.setText(font, "0-" + players.getKeyAt(i));
            font.draw(batch, scoreText.get(i), winTopLeft.x + ((i + 0.5f) * columnDistance - gameScreen.glayout.width / 2.0f),
                    winTopLeft.y - (2.28f/3f)*winSize.y, gameScreen.glayout.width, Align.right, true );
        }
        font.getData().markupEnabled = false;

    }

    private void animeLooser(SpriteBatch batch, TextureRegion region, int looserIndex) {
        columnNinePatch.draw(batch, getColumnPositionX(looserIndex) ,
                getColumnPositionY(), 150, columns_size.get(looserIndex));

        region=loser_anim.get(looserIndex).getKeyFrame(timer);
        batch.draw(region, getCharacterPositionX(looserIndex),
                getCharacterPositionY(looserIndex), 150, 150);
    }

    private void animeWinner(SpriteBatch batch, TextureRegion region, int winnerIndex){
        float increase = (Math.min(3, timer)/3) *columnStepY; // increase of one step in 3 seconds
        float jumpDuration = 1.5f;
        float timeJump = timer%jumpDuration ; // repeat
        float timeInAir = jumpDuration - win_post_anim.getAnimationDuration() - win_pre_anim.getAnimationDuration();

        columnNinePatch.draw(batch, getColumnPositionX(winnerIndex),
                getColumnPositionY(), 150, columns_size.get(winnerIndex) + increase);

        if(!win_pre_anim.isAnimationFinished(timeJump)){
            jumpY = 0;
            region = win_pre_anim.getKeyFrame(timer);
        }
        else if(win_post_anim.isAnimationFinished(jumpDuration -timeJump)){
            float local_time = timeJump-win_pre_anim.getAnimationDuration();
            region = win_jump_anim.getKeyFrame(local_time);
            jumpY = 30 * (1+MathUtils.sin(MathUtils.PI * local_time/(timeInAir)));
        }
        else {
            jumpY = 0;
            region = win_post_anim.getKeyFrame(jumpDuration -timeJump);
        }
        batch.draw(region, getCharacterPositionX(winnerIndex),
                getCharacterPositionY(winnerIndex) + increase + jumpY, 150, 150 );

    }

    private float getCharacterPositionX(int indexColumn){
        return getColumnPositionX(indexColumn);
    }

    private float getCharacterPositionY(int indexColumn){
        return getColumnPositionY() + columns_size.get(indexColumn) -70;
    }

    private float getColumnPositionX(int indexColumn){
        return winTopLeft.x + ((indexColumn + 0.5f) * columnDistance - 150 / 2.0f);
    }

    private float getColumnPositionY(){
        return winTopLeft.y - (2.2f / 3f) * winSize.y;
    }

    public void dispose(){
        for (ParticleEffect effect : effects) {
            if (effect != null)
                effect.dispose();
        }
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public boolean executeSelectedAction() {
        return false;
    }

    @Override
    public void selectNextAction() {

    }

    @Override
    public void selectPreviousAction() {

    }

    @Override
    public void selectAction(int index) {

    }

    @Override
    public void openWindow(int iplayer) {

    }

    @Override
    public int getActivePlayer() {
        return 0;
    }

    @Override
    public void closeWindow() {

    }
}
