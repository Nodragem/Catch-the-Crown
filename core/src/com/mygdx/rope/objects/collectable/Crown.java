package com.mygdx.rope.objects.collectable;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.Carriable;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 20/05/2014.
 */
public class Crown extends GameObject implements Carriable {
    public Array <Coins> linkedGroupCoins;
    private boolean neverTaken = true;
//    private Character carrier;
    private float crownGoldValue;
    private Character Carrier;
    private boolean bouncing;
    private float interpolation;
    private Vector2 bounceInSize;
    private Vector2 standardSize;
    // a part of the gold goes in the crown pocket, the other part in the player pocket
    // the crown holder score is: crown gold pocket + its own gold.

    public Crown(GameScreenTournament game, Vector2 position, Array<Coins> coinGroups) {
        super(game, position);
        bouncing = false;
        interpolation = 0f;
        linkedGroupCoins = coinGroups;
        bounceInSize = new Vector2(1.5f,1.5f);
        standardSize = new Vector2(1,1);
        for (Coins linkedGroupCoin : linkedGroupCoins) {
            linkedGroupCoin.setLinkedCrown(this);
        }
    }

    @Override
    public void initCollisionMask() {
        Filter filter = new Filter();
        filter.categoryBits = Constants.CATEGORY.get("Object");
        filter.maskBits = Constants.MASK.get("Object");
        this.body.getFixtureList().get(0).setFilterData(filter);
        ContactData data = new ContactData(1,this.body.getFixtureList().get(0));
        data.setMyColliderType(Constants.COLLIDER_TYPE.CROWN);
    }

    @Override
    public void initAnimation() {
        Array<TextureAtlas.AtlasRegion> regions = null;
        // anim normal:
        regions = atlas.findRegions("crown");
        animations.put("Main", new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP));
        this.setAnimation("Main");
        stateTime = 0;
    }

    public void setCrownGoldValue(float crownGoldValue) {
        this.crownGoldValue = crownGoldValue;
    }

    public float getCrownGoldValue() {
        return crownGoldValue;
    }

    public void addGoldValue(float gold) {
        bouncing = true;
        this.crownGoldValue += gold;
    }

    @Override
    public void setCarrier(Character newCarrier) {
        if (newCarrier == null){
            // drop the crown
            this.body.setType(BodyDef.BodyType.DynamicBody);
            this.Carrier.getPlayer().addScore(-crownGoldValue);
            this.Carrier = null;
        }
        else if (this.Carrier == null) {
            if (neverTaken) {
                newCarrier.getPlayer().addScore(Constants.BONUSCROWN);
                for (Coins coins : linkedGroupCoins) {
                    coins.allowActivation(true);
                }
                neverTaken = false;
            }
            this.Carrier = newCarrier;
            for (Coins linkedGroupCoin : linkedGroupCoins) {
                linkedGroupCoin.setCurrentDestination(newCarrier.getPlayer().getUIBox());
            }
            this.Carrier.getPlayer().addScore(crownGoldValue);
            this.body.setType(BodyDef.BodyType.KinematicBody);
            newCarrier.setCrownBody(body);
            Sound stealCrown = gamescreen.assetManager.getRandom("laugh_steal");
            gamescreen.makeAnnouncement(Constants.ANNOUNCEMENT.CROWN, newCarrier.getPlayer().getName(), "");
            stealCrown.play();
        }

    }

    @Override
    public Character getCarrier() {
        return Carrier;
    }

    @Override
    public boolean update (float deltaTime){
        super.update(deltaTime);
        if (bouncing){
            interpolation+=8*deltaTime;
            if(interpolation>=2) {
                interpolation = 0;
                bouncing = false;
            }
            else if (interpolation < 1){
                dimension.interpolate(bounceInSize, interpolation, Interpolation.circleIn);
            } else if (interpolation < 2){
                dimension.interpolate(standardSize, interpolation-1, Interpolation.circleOut);
            }
        }

        return todispose;


    }

}
