package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;
import com.mygdx.rope.util.TrajectorySolver.TrajectoryPolygon;
import com.mygdx.rope.util.TrajectorySolver.TrajectorySolver;

/**
 * Created by Geoffrey on 10/01/2015.
 */
public class MovingPlatform extends GameObject implements Triggerable {
    private boolean looping;
    /* we have here a game object which is always of one units height, and repeat its textures of one units on the width axis */
    private TrajectorySolver trajResolver;
    private Vector2 speed;
    private float angularSpeed;
    private float correctionRadius;
    private float correctionAngle;
    private boolean defaultON;
    private boolean stopped;
    private float waitingTime;
    private boolean platformAlwaysVisible;
    private Array<Animation> platformBlocks;
    // we could do a static array of arrays, and the class
    // would load oall the type of platform used in the level.
    private int blockFlag;

    public MovingPlatform(GameScreenTournament game, Vector2 position, Vector2 dimension, float angle, MapObject path,
                          float angularSpeed, float waitingTime, boolean defaultON, boolean looping, boolean alwaysVisible,
                          String objectDataID) {
        // FIXME: 2 - should we put the data from TiledMap into a json / hashmap?
        super(game, position, dimension, angle, objectDataID);
        // Note that the dimension, position and angle are given by the Factory, from the TiledMap
        mainBoxContact = new ContactData(10, this.body.getFixtureList().get(mainFixtureIndex)); // note that it is not linked to any fixture
//        initCollisionMask();
        this.platformAlwaysVisible = alwaysVisible;
        this.defaultON = defaultON;
        this.waitingTime = waitingTime;
        this.looping = looping;
        body.getFixtureList().get(0).setFriction(1.0f);

        JsonValue objectInfo = gamescreen.getObjectDataBase().get(objectDataID);
        if (objectInfo.getBoolean("oneway", false))
            mainBoxContact.setMyColliderType(Constants.COLLIDER_TYPE.ONEWAY);

        /* Libgdx body.setTransform() will places the **center** of the body to a new position because
         we made the body centered on the lef-bottom corner of GameObject to allow centered rotation.
        Also, GameObject and TileMap object give us the position of their left-bottom corner
        Because of that, we need to compute where is the center of the TileMap object to place the body
        This is very similar to the part setParentBody() and the position.sub(origin) in update(), although here our 'parent' is
        the position of the Tiledmap object*/
        // we use temporarily the origin to code the center of the TiledMap object
        origin.x = dimension.x/2f;
        origin.y = dimension.y/2f;
        correctionRadius = (float) Math.sqrt(Math.pow(origin.x, 2) + Math.pow(origin.y, 2));
        correctionAngle = MathUtils.atan2(origin.y, origin.x);
        float ox = correctionRadius * MathUtils.cos(this.rotation+correctionAngle);
        float oy = correctionRadius * MathUtils.sin(this.rotation+correctionAngle);
        body.setTransform(body.getPosition().x+ox, body.getPosition().y+oy, this.rotation);

        // the origin is used to remember the difference in pos between the texture/object and the body
        // we make a small offset compared to the usual difference between object and body
        // the body will be slightly inside the texture, so that the prabbit feet are above the 1st pixel row.
        setOrigin(dimension.x/2, 0.45f);
        // FIXME: we should be able to use the body_offsetx system instead of that

        this.angularSpeed = angularSpeed;
        this.speed = new Vector2(0,0);
        stopped = false;
        if(path == null){
            trajResolver = null;
        }
        else if (path instanceof  PolylineMapObject ){
            PolylineMapObject polygonPath = (PolylineMapObject) path;
            Polyline polyline = polygonPath.getPolyline();
            trajResolver = new TrajectoryPolygon(polyline, 1.0f);
            trajResolver.initCoolDown(waitingTime);
        }
        else if (path instanceof CircleMapObject){

        }
        else if (path instanceof RectangleMapObject){

        }
        else {
            Gdx.app.debug("MovingPlatform", "Path type not recognized!");
        }
    }

//    @Override
//    public void initCollisionMask() { // override the default / prototype object
//        Filter defaultFilter = new Filter();
//        defaultFilter.categoryBits = Constants.CATEGORY.get("Scenery");
//        defaultFilter.maskBits = Constants.MASK.get("Scenery");
//        this.body.getFixtureList().get(0).setFilterData(defaultFilter);
//    }
//
//    @Override
//    public void initAnimation(){ // override the default / prototype object
//        Pixmap pixmap2 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
//        pixmap2.setColor(0.7f, 0.2f, 0.3f, 1.0f);
//        pixmap2.fillRectangle(0, 0, 32, 32);
//        TextureRegion region =  new TextureRegion(new Texture(pixmap2));
//        animations.put("Main", new Animation(1.0f/2.0f, region));
//        setAnimation("Main");
//    }
//
//    @Override
//    public void initFixture() { // override the default / prototype object
//        PolygonShape p = new PolygonShape();
//        p.setAsBox(dimension.x / 2.0f, dimension.y / 2.0f, new Vector2(0, 0), 0);
//        FixtureDef fd = new FixtureDef();
//        fd.shape = p;
//        fd.density = 50;
//        fd.restitution = 0.0f;
//        fd.friction = 0.5f;
//        //fd.isSensor = isSensor;
//        this.body.createFixture(fd);
//        p.dispose();
//    }

