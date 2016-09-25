package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.Renderable;
import com.mygdx.rope.objects.Updatable;
import com.mygdx.rope.screens.GameScreenTournament;

import static com.badlogic.gdx.controllers.ControlType.button;

/**
 * Created by Geoffrey on 24/09/2016.
 */
public class actionProgressBar implements Updatable, Renderable {
    private final NinePatch progressBarEmpty;
    private final NinePatch progressBarFull;
    private final GameScreenTournament gameScreen;
    private final GameObject parent;
    private final ObjectMap <String,Animation> buttons;
    private float y_offset;
    private Animation currentButton;
    private float progress_ratio;
    private int increment;
    private int maxProgress;
    private float currentProgess;
    private Vector2 position;
    private float state_time;
    private TextureRegion regionToDraw;
    private boolean isActive;

    public actionProgressBar(GameScreenTournament game, GameObject obj) {
        this.gameScreen = game;
        this.parent = obj;
        this.y_offset = 0;
        this.maxProgress = 1;
        this.currentProgess = 0;
        this.increment = 0;
        this.gameScreen.getObjectsToRender().add(this);
        this.gameScreen.getObjectsToUpdate().add(this);
        progressBarEmpty = GameObject.atlas.createPatch("progressbar_small_empty");
        progressBarEmpty.scale(1/32f, 1/32f);
        progressBarFull = GameObject.atlas.createPatch("progressbar_small_full");
        progressBarFull.scale(1/32f, 1/32f);
        buttons = new ObjectMap<String, Animation>(4);
        buttons.put("A", new Animation(0.2f, GameObject.atlas.findRegions("buttons_A"), Animation.PlayMode.LOOP));
        buttons.put("B", new Animation(0.2f, GameObject.atlas.findRegions("buttons_B"), Animation.PlayMode.LOOP));
        buttons.put("X", new Animation(0.2f, GameObject.atlas.findRegions("buttons_X"), Animation.PlayMode.LOOP));
        buttons.put("Y", new Animation(0.2f, GameObject.atlas.findRegions("buttons_Y"), Animation.PlayMode.LOOP));
        currentButton = null;
    }

    public boolean update(float timeDelta){
        if(isActive) {
            state_time += timeDelta;
            progress_ratio = MathUtils.clamp(currentProgess / maxProgress, 0, 1);
            gameScreen.addDebugText("\nProgress Bar, " + progress_ratio * 100 + "% \n");
            position = new Vector2(parent.position).add(-0.5f, y_offset);
        }
        if (currentProgess >= maxProgress)
            reset();
        return parent.checkIfToDestroy();
    }

    public void reset() {
        isActive = false;
        currentProgess = 0;
    }

    public void increment(){
        currentProgess += increment;
    }

    public void startProgressBar(int maxProgress, int increment, String button, float y_offset){
        this.isActive = true;
        this.y_offset = y_offset;
        this.maxProgress = maxProgress;
        this.currentProgess = 0;
        this.increment = increment;
        this.currentButton = buttons.get(button);
    }

    public void render(SpriteBatch batch) {
        if(isActive) {
            batch.setColor(1f, 1-progress_ratio, 0, 1f);
            if(progress_ratio>0)
                progressBarFull.draw(batch, position.x + 0.4f, position.y, 1 * progress_ratio, 0.3f);
            batch.setColor(1, 1, 1, 1);
            progressBarEmpty.draw(batch, position.x + 0.4f, position.y, 1, 0.3f);
            regionToDraw = currentButton.getKeyFrame(state_time);
            batch.draw(regionToDraw.getTexture(), // we called this long draw() method just to get access the flip argument...
                    position.x, position.y,
                    0, 0, // origins
                    0.5f, 0.5f, // dimension
                    1, 1, // scale
                    0, // rotation
                    regionToDraw.getRegionX(), regionToDraw.getRegionY(),
                    regionToDraw.getRegionWidth(), regionToDraw.getRegionHeight(),
                    false, false); // flip hor / vertical
        }

    }
}
