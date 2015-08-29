package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.collectable.Crown;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

import static com.mygdx.rope.util.Constants.*;

public class Character extends GameObject {
    private float sleepingTime;
    public Player player;
    public Animation animWalk;
	//private Animation animNormal;
	private Animation animJump;
	private Animation animFalling;
    public int marks;
    public JUMP_STATE previousJumpState;
    public JUMP_STATE jumpState;
    public MOVE_STATE moveState;
    public AWAKE_STATE awakeState;
    public Fixture myFeet;
    public Fixture myBodyFixture;
    public Fixture currentHand;
    private Fixture myLeftHand;
    private Fixture myRightHand;
    private Body carriedObject;
    private Body crownBody;
    public String name;
//    public float score;
    private float awakeStateTimer; // a player can be Knock out and lose control of his character
    private Animation animSleeping;
    private Animation animDying;
    public Body lastGroundedBody;
    public String color_texture;
    private Texture MarkTexture;
    private float respawnTime;
    //private ContactData mainBoxContact;

    public Character(GameScreen game, Vector2 position, String name_texture ){
        super(game, position, new Vector2(1, 0.9f), 0, name_texture);
        respawnTime = -1;
        color_texture = name_texture;
        marks = 0;
        player = null;
        lastGroundedBody = null;
        isKillable = true;
        this.name = name;
        previousJumpState = JUMP_STATE.FALLING;
        jumpState = JUMP_STATE.FALLING;
        moveState = MOVE_STATE.NORMAL;
        awakeState = AWAKE_STATE.AWAKE;
        carriedObject = null;
        crownBody = null;
        awakeStateTimer = 0;
        sleepingTime = 0;
	}

    @Override
    public void initFilter() {
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
        filter.maskBits = (short)( CATEGORY.get("Object") | CATEGORY.get("Scenery") | CATEGORY.get("Player") );//remove the status object from LanceManager! <-- not clear :/
        this.myRightHand.setFilterData(filter);

        filter = new Filter();
        filter.categoryBits = CATEGORY.get("Sensor");
        filter.maskBits = (short)( CATEGORY.get("Scenery") | CATEGORY.get("AttachedObject") );
        this.myFeet.setFilterData(filter);
    }

    public void setJumpState(JUMP_STATE jumpState) {
        if(this.jumpState != jumpState) {
            this.previousJumpState = this.jumpState;
            this.jumpState = jumpState;
            Gdx.app.debug("Character", "jumpState from "+ previousJumpState + " to " + jumpState);
        }
    }

