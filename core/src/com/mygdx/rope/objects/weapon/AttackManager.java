package com.mygdx.rope.objects.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 15/06/2014.
 */
public class AttackManager extends GameObject {

    private Character character;
    private float shortAttackTime;
    private Constants.ATTACK_STATE attackState;
    //private GameScreenTournament myGameScreen;
    private float powerLoad;
    private float maxPower;
    private float defaultPower;
//    private Lance currentLance;
    private Array<Lance> lancePool;
    private int pool_size;
    private int currentLanceIndex;
    private float givenSleep;
    private GameObject lastTouchedObj;
    private boolean shortAttackPressed;
    private float aimingAngle;

    public AttackManager(GameScreenTournament gm, Character p, Vector2 pos, String projectileDataID,
                         String objectDataID, String color_texture) {
        // that actually a close range/ long range manager.
        super(gm, pos.cpy(), new Vector2(1,1), 0, "No Init");
        initAnimation(objectDataID, color_texture);
        initFixture();
        mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        initCollisionMask();
        this.setAnimation("Slapping");

        pool_size = 3;
        currentLanceIndex = 0;
        initLances(projectileDataID, pool_size);

        maxPower = 6.0f;
        defaultPower = 2.5f;
        powerLoad = defaultPower;
        character = p;
        parentBody = p.getBody();
        mainBoxContact.addBodyToIgnore(parentBody);

        rposition.x = 0.5f;
        rposition.y = 0.5f;

        shortAttackTime = 0.0f;
        givenSleep = 0.3f;
        attackState = Constants.ATTACK_STATE.NOTATTACKING;
        isVisible = false;

    }

    private void initLances(String projectileDataID, int sizePool) {
        lancePool = new Array<Lance>(sizePool);
        for (int i = 0; i < sizePool; i++) {
            lancePool.add(new Lance(getGamescreen(),
                    new Vector2(0, 0), 0, projectileDataID));
        }

    }

    @Override
    public void initCollisionMask() {
        Filter filter = new Filter();
        filter.categoryBits = Constants.CATEGORY.get("Sensor");
        filter.maskBits = (short) (Constants.CATEGORY.get("Scenery") | Constants.CATEGORY.get("Player"));
        this.body.getFixtureList().get(0).setFilterData(filter);
    }

    @Override
    public void initFixture() {
        PolygonShape p = new PolygonShape();
        //p.setAsBox(dimension.x *0.2f, dimension.y *0.2f, new Vector2(dimension.x * 0.85f, dimension.y *0.0f), 0);
        p.setAsBox(dimension.x *0.4f, dimension.y *0.2f, new Vector2(dimension.x * 0.5f, dimension.y *0.0f), 0);
//        p.setAsBox(dimension.x, dimension.y, new Vector2(dimension.x * 0.5f, dimension.y *0.0f), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 40;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        //fd.isSensor = true;
        this.body.createFixture(fd);
        this.body.getFixtureList().get(0).setSensor(true);
        // don't forget that GameObject will automaticcally use the first fixture as a mainBoxContact
        this.body.setType(BodyDef.BodyType.KinematicBody);
        body.setSleepingAllowed(false);
        this.body.setGravityScale(0);
        p.dispose();
    }

    public void shortDistanceAttack(boolean isPressed, float deltaTime) {
        if(isPressed && !shortAttackPressed) {
            shortAttackPressed = true;
            if (attackState != Constants.ATTACK_STATE.SHORTATTACK
                    && attackState != Constants.ATTACK_STATE.LONGATTACK
                    && character.moveState == Constants.MOVE_STATE.NORMAL) {
                Gdx.app.debug("Lance", "short attack, state: "+attackState);
                goToShortAttackState();
                if (mainBoxContact.isTouched())
                    lastTouchedObj = (GameObject) mainBoxContact.peekTouchedFixtures().getBody().getUserData();
                else
                    lastTouchedObj = null;
                if (lastTouchedObj != null) {
                    if (lastTouchedObj.getClass().equals(Character.class)) {  // should go in the Lance
                        Character p = (Character) lastTouchedObj;
                        p.addDamage(Constants.SLAPDAMAGE);
                        p.goToSleepFor(givenSleep);
                        Sound slaphit = gamescreen.assetManager.getRandom("slap_hit");
                        slaphit.play();
                        float ximpulse = 50.0f * ((character.viewDirection == Constants.VIEW_DIRECTION.RIGHT) ? 1 : -1);
                        //p.moveState = Constants.MOVE_STATE.THROWED;
                        //p.getBody().setTransform(p.getBody().getPosition().x, p.getBody().getPosition().y + 0.1f,0);
                        p.getBody().applyLinearImpulse(ximpulse, 50.0f, 0.5f, 0.5f, true);
                        p.addMarks(1);
                        Gdx.app.debug("AttackManager: ", "Marking -- " + p.getMarks());
                        p.dropObjects();
                    }
                    else {
                        Sound slapmiss = gamescreen.assetManager.getRandom("slap_miss");
                        slapmiss.play();
                    }
                }
                else {
                        Sound slapmiss = gamescreen.assetManager.getRandom("slap_miss");
                        slapmiss.play();
                }
            }
        }
        else if (!isPressed){
            shortAttackPressed = false;
        }
    }


