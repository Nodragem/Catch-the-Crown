package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
    public DAMAGE_TYPE killedBy;
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
    public ContactData myFeet;
    public Fixture myBodyFixture;


    public ContactData currentHandContact;
    private ContactData myLeftHandContact;
    private ContactData myRightHandContact;
    public String name;
    public String color_texture;
    private Texture MarkTexture;
    private actionProgressBar progressBar;
    private Character Carrier;
    private Vector2 targetInterpolation;
    public String lastKiller;
//    private float stepTime;
//    private float stepPeriod;


    public Character(GameScreenTournament game, Vector2 position, String objectDataID, String color_texture){
        super(game, position, new Vector2(1, 0.9f), 0, "No Init");
        killedBy = null;
        this.color_texture = color_texture;
        progressBar = new actionProgressBar(game, this);
        initAnimation(objectDataID, color_texture);

        // FIXME: the initFixture should be data-driven
        initFixture();
        mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
//        mainBoxContact.setMyColliderType(COLLIDER_TYPE.AIMING_POINTER);
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

//        stepPeriod = 0.2f;
//        stepTime = 0f;
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
        this.myLeftHandContact.getMyFixture().setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Object") | CATEGORY.get("Scenery") | CATEGORY.get("Player") );//remove the status object from AttackManager! <-- not clear :/
        this.myRightHandContact.getMyFixture().setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Scenery") | CATEGORY.get("Oneway") );
        this.myFeet.getMyFixture().setFilterData(filter);
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
                    if (previousMoveState==MOVE_STATE.FALLING)
                        playSoundIndex("step", 1);
                    myBodyFixture.setFriction(0.01f);
                    break;
                case IDLE:
                    setAnimation("Standing");
                    if (previousMoveState==MOVE_STATE.FALLING)
                        playSoundIndex("step", 1);
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
                    playSound("throw_character", 0.0f, 0.6f);
                    Sound sound = gamescreen.assetManager.getRandom("impact_to_ground");
                    sound.play(1f);
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
        // --- main fixture, for collision
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

        // --- Fixture for the feet:
        p = new PolygonShape();
        p.setAsBox(dimension.x/4.5f, dimension.y/10, new Vector2(dimension.x / 2, 0), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myFeet = new ContactData(8, this.body.createFixture(fd));
        p.dispose();

        // Hand fixture-sensor to detect object on the left of him
        p = new PolygonShape();
        p.setAsBox(dimension.x /2.0f, dimension.y / 3.0f, new Vector2(dimension.x/2 - dimension.x/2, dimension.y / 2), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myLeftHandContact = new ContactData(2, this.body.createFixture(fd));
        p.dispose();

        // Hand fixture-sensor to detect object on the right of him
        p = new PolygonShape();
        p.setAsBox(dimension.x /2.0f, dimension.y / 3.0f, new Vector2(dimension.x/2 + dimension.x/2, dimension.y / 2), 0);
        fd = new FixtureDef();
        fd.shape = p;
        fd.density = 5;
        fd.restitution = 0.0f;
        fd.friction = 0.0f;
        fd.isSensor = true;
        myRightHandContact = new ContactData(2, this.body.createFixture(fd));
        p.dispose();
        Gdx.app.debug("PlayerCreation", "MyBody: " +myBodyFixture +"MyFeet: " +myFeet);
        currentHandContact = myRightHandContact;
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
//        if (moveState == MOVE_STATE.GROUNDED){
//            if (stepTime >=0){
//                stepTime+=deltaTime;
//            }
//            if(stepTime>=stepPeriod){
//                playSound("step");
//                stepTime = 0;
//            }
//        }
        // that's updating the jump states used by the "animation updater" and the renderer:
        currentHandContact = this.getViewDirection() == VIEW_DIRECTION.LEFT ? myLeftHandContact : myRightHandContact; // use by the renderer directly
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
        gamescreen.addDebugText("\nposition character: "+(Math.round(position.x*10.0f)/10.0f) + ", "
                                            +(Math.round(position.y*10.0f)/10.0f));
        if(timeBackFX >= 0){
            TextureRegion reg = goldenGlowing.getKeyFrame(timeBackFX);
            batch.draw(reg, position.x-0.5f, position.y-0.5f, 0, 0, 2, 2, 1, 1, 0);
        }
        if (marks == MAXMARKS)
            this.setColor(0.5f + 0.5f*MathUtils.cos((7*stateTime%MathUtils.PI2)), 0.2f, 0.2f, 1f);
        else
            this.setColor(1,1,1,1);
        super.render(batch);
        for (int i = 0; i < Constants.MAXMARKS; i++) {
            if (marks == MAXMARKS)
                batch.setColor(1f, 0.2f, 0.2f, 1f);
            else{
                if (i < marks)
                    batch.setColor(1,1,1,1); // markers in white
                else
                    batch.setColor(0.4f, 0.4f, 0.4f, 1f); // not marked in grey
            }

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
                        if (isOutSide(-0.1f)) {
                            setLife(-100);
                            throwObject(1 ,45.0f);
                            setCarrier(null);
                            setMoveState(MOVE_STATE.FALLING);
                        }

                        if (getLife() < 0) {
                            goToConsciousState(DEAD, 0);
                            break;
                        }
                        if(moveState!=MOVE_STATE.PANICKING && moveState!=MOVE_STATE.THROWING_CHARACTER) {

                            if (myFeet.isTouched()) {
                                lastGroundedBody = myFeet.getTouchedFixtures().peek().getBody(); // returns the last item, but do not remove it
                                //Gdx.app.debug("Player", "Platform idling on: "+ lastGroundedBody);
                                if (moveState == MOVE_STATE.THROWED) {
//                                    addToLife(-101);
                                    addDamage(101, DAMAGE_TYPE.CRUSHING);
                                }
                                if (Math.abs(this.body.getLinearVelocity().x - lastGroundedBody.getLinearVelocity().x) < 0.001)
                                    // FIXME: \--> I think we could do something more intuitive, as NOT_MOVING
                                    // FIXME: \--> I think we could do something more intuitive for JumpState, IDLE is a movestate no?
                                    setMoveState(MOVE_STATE.IDLE);
                                else
                                    setMoveState(MOVE_STATE.GROUNDED);
                            } else if (moveState != MOVE_STATE.THROWED) { // if not grounded and not throwed
                                // the sensor are inactive when the character is dead, so this state is to avoid
                                if (this.body.getLinearVelocity().y > 0.1 && moveState != MOVE_STATE.FALLING) // make sure we go to fall when we reset the speed
                                    setMoveState(MOVE_STATE.RISING);
                                else
                                    setMoveState(MOVE_STATE.FALLING);
//                                if (isOutSide(-0.3f))
//                                    setLife(-100);
                            } //else if (moveState == MOVE_STATE.THROWED){
//                                if (isOutSide(-0.3f))
//                                    setLife(-100);
//                            }
                        } else if(moveState == MOVE_STATE.THROWING_CHARACTER){
                            setPosition(body.getPosition().interpolate(targetInterpolation,
                                    stateTime/current_animation.getAnimationDuration(),
                                    Interpolation.exp10Out));

                            if (current_animation.isAnimationFinished(stateTime)){
                                moveState = MOVE_STATE.FALLING;

                                throwObject(
                                        (getViewDirection()==VIEW_DIRECTION.LEFT?225:315)*MathUtils.degreesToRadians
                                        ,2060.0f);
                                getBody().setLinearVelocity(0, 0);
                                myFeet.deepFlush();
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
                            playSound("respawn");
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
            pickedObject.getBody().setActive(false);
        }

    }



    private void startProgressBar(int maxProgress, int increment, String button, float y_offset) {
        progressBar.startProgressBar(maxProgress, increment, button, y_offset);
    }

    public void throwObject(float angle, float force){

        if (carriedObject != null) {
            if (carriedObject.getClass().equals(Character.class)) {
                Character other = (Character) carriedObject;
                if (force > 50) {
                    weapon.goToAttackState(ATTACK_STATE.THROWING);
                    other.setMoveState(MOVE_STATE.THROWED);
                    other.goToPickingUpState(PICKUP_STATE.NORMAL);
                } else {
                    weapon.goToAttackState(ATTACK_STATE.NOTATTACKING);
                    other.setMoveState(MOVE_STATE.FALLING);
                    other.goToPickingUpState(PICKUP_STATE.NORMAL);
                }
                this.getPlayer().resetChallengePressCount();
                other.getPlayer().resetChallengePressCount();
                this.getProgressBar().reset();
                other.getProgressBar().reset();
                // do the same to the other character/player:
            }
            carriedObject.getBody().setActive(true);
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
        killedBy = null;
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
//                FIXME:: there are still bug when getting hurt while carrying a character!!
                if(killedBy == DAMAGE_TYPE.CRUSHING) {
                    timeFrontFX = 0;
                    playSound("impact_to_ground");
                    gamescreen.makeAnnouncement(ANNOUNCEMENT.LONG_TERM_KO, "", "");
                    if (marks==MAXMARKS) {
                        respawnTime = RESPAWNTIME + 4*marks * Constants.MARKPENALTY;
                        // thus the effect of mark max is to multiply by 2
                    } else {
                        respawnTime = RESPAWNTIME + 2*marks * Constants.MARKPENALTY;
                    }
                }
                else {
                    if (marks==MAXMARKS) {
                        respawnTime = RESPAWNTIME + 2*marks * Constants.MARKPENALTY;
                    } else {
                        respawnTime = RESPAWNTIME + marks * Constants.MARKPENALTY;
                    }
                }
                if(killedBy == DAMAGE_TYPE.BURNING_LANCE){
                    gamescreen.makeAnnouncement(ANNOUNCEMENT.BURNING_KO, "", "");
                }
                if(killedBy == DAMAGE_TYPE.SLAP){
                    gamescreen.makeAnnouncement(ANNOUNCEMENT.SLAP_KO, lastKiller, getPlayer().getName());
                }
                if(killedBy == DAMAGE_TYPE.LANCE){
                    gamescreen.makeAnnouncement(ANNOUNCEMENT.KO, lastKiller, getPlayer().getName());
                }
                timerToAwakeState = 0;
                Gdx.app.debug("Character", "Death Event");
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
        if (this.marks == MAXMARKS)
            gamescreen.makeAnnouncement(ANNOUNCEMENT.WEAK_PRABBIT, this.getPlayer().getName(), null);
    }

    public void addMarks(int marks){
        if (this.marks < MAXMARKS) {
            this.marks += marks;
            if (this.marks == MAXMARKS)
                gamescreen.makeAnnouncement(ANNOUNCEMENT.WEAK_PRABBIT, this.getPlayer().getName(), null);
        }

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
        playSound("laugh_kill");
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
            playSound("hurt");
            dropObjects();
        }

    }
    @Override
    public void addDamage(float damage, Constants.DAMAGE_TYPE damagedBy){
        super.addDamage(damage, damagedBy);
        if(life < 0 & damage > 0)
            // damage > 0 is to avoid being ' killed' be an interactive platform
            // indeed all interactive objects call addDamage, and inoffensive platforms call addDamage(0)
            killedBy = damagedBy;

    }



    public actionProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void setCarrier(Character newCarrier) {
        // the newCarrier use its object to take the new one
        if (awakeState == DEAD){
            return;
        }

        if (newCarrier == null){
            if(this.Carrier !=null) {
                this.Carrier.carriedObject = null;
                Carrier.myLeftHandContact.deepFlush();
                Carrier.myRightHandContact.deepFlush();
                this.Carrier = null;
            }
            // if the object is not carried anymore, it needs to becomes dynamics/activated again:
            body.setType(BodyDef.BodyType.DynamicBody);
            body.setActive(true);
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
            myFeet.deepFlush(); // to deepFlush
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

    public ContactData getCurrentHandContact() {
        return currentHandContact;
    }

    public void setCurrentHandContact(ContactData currentHandContact) {
        this.currentHandContact = currentHandContact;
    }

    public void setTargetInterpolation(float x, float y) {
        targetInterpolation = new Vector2(body.getPosition().x + x, body.getPosition().y + y);
    }
}
