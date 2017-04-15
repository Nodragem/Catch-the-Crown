package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;

/**
* Created by Geoffrey on 30/07/2014.
*/

public class SimpleSwitcher extends GameObject  implements Integrable, Triggerable {
    Animation animSwitchON;
    Animation animSwitchOFF;
    boolean holding;
    float myWeight;
    Constants.SWITCHSTATE switchstate;
    private Animation animSwitchACTIVATED;
    private boolean isActiveByDefault; // note that this is different from being on or off.

    public SimpleSwitcher(GameScreenTournament game, Vector2 position,  Vector2 dimension, float angle,
                          String objectDataID, float weight, boolean isEnabledByDefault) {
        super(game, position, dimension, angle, objectDataID);

        Gdx.app.debug("Switcher:", "ID is: "+ objectDataID );
        this.holding = game.getObjectDataBase().get(objectDataID).getBoolean("hold", true);
        this.isVisible = game.getObjectDataBase().get(objectDataID).getBoolean("visible", true);
        this.myWeight = weight;
        this.isActiveByDefault = isEnabledByDefault;
        switchOFF();
//        this.body.setType(BodyDef.BodyType.StaticBody);

    }

    public void initAnimation() {
        Pixmap pixmap1 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap1.setColor(1, 0, 0, 1.0f);
        pixmap1.fillRectangle(0, 24, 32, 8);
        Pixmap pixmap2 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap2.setColor(0, 0, 1, 1.0f);
        pixmap2.fillRectangle(0, 16, 32, 16);
        Array <TextureRegion> textures = new Array<TextureRegion>(
                new TextureRegion[]{  new TextureRegion(new Texture(pixmap1)), new TextureRegion(new Texture(pixmap2)) }
        );
        animations.put("SwitchingOFF",
                new Animation(1 / 6.0f, textures,  Animation.PlayMode.NORMAL));
        textures.reverse();
        animations.put("SwitchingON",
                new Animation(1 / 6.0f, textures ,  Animation.PlayMode.NORMAL));
        animations.put("Activated",
                new Animation(1 / 6.0f, textures ,  Animation.PlayMode.LOOP_RANDOM));

        setAnimation("SwitchingON");
        stateTime = 0;
    }

    @Override
    public boolean update(float deltaTime){
        if(holding) {
            if (mainBoxContact.isTouched()) {
                Gdx.app.debug("Switcher: ", "Touched");
                if (switchstate != Constants.SWITCHSTATE.OFF)
                    switchOFF();
                else
                    switchON();
                mainBoxContact.flush();
            }
        }
        else{
            if (mainBoxContact.isTouched()){
                Gdx.app.debug("Switcher: ", "Touched");
                if(switchstate == Constants.SWITCHSTATE.OFF)
                    switchON();
            }
            else{
                if(switchstate != Constants.SWITCHSTATE.OFF)
                    switchOFF();
            }
        }
        return super.update(deltaTime);
    }

    private void switchON() {
        setAnimation("SwitchingON");
        playSound("switcher_on");
        switchstate = Constants.SWITCHSTATE.ON;
    }

    private void switchOFF() {
        setAnimation("SwitchingOFF");
        playSound("switcher_off");
        switchstate = Constants.SWITCHSTATE.OFF;
    }

    private void switchACTIVATED() {
        setAnimation("Activated");
        switchstate = Constants.SWITCHSTATE.ACTIVATED;
    }

    @Override
    public float getIntegratedValue() {
        return switchstate!= Constants.SWITCHSTATE.OFF?myWeight:0;
    }

    @Override
    public void reactToHubFeedback(boolean activated) {
        if (activated) {
            if(switchstate == Constants.SWITCHSTATE.ON)
                switchACTIVATED();
        }
        else{
            if(switchstate == Constants.SWITCHSTATE.ACTIVATED)
                switchON();
        }
    }

    @Override
    public void reset() {
//        setActivate(isActiveByDefault);
        if(isActiveByDefault)
            goToActivation();
        else
            goToDesactivation();
    }


    @Override
    public boolean isActiveByDefault() {
        return isActiveByDefault;
    }

    @Override
    public void triggerONActions(HubInterface hub) {
        goToActivation();
    }

    @Override
    public void triggerOFFActions(HubInterface hub) {
        goToDesactivation();
    }
}


