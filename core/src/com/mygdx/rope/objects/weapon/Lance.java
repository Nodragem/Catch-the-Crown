package com.mygdx.rope.objects.weapon;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.objects.traps.Launcher;
import com.mygdx.rope.objects.traps.SimpleLauncher;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

import static java.lang.Math.abs;

/**
 * Created by Nodragem on 05/05/2014.
 */
public class Lance extends GameObject {
    private ContactData collisionStick;
    private Vector2 anchorPoint;
//    private WeldJoint myAnchor;
    boolean isAttached = false;
    private Filter pickFilter;
    private Filter handleFilter;
    private boolean isToDetroy;
    private float defaultDamage;
    private GameObject parentObject;
    public Launcher user; // as for projectile
    private boolean isBurning;
    private Sound burningSound;


    public Lance(GameScreenTournament game, Vector2 position, float angle, String objectDataID, Launcher user) {
        super(game, position, new Vector2(2.0f, 0.6875f), angle, "No Init");
        // \--> old size (1.5f, 0.25f)
        this.user = user;
        burningSound = gamescreen.assetManager.getRandom("burning_lance");
        initAnimation(objectDataID, "");
        // FIXME: the initFixture should be data-driven
        initFixture();
        mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        initCollisionMask();
        setOrigin(0.0f, 0.34f);
//        setTransform(position.x, position.y, angle);
        anchorPoint = new Vector2(1.2f, 0.125f);
        stateTime = 0;
        isVisible = false;
        isKillable = false;
        defaultDamage = 34.0f;
        // the givenDamage (from GameObject) is defaultDamage times a multiplicator

    }