    public void longDistanceAttack(boolean isPressed, float angle, boolean isAiming, float deltaTime) {
        aimingAngle = angle;
        if (attackState == Constants.ATTACK_STATE.SHORTATTACK || attackState == Constants.ATTACK_STATE.LONGATTACK || character.moveState != Constants.MOVE_STATE.NORMAL){
            return;
        }
        if (!isPressed) {
            if (powerLoad > defaultPower) {
                goToLongAttackState();
                throwLance(this.position, angle, powerLoad, powerLoad>maxPower?1.0f:0.16f);
                powerLoad = defaultPower;
            }
            else if (isAiming){
                goToAimingState();
            }
        }
        else { // if is Pressed
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


    private void throwLance(Vector2 start_point, float angle, float force, float damageMultiplicator) {
        // WARNING angle in radians
        lancePool.get(currentLanceIndex).use(
                new Vector2(start_point),angle, force, damageMultiplicator);
        currentLanceIndex = (currentLanceIndex + 1)%pool_size;
//        if(currentLanceIndex == 0){ // we ran all the pool
//            Gdx.app.debug("SimpleLauncher", "relaoding time");
//            timer = reloadTime;
//            trapstate = Constants.TRAPSTATE.RELOADING;
//        }
    }

    @Override
    public boolean update(float deltaTime) {
        // FIXME: Copy paste from GameObject -- need to be parented only for the position
        stateTime += deltaTime;
        body.setTransform(
                parentBody.getPosition().x + rposition.x + 0.5f*MathUtils.cos(aimingAngle),
                parentBody.getPosition().y + rposition.y + 0.5f*MathUtils.sin(aimingAngle),
                aimingAngle
                );
        rotation = body.getAngle(); 
        position.set(body.getPosition()).sub(origin);
        // FIXME (WIP): Orgin problem: can't rotate on something else than the left-bottom corner
        // previous solution was to center the center of the body fixture (the y),
        // on the left-bottom corner of the texture
        // should check what we did for the rotating platform

        switch(attackState){
            case LONGATTACK:
                //position.set(body.getPosition());
                if (current_animation != animations.get("Throwing")) {
                    setAnimation("Throwing");
                }
                else if (current_animation.isAnimationFinished(stateTime)){
                    goToNotAttackingState();
                    Gdx.app.debug("LAnce", "not attacking");
                }
                break;
            case SHORTATTACK:
                //position.set(body.getPosition());
                if(current_animation != animations.get("Slapping")) {
                    setAnimation("Slapping");
                }
                else if (current_animation.isAnimationFinished(stateTime)){
                    goToNotAttackingState();
                    Gdx.app.debug("LAnce", "not attacking 2");
                }
                break;
            case NOTATTACKING:
                // note that we can put the animation to null without
                // upsetting the renderer because the isVisible is at False when NOTATTACKING
                if(current_animation != null)
                    setAnimation("");
                //Gdx.app.debug("LAnce", "NOTATTACKING");
                break;
            case AIMING:
                if (current_animation != animations.get("Aiming")) {
                    setAnimation("Aiming");
                }
                break;
            case CHARGING:
                if (current_animation != animations.get("Charging")) {
                    setAnimation("Charging");
                }
                break;

        }
        return false;
    }


    private void goToAimingState() { // change that as a case switch
        attackState = Constants.ATTACK_STATE.AIMING;
        isVisible = true;
        setOrigin(0.0f, 0.5f);

    }

    private void goToChargingState() {
        // setOrigin move the texture only, it does not affect the Body
        // we need that because the body needed to be shifted to get its origins where we wanted
        // See initFixture (setAsBox has a center at 0 for y)
        attackState = Constants.ATTACK_STATE.CHARGING;
        isVisible = true;
        setOrigin(0.0f, 0.5f);


    }

    private void goToLongAttackState() {
        // here we need an even bigger shift of the texture  (the arms of the prabbit)
        // (the arms of the prabit has nothing to do with the Body of AttackMAnager,
        // but we can still infer its positions)
        attackState = Constants.ATTACK_STATE.LONGATTACK;
        isVisible = true;
        setOrigin(1.0f, 0.5f);
//        rposition.x = -0.5f;
//        rposition.y = -0.5f;

    }


    private void goToShortAttackState() {
        attackState = Constants.ATTACK_STATE.SHORTATTACK;
        isVisible = true;
        setOrigin(1.0f, 0.5f);
//        rposition.x = -0.5f;
//        rposition.y = -0.5f;


    }

    private void goToNotAttackingState() {
//        rposition.x = 0.0f;
//        rposition.y = 0.0f;
        attackState = Constants.ATTACK_STATE.NOTATTACKING;
        isVisible = false;
        stateTime = 0;
    }

}
