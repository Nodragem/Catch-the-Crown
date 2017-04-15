package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.Updatable;
import com.mygdx.rope.screens.GameScreenTournament;

/**
* Created by Nodragem on 22/06/2014.
*/
public class SimpleHub implements HubInterface, Updatable {
     //extends GameObject cause it could processInputs in the future
    /* it is kind of an Observable with observers.
    The Hub has a collection of input integrators, from which it retrieves and sums the current values.
     It stores the result in myCurrentValue. If myCurrentValue passes through a certain threshold (myThreshold)
     the SimpleHub will notify all the triggerable objects linked to it.
     */
    protected Array<Triggerable> triggerables; // there are the object which will be appear/desappear on the activation of the trap
    protected Array<Integrable> integrators; // there are the switch and they should contains their contactor, and a sprite
    protected float myThreshold;
    protected boolean active;
    protected float myCurrentValue;
    protected GameScreenTournament gameScreen;
    private boolean isSilent;

    public SimpleHub(GameScreenTournament game, boolean silent) {
        gameScreen = game;
        gameScreen.getObjectsToUpdate().add(this);
        myThreshold = 100.0f; // percent
        triggerables = new Array<Triggerable>();
        integrators = new Array<Integrable>();
        myCurrentValue = 0.0f;
        active = false;
        isSilent = silent;
    }

    public boolean isThresholdReached() {
        float sum_input = 0;
        for (Integrable integrator : integrators) {
            sum_input += integrator.getIntegratedValue();
        }
        gameScreen.setDebugText("hub: " + sum_input + "on "+myThreshold);
        myCurrentValue = sum_input;
        return  (myCurrentValue >= myThreshold);
    }

    public boolean update(float deltaTime) {
        if (isThresholdReached() && !active) {
            active = true;
            Gdx.app.debug("Hub", "notifyTriggerables("+active+") --> " + triggerables);
            Gdx.app.debug("Hub", "list of integrators: " + integrators);
            notifyTriggerables(active);
            notifyIntegrators(active);
            if(!isSilent) {
                Sound sound = gameScreen.assetManager.getRandom("switch_music_on");
                sound.play(0.8f);
            }
        }
        else if(!isThresholdReached() && active) {
            active = false;
            Gdx.app.debug("Hub", "notifyTriggerables("+active+") --> " + triggerables);
            notifyTriggerables(active);
            notifyIntegrators(active);
            if(!isSilent) {
                Sound sound = gameScreen.assetManager.getRandom("switch_music_off");
                sound.play(0.8f);
            }
        }
        return false;
    }

    private void notifyIntegrators(boolean set_on) {
            for (Integrable integrator : integrators) {
                integrator.reactToHubFeedback(set_on);
            }
    }

    private void notifyTriggerables(boolean set_on) {
        if (set_on){
            for (Triggerable trigerrable : triggerables) {
                if(!trigerrable.isActiveByDefault())
                    trigerrable.triggerONActions(this);
                else
                    trigerrable.triggerOFFActions(this);
            }
        }
        else {
            for (Triggerable trigerrable : triggerables) {
                if(!trigerrable.isActiveByDefault())
                    trigerrable.triggerOFFActions(this);
                else
                    trigerrable.triggerONActions(this);
            }
        }
    }

    @Override
    public void addIntegrator(Integrable integrator) {
        integrators.add(integrator);
    }

    @Override
    public void removeIntegrator(Integrable integrator) {
        integrators.removeValue(integrator, true);
    }

    @Override
    public void addTriggerable(Triggerable triggerable) {
        //triggerable.reset();
        triggerables.add(triggerable);
    }

    @Override
    public void resetTriggerables(){
        for (Triggerable triggerable:
             triggerables) {
            triggerable.reset();
        }
    }

    @Override
    public void removeTriggerable(Triggerable triggerable){
        triggerables.removeValue(triggerable, true); // this not enough
    }

    public void setMyThreshold(float myThreshold) {
        this.myThreshold = myThreshold;
    }
}
