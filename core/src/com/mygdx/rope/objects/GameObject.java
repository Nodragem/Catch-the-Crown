package com.mygdx.rope.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

import static com.mygdx.rope.util.Constants.DAMAGE_STATE.IMMUNE;
import static com.mygdx.rope.util.Constants.DAMAGE_STATE.NOT_IMMUNE;

public class GameObject implements Updatable, Renderable {
    //public final static TextureAtlas atlas = new TextureAtlas("texture_obj.pack"); // all the game object has access to a class Atlas
    public final static TextureAtlas atlas = new TextureAtlas("texture_obj.atlas"); // all the game object has access to a class Atlas
    public boolean isKillable;
    public Integer ID = null;
    public Array<Float> color;
    public Constants.DAMAGE_STATE damageState;
    protected Sound soundCache;
    public float currentImmunity;
    public float immunityReset;
    protected float givenDamage;
    protected float bufferReceivedDamage;
    protected float timeStepDamage;
    public GameScreenTournament gamescreen;
    public Body parentBody = null;
    public Array <GameObject> children;
    public int myRenderID;
    public boolean isVisible;
    public Body body;
    public BodyType defaultBodyType; // sometimes we need to change the bodyType temporarily, so we need to remember what's the default
    public ContactData mainBoxContact;
    protected int mainFixtureIndex;
	public Animation current_animation;
    private String currentAnimationName;
    protected ObjectMap<String, Animation> animations;
    private Constants.VIEW_DIRECTION viewDirection;
	public Vector2 position;
	public Vector2 rposition; // in polar coordinate
	public Vector2 dimension;
	public Vector2 origin;
	public Vector2 scale;
	public float rotation;
    private float rrotation;
	public float stateTime;
    public float life;
//    public Character Carrier = null;
    public Constants.ACTIVE_STATE activeState;
    public Constants.ACTIVE_STATE previousActiveState = null;
    protected boolean todispose;
    private float soundVol;
    private float soundDelay;
    private float soundTime;

