package com.mygdx.rope.objects;

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
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

public class GameObject implements Updatable {
    //public final static TextureAtlas atlas = new TextureAtlas("texture_obj.pack"); // all the game object has access to a class Atlas
    public final static TextureAtlas atlas = new TextureAtlas("texture_obj.atlas"); // all the game object has access to a class Atlas
    public boolean isKillable;
    public Integer ID = null;
    public Array<Float> color;
    public float currentImmunity;
    public float immunityReset;
    protected float givenDamage;
    protected float bufferReceivedDamage;
    protected float timeStepDamage;
    public GameScreenTournament gamescreen;
    public GameObject parent = null;
    public int myRenderID;
    public boolean isVisible;
    public Body body;
    public ContactData mainBoxContact;
    protected int mainFixtureIndex;
	public Animation current_animation;
    public Animation main_animation;
    public Animation onset_animation = null;
    public Animation offset_animation = null;
    public Constants.VIEW_DIRECTION viewDirection;
	public Vector2 position;
	public Vector2 rposition; // in polar coordinate
	public Vector2 dimension;
	public Vector2 origin;
	public Vector2 scale;
	public float rotation;
    private float rrotation;
	public float stateTime;
    public float life;
    public Character Carrier = null;
    public Constants.ACTIVE_STATE activeState;
    private Constants.ACTIVE_STATE previousActiveState = null;

