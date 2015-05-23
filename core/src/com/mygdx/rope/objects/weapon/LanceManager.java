package com.mygdx.rope.objects.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 15/06/2014.
 */
public class LanceManager extends GameObject {

    private Character player;
    private float shortAttackTime;
    private Constants.ATTACK_STATE attackState;
    //private GameScreen myGameScreen;
    private float powerLoad;
    private float maxPower;
    private float defaultPower;
    private Lance currentLance;
    private Array<Lance> lances;
    private Animation animShortAttack;
    private Animation animLongAttack;
    private Animation animLance;
    private Animation animAiming;
    private float givenSleep;
    private Animation animCharging;

    public LanceManager(GameScreen gm, Character p, Vector2 pos, String color_texture) {
        // that actually a close range/ long range manager.
        super(gm, pos.cpy(), new Vector2(1,1), 0, color_texture);
        maxPower = 6.0f;
        defaultPower = 2.5f;
        powerLoad = defaultPower;
        this.player = p;
        lances = new Array<Lance>(3);

        shortAttackTime = 0.0f;
        givenSleep = 0.3f;
        givenDamage = 10.0f;
        attackState = Constants.ATTACK_STATE.NOTATTACKING;
        isVisible = false;

    }

    @Override
    public void initFilter() {
        Filter filter = new Filter();
        filter.categoryBits = Constants.CATEGORY.get("Sensor");
        filter.maskBits = (short) (Constants.CATEGORY.get("Scenery") | Constants.CATEGORY.get("Player"));
        this.body.getFixtureList().get(0).setFilterData(filter);

    }

    @Override
    public void initFixture() {
        PolygonShape p = new PolygonShape();
        p.setAsBox(dimension.x *0.2f, dimension.y *0.2f, new Vector2(dimension.x * 0.85f, dimension.y *0.0f), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 40;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        //fd.isSensor = true;
        this.body.createFixture(fd);
        this.body.getFixtureList().get(0).setSensor(true); // don't forget that GameObject will automaticcally use the first fixture as a mainBoxContact
        this.body.setType(BodyDef.BodyType.DynamicBody);
        body.setSleepingAllowed(false);
        this.body.setGravityScale(0);
        p.dispose();
    }

    @Override
    public void initAnimation(String texture_name) {
        //TextureAtlas atlas = new TextureAtlas("texture_obj.atlas");
        Array<TextureAtlas.AtlasRegion> regions = null;
        // anim normal:
        regions = atlas.findRegions("Lance_"+texture_name+"_short"); // weapon specific, should go in Lance
        animShortAttack = new Animation(1.0f/12.0f, regions, Animation.PlayMode.NORMAL);
        // anim walk:
        regions = atlas.findRegions("Lance_"+texture_name+"_long");
        animLongAttack = new Animation(1.0f/12.0f, regions, Animation.PlayMode.NORMAL);
        // anim lance:
        regions = atlas.findRegions("lance");
        animLance = new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP);

        regions = atlas.findRegions("aiming");
        animAiming = new Animation(1.0f/6.0f, regions, Animation.PlayMode.NORMAL);

        regions = atlas.findRegions("loading");
        animCharging = new Animation(1.0f/6.0f, regions, Animation.PlayMode.NORMAL);

        this.setAnimation(animShortAttack);
        isVisible = false;
        stateTime = 0;
    }

    public void shortDistanceAttack(boolean isPressed, GameObject obj, float deltaTime) {
        if(isPressed) {
            Gdx.app.debug("Lance", "short attack");
            goToShortAttackState();
            if (obj != null) {
                if (obj.getClass().equals(Character.class)) { // should go in the Lance
                    Character p = (Character) obj;
                    p.addDamage(givenDamage);
                    p.goToSleepFor(givenSleep);
                    float ximpulse = 5*((player.viewDirection == Constants.VIEW_DIRECTION.RIGHT)?1:-1);
                    p.getBody().applyLinearImpulse(ximpulse, 50.0f, 0.5f, 0.5f, true);
                    p.dropObjects();
                }
            }
        }



    }


