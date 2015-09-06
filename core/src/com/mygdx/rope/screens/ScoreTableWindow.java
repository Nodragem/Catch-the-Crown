package com.mygdx.rope.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
public class ScoreTableWindow extends DefaultWindow {

    private final GameScreenTournament gameScreen;
    private Array<String> scoreText;
    private float columnStepY;
    private float columnDistance;
    private Array<Float> columns_size;
    private ParticleEffect[] effects;
    private ArrayMap<String, Player> players;
    private Animation win_pre_anim;
    private Animation win_jump_anim;
    private Animation win_post_anim;
    private int winnerIndex;
    private float numberOfWinners;
    private String resultAnnouncement;
    private float timer;
    private Array<Animation> loser_anim = new Array<Animation>(4);
    private Array<Label> scoreLabels = new Array<Label>(4);
    private float jumpY;

    public ScoreTableWindow(GameScreenTournament gameScreen, Viewport viewport, BitmapFont font) {
        super(gameScreen.getBatch(), viewport, font, new String[] {"Stop Tournament","Next Turn"});
        Yspread = 0;
        Xspread = 1;
        Xspacing = 200f;
        updateYPositions();
        setWinSize(new Vector2(viewport.getWorldWidth() - 300, viewport.getWorldHeight() - 200));
        centerWindow();
        this.selectedAction =1;
        this.gameScreen = gameScreen;
        this.titleText = "Score Table";

        titleYOffset = 110;
        colorTitle = "[RED]";
        columns_size = new Array<Float>();
        columnDistance = (1f / 2f) * (winSize.x - 2 * 110f); // default value
        winnerIndex = -1;
        timer = 0;
        players = new ArrayMap<String, Player>(0);

    }

    public void updateScore(ArrayMap<String, Integer> victoryTable, ArrayMap<String, Integer> scoreTable, ArrayMap<String, Integer> rankTable) {
        this.selectedAction =1;
        initWinLoseAnimation(scoreTable);
        scoreText = new Array<String>(4);
        columnStepY = (400.0f / gameScreen.getVictoryThreshold());
        for (int i = 0; i < players.size; i++) {
            String playerName = players.getKeyAt(i);
            scoreText.add("[#9E5D41]" + rankTable.get(playerName) + "-" + String.format(playerName + "\n   [#C4B78F]" + scoreTable.get(playerName) + "\n[]"));
            columns_size.add(100 + victoryTable.getValueAt(i) * columnStepY);
        }
        initParticleEffect();
    }

    public void setPlayerList(ArrayMap<String, Player> players){
        this.players = players;
        this.columnDistance = (1f / players.size) * (winSize.x - 2 * 110f);
    }

    private void initWinLoseAnimation(ArrayMap<String, Integer> scoreTable) {
        //debug:
        // winnerIndex = 2;
        //numberOfWinners =1;
        numberOfWinners = gameScreen.getNumberOfWinners(scoreTable); // we should just have one winner normally, but a draw between two or more players is possible!
        Array<TextureAtlas.AtlasRegion> regions = null;
        Gdx.app.debug("Table Of Score:", "" + numberOfWinners);
        resultAnnouncement = "Seems We got a Draw!";
        if (numberOfWinners == 1) {
            String winnerName = scoreTable.getKeyAt(0);
            winnerIndex = players.indexOfKey(winnerName);
            if (gameScreen.getStateGame() == Constants.GAME_STATE.TOURNAMENT_END)
                resultAnnouncement = winnerName + " is the KING OF THE TOURNAMENT !!";
            else
                resultAnnouncement = winnerName + " wins !!";
            regions = GameObject.atlas.findRegions("Piaf_" + players.get(winnerName).getCharacter().color_texture + "_win_jump");
            win_jump_anim = new Animation(1.0f / 4.0f, regions, Animation.PlayMode.NORMAL);
            regions = GameObject.atlas.findRegions("Piaf_" + players.get(winnerName).getCharacter().color_texture + "_win_pre");
            win_pre_anim = new Animation(1.0f / 6.0f, regions, Animation.PlayMode.LOOP_REVERSED);
            regions = GameObject.atlas.findRegions("Piaf_" + players.get(winnerName).getCharacter().color_texture + "_win_post");
            win_post_anim = new Animation(1.0f / 6.0f, regions, Animation.PlayMode.REVERSED);
        }
        for (int i = 0; i < players.size; i++) {
            regions = GameObject.atlas.findRegions("Piaf_" + players.getValueAt(i).getCharacter().color_texture + "_loosing");
            loser_anim.add(new Animation(1.0f / 2.0f, regions, Animation.PlayMode.LOOP));
        }
    }

