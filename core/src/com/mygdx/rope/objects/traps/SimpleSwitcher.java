package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;

/**
* Created by Geoffrey on 30/07/2014.
*/

public class SimpleSwitcher extends GameObject  implements Integrable {
    Animation animSwitchON;
    Animation animSwitchOFF;
    boolean holding;
    float myWeight;
    Constants.SWITCHSTATE switchstate;
    private Animation animSwitchACTIVATED;

    public SimpleSwitcher(GameScreen game, Vector2 position,  Vector2 dimension, float angle, String name_texture, JsonValue fd, float weight) {
       // (GameScreen game, Vector2 position,  Vector2 dimension, float angle, String name_texture, JsonValue fd
        super(game, position, dimension, angle, name_texture, fd);
        Gdx.app.debug("Switcher:", "FixtureDef is: "+ fd );
        holding = fd.getBoolean("hold", true);
        isVisible = fd.getBoolean("visible", true);
        myWeight = weight;
        switchOFF();
        this.body.setType(BodyDef.BodyType.StaticBody);
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
        animSwitchON = new Animation(1 / 6.0f, textures,  Animation.PlayMode.NORMAL);
        textures.reverse();
        animSwitchOFF = new Animation(1 / 6.0f, textures ,  Animation.PlayMode.NORMAL);
        //main_animation = new Animation(1 / 6.0f, textures.get(0));
        setAnimation( animSwitchON);
        //pixmap1.dispose();
        //pixmap2.dispose();
        stateTime = 0;
    }

    public void initAnimation(String name_texture) {
        if (name_texture == null){
            initAnimation();
        }
        else {
            Array<TextureAtlas.AtlasRegion> regions = null;
            // anim normal:
            regions = atlas.findRegions(name_texture + "_off");
            if (regions.size > 0)
                animSwitchOFF = new Animation(1.0f / 4.0f, regions, Animation.PlayMode.LOOP);
            setAnimation(main_animation);

            regions = atlas.findRegions(name_texture + "_on");
            if (regions.size > 0)
                animSwitchON = new Animation(1.0f / 4.0f, regions, Animation.PlayMode.LOOP);

            regions = atlas.findRegions(name_texture + "_activated");
            if (regions.size > 0)
                animSwitchACTIVATED = new Animation(1.0f / 4.0f, regions, Animation.PlayMode.LOOP);

            setAnimation( animSwitchON);
            stateTime = 0;
        }
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
        setAnimation(animSwitchON);
        switchstate = Constants.SWITCHSTATE.ON;
    }

    private void switchOFF() {
        setAnimation(animSwitchOFF);
        switchstate = Constants.SWITCHSTATE.OFF;
    }

    private void switchACTIVATED() {
        setAnimation(animSwitchACTIVATED);
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
}


