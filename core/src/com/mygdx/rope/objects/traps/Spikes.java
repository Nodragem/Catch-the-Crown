package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;

/**
 * Created by Nodragem on 22/06/2014.
 */
public class Spikes extends GameObject implements Triggerable {
    //private final float spikesRotation;
    private  float intervalOFF;
    private  float intervalON;
    private float timer;
    private Constants.TRAPSTATE trapstate;
    /* we have here a game object which is always of one units height, and repeat its textures of one units on the width axis */
    private boolean isVertical;
    private boolean defaultON;

    public Spikes(GameScreenTournament game, Vector2 position, Vector2 dimension, float rotation, String name_texture, float intervalON, float intervalOFF, boolean defaultON) {
        // instead of Overriding I could just do a simple GameObject Factory which
        // would use the GameObject constructor to give the Fixture, the filter and the animation definition to GameObject.
        // Further more, the Factory could also set up the Body Type and give an animation of switching off or on.
        // The only thing missing to GameObject is float "damage", which if it is > 0 is called and removed from the other object --> We tried to do something
        super(game, position, dimension, rotation , name_texture);
        Gdx.app.debug("Spikes", "Rotation parameter: " + rotation + "; mainBoxContact: " + mainBoxContact);
        this.defaultON = defaultON;
        this.intervalOFF = intervalOFF;
        this.intervalON = intervalON;
        timer = 0f;
        this.rotation = rotation;
        givenDamage = 110;
    }

    @Override
    public boolean update(float deltaTime){
        if (trapstate == Constants.TRAPSTATE.ON && intervalOFF != 0){
            timer +=deltaTime;
            if (activeState == Constants.ACTIVE_STATE.DESACTIVATED && timer > intervalOFF){
                goToActivation();
                timer = 0f;
            }
            else if(activeState == Constants.ACTIVE_STATE.ACTIVATED && timer > intervalON){
                goToDesactivation();
                timer = 0f;
            }
        }
        return super.update(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        if(isVisible && activeState != Constants.ACTIVE_STATE.DESACTIVATED) {
            TextureRegion reg = null;
            reg = current_animation.getKeyFrame(stateTime);
            for (int i = 0; i < dimension.x; i++) {
                batch.draw(reg, position.x + i*MathUtils.cos(rotation), position.y + i*MathUtils.sin(rotation),
                        0.0f, 0.0f, // origins
                        1, 1, 1, 1, // dimension and scale
                        rotation * MathUtils.radiansToDegrees
                );
            }
        }
    }

    @Override
    public void reset() {
        setActivate(defaultON);
    }

    @Override
    public boolean isActiveByDefault() {
        return defaultON;
    }

    @Override
    public void triggerONActions(HubInterface hub) {
        Gdx.app.debug("Spikes", "GoToActivation()");
        trapstate = Constants.TRAPSTATE.ON;
        goToActivation();
    }

    @Override
    public void triggerOFFActions(HubInterface hub) {
        trapstate = Constants.TRAPSTATE.OFF;
        goToDesactivation();
    }
}
