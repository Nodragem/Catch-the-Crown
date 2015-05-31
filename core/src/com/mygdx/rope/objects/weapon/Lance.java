package com.mygdx.rope.objects.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.*;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

import static java.lang.Math.abs;

/**
 * Created by Nodragem on 05/05/2014.
 */
public class Lance extends GameObject {
    private ContactData collisionStick;
    private Vector2 anchorPoint;
    private WeldJoint myAnchor;
    boolean isAttached =true;
    private Filter pickFilter;
    private Filter handleFilter;
    private Animation animThrowed;

    public Lance(GameScreen game, Vector2 position, float angle, Animation animThrowed) {
        super(game, position, new Vector2(1.5f, 0.25f), angle);
        setOrigin(0.0f, 0.0f);
//        setTransform(position.x, position.y, angle);
        givenDamage = 10.0f; // should change with the charge duration
        anchorPoint = new Vector2(1.2f, 0.125f);
        this.animThrowed = animThrowed;
        stateTime = 0;
        isVisible = false;

    }

    @Override
    public void initAnimation() {
        //setAnimation(animThrowed);
    }

    @Override
    public void initFixture() {
        //mytruesize = new Vector2(1.5f, 0.25f);
        myAnchor = null;
        // create the pick Ficture:
        PolygonShape p = new PolygonShape();
        p.setAsBox(dimension.x *0.4f, dimension.y *0.25f, new Vector2(dimension.x *0.4f, dimension.y *0.5f), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 10;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        //fd.isSensor = true;
        this.body.createFixture(fd);
        collisionStick = new ContactData(8, this.body.getFixtureList().get(0)); // that's the origin of the flying bug! the lance cannot send a message to the people it touched cause its list was too small!!
        p.dispose();
        // the main stick Fixture, which is the main contact box
        p = new PolygonShape();
        p.setAsBox(dimension.x *0.1f, dimension.y *0.5f, new Vector2(dimension.x * 0.9f, dimension.y *0.5f), 0);
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
        //ContactData d = (ContactData) body.getFixtureList().get(0).getUserData();
        //Gdx.app.debug("Lance", "my Stick ContactData is "+d + " and it is colliding: "+d.getTouchedFixtures());
        if (!isAttached && mainBoxContact.isTouched()){ // mainBoxContact here is the pick
            goToPlatformState();
        }
        if (!isAttached && abs(body.getLinearVelocity().len()) > 0.1f) { //follow the direction of speed
            body.setTransform(body.getPosition(), body.getLinearVelocity().angleRad());
        }
        // udate drawing position::
        stateTime += deltaTime;
        // the origin is really at the origin
        position.set(body.getPosition().sub(origin));
        // /position.set(origin.x - dimension.x/2.0f, origin.y - dimension.y/2.0f); // the origin of the body is on the left bottom corner
        rotation = body.getAngle() * MathUtils.radiansToDegrees;
        mainBoxContact.flush();
        return checkIfToDestroy();
    }

    public void goToPlatformState() {
        Vector2 worldAnchorPoint = body.getWorldPoint(anchorPoint);
        WeldJointDef wJD = new WeldJointDef();
        Body touchedBody = mainBoxContact.popTouchedFixtures().getBody();
        GameObject touchedObj = (GameObject) touchedBody.getUserData();
        if( touchedObj != null) {
            touchedObj.addDamage(givenDamage);
            if(touchedObj instanceof com.mygdx.rope.objects.characters.Character){
                ((Character) touchedObj).dropObjects();
            }
        }
        wJD.initialize(touchedBody, body, worldAnchorPoint);
        //wJD.referenceAngle = body.getAngle();
        myAnchor = (WeldJoint) gamescreen.getB2World().createJoint(wJD);

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
    public void initFilter() {
        handleFilter = new Filter();
        pickFilter = new Filter();
        goToGhostState();
    }

    public void goToGhostState() {
        handleFilter.categoryBits = Constants.CATEGORY.get("Object");
        handleFilter.maskBits = 0;
        this.body.getFixtureList().get(0).setFilterData(handleFilter);
        pickFilter.categoryBits = Constants.CATEGORY.get("Object");
        pickFilter.maskBits = 0;
        this.body.getFixtureList().get(1).setFilterData(pickFilter);
        this.body.setType(BodyDef.BodyType.KinematicBody);
    }




    public void throwMe(float angle, float force, float damage){
        givenDamage = damage;
        goToWeaponState();
        isVisible = true;
        setAnimation(animThrowed);
        //this.body.setTransform(body.getPosition(), angle);
        this.body.applyForceToCenter(new Vector2(MathUtils.cos(angle), MathUtils.sin(angle)).scl(force*1000), true);
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

}
