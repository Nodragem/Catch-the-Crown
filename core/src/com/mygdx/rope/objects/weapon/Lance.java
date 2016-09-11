package com.mygdx.rope.objects.weapon;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
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
    boolean isAttached =true;
    private Filter pickFilter;
    private Filter handleFilter;
    private boolean isToDetroy;
    private float defaultDamage;


    public Lance(GameScreenTournament game, Vector2 position, float angle, String objectDataID) {
        super(game, position, new Vector2(2.0f, 0.6875f), angle, "No Init");
        // \--> old size (1.5f, 0.25f)
        initAnimation(objectDataID, "");
        // FIXME: the initFixture should be data-driven
        initFixture();
        mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        initCollisionMask();
        //setOrigin(0.0f, 0.0f);
//        setTransform(position.x, position.y, angle);
        anchorPoint = new Vector2(1.2f, 0.125f);
        stateTime = 0;
        isVisible = false;
        defaultDamage = 34.0f;
        // the givenDamage (from GameObject) is defaultDamage times a multiplicator

    }

    @Override
    public void initFixture() {
        // ---- create the pick Ficture:
        PolygonShape p = new PolygonShape();
        // p.setAsBox(dimension.x *0.4f, dimension.y *0.25f, new Vector2(dimension.x *0.4f, dimension.y *0.5f), 0);
        p.setAsBox(dimension.x *0.3f, dimension.y *0.25f/2.75f, new Vector2(dimension.x *0.4f, dimension.y *0.5f), 0);
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
        p.setAsBox(dimension.x *0.075f, dimension.y *0.5f/2.75f, new Vector2(dimension.x * 0.9f, dimension.y *0.5f), 0);
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
        //ContactData d = (ContactData) body.getFixtureList().get(0).getUserData();
        //Gdx.app.debug("Lance", "my Stick ContactData is "+d + " and it is colliding: "+d.getTouchedFixtures());
        if (!isAttached && mainBoxContact.isTouched()){ // mainBoxContact here is the pick
            goToPlatformState();
        }
        if (!isAttached && abs(body.getLinearVelocity().len()) > 0.1f) {
            //the rotation follows the direction of speed
            // FIXME: note that the super.update already gave an angle, so that is not really sexy to do it twice
            body.setTransform(body.getPosition(), body.getLinearVelocity().angleRad());
        }
        // the origin is really at the origin
        // FIXME (done?): I dont understand why this shift in origin is necessary
        // Note that we can find that in Attack manager also, but not in GameObject... why?
        // OK so it was not necessary ...
//        position.set(body.getPosition().sub(origin));
        mainBoxContact.flush();
        return isToDetroy;
    }

    public void goToPlatformState() {
        Sound soundLanceHit = gamescreen.assetManager.getRandom("lance_hit");
        soundLanceHit.play();
        Vector2 worldAnchorPoint = body.getWorldPoint(anchorPoint);

        this.body.setType(BodyDef.BodyType.KinematicBody);
        // FIXME: maybe that won't work out of the box to use the parenting relationship
        Body touchedBody = mainBoxContact.popTouchedFixtures().getBody();
        setParentBody(touchedBody);
        GameObject touchedObj = (GameObject) touchedBody.getUserData();
        if( touchedObj != null) {
            touchedObj.addDamage(givenDamage);
            if(touchedObj instanceof com.mygdx.rope.objects.characters.Character){
                ((Character) touchedObj).dropObjects();
                Sound hurtSound = gamescreen.assetManager.getRandom("hurt");
                hurtSound.play(0.5f);
            }
        }

        if (!(touchedObj instanceof com.mygdx.rope.objects.characters.Character))
        {
            //color.set(0, 0.0f);
            collisionStick.setMyColliderType(Constants.COLLIDER_TYPE.ONEWAY);
            handleFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
            handleFilter.maskBits = Constants.MASK.get("AttachedObject");
            this.body.getFixtureList().get(0).setFilterData(handleFilter);
            pickFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
            pickFilter.maskBits = Constants.MASK.get("AttachedObject");
            this.body.getFixtureList().get(1).setFilterData(pickFilter);
            isAttached = true;
        }
        else{
            //goToGhostState();
            handleFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
            handleFilter.maskBits = 0; // you can't collide a lance which is attached to a character (that would have been funny, but not really playable :P )
            this.body.getFixtureList().get(0).setFilterData(handleFilter);
            pickFilter.categoryBits = Constants.CATEGORY.get("AttachedObject");
            pickFilter.maskBits = Constants.MASK.get("AttachedObject");
            this.body.getFixtureList().get(1).setFilterData(pickFilter);
            isAttached = true;
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




    public void use(Vector2 startPos, float angle, float force, float damageMultiplicator){
        Sound throwSound = gamescreen.assetManager.getRandom("lance_thrown");
        throwSound.play();
        givenDamage = damageMultiplicator*defaultDamage;
        goToWeaponState();
        isVisible = true;
        setAnimation("Moving");
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        body.setTransform(startPos, angle);
        this.body.applyForceToCenter(
                new Vector2(MathUtils.cos(angle), MathUtils.sin(angle))
                        .scl(force*1000), true);
// FIXME: check if it could be a projectile implementing usable
//        this.user = obj;
//        this.goToActivation();
//        this.setLife(100f);
//        body.setLinearVelocity(0, 0);
//        body.setAngularVelocity(0);
//        body.setTransform(position, 0f);
//        Gdx.app.debug("Projectile: ", "impulse * body.getMass() = " + body.getMass() * impulse.x + ", " + body.getMass() * impulse.y);
//        if (body.getType() == BodyDef.BodyType.DynamicBody)
//            body.applyLinearImpulse(body.getMass() * impulse.x, body.getMass() * impulse.y, body.getPosition().x, body.getPosition().y, true);
//        else {
//            body.setLinearVelocity(impulse.x, impulse.y);
//        }
    }

    public void goToWeaponState() {
        handleFilter.categoryBits = Constants.CATEGORY.get("Object");
        handleFilter.maskBits = Constants.MASK.get("Object");
        this.body.getFixtureList().get(0).setFilterData(handleFilter);
        pickFilter.categoryBits = Constants.CATEGORY.get("Weapon");
        pickFilter.maskBits =  Constants.MASK.get("Weapon"); //Constants.CATEGORY_PLAYER | Constants.CATEGORY_SCENERY;
        this.body.getFixtureList().get(1).setFilterData(pickFilter);
        body.setType(BodyDef.BodyType.DynamicBody);
        isAttached = false;
    }

    protected boolean checkIfToDestroy() {
        if (life < 0)
            goToDesactivation();
        return false;
    }

}
