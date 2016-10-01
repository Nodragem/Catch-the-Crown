package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.Carriable;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.collectable.Crown;
import com.mygdx.rope.objects.weapon.AttackManager;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

import static com.mygdx.rope.util.Constants.*;
import static com.mygdx.rope.util.Constants.AWAKE_STATE.AWAKE;
import static com.mygdx.rope.util.Constants.AWAKE_STATE.DEAD;

public class Character extends GameObject implements  Carriable {
    private float timeToSleep;
    private float timerToAwakeState; // a player can be Knock out and lose control of his character
    private float respawnTime;
    public int marks;
    public Player player;
    public AttackManager weapon;
    public Animation groundShock;
    public Animation goldenGlowing;
    private float timeFrontFX;
    private float timeBackFX;
    public MOVE_STATE previousMoveState;
    public MOVE_STATE moveState;
    public PICKUP_STATE pickupState;
    public AWAKE_STATE awakeState;
    private Carriable carriedObject;
    private Body crownBody;
    public Body lastGroundedBody;
    public Fixture myFeet;
    public Fixture myBodyFixture;
    public Fixture currentHand;
    private Fixture myLeftHand;
    private Fixture myRightHand;
    public String name;
    public String color_texture;
    private Texture MarkTexture;
    private actionProgressBar progressBar;
    private Character Carrier;


    public Character(GameScreenTournament game, Vector2 position, String objectDataID, String color_texture){
        super(game, position, new Vector2(1, 0.9f), 0, "No Init");
        this.color_texture = color_texture;
        progressBar = new actionProgressBar(game, this);
        initAnimation(objectDataID, color_texture);

        // FIXME: the initFixture should be data-driven
        initFixture();
        mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        initCollisionMask();
        respawnTime = -1;
        marks = 0;
        Carrier = null;
        player = null;
        lastGroundedBody = null;
        isKillable = true;
        // to do that would ultimately asks us to re-do the rendering system :/
        // and we dont want that now :D
//        setWeapon(weapon);
//         we need to init the weapon before to call goToPickingState
//        goToPickingUpState(PICKUP_STATE.NORMAL);
        goToConsciousState(AWAKE, 0);
        setMoveState(MOVE_STATE.FALLING);
        previousMoveState = MOVE_STATE.FALLING;
        carriedObject = null;
        crownBody = null;
        timerToAwakeState = 0;
        timeToSleep = 0;
	}

    @Override
    public void initCollisionMask() {
        Filter filter = new Filter();
        filter.categoryBits = CATEGORY.get("Player");
        filter.maskBits = MASK.get("Player");
        myBodyFixture.setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Object") | CATEGORY.get("Scenery") | CATEGORY.get("Player") );
        this.myLeftHand.setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Object") | CATEGORY.get("Scenery") | CATEGORY.get("Player") );//remove the status object from AttackManager! <-- not clear :/
        this.myRightHand.setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Scenery") | CATEGORY.get("AttachedObject") );
        this.myFeet.setFilterData(filter);
    }

    public void setMoveState(MOVE_STATE moveState) {
        if(this.moveState != moveState) {
            this.previousMoveState = this.moveState;
            this.moveState = moveState;
            Gdx.app.debug("Character", "moveState from "+ previousMoveState + " to " + moveState);
            switch (moveState){
                case RISING:
                    setAnimation("Rising");
                    myBodyFixture.setFriction(0);
                    break;
                case FALLING:
                    setAnimation("Falling");
                    myBodyFixture.setFriction(0);
                    break;
                case GROUNDED:
                    setAnimation("Walking");
                    myBodyFixture.setFriction(0.01f);
                    break;
                case IDLE:
                    setAnimation("Standing");
                    myBodyFixture.setFriction(1.0f);
                    break;
                case THROWED:
//                    setAnimation("Stunned");
                    myBodyFixture.setFriction(0.0f);
                    break;
                case PANICKING:
                    setAnimation("PickedUp");
                    break;
                case THROWING_CHARACTER:
                    setAnimation("Throwing");
                    break;
            }
        }
    }