    // we should separated the GameObject from their textures :/ like that the GameObjects of same type would use the same texture set, instead of loading several time the same textures in memory

    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension, float angle, String name_texture, JsonValue bodyDef, Filter filter){ // angle in radians
        gamescreen = game;
        color = new Array<Float>(new Float[]{1.0f,1.0f,1.0f,1.0f});
        //stateTime = 0;
        life = 100;
        isKillable =  false;
        isVisible = true;
        currentImmunity = 0.0f;
        immunityReset = 0.25f; // if an object has a negative immunityReset, he can't be injured
        bufferReceivedDamage = 0.0f;
        givenDamage = 0.0f;
        timeStepDamage = 0.0f;
        World b2world = game.getB2World();
        this.viewDirection = Constants.VIEW_DIRECTION.RIGHT;
        //Gdx.app.debug("ObjGame", "Position: "+position);
        this.dimension = dimension;
        origin = new Vector2(0,0); // the origin is really at the origin
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
        if(name_texture == null) initAnimation();
        else initAnimation(name_texture);

        mainFixtureIndex = 0;
        if(bodyDef == null) initFixture();
        else initFixture(bodyDef);
        if (this.body.getFixtureList().size >0) { // > mainFixtureIndex
            mainBoxContact = new ContactData(3, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
        }
        if(bodyDef == null) initFilter();
        else initFilter(bodyDef);

        this.goToActivation();

    }
    public GameObject(GameScreenTournament game, Vector2 position,  Vector2 dimension, float angle, String name_texture, JsonValue fd) {
        this(game, position, dimension, angle, name_texture, fd, null);
    }

    public GameObject(GameScreenTournament game, Vector2 position,  Vector2 dimension,float angle, String name_texture) {
        this(game, position, dimension, angle, name_texture, null, null);
    }
	
    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension, float angle) {
        this(game, position, dimension, angle, null, null, null);
	}

    public GameObject(GameScreenTournament game, Vector2 position, Vector2 dimension ) {
        this(game, position, dimension, 0, null, null, null);
	}

    public GameObject(GameScreenTournament game, Vector2 position ) {
        this(game, position, new Vector2(1, 1.0f), 0, null, null, null);
    }


    public void initFilter() {
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
            p.dispose();
    }

    public void initFixture(JsonValue fixtureInfo) {
        if (fixtureInfo.isArray()) {
            for (JsonValue info : fixtureInfo) {
                createFixtureFromJson(info);
            }
        } else {
            createFixtureFromJson(fixtureInfo);
        }
    }

    public void createFixtureFromJson(JsonValue info){ // maybe coulb be static
        PolygonShape p = new PolygonShape();
        dimension.x = info.getFloat("dimensionx", dimension.x);
        dimension.y = info.getFloat("dimensiony", dimension.y);
        float scaleX = info.getFloat("scalex", 1f); // if you prefer to get the fixture dimension as a scale of the object dimension. usefull for switcher
        float scaleY =  info.getFloat("scaley", 1f);
        float offsetx = info.getFloat("offsetx", 0.5f);
        float offsety = info.getFloat("offsety", 0.5f);
        p.setAsBox(scaleX*dimension.x / 2.0f, scaleY * dimension.y / 2.0f, new Vector2(offsetx*dimension.x , offsety* dimension.y), 0);
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
        }
        p.dispose();
    }

    public void initFilter(JsonValue infoFilters) {
        if (infoFilters.isArray()) {
            int i = 0;
            for (JsonValue info : infoFilters) {
                createFilterFromJson(info, i);
                i++;
            }
        } else {
            createFilterFromJson(infoFilters, mainFixtureIndex);
        }

    }

    private void createFilterFromJson(JsonValue info, int fixtureIndex){
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
            main_animation = new Animation(1 / 6.0f, new TextureRegion(new Texture(pixmap)));
            setAnimation(main_animation);
            pixmap.dispose();
            stateTime = 0;
    }

    public void initAnimation(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fillRectangle(0, 0, 64, 64);
        main_animation = new Animation(1 / 6.0f, new TextureRegion(new Texture(pixmap)));
        setAnimation(main_animation);
        pixmap.dispose();
        stateTime = 0;
    }

    public void initAnimation(String name_texture) {
        Array<TextureAtlas.AtlasRegion> regions = null;
        // anim normal:
        regions = atlas.findRegions(name_texture+"_main");
        if (regions.size >0)
            main_animation = new Animation(1.0f/4.0f, regions, Animation.PlayMode.LOOP);
        setAnimation(main_animation);

        regions = atlas.findRegions(name_texture+"_onset");
        if (regions.size >0)
            onset_animation = new Animation(1.0f/4.0f, regions, Animation.PlayMode.NORMAL);

        regions = atlas.findRegions(name_texture+"_offset");
        Gdx.app.debug("GameObject", "texture regions: "+regions);
        if (regions.size >0)
            offset_animation = new Animation(1.0f/4.0f, regions, Animation.PlayMode.NORMAL);
        Gdx.app.debug("GameObject", "texture offset: "+offset_animation);
    }

    public Animation getMain_animation() {
        return main_animation;
    }

    public void setAnimation(Animation animation){
		current_animation = animation;
		stateTime = 0;
	}

    @Override
	public boolean update(float deltaTime) {
        stateTime += deltaTime;
        // we update the position even when the object is not activated
        if (parent == null || body.getType() == BodyType.DynamicBody) {
            position.set(body.getPosition());
            rotation = body.getAngle(); // * MathUtils.radiansToDegrees;
        }
        else{
            // rposition. x = angle; rposition.y = radius
            body.setTransform(
                    parent.position.x + rposition.y*MathUtils.cos(rposition.x + parent.rotation/* *MathUtils.degreesToRadians*/),
                    parent.position.y + rposition.y*MathUtils.sin(rposition.x + parent.rotation /* *MathUtils.degreesToRadians */),
                    (parent.rotation + rrotation) //*MathUtils.degreesToRadians
            );
//            body.setTransform(parent.position.x,
//                    parent.position.y,
//                    absoluteAngle);
            position.set(body.getPosition());
            rotation = body.getAngle(); //* MathUtils.radiansToDegrees;
            //this.isVisible = parent.isVisible;
        }
        switch (activeState){
            case ACTIVATED:
                if(mainBoxContact.isTouched()){
                    onCollision();
                    //mainBoxContact.flush();
                }
                checkForDamage(deltaTime);
                break;
            case ACTIVATION:
                if (previousActiveState != Constants.ACTIVE_STATE.ACTIVATION)
                    setAnimation(onset_animation);
                if(current_animation.isAnimationFinished(stateTime)) {
                    Gdx.app.debug("GameObject", "activated!");
                    //activationActions();
                    this.setActivate(true);
                    setAnimation(main_animation);
                }
                break;
            case DESACTIVATION:
                if (previousActiveState != Constants.ACTIVE_STATE.DESACTIVATION)
                    setAnimation(offset_animation);
                if(current_animation.isAnimationFinished(stateTime)) {
                    //desactivationActions();
                    this.setActivate(false);
                    setAnimation(main_animation);
                }
                break;
        }
        previousActiveState = activeState;
        return checkIfToDestroy();

	}

    protected void onCollision() {
        for(Fixture fixture: mainBoxContact.getTouchedFixtures()){
            GameObject object = (GameObject) fixture.getBody().getUserData();
            if (object != null){
                object.addDamage(givenDamage);
            }
        }
    }

    private void checkForDamage(float deltaTime) {
        if (currentImmunity > 0)
            currentImmunity -= 1.0 * deltaTime; // decrease of one per second;
        if( isKillable && currentImmunity <= 0 && bufferReceivedDamage > 0) {
            addToLife(-bufferReceivedDamage);
            Gdx.app.debug("GameObect", "Received damage: "+bufferReceivedDamage);
            bufferReceivedDamage = 0;
            currentImmunity = immunityReset;
         } else if (isKillable & currentImmunity > 0 & bufferReceivedDamage > 0){
            Gdx.app.debug("GameObect", "Reset Damage from "+bufferReceivedDamage + " to zero");
            bufferReceivedDamage = 0; // we don't want to accumulate the received damage during the immunity phase
        }
    }

    private void desactivationActions() { }

    private void activationActions() { }

    public void render(SpriteBatch batch) {
        if(isVisible) {
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
                    viewDirection == Constants.VIEW_DIRECTION.LEFT, false);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public void setColor(float r, float g, float b, float a){
        color.set(0,r); color.set(1,g); color.set(2,b); color.set(2,a);
    }

    protected boolean checkIfToDestroy() {
        if (life < 0){
            return true; //true im dead
        }
        return false;
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
        bufferReceivedDamage += damage;
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

    public boolean setCarrier(Character player) { // maybe this part should be in player and not here
        if(player == null)
            body.setType(BodyType.DynamicBody);
        else
            body.setType(BodyType.KinematicBody);
        this.Carrier = player;
        return true; //suceessfull
    }

    public Character getCarrier() {
        return Carrier;
    }

    public Body getBody() {
        return body;
    }

    protected void setActivate(boolean b){
        activeState = (b? Constants.ACTIVE_STATE.ACTIVATED: Constants.ACTIVE_STATE.DESACTIVATED);
        isVisible = b;
        body.setActive(b); // I think that this things make the projectile remove itself from what it touched
        if(!b){ // if we desactivate, we need to flush
            for (Fixture fixture : body.getFixtureList()) { // FIXME note that we dont flush properly, we should remove the contact from the contacted fixtures
                ContactData d = (ContactData) fixture.getUserData();
                if (d != null)
                    d.deepFlush();
            }
        }
    }

    public void goToDesactivation(){
            if (offset_animation != null){ // allows a delayed desactivation after playing a last animation (like an explosion)
                activeState = Constants.ACTIVE_STATE.DESACTIVATION;
            }
            else {
                setActivate(false);
            }
    }

    public void goToActivation(){
            if (onset_animation != null){
                isVisible = true;
                Gdx.app.debug("GameObject", "onset_anim not null");
                activeState = Constants.ACTIVE_STATE.ACTIVATION;
            }
            else {
                setActivate(true);
            }
    }

    public void setParent(GameObject parent) {
        this.parent = parent;
        rposition.set(MathUtils.atan2(position.y-parent.position.y, position.x-parent.position.x)-parent.rotation,
                        position.dst(parent.position)); // angle, radius
        rrotation = rotation - parent.rotation;
    }

    public GameObject getParent() {
        return parent;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getID() {
        return ID;
    }
}