    @Override
    public void render(float delta) {
        timer += delta;
        super.render(delta);
        font.draw(batch, resultAnnouncement, winTopLeft.x, winTopLeft.y - 110); // 110 if the border size

        font.getData().markupEnabled = true;
        TextureRegion region = null;
        for (int i = 0; i < players.size; i++) {
            if (i == winnerIndex)
                animeWinner(batch, region, i);
            else
                animeLooser(batch, region, i);
            gLayout.setText(font, "0-" + players.getKeyAt(i));
            font.draw(batch, scoreText.get(i), winTopLeft.x + ((i + 0.5f) * columnDistance - gLayout.width / 2.0f),
                    winTopLeft.y - (2.28f / 3f) * winSize.y, gLayout.width, Align.right, true);
        }
        font.getData().markupEnabled = false;

        renderParticleEffect(batch);
    }


    private void animeLooser(SpriteBatch batch, TextureRegion region, int looserIndex) {
        columnNinePatch.draw(batch, getColumnPositionX(looserIndex),
                getColumnPositionY(), 150, columns_size.get(looserIndex));

        region = loser_anim.get(looserIndex).getKeyFrame(timer);
        batch.draw(region, getCharacterPositionX(looserIndex),
                getCharacterPositionY(looserIndex), 150, 150);
    }

    private void animeWinner(SpriteBatch batch, TextureRegion region, int winnerIndex) {
        float increase = (Math.min(3, timer) / 3) * columnStepY; // increase of one step in 3 seconds
        float jumpDuration = 1.5f;
        float timeJump = timer % jumpDuration; // repeat
        float timeInAir = jumpDuration - win_post_anim.getAnimationDuration() - win_pre_anim.getAnimationDuration();

        columnNinePatch.draw(batch, getColumnPositionX(winnerIndex),
                getColumnPositionY(), 150, columns_size.get(winnerIndex) + increase);

        if (!win_pre_anim.isAnimationFinished(timeJump)) {
            jumpY = 0;
            region = win_pre_anim.getKeyFrame(timer);
        } else if (win_post_anim.isAnimationFinished(jumpDuration - timeJump)) {
            float local_time = timeJump - win_pre_anim.getAnimationDuration();
            region = win_jump_anim.getKeyFrame(local_time);
            jumpY = 30 * (1 + MathUtils.sin(MathUtils.PI * local_time / (timeInAir)));
        } else {
            jumpY = 0;
            region = win_post_anim.getKeyFrame(jumpDuration - timeJump);
        }
        batch.draw(region, getCharacterPositionX(winnerIndex),
                getCharacterPositionY(winnerIndex) + increase + jumpY, 150, 150);

    }

    private void initParticleEffect() {
        effects = new ParticleEffect[2];
        boolean debug = false;
        if ((gameScreen != null && gameScreen.getStateGame() == Constants.GAME_STATE.TOURNAMENT_END) | debug) {
            effects[0] = new ParticleEffect();
            effects[0].load(Gdx.files.internal("particles/confetti.particles"), GameObject.atlas);
            effects[0].setPosition(getCharacterPositionX(winnerIndex) + 50, getCharacterPositionY(winnerIndex) + 200);
            effects[0].scaleEffect(4f);
            effects[0].start();
            int indexGoldSmith = gameScreen.getMaxIndex(gameScreen.totalScoreTable);
            //int indexGoldSmith = 3;
            if (indexGoldSmith != winnerIndex) {
                effects[1] = new ParticleEffect();
                effects[1].load(Gdx.files.internal("particles/money.particles"), GameObject.atlas);
                effects[1].setPosition(getCharacterPositionX(indexGoldSmith) - 0f, getCharacterPositionY(indexGoldSmith) + 1750);
                effects[1].scaleEffect(4f);
                effects[1].start();
            }
        }
    }

    private void renderParticleEffect(SpriteBatch batch) {
        for (int i = 0; i < effects.length; i++) {
            if (effects[i] != null) {
                effects[i].draw(batch, Gdx.graphics.getDeltaTime());
                if (effects[i].isComplete())
                    effects[i].reset();
            }
        }
    }

    private float getCharacterPositionX(int indexColumn) {
        return getColumnPositionX(indexColumn);
    }

    private float getCharacterPositionY(int indexColumn) {
        return getColumnPositionY() + columns_size.get(indexColumn) - 70;
    }

    private float getColumnPositionX(int indexColumn) {
        return winTopLeft.x + ((indexColumn + 0.5f) * columnDistance - 150 / 2.0f);
    }

    private float getColumnPositionY() {
        return winTopLeft.y - (2.2f / 3f) * winSize.y;
    }

    public void dispose() {
        if(effects != null) {
            for (ParticleEffect effect : effects) {
                if (effect != null)
                    effect.dispose();
            }
        }
    }

    @Override
    public boolean executeSelectedAction() {
        switch(selectedAction){
            case 0:
                requestClosing();
                break;
            case 1:
                gameScreen.toNextLevel();
                break;

        }
        return false;
    }


}

