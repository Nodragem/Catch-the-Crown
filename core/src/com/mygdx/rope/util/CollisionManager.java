package com.mygdx.rope.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;

/**
 * Created by Nodragem on 04/05/2014.
 */
public class CollisionManager implements ContactListener {
    private Fixture fixtA;
    private Fixture fixtB;
    private ContactData contactDataFixtA;
    private ContactData contactDataFixtB;
    private ContactData contactPlatform;
    private ContactData contactObject;


    @Override
    public void beginContact(Contact contact) {
        fixtA = contact.getFixtureA();
        fixtB = contact.getFixtureB();

//        if (fixtA.getBody() == fixtB.getBody()){
//            Gdx.app.debug("ContactListener: ", "this contact won't be added to ContactData.");
//            return;
//        }

        if(fixtA.getUserData() != null){
            contactDataFixtA = (ContactData)fixtA.getUserData();
            contactDataFixtA.pushTouchedFixtures(fixtB);
        }
        if(fixtB.getUserData() != null) {
            contactDataFixtB = (ContactData) fixtB.getUserData();
            contactDataFixtB.pushTouchedFixtures(fixtA);
        }
    }

    @Override
    public void endContact(Contact contact) {
        fixtA = contact.getFixtureA();
        fixtB = contact.getFixtureB();

//        if (fixtA.getBody() == fixtB.getBody()){
//            return;
//        }

        if(fixtA!=null && fixtA.getUserData() != null){
            contactDataFixtA = (ContactData)fixtA.getUserData();
            contactDataFixtA.removeTouchedFixtures(fixtB);
        }
        if(fixtB!=null && fixtB.getUserData() != null) {
            contactDataFixtB = (ContactData) fixtB.getUserData();
            contactDataFixtB.removeTouchedFixtures(fixtA);
        }


    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        fixtA = contact.getFixtureA();
        fixtB = contact.getFixtureB();

        if(fixtA.getUserData() != null && fixtB.getUserData() !=null) {
            contactDataFixtA = (ContactData) fixtA.getUserData();
            contactDataFixtA.pushTouchedFixtures(fixtB);
            contactDataFixtB = (ContactData) fixtB.getUserData();
            contactDataFixtB.pushTouchedFixtures(fixtA);
            if (contactDataFixtA.getMyColliderType().equals(Constants.COLLIDER_TYPE.ONEWAY)) {
                contactPlatform = contactDataFixtA;
                contactObject = contactDataFixtB;
            } else if (contactDataFixtB.getMyColliderType().equals(Constants.COLLIDER_TYPE.ONEWAY)) {
                contactPlatform = contactDataFixtB;
                contactObject = contactDataFixtA;
            } else {
                contactPlatform = null;
                contactObject = null;
            }
            if (contactPlatform != null) {
                // if you are here, that means that the Fixture A was part of a Lance,
                // or another one-way plateform
                /// We get the position of the collider (e.g. the player character) and add 0.1 to it
                GameObject obj = contactObject.getMyGameObject();
                float posy = obj.getBody().getWorldCenter().y;
                // if the character come from under where was the collision, we let it pass.
                if (posy < contact.getWorldManifold().getPoints()[0].y) {
                    contact.setEnabled(false);
                    Gdx.app.debug("CollisionManager", "one way detected!");
                } else {
                    if (obj instanceof Character) {
                        Character char1 = (Character) obj;
                        if (char1 != null && char1.getPlayer().getCurrentMovingVector().y < -0.75)
                            contact.setEnabled(false);
                    }
                }

            }
        }

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        fixtA = contact.getFixtureA();
        fixtB = contact.getFixtureB();

//        if (fixtA.getBody() == fixtB.getBody()){
//            return;
//        }

        if(fixtA.getUserData() != null){
            contactDataFixtA = (ContactData)fixtA.getUserData();
            contactDataFixtA.setLastImpulse(impulse.getNormalImpulses()[0]);
        }
        if(fixtB.getUserData() != null) {
            contactDataFixtB = (ContactData) fixtB.getUserData();
            contactDataFixtB.setLastImpulse(impulse.getNormalImpulses()[0]);
        }

    }
}