    public void longDistanceAttack(boolean isPressed, Vector2 pos, float angle, boolean isAiming, float deltaTime) {
        if (!isPressed) {
            if (powerLoad > defaultPower) {
                goToLongAttackState();
                setCurrentLance(pos, angle);
                currentLance.throwMe(angle, powerLoad, powerLoad>maxPower?givenDamage*6:givenDamage*3);
                powerLoad = defaultPower;
            }
            else if (isAiming){
                goToAimingState();
            }
        }
        else {
            //Gdx.app.debug("touched by", ""+sensorData.getTouchedFixtures());
            if (powerLoad == defaultPower){ // equals??
                goToChargingState();
            }

            if (powerLoad < maxPower) {
                //Gdx.app.debug("TQG", "powerLoad: " + powerLoad);
                //goToAimingState();
                powerLoad += 10.0f * deltaTime;
                //if (powerLoad >=20) Gdx.app.debug("TQG", "PowerMax reached!");
            }

        }
    }


    private void setCurrentLance(Vector2 pos, float angle) {
        currentLance = new Lance(getGamescreen(), new Vector2(pos.x + 0.5f, pos.y + 0.5f), angle, animLance); // can't do a true pool cause of Weld join
        if (lances.size == 3) {
            lances.get(0).setLife(-10);
            lances.removeIndex(0);
            lances.add(currentLance);
        }
        else {
            lances.add(currentLance);
        }
    }

    public void setAttack(boolean isPressed, Vector2 pos, float angle, boolean isAiming, float deltaTime){
        body.setTransform(pos.x+0.5f, pos.y+0.5f, angle); // look at that, how it is uggly :P see the comments in Player::processInputs, and in PlayerController about Child system
        //if (sensorData.isTouched() && attackState != Constants.ATTACK_STATE.AIMING) {
        //Gdx.app.debug("LanceSensor", "setAttack");
        if (mainBoxContact.isTouched())
            Gdx.app.debug("LanceSensor", "touched and almost ready to slap");
        if (mainBoxContact.isTouched() && attackState == Constants.ATTACK_STATE.NOTATTACKING) {
            Gdx.app.debug("LanceSensor", "touched and ready to slap");
            GameObject obj = (GameObject) mainBoxContact.peekTouchedFixtures().getBody().getUserData();
            shortDistanceAttack(isPressed, obj, deltaTime);
            //sensorBuffer.flush();
        } else {
            if(attackState != Constants.ATTACK_STATE.SHORTATTACK)
                longDistanceAttack(isPressed, pos, angle, isAiming, deltaTime);
        }


    }

    @Override
    public boolean update(float deltaTime) {
        stateTime += deltaTime;
        rotation = body.getAngle() * MathUtils.radiansToDegrees;
        position.set(body.getPosition()).sub(origin);

        switch(attackState){
            case LONGATTACK:

                //position.set(body.getPosition());
                if (current_animation != animLongAttack) {
                    setAnimation(animLongAttack);
                }
                else if (current_animation.isAnimationFinished(stateTime)){
                    goToNotAttackingState();
                    Gdx.app.debug("LAnce", "not attacking");
                }
                break;
            case SHORTATTACK:

                //position.set(body.getPosition());
                if(current_animation != animShortAttack) {
                    setAnimation(animShortAttack);
                }
                else if (current_animation.isAnimationFinished(stateTime)){
                    goToNotAttackingState();
                    Gdx.app.debug("LAnce", "not attacking 2");
                }
                break;
            case NOTATTACKING:
                //Gdx.app.debug("LAnce", "NOTATTACKING");
                break;
            case AIMING:
                if (current_animation != animAiming) {
                    setAnimation(animAiming);
                }
                break;
            case CHARGING:
                if (current_animation != animCharging) {
                    setAnimation(animCharging);
                }
                break;

        }
        return false;
    }


    private void goToAimingState() { // change that as a case switch
        attackState = Constants.ATTACK_STATE.AIMING;
        isVisible = true;
        setOrigin(-0.5f, 0.5f);

    }

    private void goToChargingState() { // change that as a case switch
        attackState = Constants.ATTACK_STATE.CHARGING;
        isVisible = true;
        setOrigin(-0.5f, 0.5f);

    }

    private void goToLongAttackState() {
        attackState = Constants.ATTACK_STATE.LONGATTACK;
        isVisible = true;
        setOrigin(0.5f, 0.5f);

    }


    private void goToShortAttackState() {
        attackState = Constants.ATTACK_STATE.SHORTATTACK;
        isVisible = true;
        setOrigin(0.5f, 0.5f);

    }

    private void goToNotAttackingState() {
        attackState = Constants.ATTACK_STATE.NOTATTACKING;
        isVisible = false;
        stateTime = 0;
    }

}
