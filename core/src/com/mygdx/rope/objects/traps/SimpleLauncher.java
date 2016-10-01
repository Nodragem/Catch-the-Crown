package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.Usable;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 27/07/2014.
 */
public class SimpleLauncher extends GameObject implements Triggerable {
    private final boolean defaultON;
    private final float radiusToStartPoint;
    private final float angleToStartPoint;
    private float intervalProjectile;
    private float reloadTime;
    private float timer;
    public float undirectional_impulse;
    public Vector2 impulse;
    public Vector2 start_point;
    private Array <Usable> poolObj;
    private int currentIndex = 0;
    private int pool_size;
    private Constants.TRAPSTATE trapstate;

    public SimpleLauncher(GameScreenTournament game, Vector2 position, Vector2 dimension, float rotation,
                          float intervalProjectile, float reloadTime, float impulse, int nb_pool, boolean defaultON,
                          String objectDataIDOfProjectile) {
        super(game, position, dimension, rotation, "simple_launcher"); // there is only one type of launcher for now...
        /*
        # rotation is is degree !! but this.rotation is in radians.
        * here, intervalProjectile is the time between delivering a new object,
        * while intervalOFF is how long the delivered object will stay ON.
        **/
        body.setType(BodyDef.BodyType.StaticBody);
        timer = intervalProjectile+1;
        this.defaultON = defaultON;
        this.trapstate = Constants.TRAPSTATE.ON;
        pool_size = nb_pool;
        poolObj = new Array<Usable>(nb_pool);

        JsonValue infoProjectile = game.getObjectDataBase().get(objectDataIDOfProjectile);

        if (infoProjectile != null) {
            for (int i = 0; i < pool_size; i++) {
                    poolObj.add(new Projectile(this.gamescreen,
                            new Vector2(position).add(0,i), new Vector2(dimension),
                            this.rotation, objectDataIDOfProjectile));
            }
        } else {
            Gdx.app.error("SimpleLauncher", "projectile: "+ objectDataIDOfProjectile+ " not found in Object DataBase (json)");
        }
        this.intervalProjectile = intervalProjectile;
        this.reloadTime = reloadTime;
        this.undirectional_impulse = impulse;
        //this.impulse = new Vector2(2 , 0);
        float projectile_sizey = infoProjectile.getFloat("dimensiony", 0.5f);
        radiusToStartPoint = (float) Math.sqrt(Math.pow(1.1f, 2) + Math.pow((1-projectile_sizey)/2,2)); // the start point will be at 1.1 units from the launcher origin on the relative x, and on the y center of the launcher.
        angleToStartPoint = MathUtils.atan2((1-projectile_sizey)/2, 1.1f);
        updateProjectileStartPoint();
        //Gdx.app.debug("SimpleLauncher: ", "impulse vector: "+this.impulse.x +", "+this.impulse.y);
        Gdx.app.debug("SimpleLauncher: ", "impulse from Tiled: "+impulse);

    }

    public void initCollisionMask() {
        Filter defaultFilter = new Filter();
        defaultFilter.categoryBits = Constants.CATEGORY.get("Scenery");
        defaultFilter.maskBits = Constants.MASK.get("Scenery");
        this.body.getFixtureList().get(0).setFilterData(defaultFilter);
    }

    public boolean update(float deltaTime){
        super.update(deltaTime);
        if(trapstate == Constants.TRAPSTATE.ON){
            timer +=deltaTime;
            if (timer > intervalProjectile){
                timer = 0;
                deliver();
            }
        }
        else if(trapstate == Constants.TRAPSTATE.RELOADING){
            timer -= deltaTime;
            if (timer < 0){
                timer=intervalProjectile; // ready to shoot
                trapstate = Constants.TRAPSTATE.ON;
            }
        }
        updateProjectileStartPoint();
        return todispose;

    }

    private void updateProjectileStartPoint(){
        start_point = new Vector2(position.x+radiusToStartPoint*MathUtils.cos(this.rotation + angleToStartPoint),
                position.y+radiusToStartPoint*MathUtils.sin(this.rotation + angleToStartPoint));
        impulse = new Vector2(undirectional_impulse * MathUtils.cos(this.rotation) , undirectional_impulse * MathUtils.sin(this.rotation));
        // FIXME: as with Lance and AttackManager, the directional impulse
        // should be computed when a Lance is actually throwed
    }

    public void deliver() {
        goToActivation();
        poolObj.get(currentIndex).use(this, new Vector2(start_point), impulse);
        currentIndex = (currentIndex + 1)%pool_size;
        if(currentIndex == 0){ // we ran all the pool
            Gdx.app.debug("SimpleLauncher", "relaoding time");
            timer = reloadTime;
            trapstate = Constants.TRAPSTATE.RELOADING;
        }
    }

    @Override
    public void reset() {
        trapstate = defaultON?Constants.TRAPSTATE.ON: Constants.TRAPSTATE.OFF;
        setActivate(true);
    }

    @Override
    public boolean isActiveByDefault() {
        return defaultON;
    }

    @Override
    public void triggerONActions(HubInterface hub) {
        //goToActivation(); // play its animation
        trapstate = Constants.TRAPSTATE.ON;
    }

    @Override
    public void triggerOFFActions(HubInterface hub) {
        timer = intervalProjectile; // ready to fire next time
        trapstate = Constants.TRAPSTATE.OFF; //do not disappear
    }

    @Override
    public void render(SpriteBatch batch) {
        if(isVisible) {
            TextureRegion reg = null;
            reg = current_animation.getKeyFrame(stateTime);
            for (int i = 0; i < dimension.x; i++) {
                batch.draw(reg, position.x + i*MathUtils.cos(rotation), position.y + i*MathUtils.sin(rotation),
                        0.0f, 0.0f, // origins
                        1, 1, 1, 1, // dimension and scale
                        rotation * MathUtils.radiansToDegrees
                );
            }
        }
    }
}