    // we should separated the GameObject from their textures :/ like that the GameObjects of same type would use the same texture set, instead of loading several time the same textures in memory

    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension,
                      float angle, String objectDataID, Filter filter){ // angle in radians
        todispose = false;
        gamescreen = game;
        color = new Array<Float>(new Float[]{1.0f,1.0f,1.0f,1.0f});
        children = new Array<GameObject>(1);
        //stateTime = 0;
        life = 100;
        isKillable =  false;
        isVisible = true;
        damageState = NOT_IMMUNE;
        currentImmunity = 0.0f;
        immunityReset = 0.5f; // if an object is in his immune period, he can't be injured
        bufferReceivedDamage = 0.0f;
        givenDamage = 0.0f;
        timeStepDamage = 0.0f;
        World b2world = game.getB2World();
        this.viewDirection = Constants.VIEW_DIRECTION.RIGHT;
        //Gdx.app.debug("ObjGame", "Position: "+position);
        this.dimension = dimension;
//        origin = new Vector2(0.5f,0.5f); // the origin is really at the origin
        origin = new Vector2(0f,0f); // the origin is really at the origin
        scale = new Vector2(1, 1);
        this.position = position; // position and rotation may be not useful and redundant with the body
        this.rposition = new Vector2(0,0);
        rotation = angle * MathUtils.degreesToRadians; // in radians
        rrotation = 0; // radians
        BodyDef def = new BodyDef();
        this.body = b2world.createBody(def);
        this.body.setTransform(this.position, rotation);
        this.body.setType(BodyType.DynamicBody);
        this.body.setUserData(this);
        myRenderID = gamescreen.getObjectsToRender().size;
        gamescreen.getObjectsToRender().add(this);
        gamescreen.getObjectsToUpdate().add(this);
        Gdx.app.debug("GameObject", this.getClass() + " created with ID " +myRenderID);

        animations = new ObjectMap<String, Animation>(3);
        mainFixtureIndex = 0;
        if(objectDataID == null) {
            initAnimation();
            initFixture();
            initCollisionMask();
        } else if (!objectDataID.equals("No Init")){
            initAnimation(objectDataID, "");
            initFixture(objectDataID);
            initCollisionMask(objectDataID);
        }
        if (this.body.getFixtureList().size >0) { // > mainFixtureIndex
            mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        }

        this.goToActivation();

    }
    public GameObject(GameScreenTournament game, Vector2 position,  Vector2 dimension, float angle, String objectDataID) {
        this(game, position, dimension, angle, objectDataID, null);
    }
	
    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension, float angle) {
        this(game, position, dimension, angle, null, null);
	}

    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension ) {
        this(game, position, dimension, 0, null, null);
	}

    public GameObject(GameScreenTournament game, Vector2 position ) {
        this(game, position, new Vector2(1, 1.0f), 0, null, null);
    }


    public void initCollisionMask() {
            Filter defaultFilter = new Filter();
            defaultFilter.categoryBits = Constants.CATEGORY.get("Object");
            defaultFilter.maskBits = Constants.MASK.get("Object");
            this.body.getFixtureList().get(0).setFilterData(defaultFilter);
    }

    public void initFixture() {
        PolygonShape p = new PolygonShape();
        p.setAsBox(dimension.x / 2.0f, dimension.y / 2.0f, new Vector2(dimension.x / 2, dimension.y / 2), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 50;
        fd.restitution = 0.0f;
        fd.friction = 0.5f;
        //fd.isSensor = isSensor;
        this.body.createFixture(fd);
        defaultBodyType = body.getType();
        p.dispose();
    }

    public void initFixture(String objectDataID) {
        JsonValue objectInfo = gamescreen.getObjectDataBase().get(objectDataID);
        if (objectInfo.isArray()) {
            for (JsonValue fixtureInfo : objectInfo) {
                createFixtureFromJson(fixtureInfo);
            }
        } else {
            createFixtureFromJson(objectInfo);
        }
    }

    public void createFixtureFromJson(JsonValue info){ // maybe coulb be static
        PolygonShape p = new PolygonShape();
        dimension.x = info.getFloat("dimensionx", dimension.x);
        dimension.y = info.getFloat("dimensiony", dimension.y);
        float scaleX = info.getFloat("scalex", 1f); // if you prefer to get the fixture dimension as a scale of the object dimension. usefull for switcher
        float scaleY =  info.getFloat("scaley", 1f);
        float scale_offsetX = info.getFloat("scale_offsetx", 0f); // if you prefer to get the fixture dimension as a scale of the object dimension. usefull for switcher
        float scale_offsetY =  info.getFloat("scale_offsety", 0f);
        float offsetx = info.getFloat("offsetx", 0.5f);
        float offsety = info.getFloat("offsety", 0.5f);
        p.setAsBox(scaleX*dimension.x / 2.0f + scale_offsetX,
                scaleY * dimension.y / 2.0f + scale_offsetY,
                new Vector2(offsetx*dimension.x , offsety* dimension.y), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = info.getFloat("density", 5f);
        fd.restitution = info.getFloat("restitution", 0.0f);
        fd.friction = info.getFloat("friction", 0.5f);
        fd.isSensor = info.getBoolean("sensor", false);
        this.body.createFixture(fd);
        body.setFixedRotation(info.getBoolean("isRotation", true));
        String bodyTypeString = info.getString("bodyType", null);
        if (bodyTypeString != null){
            BodyType bodyType = BodyType.valueOf(bodyTypeString);
            body.setType(bodyType);
            defaultBodyType = bodyType;
        }
        p.dispose();
    }

    public void initCollisionMask(String objectDataID) {
        JsonValue objectInfo = gamescreen.getObjectDataBase().get(objectDataID);
        if (objectInfo.isArray()) {
            int i = 0;
            for (JsonValue info : objectInfo) {
                createCollisionMaskFromJson(info, i);
                i++;
            }
        } else {
            createCollisionMaskFromJson(objectInfo, mainFixtureIndex);
        }

    }

    private void createCollisionMaskFromJson(JsonValue info, int fixtureIndex){
        Filter defaultFilter = new Filter();
//        defaultFilter.categoryBits = Constants.CATEGORY.get("Sensor");
//        defaultFilter.maskBits = Constants.MASK.get("Sensor"); // make that data driven
        String Mask = info.getString("Mask", null);
        defaultFilter.maskBits = 0; // a ghost
        if (Mask != null){
            defaultFilter.maskBits = Constants.MASK.get(Mask);
        }
        Gdx.app.debug("GameObject", "Mask from Json: " + Mask + " equals " + Constants.MASK.get(Mask));
        JsonValue CustomMask = info.get("CustomMask");
        if (CustomMask != null && CustomMask.isArray()){
            defaultFilter.maskBits = 0;
            for (JsonValue category:CustomMask) {
                defaultFilter.maskBits |= Constants.CATEGORY.get(category.asString());
                Gdx.app.debug("CustomMask: ", "add "+category.asString());
            }
        }

        String Category = info.getString("Category", null);
        if (Category != null){
            defaultFilter.categoryBits = Constants.CATEGORY.get(Category);
        }
        else {
            defaultFilter.categoryBits = Constants.CATEGORY.get("Object");
        }

        this.body.getFixtureList().get(fixtureIndex).setFilterData(defaultFilter);
    }

    public void initAnimation() {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 1, 0, 0.5f);
            pixmap.fillCircle(32, 32, 32);
            animations.put("Main", new Animation(1 / 6.0f, new TextureRegion(new Texture(pixmap))) );
            setAnimation("Main");
            pixmap.dispose();
            stateTime = 0;
    }

    public void initAnimation(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fillRectangle(0, 0, 64, 64);
        animations.put("Main",
                new Animation(1 / 6.0f, new TextureRegion(new Texture(pixmap))));
        setAnimation("Main");
        pixmap.dispose();
        stateTime = 0;
    }

    public void initAnimation(String objectDataID, String color_texture) {
        JsonValue objectInfo = gamescreen.getObjectDataBase().get(objectDataID);
        if(objectInfo == null) Gdx.app.error("GameObject",
                        "Object ID:"+objectDataID+
                        " not found in the json DataBase");
        String texture_name = objectInfo.getString("texture_name", null);
        if (texture_name == null)
            Gdx.app.error("GameObject", "No Texture Name found in the json DataBase");

        Array<String> animation_names;
        if (objectInfo.has("animation_names")) {
            animation_names = new Array<String>(
                    objectInfo.get("animation_names").asStringArray());
        } else {
            animation_names = new Array<String>(
                    new String[]{"Main", "Onset", "Offset"});
        }
        Array<String> animation_loops;
        if (objectInfo.has("animation_loops")) {
            animation_loops = new Array<String>(
                    objectInfo.get("animation_loops").asStringArray());
        } else {
            animation_loops = new Array<String>(
                    new String[]{"LOOP", "NORMAL", "NORMAL"});
        }
        FloatArray animation_spf;
        if (objectInfo.has("animation_spf")) {
            animation_spf = new FloatArray(
                    objectInfo.get("animation_spf").asFloatArray());
                    // --> spf: second per frame
        } else {
            animation_spf = new FloatArray (
                    new float[]{0.2f, 0.2f, 0.2f});
        }

        if(!color_texture.equals(""))
            color_texture = "_"+color_texture;


        Array<TextureAtlas.AtlasRegion> regions = null;
        for (int i = 0; i < animation_names.size; i++) {
            Gdx.app.debug("Search Atlas", texture_name + color_texture + "_" + animation_names.get(i));
            regions = atlas.findRegions(texture_name + color_texture + "_" + animation_names.get(i));
            Gdx.app.debug("Search Atlas", " " + regions);
            if (regions.size >0) {
                Gdx.app.debug("Search Atlas", " " + animation_names.get(i) +
                        " will play as "+animation_loops.get(i));
                animations.put(animation_names.get(i),
                        new Animation(animation_spf.get(i), regions,
                                Animation.PlayMode.valueOf(animation_loops.get(i))
                        )
                );
            }
            else { // create a place holder in case no texture is found:
                Pixmap pixmap1 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pixmap1.setColor(1, 0, 0, 1.0f);
                pixmap1.fillRectangle(0, 0, 32, 32);
                Pixmap pixmap2 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pixmap2.setColor(0, 0, 1, 1.0f);
                pixmap2.fillRectangle(0, 0, 32, 32);
                Array <TextureRegion> placeHolder = new Array<TextureRegion>(
                        new TextureRegion[]{  new TextureRegion(new Texture(pixmap1)), new TextureRegion(new Texture(pixmap2)) }
                );
                Gdx.app.error("GameObject", animation_names.get(i) +
                        " could not be created from the texture atlas!");
                animations.put(animation_names.get(i),
                        new Animation(animation_spf.get(i), placeHolder)
                );

            }

        }
        setAnimation("Main");

    }


    public Animation getAnimation(String animName) {
        return animations.get(animName);
    }

    public void setAnimation(String animation_name){
        current_animation = animations.get(animation_name);
        currentAnimationName = animation_name;
        if (current_animation == null)
            Gdx.app.error("GameObject.setAnimation()", "can't find animation "+animation_name);
		stateTime = 0;
	}

    @Override
	public boolean update(float deltaTime) {
        stateTime += deltaTime;
        if (soundCache != null && soundTime > soundDelay){
            soundDelay = 0;
            soundTime = 0;
            soundCache.play(soundVol);
            soundCache = null;
        } else {
            soundTime+=deltaTime;
        }
        // we update the position even when the object is not activated
        if (parentBody == null || body.getType() == BodyType.DynamicBody) {
            position.set(body.getPosition()).sub(origin); // the sub is particularly useful for the lances attached, because we changed their origin
            rotation = body.getAngle();
        }
        else{
            body.setTransform(
                    parentBody.getPosition().x + rposition.y*MathUtils.cos(rposition.x + parentBody.getAngle() ),
                    parentBody.getPosition().y + rposition.y*MathUtils.sin(rposition.x + parentBody.getAngle() ),
                    (parentBody.getAngle() + rrotation)
            );
            rotation = body.getAngle();
            position.set(body.getPosition()).sub(origin);
            // \--> the sub is particularly useful for the lances attached, because we changed their origin
            //this.isVisible = parentBody.isVisible;
        }

        switch (activeState){
            case ACTIVATED:
                if(mainBoxContact.isTouched()){
                    onCollision(true);
                    //mainBoxContact.flush();
                }
                switch (damageState){
                    case IMMUNE:
                        currentImmunity -= 1.0 * deltaTime; // decrease of one per second;
                        if (currentImmunity<0)
                            goToImmuneState(NOT_IMMUNE);
                        break;
                    case NOT_IMMUNE:
                        break;
                }
                break;
            case ACTIVATION:
                if (previousActiveState != Constants.ACTIVE_STATE.ACTIVATION)
                    setAnimation("Onset");
                if(current_animation.isAnimationFinished(stateTime)) {
                    Gdx.app.debug("GameObject", "activated!");
                    //activationActions();
                    this.setActivate(true);
                    setAnimation("Main");
                }
                break;
            case DESACTIVATION:
                if (previousActiveState != Constants.ACTIVE_STATE.DESACTIVATION)
                    setAnimation("Offset");
                if(current_animation.isAnimationFinished(stateTime)) {
                    //desactivationActions();
                    this.setActivate(false);
                    setAnimation("Main");
                }
                break;
        }
        previousActiveState = activeState;
        return checkIfToDestroy() || todispose;

	}

    public Array<GameObject> onCollision(boolean dealDamage) {
        Array<GameObject> touchedObjects = new Array<GameObject>(1);
        for(Fixture touchedFixture: mainBoxContact.getTouchedFixtures()){
            GameObject touchedObject = (GameObject) touchedFixture.getBody().getUserData();
            if (touchedObject != null){
                if(!touchedObjects.contains(touchedObject, true)) { // if we touch two fixtures of the same object, we won't damage it twice
                    touchedObject.addDamage(dealDamage?givenDamage:0);
                    touchedObjects.add(touchedObject);
                }
            }
        }
        return touchedObjects;
    }

    private void goToImmuneState(Constants.DAMAGE_STATE state) {
        damageState = state;
        switch (state){
            case IMMUNE:
                currentImmunity = immunityReset;
                break;
            case NOT_IMMUNE:
                currentImmunity = 0;
                break;
        }

    }

    public void render(SpriteBatch batch) {
        if(isVisible && activeState != Constants.ACTIVE_STATE.DESACTIVATED) {
            TextureRegion reg = null;
            batch.setColor(color.get(0), color.get(1), color.get(2), color.get(3));
            reg = current_animation.getKeyFrame(stateTime);
            batch.draw(reg.getTexture(), // we called this long draw() method just to get access the flip argument...
                    position.x, position.y,
                    origin.x, origin.y,
                    dimension.x, dimension.y,
                    1, 1,
                    rotation*MathUtils.radiansToDegrees,
                    reg.getRegionX(), reg.getRegionY(),
                    reg.getRegionWidth(), reg.getRegionHeight(),
                    viewDirection == Constants.VIEW_DIRECTION.LEFT, viewDirection == Constants.VIEW_DIRECTION.DOWN);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public void setColor(float r, float g, float b, float a){
        color.set(0,r); color.set(1,g); color.set(2,b); color.set(3,a);
    }

    public boolean checkIfToDestroy() {
        return life < 0;
    }

    public void setPosition(float x, float y) {
        this.position.set(x,y);
        //this.origin.set(x,y).add(originshift);
        this.body.setTransform(this.position, 0); // should return the current rotation
    }

    public void setPosition(Vector2 v) {
        this.position.set(v.x,v.y);
        //this.origin.set(v.x,v.y).add(originshift);
        this.body.setTransform(this.position, 0);
    }

    public void setPosition(Vector2 v, boolean resetSpeed) {
        this.position.set(v.x,v.y);
        //this.origin.set(v.x,v.y).add(originshift);
        this.body.setTransform(this.position, 0);
        if(resetSpeed)
            this.body.setLinearVelocity(0, 0);
    }

    public void setOrigin(float x, float y) {
        this.origin.x = x;
        this.origin.y = y;
    }

    public void setTransform(float x, float y, float angle){
        body.setTransform(x, y, angle);
    }

    protected void addToLife(float f){
        life+=f;
    }

    public void addDamage(float damage){
        if(damageState == NOT_IMMUNE && isKillable && damage > 0) {
            addToLife(-damage);
            goToImmuneState(IMMUNE);
        }
    }

    public void setLife(float life) {
        this.life = life;
    }

    public float getLife() {
        return life;
    }

    public GameScreenTournament getGamescreen() {
        return gamescreen;
    }

//    public boolean setCarrier(Character character) { // maybe this part should be in player and not here
//        if(character == null)
//            body.setType(BodyType.DynamicBody);
//        else
//            body.setType(BodyType.KinematicBody);
//        this.Carrier = character;
//        return true; //suceessfull
//    }

//    public Character getCarrier() {
//        return Carrier;
//    }

    public Body getBody() {
        return body;
    }

    protected void setActivate(boolean b){
        activeState = (b? Constants.ACTIVE_STATE.ACTIVATED: Constants.ACTIVE_STATE.DESACTIVATED);
        body.setActive(b);
        if(!b){ // if we desactivate, we need to flush
            for (Fixture myfixture : body.getFixtureList()) {
                ContactData mydata = (ContactData) myfixture.getUserData();
                if (mydata != null)
                    mydata.deepFlush();
            }
        }
    }

    public void goToDesactivation(){
        if (animations.get("Offset") != null){
            // allows a delayed desactivation after playing a last animation
            // (like an explosion)
            activeState = Constants.ACTIVE_STATE.DESACTIVATION;
        }
        else {
            setActivate(false);
        }
        if(children.size>0){
            for (GameObject child:children)
                child.goToDesactivation();
        }
    }

    public void goToActivation(){
        if (animations.get("Onset") != null){
            activeState = Constants.ACTIVE_STATE.ACTIVATION;
        }
        else {
            setActivate(true);
        }
        if(children.size>0){
            for (GameObject child:children)
                child.goToActivation();
        }
    }

    public void setParentBody(Body parentBody, Vector2 rposition, Float rangle) {
        this.parentBody = parentBody;
        if(parentBody != null) {
            GameObject parentObject = (GameObject) parentBody.getUserData();
            if(parentObject != null){
                parentObject.addChild(this);
            }
            if (rposition == null) {
                this.rposition.set(
                        MathUtils.atan2(body.getPosition().y - parentBody.getPosition().y,
                                body.getPosition().x - parentBody.getPosition().x) - parentBody.getAngle(),
                        position.dst(parentBody.getPosition())); // angle, radius
            } else
                this.rposition = rposition;
            if(rangle == null)
                rrotation = rotation - parentBody.getAngle();
            else
                rrotation = rangle;
        }
    }

    public void setParentBody(Body parentBody, boolean override) {
        this.parentBody = parentBody;
        if(parentBody != null) {
            GameObject parentObject = (GameObject) parentBody.getUserData();
            if(parentObject != null){
                parentObject.addChild(this);
            }
            if(override) {
                this.rposition.set(
                        MathUtils.atan2(body.getPosition().y - parentBody.getPosition().y,
                                body.getPosition().x - parentBody.getPosition().x) - parentBody.getAngle(),
                        position.dst(parentBody.getPosition())); // angle, radius
                rrotation = rotation - parentBody.getAngle();
            }
        }
    }

    public Body getParentBody() {
        return parentBody;
    }

    public void addChild(GameObject gameObject){
        children.add(gameObject);
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getID() {
        return ID;
    }

    public void setViewDirection(Constants.VIEW_DIRECTION viewDirection) {
        this.viewDirection = viewDirection;
    }

    public Constants.VIEW_DIRECTION getViewDirection() {
        return viewDirection;
    }

    public ObjectMap<String,Animation> getAnimationSet() {
        return animations;
    }

    public void setAnimationSet(ObjectMap<String, Animation> animations) {
        this.animations = animations;
        current_animation = animations.get(getCurrentAnimationName());
    }

    public String getCurrentAnimationName(){
        return currentAnimationName;
    }

    public void setTodispose(boolean todispose) {
        this.todispose = todispose;
    }

    public void playSound(String soundName) {
        soundCache = gamescreen.assetManager.getRandom(soundName);
        this.soundVol = 1f;
        this.soundTime = 0;
        this.soundDelay = 0;
        soundCache.play(1f);
        soundCache = null;

    }

    public void playSound(String soundName, float delay) {
        soundCache = gamescreen.assetManager.getRandom(soundName);
        this.soundVol = 1f;
        this.soundTime = 0;
        this.soundDelay = delay;

    }
    public void playSound(String soundName, float delay, float vol) {
        soundCache = gamescreen.assetManager.getRandom(soundName);
        this.soundVol = vol;
        this.soundTime = 0;
        this.soundDelay = delay;

    }

    public void playSoundIndex(String soundName, int index) {
        soundCache = gamescreen.assetManager.get(soundName, index);
        this.soundVol = 1f;
        this.soundTime = 0;
        this.soundDelay = 0;
        soundCache.play(1f);
        soundCache = null;

    }


}
