package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.Usable;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 15/02/2015.
 */
public class Projectile extends GameObject implements Usable {
    public GameObject user;
    private float returnedDamage;
    private float rateOfDeath;


    public Projectile(GameScreenTournament game, Vector2 position, Vector2 dimension, float angle, String objectDataID) {
        super(game, position, dimension, angle, objectDataID);
        JsonValue objectInfo = game.getObjectDataBase().get(objectDataID);
        Gdx.app.debug("projectile", "sensor? " + getBody().getFixtureList().get(mainFixtureIndex).isSensor());
        returnedDamage =  objectInfo.getFloat("returned", 1f);
        givenDamage = objectInfo.getFloat("damage", 101f);
        life = 100;
        rateOfDeath = objectInfo.getFloat("rateDeath", 0); // note that in any case the launcher will kill and call them back after reloading time.
        body.setGravityScale(objectInfo.getFloat("gravity", 0));
        isKillable = true;
        //FIXME: should be resetted by the Launcher instead
        setActivate(false);


    }

//    @Override
//    public void initCollisionMask() {
//        Filter defaultFilter = new Filter();
//        defaultFilter.categoryBits = Constants.CATEGORY.get("Weapon");
//        defaultFilter.maskBits = Constants.MASK.get("Weapon");
//        this.body.getFixtureList().get(0).setFilterData(defaultFilter);
//    }

    @Override
    public boolean update(float deltaTime){
        life -= rateOfDeath * deltaTime;
        body.setTransform(body.getPosition(), body.getLinearVelocity().angleRad());
        return super.update(deltaTime);
    }

    @Override
    protected void onCollision() {
        Gdx.app.debug("Projectile", "Collision Detected");
        for(Fixture fixture: mainBoxContact.getTouchedFixtures()){
            GameObject touchedObject = (GameObject) fixture.getBody().getUserData();
            if (touchedObject instanceof Projectile){ // we need that cause goToActivation will remove (this) projectile to the touched object contactlist.
                touchedObject.addDamage(((Projectile) touchedObject).getReturnedDamage());
            } else if (touchedObject != null){
                touchedObject.addDamage(givenDamage);
            }
            this.addDamage(returnedDamage);
        }
        Gdx.app.debug("Projectile", "life after return damage: " + getLife());
    }

    @Override
    public void use(GameObject obj, Vector2 position, Vector2 impulse) {
        // FIXME: note that the user of the pool should be know since the creation of the pool
        Gdx.app.debug("projectile: ", "state activation: "+ activeState);
//        if(activeState == Constants.ACTIVE_STATE.DESACTIVATED) {
        this.user = obj;
        this.goToActivation();
        this.setLife(100f);
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        // FIXME: if we shot upward, does the angle change in accordance?
        // maybe take example on Lance
        body.setTransform(position, 0f);
        Gdx.app.debug("Projectile: ", "impulse * body.getMass() = " + body.getMass() * impulse.x + ", " + body.getMass() * impulse.y);
        if (body.getType() == BodyDef.BodyType.DynamicBody)
            body.applyLinearImpulse(body.getMass() * impulse.x, body.getMass() * impulse.y, body.getPosition().x, body.getPosition().y, true);
        else {
            body.setLinearVelocity(impulse.x, impulse.y);
        }
//        }
    }
    public float getReturnedDamage() {
        return returnedDamage;
    }

    protected boolean checkIfToDestroy() {
        // FIXME: we should just rely on DEACTIVATED (some object would be permanent and would not be destroyed when DEACTIVATED)
        if (life < 0 && activeState != Constants.ACTIVE_STATE.DESACTIVATED)
            goToDesactivation();
        return false;
    }
}