    @Override
    public void initAnimation(String objectDataID, String color_texture){
        super.initAnimation(objectDataID, color_texture);

        if (animations.size != 5) {
            Gdx.app.error("Moving Platform",
                    "Moving Platform style is incorrect for: "+
                            objectDataID + "_"+ color_texture +
                            "\n Checks if you have all the textures needed: " +
                            "left, rleft, center, rright, right");
        }

        platformBlocks = new Array<Animation>((int)dimension.x);
        for (int i = 0; i < dimension.x; i++) {
            if (i > ((dimension.x-1)/2.0f) || i < ((dimension.x-1)/2.0f)){
                // \--> if on the right or the left side:
                if(i == (dimension.x - 1)){ // the final right block
                    platformBlocks.add(animations.get("right"));
                }
                else if(i == 0 ){ // the first left block
                    platformBlocks.add(animations.get("left"));
                }
                else{ // an usual left/right block
                    if (MathUtils.random()>0.5f)
                        platformBlocks.add(animations.get("rright")); // rright: repeat right
                    else
                        platformBlocks.add(animations.get("rleft"));
                }
            } else { // \--> if on the center piece:
                    platformBlocks.add(animations.get("center"));
             }
        }

    }

    @Override
    public boolean update(float deltaTime){
        if(trajResolver != null) {
            if (stopped & !looping &
                    trajResolver.getTrajectoryState() == Constants.PLATFORM_STATE.FINAL_STOP) {
                body.setLinearVelocity(0f, 0f);
                body.setAngularVelocity(0f);
            } else if (stopped & looping) {
                body.setLinearVelocity(0f, 0f);
                body.setAngularVelocity(0f);
            } else {
                speed = trajResolver.getSpeedFrom(deltaTime, waitingTime, looping);
                body.setLinearVelocity(speed);
                body.setAngularVelocity(angularSpeed);
            }
        } else {
            if (!stopped){
                body.setAngularVelocity(angularSpeed);
            } else {
                body.setAngularVelocity(0f);
            }
        }
        super.update(deltaTime);
        return false;
    }

    @Override
    public void reset() {
        stopPlatform(!defaultON);
        if (!platformAlwaysVisible)
            setActivate(defaultON);
        else
            setActivate(true);
    }

    @Override
    public boolean isActiveByDefault() {
        return defaultON;
    }

    @Override
    public void triggerONActions(HubInterface hub) {
        Gdx.app.debug("Spikes", "GoToActivation()");
        if(!platformAlwaysVisible)
            goToActivation();
        stopPlatform(false);

    }

    @Override
    public void triggerOFFActions(HubInterface hub) {
        stopPlatform(true);

        if(!platformAlwaysVisible)
            goToDesactivation();
    }

    private void desactivationActions() {
        stopPlatform(true);
    }

    private void activationActions() {
        stopPlatform(false);
    }

    private void stopPlatform(boolean b) {
        stopped = b;
        if(!stopped && !looping && trajResolver.getTrajectoryState() == Constants.PLATFORM_STATE.FINAL_STOP)
            trajResolver.leaveStopState();
    }

    public void render(SpriteBatch batch) {
        if(isVisible && activeState != Constants.ACTIVE_STATE.DESACTIVATED) {
            for (int i = 0; i < platformBlocks.size; i++) {
                batch.draw(platformBlocks.get(i).getKeyFrame(stateTime),
                        position.x + i*MathUtils.cos(rotation),
                        position.y + i*MathUtils.sin(rotation),
                        origin.x, origin.y, // origins
                        1, 1, 1, dimension.y, // dimension and scale
                        rotation * MathUtils.radiansToDegrees
                );


            }
        }


    }



}