    @Override
    public void initFixture() {
        // ---- create the pick Ficture:
        PolygonShape p = new PolygonShape();
        // p.setAsBox(dimension.x *0.4f, dimension.y *0.25f, new Vector2(dimension.x *0.4f, dimension.y *0.5f), 0);
        p.setAsBox(dimension.x *0.3f, dimension.y *0.25f/2.75f, new Vector2(dimension.x *0.55f, dimension.y *0.0f), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 10;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        //fd.isSensor = true;
        this.body.createFixture(fd);
        collisionStick = new ContactData(8, this.body.getFixtureList().get(0));
        p.dispose();
        // ---- the main stick Fixture, which is the main contact box
        p = new PolygonShape();
//        p.setAsBox(dimension.x *0.1f, dimension.y *0.5f, new Vector2(dimension.x * 0.9f, dimension.y *0.5f), 0);
        p.setAsBox(dimension.x *0.075f, dimension.y *0.5f/3.5f, new Vector2(dimension.x * 0.9f, dimension.y *0.0f), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 40;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        //fd.isSensor = true;
        this.body.createFixture(fd);
        this.body.getFixtureList().get(1).setSensor(true);
        this.body.setBullet(true);
        p.dispose();
        mainFixtureIndex = 1; // no need to create a ContactData, that will be done automatically
    }

    @Override
    public boolean update(float deltaTime) {
        isToDetroy = super.update(deltaTime);

        if(isBurning & ((position.x < -2) |
                (position.x > Constants.VIEWPORT_WIDTH + 2) |
                (position.y < -2) |
                (position.y > Constants.VIEWPORT_HEIGHT + 2))){
            setAnimation("Moving");
            isBurning = false;
            body.setLinearVelocity(0,0);
            burningSound.stop();
        }

         if (!isAttached) {
             // FIXME: maybe we should override onCollision() instead
             // FIXME: maybe we should call onCollision in the box2d collision manager
            if (mainBoxContact.isTouched()){ // mainBoxContact here is the pick
                goToPlatformState();
            }
            if (abs(body.getLinearVelocity().len()) > 0.1f) {
                //the rotation follows the direction of speed
                body.setTransform(body.getPosition(), body.getLinearVelocity().angleRad());
            }
        }
        // FIXME: for now we make the lances invisible when they got deactivated by the deactivation of their parents
        // (e.g. death of Character, Block that disappears...)
        // a better solution could be to have a IN_POOL state
        if (activeState == Constants.ACTIVE_STATE.DESACTIVATED){
            goToGhostState();
            isVisible = false;
        }
        // why do we need to flush at each frame? WARNING, that line was actually causing the flying prabbit bug!
//        mainBoxContact.flush();
        return todispose;
    }

    public void goToPlatformState() {
        playSound("lance_hit");
        givenDamage = 0;
        if(isBurning){
            setAnimation("Moving");
            isBurning = false;
            burningSound.stop();
        }


        Body touchedBody = mainBoxContact.popTouchedFixtures().getBody();

        if (touchedBody != null) {
            GameObject touchedObject = (GameObject) touchedBody.getUserData();
            if(!(touchedObject instanceof Lance) || !children.contains(touchedObject, true)){
                setParentBody(touchedBody, true);
                isAttached = true;
                parentObject = (GameObject) touchedBody.getUserData();
                this.body.setType(BodyDef.BodyType.KinematicBody);
                this.body.setLinearVelocity(0f, 0f);
            } else {
                goToDesactivation();
            }

            if (parentObject != null && parentObject instanceof com.mygdx.rope.objects.characters.Character) {
                goToGhostState();
            }else {
                collisionStick.setMyColliderType(Constants.COLLIDER_TYPE.ONEWAY);
                handleFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
                handleFilter.maskBits = Constants.MASK.get("AttachedObject");
                this.body.getFixtureList().get(0).setFilterData(handleFilter);
                pickFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
                pickFilter.maskBits = Constants.MASK.get("AttachedObject");
                this.body.getFixtureList().get(1).setFilterData(pickFilter);
            }
            if(parentObject!=null && parentObject instanceof SimpleLauncher){
                ((SimpleLauncher) parentObject).triggerOBSTRUCTEDActions(true);
            }

        }

    }





    @Override
    public void initCollisionMask() {
        handleFilter = new Filter();
        pickFilter = new Filter();
        goToGhostState();
    }

    public void goToGhostState() {
        handleFilter.categoryBits = Constants.CATEGORY.get("Object");
        handleFilter.maskBits = 0; // sense nobody?
        this.body.getFixtureList().get(0).setFilterData(handleFilter);
        pickFilter.categoryBits = Constants.CATEGORY.get("Object");
        pickFilter.maskBits = 0;
        this.body.getFixtureList().get(1).setFilterData(pickFilter);
        this.body.setType(BodyDef.BodyType.KinematicBody);
    }


    @Override
    public Array<GameObject> onCollision(boolean dealDamage) {
        Array<GameObject> touchedObjects = super.onCollision(true);
        for (GameObject touchedObject:touchedObjects) {
            if(touchedObject.getLife()<0)
                notifyKill(touchedObject);
        }
        return touchedObjects;
    }

    public void use(Vector2 startPos, float angle, float force, float damageMultiplicator){
        Sound throwSound = gamescreen.assetManager.getRandom("lance_thrown");
        throwSound.play();
        if(parentObject!=null && parentObject instanceof SimpleLauncher){
            ((SimpleLauncher) parentObject).triggerOBSTRUCTEDActions(false);
        }
        setParentBody(null, true);
        clearChildren();
        givenDamage = damageMultiplicator*defaultDamage;
        if (damageMultiplicator == 3.0f) {
            isBurning = true;
            burningSound.loop();
        }
        else {
            isBurning = false;
        }
        goToWeaponState();

        goToActivation();
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setTransform(startPos, angle);
        if (isBurning){ // goToWeaponState should have made it a kinematic body
            // we want burning lance to go straight
            body.setLinearVelocity(new Vector2(MathUtils.cos(angle), MathUtils.sin(angle))
                    .scl(force));

        } else { // it is a dynamic body
            this.body.applyForceToCenter(
                    new Vector2(MathUtils.cos(angle), MathUtils.sin(angle))
                            .scl(force*1000), true);
        }

// FIXME: check if it could be a projectile implementing usable
    }

    public void goToWeaponState() {
        mainBoxContact.deepFlush();
        collisionStick.deepFlush();
        if (isBurning) {
            setAnimation("Burning");
            body.setType(BodyDef.BodyType.KinematicBody);
        }
        else {
            setAnimation("Moving");
            body.setType(BodyDef.BodyType.DynamicBody);
        }
        handleFilter.categoryBits = Constants.CATEGORY.get("Object");
        handleFilter.maskBits = Constants.MASK.get("Object");
        this.body.getFixtureList().get(0).setFilterData(handleFilter);
        pickFilter.categoryBits = Constants.CATEGORY.get("Weapon");
        pickFilter.maskBits =  Constants.MASK.get("Weapon"); //Constants.CATEGORY_PLAYER | Constants.CATEGORY_SCENERY;
        this.body.getFixtureList().get(1).setFilterData(pickFilter);

        isAttached = false;
        isVisible = true;

    }

    public boolean checkIfToDestroy() {
        if (life < 0)
            goToDesactivation();
        return false;
    }

    public void notifyKill(GameObject gameObject){
        user.notifyKill(gameObject);
    }


}