    @Override
    public void initAnimation(String name_texture) {
        // -- Marks:
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1.0f);
        pixmap.fillRectangle(0, 0, 1, 2);
        MarkTexture =  new Texture(pixmap);
        // -- Texture:
        Array<AtlasRegion> regions = null;
        // anim normal:
        regions = atlas.findRegions("Piaf_"+name_texture+"_stand");
        if (regions != null)
            main_animation = new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP);
            //animNormal = new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP);

        regions = atlas.findRegions("Piaf_" + name_texture + "_walk");
        if (regions != null)
            animWalk = new Animation(1.0f/4.0f, regions, Animation.PlayMode.LOOP);

        regions = atlas.findRegions("Piaf_"+name_texture+"_rise");
        if (regions != null)
            animJump = new Animation(1.0f/4.0f, regions, Animation.PlayMode.LOOP);

        regions = atlas.findRegions("Piaf_"+name_texture+"_fall");
        if (regions != null)
            animFalling = new Animation(1.0f/4.0f, regions, Animation.PlayMode.LOOP);

        regions = atlas.findRegions("Piaf_"+name_texture+"_dead");
        if (regions != null)
            animDying = new Animation(1.0f/4.0f, regions, Animation.PlayMode.NORMAL);

        regions = atlas.findRegions("Piaf_" + name_texture + "_sleeping");
        if (regions != null)
            animSleeping = new Animation(1.0f/4.0f, regions, Animation.PlayMode.LOOP);

        this.setAnimation(animFalling);
        stateTime = 0;
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
        if(currentImmunity>0){
            setColor(1f*Math.abs(MathUtils.sin(currentImmunity*MathUtils.PI/immunityReset)), 0.25f, 0.25f, 0);
        }
        else{
            setColor(1, 1, 1, 1);
        }
        // that's updating the jump states used by the "animation updater" and the renderer:
        currentHand = this.viewDirection == VIEW_DIRECTION.LEFT ? myLeftHand:myRightHand; // use by the renderer directly
        // Animation updater: (could be an outside component)
        updateTheStateMachine(deltaTime);
        // Damage from collision management: (might part of the GameObject themself), or should be a component...
        if(mainBoxContact.isTouched()){
            //Gdx.app.debug("Player", "impulse of "+ mainBoxContact.getLastImpulse());
            if(mainBoxContact.getLastImpulse() > 110.0 && moveState == MOVE_STATE.THROWED){ // threshold before to get injured
                addToLife(-101);  // the injure could be proportionnal to the chock, don't care about invincibility frame
                //goToSleepFor(1.0f);  // knock out from the collision should be optionnal
                Gdx.app.debug("Player", "impulse of "+ mainBoxContact.getLastImpulse());
            }
        }
        // roughly that part look like updating Child objects, but we dont have a Child system :P so we are managing specifically a carried body and a crown.
        // Note that the child system would be useful also to attached a weapon to the player and to throw bullets from this weapon...
        if (carriedObject != null){
            carriedObject.setTransform(this.position.x + 0.5f * ((viewDirection == VIEW_DIRECTION.LEFT)?-1:1),
                    this.position.y + 0.5f, 0);
        }
        if (crownBody != null){
            crownBody.setTransform(this.position.x,
                    this.position.y + this.dimension.y, 0);
        }
        return super.update(deltaTime);

    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        for (int i = 0; i < Constants.MAXMARKS; i++) {
            if (i < marks)
                batch.setColor(1,1,1,1); // markers in white
            else
                batch.setColor(0.4f, 0.4f, 0.4f, 1f); // not marked in grey
            batch.draw(MarkTexture, position.x+0.15f*i, position.y+dimension.y + 0.2f, 0.1f, 0.2f);
            batch.setColor(1, 1, 1, 1); // markers in white
        }

    }

    private void updateTheStateMachine(float deltaTime) {
        ContactData sensorData = (ContactData) myFeet.getUserData();
        if (sensorData.isTouched()){
            lastGroundedBody = sensorData.getTouchedFixtures().peek().getBody(); // returns the last item, but do not remove it
            //Gdx.app.debug("Player", "Platform idling on: "+ lastGroundedBody);
            if(moveState == MOVE_STATE.THROWED)
                moveState = MOVE_STATE.NORMAL;
            if (Math.abs(this.body.getLinearVelocity().x - lastGroundedBody.getLinearVelocity().x)< 0.001)
                setJumpState(JUMP_STATE.IDLE);
            else
                setJumpState(JUMP_STATE.GROUNDED);
        }
        else {
            if (this.body.getLinearVelocity().y > 0.1 && jumpState != JUMP_STATE.FALLING) // make sure we go to fall when we reset the speed
                setJumpState(JUMP_STATE.RISING);
            else
                setJumpState(JUMP_STATE.FALLING);
        }
        gamescreen.addDebugText("\n " + getPlayer().getName() + " JUMP_STATE: " + jumpState);
        switch (awakeState){
            case AWAKE:
                if (justDied())
                    break;
                switch (jumpState){
                    case RISING:
                        if (current_animation != animJump) {
                            setAnimation(animJump);
                            myBodyFixture.setFriction(0);
                            Gdx.app.debug("Player", "RISING, Friction: " + myBodyFixture.getFriction());
                        }
                        break;
                    case FALLING:
                        if(current_animation != animFalling) {
                            setAnimation(animFalling);
                            myBodyFixture.setFriction(0);
                            Gdx.app.debug("Player", "FALLING, Friction:  " + myBodyFixture.getFriction());
                        }
                        break;
                    case GROUNDED:
                        if(current_animation!= animWalk) {
                            setAnimation(animWalk);
                            myBodyFixture.setFriction(0.01f);
                            Gdx.app.debug("Player", "GROUNDED, Friction: " + myBodyFixture.getFriction());
                        }
                        break;
                    case IDLE:
                        if(current_animation!= main_animation){
                            setAnimation(main_animation);
                            myBodyFixture.setFriction(1.0f);
                            Gdx.app.debug("Player", "IDLE, Friction: " + myBodyFixture.getFriction());
                        }
                        break;
                }

                break;
            case SLEEPING:
                if (justDied())
                    break;
                if (current_animation != animSleeping)
                    setAnimation(animSleeping);

                awakeStateTimer += deltaTime;
                if (awakeStateTimer > sleepingTime) {
                    awakeState = AWAKE_STATE.AWAKE;
                    awakeStateTimer = 0;
                    sleepingTime = 0; // no accumulation of the sleeping time during the round
                }
                break;
            case DEAD:
                if (current_animation != animDying)
                    setAnimation(animDying);
                awakeStateTimer += deltaTime;
                if (awakeStateTimer > respawnTime) {
                    reSpawn();
                }
                break;

        }
    }

    public boolean hasCarriedObject(){
        return this.carriedObject != null;
    }

    public boolean hasTheCrown() {return this.crownBody != null;}

    public void setCarriedObject(Body b){
        if (b == null){
            GameObject g = (GameObject) carriedObject.getUserData();
            g.setCarrier(null);
            carriedObject = null;
            moveState = MOVE_STATE.NORMAL;
//            ContactData d = (ContactData) myLeftHand.getUserData();
//            d.deepFlush();
//            d = (ContactData) myRightHand.getUserData();
//            d.deepFlush();
            return;
        }
        GameObject newItem = (GameObject) b.getUserData();
        if (carriedObject != null && !newItem.getClass().equals(Crown.class)){ // if you already have an object in hand, you throw the current one and get the new one
            useCarriedBody(45.0f, 50.0f);
            //b.setType(BodyDef.BodyType.KinematicBody);
        }
        if (newItem.getClass().equals(Character.class)){
            Character p = (Character) newItem;
            if(p.awakeState!=AWAKE_STATE.DEAD) {
                p.moveState = MOVE_STATE.PICKUPCHALLENGED;
                moveState = MOVE_STATE.PICKUPCHALLENGER;
            }
            else{
                moveState = MOVE_STATE.PICKINGUP;
                p.moveState = MOVE_STATE.NORMAL;
            }
            ContactData dd = (ContactData) p.myFeet.getUserData();
            dd.deepFlush(); // to deepFlush
            carriedObject = b;
            p.setCarrier(this);


        }
        else if (newItem.getClass().equals(Crown.class)){
            Sound stealCrown = gamescreen.assetManager.getRandom("laugh_steal");
            stealCrown.play();
            if (newItem.setCarrier(this)) // weird way to express it, the boolean if unnecessary
                crownBody = b;
        }

        // we will do: else if ((newItem.getClass().equals(Item.class)
//        else {
//            carriedObject = b;
//            newItem.setCarrier(this);
//            moveState = MOVE_STATE.PICKINGUP;
//        }

    }

    public void useCarriedBody(float angle, float force){
        if (carriedObject != null) {
            GameObject gobj = (GameObject) carriedObject.getUserData();
            gobj.setCarrier(null);

            if (gobj.getClass().equals(Character.class)) {
                Character p = (Character) gobj;
                p.moveState = MOVE_STATE.THROWED;
            }
            carriedObject.applyLinearImpulse(MathUtils.cos(angle) * force,
                    MathUtils.sin(angle) * force, 0.5f, 0.5f, true);
            carriedObject = null;
        }
        moveState = MOVE_STATE.NORMAL;
        //ContactData d = (ContactData) myLeftHand.getUserData();
        //d.flush();
        //d = (ContactData) myRightHand.getUserData();
        //d.flush();
//        d = (ContactData) currentHand.getUserData();
//        d.flush();

    }

    public void dropTheCrown(float angle){
        if (crownBody != null) {
            crownBody.setType(BodyDef.BodyType.DynamicBody);
            crownBody.applyForceToCenter(new Vector2(MathUtils.cos(angle),
                    MathUtils.sin(angle)).scl(20 * 1000), true);
            GameObject crownObject = (GameObject) crownBody.getUserData();
            crownObject.setCarrier(null);
            crownBody = null;
        }

    }

    private void reSpawn() {
        for ( JointEdge jointEdge: body.getJointList()) { // iterate over the joints that the character is involved in
            GameObject attachedObject = (GameObject) jointEdge.other.getUserData(); // find the *other* member of the joint
            if (attachedObject != null) // kill the other member
                attachedObject.setLife(-1);
        }
        GameObject o = this.getCarrier();
        if(o != null)
            this.getCarrier().setCarriedObject(null);
        mainBoxContact.deepFlush();
        this.setPosition(gamescreen.getSpawnPosition());
        awakeState = AWAKE_STATE.AWAKE;
        life = 100.0f;
        setMarks(0);
        awakeStateTimer = 0;
        body.setTransform(body.getPosition(), 0);
    }

    private boolean justDied() {
        if (life <= 0 ){
            Sound killSound = gamescreen.assetManager.getRandom("laugh_kill");
            killSound.play();
            awakeState = AWAKE_STATE.DEAD;
            awakeStateTimer = 0;
            Gdx.app.debug("Character", "Death Event");
            respawnTime = RESPAWNTIME + marks*Constants.MARKPENALTY;
            //body.setTransform(body.getPosition(),MathUtils.PI/2.0f);
            //body.setType(K);
            dropObjects();
            for(Fixture fixt : body.getFixtureList()){
                ContactData data = (ContactData) fixt.getUserData();
                if(data != null)
                    data.deepFlush(); // should be in the respawn function!!!
                    //data.flush();
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkIfToDestroy() {
        return false;
    }

    public float getSleepingTime() {
        return sleepingTime;
    }

    public void addSleepingTime(float sleepingTime) {
        this.sleepingTime += sleepingTime;
    }

    public void setSleepingTime(float sleepingTime) {
        this.sleepingTime = sleepingTime;
    }

    public void goToSleepFor(float time){
        if(awakeState == AWAKE_STATE.AWAKE) {
            dropObjects();
            awakeState = Constants.AWAKE_STATE.SLEEPING;
            addSleepingTime(time);
        }
    }

    @Override
    public float getLife() {
            return life;
    }

    public float getRespawnTime(){
        if (awakeState == AWAKE_STATE.DEAD)
            return (respawnTime - awakeStateTimer);
        else
            return -1;
    }

    public void dropObjects() {
        useCarriedBody(45.0f, 50.0f);
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
}
