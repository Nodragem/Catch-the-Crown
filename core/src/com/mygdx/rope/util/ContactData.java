package com.mygdx.rope.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;

/**
 * Created by Nodragem on 11/05/2014.
 */
public class ContactData {
    private final Fixture observedFixture;
    public Constants.COLLIDER_TYPE myColliderType;
    public Array<Fixture> touchedFixtures;
    //public Array<GameObject> touchedObjects;
    private int capacity;
    private float lastImpulse;

    public ContactData(int capacity, Fixture fixture){
        observedFixture = fixture;
        observedFixture.setUserData(this);
        myColliderType = Constants.COLLIDER_TYPE.SENSOR;
        this.capacity = capacity;
        touchedFixtures = new Array<Fixture>(capacity);
    }

    public void flush(){
        touchedFixtures.clear();
    }

    public void deepFlush(){
        for (Fixture touchedFixture : touchedFixtures) {
            ContactData d = (ContactData) touchedFixture.getUserData();
            if(d!=null)
                d.removeTouchedFixtures(observedFixture);
        }
        touchedFixtures.clear();
    }

    public float getLastImpulse() {
        return lastImpulse;
    }

    public void setLastImpulse(float lastImpulse) {
        this.lastImpulse = lastImpulse;
    }

    public void pushTouchedFixtures(Fixture f){
        if (!isTouchedBy(f)) {
            if (touchedFixtures.size == capacity)
                touchedFixtures.removeIndex(capacity - 1);
            touchedFixtures.add(f);
        }
    }

    public boolean removeTouchedFixtures(Fixture f){
        boolean isfound = true;
        while (isfound) {
            isfound = touchedFixtures.removeValue(f, true);
            //Gdx.app.debug("Removed", "removed "+ isfound);
        }
        return true;
    }

    public boolean isTouched(){
        return touchedFixtures.size > 0? true :false;
    }

    public boolean isTouchedBy(Fixture fixtA){
        for (Fixture fixtB:touchedFixtures){
            if (fixtA == fixtB){
                return true;
            }
        }
        return false;
    }

    public Array<Fixture> getTouchedFixtures() {
        return touchedFixtures;
    }

    public Fixture popTouchedFixtures() {
        return touchedFixtures.pop();
    } // return and remove

    public Fixture peekTouchedFixtures() {
        return touchedFixtures.peek();
    } // return but not remove

    public void setMyColliderType(Constants.COLLIDER_TYPE myColliderType) {
        this.myColliderType = myColliderType;
    }

    public Constants.COLLIDER_TYPE getMyColliderType() {
        return myColliderType;
    }


}