    @Override
    public void initAnimation(String objectDataID, String color_texture) {
        super.initAnimation(objectDataID, color_texture);
        // -- Marks:
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1.0f);
        pixmap.fillRectangle(0, 0, 1, 2);
        MarkTexture =  new Texture(pixmap);
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions("ground_shock");
        groundShock = new Animation(0.25f, regions, Animation.PlayMode.NORMAL);
        timeFrontFX = -1;
        regions = atlas.findRegions("golden_halo_back");
        goldenGlowing = new Animation(0.25f, regions, Animation.PlayMode.LOOP);
        timeBackFX = -1;
    }

    @Override
    public void initFixture(){
        // main fixture, for collision
        Vector2[] vertices = new Vector2[6];
        Vector2 scaleVector = new Vector2(dimension.x/1.8f, dimension.y/1.25f);
        vertices[0] = new Vector2(0f  , 1f  ).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        vertices[1] = new Vector2(1f , 1f  ).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        vertices[2] = new Vector2(1f , 0.25f).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        vertices[3] = new Vector2(0.75f , 0f).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        vertices[4] = new Vector2(0.25f , 0f).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        vertices[5] = new Vector2(0f , 0.25f).scl(scaleVector).add((1-scaleVector.x)/2, (1-scaleVector.y)/2-0.12f);
        body.setGravityScale(4.0f);
        PolygonShape p = new PolygonShape();
        p.set(vertices);
        //p.setAsBox(dimension.x / 3.0f, dimension.y /2.5f, new Vector2(dimension.x / 2, dimension.y / 2.5f), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5.0f;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        myBodyFixture = this.body.createFixture(fd); // has to be the first one
        p.dispose();
        mainFixtureIndex = 0;

        p = new PolygonShape();
        p.setAsBox(dimension.x/4.5f, dimension.y/10, new Vector2(dimension.x / 2, 0), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myFeet = this.body.createFixture(fd);
        new ContactData(8, myFeet);
        p.dispose();
        // fixture-sensor to detect object on the left of him
        p = new PolygonShape();
        p.setAsBox(dimension.x /2.0f, dimension.y / 3.0f, new Vector2(dimension.x/2 - dimension.x/2, dimension.y / 2), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myLeftHand = this.body.createFixture(fd);
        new ContactData(2, myLeftHand);
        p.dispose();
        // fixture-sensor to detect object on the right of him
        p = new PolygonShape();
        p.setAsBox(dimension.x /2.0f, dimension.y / 3.0f, new Vector2(dimension.x/2 + dimension.x/2, dimension.y / 2), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myRightHand = this.body.createFixture(fd);
        new ContactData(2, myRightHand);
        p.dispose();
        Gdx.app.debug("PlayerCreation", "MyBody: " +myBodyFixture +"MyFeet: " +myFeet);
        currentHand = myRightHand;
        this.body.isBullet();
        this.body.setFixedRotation(true);

    }

    @Override
    public boolean update(float deltaTime){
        if(damageState==DAMAGE_STATE.IMMUNE){
            setColor(1f*Math.abs(MathUtils.sin(5*currentImmunity*MathUtils.PI)), 0.25f, 0.25f, 0);
        }
        else{
            setColor(1, 1, 1, 1);
        }
        // that's updating the jump states used by the "animation updater" and the renderer:
        currentHand = this.getViewDirection() == VIEW_DIRECTION.LEFT ? myLeftHand:myRightHand; // use by the renderer directly
        // Animation updater: (could be an outside component)
        updateTheStateMachine(deltaTime);

        // roughly that part look like updating Child objects, but we dont have a Child system :P so we are managing specifically a carried body and a crown.
        // Note that the child system would be useful also to attached a weapon to the player and to throw bullets from this weapon...
        if (pickupState == PICKUP_STATE.CHALLENGED){
            setTransform(Carrier.position.x + 0.0f * ((Carrier.getViewDirection() == VIEW_DIRECTION.LEFT)?-1:1),
                    Carrier.position.y + 0.55f, 0);
        }

        if (crownBody != null){
            crownBody.setTransform(this.position.x,
                    this.position.y + this.dimension.y, 0);
        }
        if (timeFrontFX >=0 && !groundShock.isAnimationFinished(timeFrontFX)) {
            timeFrontFX += deltaTime;
            Gdx.app.debug("Character", "FXState: "+groundShock.isAnimationFinished(timeFrontFX));
        }
        if (timeBackFX >=0)
            timeBackFX+= deltaTime;
        return super.update(deltaTime);

    }

    @Override
    public void render(SpriteBatch batch) {
        if(timeBackFX >= 0){
            TextureRegion reg = goldenGlowing.getKeyFrame(timeBackFX);
            batch.draw(reg, position.x-0.5f, position.y-0.5f, 0, 0, 2, 2, 1, 1, 0);
        }
        super.render(batch);
        for (int i = 0; i < Constants.MAXMARKS; i++) {
            if (i < marks)
                batch.setColor(1,1,1,1); // markers in white
            else
                batch.setColor(0.4f, 0.4f, 0.4f, 1f); // not marked in grey
            batch.draw(MarkTexture, position.x+0.15f*i, position.y+dimension.y + 0.2f, 0.1f, 0.2f);
            batch.setColor(1, 1, 1, 1); // markers in white
        }
        if (timeFrontFX >=0 && !groundShock.isAnimationFinished(timeFrontFX)){
            TextureRegion reg = groundShock.getKeyFrame(timeFrontFX);
            batch.draw(reg, position.x, position.y, 0, 0, 1, 1, 1, 1, 0);
        }
    }

    private void updateTheStateMachine(float deltaTime) {
//        gamescreen.addDebugText("\n " + getPlayer().getName() + " MOVE_STATE: " + moveState);
        switch (activeState) {
            case ACTIVATED:
                switch (awakeState) {
                    case AWAKE:
                        if (getLife() < 0) {
                            goToConsciousState(DEAD, 0);
                            break;
                        }
                        if(moveState!=MOVE_STATE.PANICKING && moveState!=MOVE_STATE.THROWING_CHARACTER) {
                            ContactData feetSensor = (ContactData) myFeet.getUserData();
                            if (feetSensor.isTouched()) {
                                lastGroundedBody = feetSensor.getTouchedFixtures().peek().getBody(); // returns the last item, but do not remove it
                                //Gdx.app.debug("Player", "Platform idling on: "+ lastGroundedBody);
                                if (moveState == MOVE_STATE.THROWED) {
                                    addToLife(-101);
                                }
                                if (Math.abs(this.body.getLinearVelocity().x - lastGroundedBody.getLinearVelocity().x) < 0.001)
                                    // FIXME: \--> I think we could do something more intuitive, as NOT_MOVING
                                    // FIXME: \--> I think we could do something more intuitive for JumpState, IDLE is a movestate no?
                                    setMoveState(MOVE_STATE.IDLE);
                                else
                                    setMoveState(MOVE_STATE.GROUNDED);
                            } else if (moveState != MOVE_STATE.THROWED) { // the sensor are inactive when the character is dead, so this state is to avoid
                                if (this.body.getLinearVelocity().y > 0.1 && moveState != MOVE_STATE.FALLING) // make sure we go to fall when we reset the speed
                                    setMoveState(MOVE_STATE.RISING);
                                else
                                    setMoveState(MOVE_STATE.FALLING);
                            }
                        } else if(moveState == MOVE_STATE.THROWING_CHARACTER){
                            if (current_animation.isAnimationFinished(stateTime)){
                                moveState = MOVE_STATE.FALLING;
                                throwObject(
                                        (getViewDirection()==VIEW_DIRECTION.LEFT?225:315)*MathUtils.degreesToRadians
                                        ,2060.0f);
                                getBody().setLinearVelocity(0, 0);
                                ContactData feetSensor = (ContactData) myFeet.getUserData();
                                feetSensor.deepFlush();
                                getBody().setType(BodyDef.BodyType.DynamicBody);
                            }
                        }

                        break;
                    case SLEEPING:
                        if (getLife() < 0) {
                            goToConsciousState(DEAD, 0);
                            break;
                        }
                        timerToAwakeState += deltaTime;
                        if (timerToAwakeState > timeToSleep) {
                            goToConsciousState(AWAKE, 0);
                        }
                        break;
                    case DEAD:
                        timerToAwakeState += deltaTime;
                        if (timerToAwakeState > respawnTime) {
//                            goToConsciousState(AWAKE, 0);
                            goToDesactivation();
                        }
                        break;
                }
                break;
            case DESACTIVATED:
                goToActivation();
                goToConsciousState(AWAKE, 0);
                break;

        }


    }

    public boolean hasCarriedObject(){
        return this.carriedObject != null;
    }

    public boolean hasTheCrown() {return this.crownBody != null;}

    public void pickUpObject(Carriable pickedObject){
        if (pickedObject == null){
            // note that setCarrier(null) will also remove the carriedObject
            // from this character (i.e., this.carriedObject = null)
            carriedObject.setCarrier(null);
            goToPickingUpState(PICKUP_STATE.NORMAL);
        }
        else{
            pickedObject.setCarrier(this);
        }

    }



    private void startProgressBar(int maxProgress, int increment, String button, float y_offset) {
        progressBar.startProgressBar(maxProgress, increment, button, y_offset);
    }

    public void throwObject(float angle, float force){

        if (carriedObject != null) {
            if (force > 50 && carriedObject.getClass().equals(Character.class)) {
                weapon.goToAttackState(ATTACK_STATE.THROWING);
                Character p = (Character) carriedObject;
                p.setMoveState(MOVE_STATE.THROWED);
                p.goToPickingUpState(PICKUP_STATE.NORMAL);
            }
            carriedObject.getBody().setType(BodyDef.BodyType.DynamicBody);
//            carriedObject.getBody().applyLinearImpulse(MathUtils.cos(angle) * force,
//                    MathUtils.sin(angle) * force, 0.5f, 0.5f, true);
            carriedObject.getBody().applyForceToCenter(new Vector2(MathUtils.cos(angle),
                    MathUtils.sin(angle)).scl(force * 10000), true);
//           Again note that we don't need to add carriedObject = null,
//           because the carriedObject.setCarrier(null) will do it.
            carriedObject.setCarrier(null);

        }
        goToPickingUpState(PICKUP_STATE.NORMAL);
    }

    public void dropTheCrown(float angle){
        if (crownBody != null) {
            crownBody.setType(BodyDef.BodyType.DynamicBody);
            crownBody.applyForceToCenter(new Vector2(MathUtils.cos(angle),
                    MathUtils.sin(angle)).scl(20 * 1000), true);
            Crown crownObject = (Crown) crownBody.getUserData();
            crownObject.setCarrier(null);
            crownBody = null;
        }

    }

    @Override
    public boolean checkIfToDestroy() {
        return todispose;
    }


    public float getTimeToSleep() {
        return timeToSleep;
    }

    public void addSleepingTime(float sleepingTime) {
        this.timeToSleep += sleepingTime;
    }

    public void setTimeToSleep(float timeToSleep) {
        this.timeToSleep = timeToSleep;
    }

    private void reSpawn() {
        for ( JointEdge jointEdge: body.getJointList()) { // iterate over the joints that the character is involved in
            GameObject attachedObject = (GameObject) jointEdge.other.getUserData(); // find the *other* member of the joint
            if (attachedObject != null) // kill the other member
                attachedObject.setLife(-1);
        }
        GameObject o = this.getCarrier();
        if(o != null)
            this.getCarrier().pickUpObject(null);
        mainBoxContact.deepFlush();
        this.setPosition(gamescreen.getSpawnPosition());
        life = 100.0f;
        setMarks(0);
        body.setTransform(body.getPosition(), 0);
    }

    public void goToConsciousState(AWAKE_STATE state, float time) {
        switch (state){
            case AWAKE:
                timerToAwakeState = 0;
                timeToSleep = 0; // no accumulation of the sleeping time during the round
                if (awakeState == DEAD)
                    reSpawn();
                awakeState = state;
                setAnimation("Standing");
                break;
            case DEAD:
                setAnimation("Death");
                if(previousMoveState==MOVE_STATE.THROWED) {
                    timeFrontFX = 0;
                    soundCache = gamescreen.assetManager.getRandom("impact_to_ground");
                    soundCache.play();
                }
                timerToAwakeState = 0;
                Gdx.app.debug("Character", "Death Event");
                respawnTime = RESPAWNTIME + marks*Constants.MARKPENALTY;
                for(Fixture fixt : body.getFixtureList()) {
                    ContactData data = (ContactData) fixt.getUserData();
                    if (data != null)
                        data.deepFlush(); // should be in the respawn function!!!
                }
                awakeState = state;
                // must call dropObject once the awake state is DEAD, to avoid throwing
//                dropObjects();
                break;
            case SLEEPING:
                if(awakeState == AWAKE) {
                    setAnimation("Stunned");
                    dropObjects();
                    addSleepingTime(time);
                    awakeState = state;
                }
        }

    }



    @Override
    public float getLife() {
            return life;
    }

    public float getRespawnTime(){
        if (awakeState == AWAKE_STATE.DEAD)
            return (respawnTime - timerToAwakeState);
        else
            return -1;
    }

    public void dropObjects() {
        throwObject(45.0f, 50.0f);
        dropTheCrown(-45.0f);
        // may drop some money

    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = Math.min(Constants.MAXMARKS, marks);
    }

    public void addMarks(int marks){
        this.marks += marks;
        this.marks = Math.min(Constants.MAXMARKS, this.marks);

    }

    public void setWeapon(AttackManager weapon) {
        this.weapon = weapon;
        this.weapon.setCharacter(this);
        this.weapon.setParentBody(this.getBody(), false);
        this.weapon.setPosition(new Vector2(position));
        goToPickingUpState(PICKUP_STATE.NORMAL);
        // /--> that is a little hack to avoid to redo the rendering system:
        // if we ask the character to be created directly with a weapon,
        // the weapon will be rendered behind him
    }

    public AttackManager getWeapon() {
        return weapon;
    }

    public void registerKill(GameObject obj){
        soundCache = gamescreen.assetManager.getRandom("laugh_kill");
        soundCache.play();
        player.registerKill(obj);
    }

    @Override
    public void setViewDirection(VIEW_DIRECTION viewDirection) {
        super.setViewDirection(viewDirection);
        weapon.setViewDirection(viewDirection);
    }

    @Override
    protected void addToLife(float f) {
        super.addToLife(f);
        if(f<0){
            soundCache = gamescreen.assetManager.getRandom("hurt");
            soundCache.play(1f);
            dropObjects();
        }

    }



    public actionProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void setCarrier(Character newCarrier) {
        // the newCarrier use its object to take the new one
        if (newCarrier == null){
            if(this.Carrier !=null) {
                this.Carrier.carriedObject = null;
                ContactData dd = (ContactData) this.Carrier.currentHand.getUserData();
                dd.deepFlush();
                this.Carrier = null;
            }
            body.setType(BodyDef.BodyType.DynamicBody);
            //FIXME: we should not make it Dynamic before to throw it ... (see throwObject()) !!!!
        }
        else {
            if (newCarrier.carriedObject != null)
                newCarrier.throwObject(45.0f, 50.0f);
            this.Carrier = newCarrier;
            newCarrier.carriedObject = this;
            body.setType(BodyDef.BodyType.KinematicBody);
            if (this.awakeState != AWAKE_STATE.DEAD & this.pickupState !=PICKUP_STATE.CHALLENGER) {
                this.goToPickingUpState(PICKUP_STATE.CHALLENGED);
                this.startProgressBar(Constants.MOVESTOFREE, 1, "A", 1);
                newCarrier.goToPickingUpState(PICKUP_STATE.CHALLENGER);
                newCarrier.startProgressBar(Constants.MOVESTOTHROW, 1, "Y", -1);
            } else if (this.pickupState == PICKUP_STATE.CHALLENGER){
                // if the newCarrier was CHALLENGED, we need to remove its panicked state,
                // We do that in the call to goToPickingUpState()
                newCarrier.goToPickingUpState(PICKUP_STATE.PICKINGUP);
                this.goToPickingUpState(PICKUP_STATE.NORMAL);
            } else {
                // if the player is dead we don't challenge it.
                newCarrier.goToPickingUpState(PICKUP_STATE.PICKINGUP);
                this.goToPickingUpState(PICKUP_STATE.NORMAL);
            }
            ContactData dd = (ContactData) this.myFeet.getUserData();
            dd.deepFlush(); // to deepFlush
        }
    }

    public void goToPickingUpState(Constants.PICKUP_STATE state) {
        switch (state){
            case CHALLENGED:
                setMoveState(MOVE_STATE.PANICKING);
                break;
            case CHALLENGER:
                weapon.goToAttackState(ATTACK_STATE.CARRYING);
                break;
            case PICKINGUP:
                if (pickupState == PICKUP_STATE.CHALLENGED)
                    setMoveState(MOVE_STATE.FALLING);
                weapon.goToAttackState(ATTACK_STATE.CARRYING);
                break;
            case NORMAL:
                weapon.goToAttackState(ATTACK_STATE.NOTATTACKING);
                break;

        }
        this.pickupState = state;

    }

    @Override
    public Character getCarrier() {
        return Carrier;
    }

    public Carriable getCarriedObject() {
        return carriedObject;
    }

    public void setCrownBody(Body crownBody) {
        this.crownBody = crownBody;
    }

    @Override
    public void setTodispose(boolean todispose) {
        weapon.setTodispose(todispose);
        super.setTodispose(todispose);
    }

    public void goToGoldenState(boolean b) {
        if(b)
            timeBackFX = 0;
        else
            timeBackFX = -1;
    }
}
