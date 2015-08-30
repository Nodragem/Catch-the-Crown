package com.mygdx.rope.objects.collectable;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 20/05/2014.
 */
public class Crown extends GameObject {
    public Array <Coins> linkedGroupCoins;
    private boolean neverTaken = true;
//    private Character carrier;
    private float crownGoldValue;
    // a part of the gold goes in the crown pocket, the other part in the player pocket
    // the crown holder score is: crown gold pocket + its own gold.

    public Crown(GameScreenTournament game, Vector2 position, Array<Coins> coinGroups) {
        super(game, position);
        linkedGroupCoins = coinGroups;
        for (Coins linkedGroupCoin : linkedGroupCoins) {
            linkedGroupCoin.setLinkedCrown(this);
        }
    }

    @Override
    public void initFilter() {
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
        main_animation = new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP);
        this.setAnimation(main_animation);
        stateTime = 0;
    }

    public void setCrownGoldValue(float crownGoldValue) {
        this.crownGoldValue = crownGoldValue;
    }

    public float getCrownGoldValue() {
        return crownGoldValue;
    }

    public void addGoldValue(float gold) {
        this.crownGoldValue += gold;
    }

    @Override
    public boolean setCarrier(Character character) {
        if (character == null){
            this.body.setType(BodyDef.BodyType.DynamicBody);
            this.Carrier.getPlayer().addScore(-crownGoldValue);
            this.Carrier = null;
            return true;
        }
        else if (this.Carrier == null) {
            if (neverTaken) {
                character.getPlayer().addScore(Constants.BONUSCROWN);
                for (Coins coins : linkedGroupCoins) {
                    coins.allowActivation(true);
                }
                neverTaken = false;
            }
            this.Carrier = character;
            this.Carrier.getPlayer().addScore(crownGoldValue);
            this.body.setType(BodyDef.BodyType.KinematicBody);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean update (float deltaTime){
        super.update(deltaTime);
        return false;


    }

}
