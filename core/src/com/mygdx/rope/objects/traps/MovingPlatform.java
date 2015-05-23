package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.TrajectorySolver.MockTrajectory;
import com.mygdx.rope.util.TrajectorySolver.TrajectoryPolygon;
import com.mygdx.rope.util.TrajectorySolver.TrajectorySolver;

/**
 * Created by Geoffrey on 10/01/2015.
 */
public class MovingPlatform extends GameObject implements Triggerable {
    /* we have here a game object which is always of one units height, and repeat its textures of one units on the width axis */
    private TrajectorySolver trajResolver;
    private Vector2 speed;
    private float angularSpeed;
    private float correctionRadius;
    private float correctionAngle;
    private boolean defaultON;
    private boolean stopped;
    private boolean platformAlwaysVisible;
    private Array<TextureRegion> platformBlocks;// we could do a static array of arrays, and the class would load oall the type of platform used in the level.
    private int blockFlag;

    public MovingPlatform(GameScreen game, Vector2 position, Vector2 dimension, MapObject path, float angularSpeed, boolean defaultON, boolean alwaysVisible, String nameTexture) {
        // instead of Overriding I could just do a simple GameObject Factory which
        // would use the GameObject constructor to give the Fixture, the filter and the animation definition to GameObject.
        // Further more, the Factory could also set up the Body Type and give an animation of switching off or on.
        // The only thing missing to GameObject is float "damage", which if it is > 0 is called and removed from the other object --> We tried to do something
        super(game, position, dimension, 0f, nameTexture);

        this.platformAlwaysVisible = alwaysVisible;
        this.defaultON = defaultON;
        body.getFixtureList().get(0).setFriction(1.0f);
        origin.x = dimension.x/2;
        origin.y = dimension.y/2;
        correctionRadius = (float) Math.sqrt(Math.pow(origin.x, 2) + Math.pow(origin.y, 2)); // the start point will be at 1.1 units from the launcher origin on the relative x, and on the y center of the launcher.
        correctionAngle = MathUtils.atan2(origin.y, origin.x);
        body.setTransform(body.getPosition().x+origin.x, body.getPosition().y+origin.y, 0);
        Gdx.app.debug("Platform", "Platform in creation!, origin ="+ origin.x + "; "+origin.y);
        body.setType(BodyDef.BodyType.KinematicBody);
        //body.getFixtureList().get(0).setSensor(true);
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
        }
        else if (path instanceof CircleMapObject){

        }
        else if (path instanceof RectangleMapObject){

        }
        else {
            Gdx.app.debug("MovingPlatform", "Path type not recognized!");
        }
    }

    @Override
    public void initFilter() {
        Filter defaultFilter = new Filter();
        defaultFilter.categoryBits = Constants.CATEGORY.get("Scenery");
        defaultFilter.maskBits = Constants.MASK.get("Scenery");
        this.body.getFixtureList().get(0).setFilterData(defaultFilter);
    }

    @Override
    public void initAnimation(){
        Pixmap pixmap2 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap2.setColor(0.7f, 0.2f, 0.3f, 1.0f);
        pixmap2.fillRectangle(0, 0, 32, 32);
        TextureRegion region =  new TextureRegion(new Texture(pixmap2));
        main_animation = new Animation(1.0f/2.0f, region);
        setAnimation(main_animation);
    }
    @Override
    public void initAnimation(String name_texture){
        TextureRegion region = null;
        Array <TextureRegion> bufferBlocks = new Array <TextureRegion>(5);
         // anim normal:
        region = atlas.findRegion("moving_" + name_texture + "_left");
        if (region!=null)
            bufferBlocks.add(region);
        region = atlas.findRegion("moving_" + name_texture + "_rleft");
        if (region!=null)
            bufferBlocks.add(region);

        region = atlas.findRegion("moving_" + name_texture + "_center");
        if (region!=null)
            bufferBlocks.add(region);

        region = atlas.findRegion("moving_" + name_texture + "_rright");
        if (region!=null)
            bufferBlocks.add(region);

        region = atlas.findRegion("moving_" + name_texture + "_right");
        if (region!=null)
            bufferBlocks.add(region);

        if (bufferBlocks.size != 5) {
            Gdx.app.error("Moving Platform", "Moving Platform style is incorrect for "+ name_texture + " checks if you have all the textures needed: left, repeat_01, repeat_02, center, right");
        }

        platformBlocks = new Array<TextureRegion>((int)dimension.x);
        for (int i = 0; i < dimension.x; i++) {
            if (i > ((dimension.x-1)/2.0f) || i < ((dimension.x-1)/2.0f)){ // the right or left side
                if(i == (dimension.x - 1)){ // the final right block
                    platformBlocks.add(bufferBlocks.get(4));
                }
                else if(i == 0 ){ // the first left block
                    platformBlocks.add(bufferBlocks.get(0));
                }
                else{ // an usual left/right block
                    if (MathUtils.random()>0.5f)
                        platformBlocks.add(bufferBlocks.get(3));
                    else
                        platformBlocks.add(bufferBlocks.get(1));
                }
            } else { // the center piece
                    platformBlocks.add(bufferBlocks.get(2));
             }
        }

        Pixmap pixmap2 = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap2.setColor(0.7f, 0.2f, 0.3f, 1.0f);
        pixmap2.fillRectangle(0, 0, 32, 32);
        region =  new TextureRegion(new Texture(pixmap2));
        main_animation = new Animation(1.0f/2.0f, region);
        setAnimation(main_animation);
    }

    public void initFixture() {
        PolygonShape p = new PolygonShape();
        p.setAsBox(dimension.x / 2.0f, dimension.y / 2.0f, new Vector2(0, 0), 0);
        FixtureDef fd = new FixtureDef();
        fd.shape = p;
        fd.density = 50;
        fd.restitution = 0.0f;
        fd.friction = 0.5f;
        //fd.isSensor = isSensor;
        this.body.createFixture(fd);
        p.dispose();
    }

    @Override
    public boolean update(float deltaTime){
        if(!stopped) {
            if(trajResolver != null)
                speed = trajResolver.getSpeedFrom(deltaTime);
            body.setLinearVelocity(speed);
            body.setAngularVelocity(angularSpeed);
        }
        else{
            body.setLinearVelocity(0f,0f);
            body.setAngularVelocity(0f);
        }
        super.update(deltaTime);
        position.add(-correctionRadius * MathUtils.cos(body.getAngle() + correctionAngle),
                -correctionRadius * MathUtils.sin(body.getAngle() + correctionAngle));
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
    public boolean isDefaultON() {
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
//        if (stopped) {
//            speed = new Vector2(0, 0);
//            angularSpeed = 0;
//        }
//        else{
//            angularSpeed =
//        }
    }

    public void render(SpriteBatch batch) {
//        if(isVisible) {
//            TextureRegion reg = null;
//            //Gdx.app.debug(TAG, "Key Frame: " + stateTime);
//            batch.setColor(color.get(0), color.get(1), color.get(2), color.get(3));
//            reg = current_animation.getKeyFrame(stateTime);
//            batch.draw(reg.getTexture(), // we called this long draw() method just to get access the flip argument...
//                    position.x, position.y,
//                    0, 0,
//                    dimension.x, dimension.y,
//                    1, 1,
//                    rotation,
//                    reg.getRegionX(), reg.getRegionY(),
//                    reg.getRegionWidth(), reg.getRegionHeight(),
//                    viewDirection == Constants.VIEW_DIRECTION.LEFT, false);
//            batch.setColor(1, 1, 1, 1);
//        }
        if(isVisible) {
            TextureRegion reg = null;
            reg = current_animation.getKeyFrame(stateTime);
            for (int i = 0; i < platformBlocks.size; i++) {
                batch.draw(platformBlocks.get(i), position.x + i*MathUtils.cosDeg(rotation), position.y + i*MathUtils.sinDeg(rotation),
                        0.0f, 0.0f, // origins
                        1, 1, 1, dimension.y, // dimension and scale
                        rotation
                );
            }
        }


    }



}
