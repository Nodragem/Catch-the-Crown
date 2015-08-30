package com.mygdx.rope.util;

import com.badlogic.gdx.physics.box2d.*;

/**
 * Created by Nodragem on 04/05/2014.
 */
public class MyContactListener implements ContactListener {
    private Fixture fixtA;
    private Fixture fixtB;
    private ContactData contactDataFixtA;
    private ContactData contactDataFixtB;


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

//        if (fixtA.getBody() == fixtB.getBody()){
//            return;
//        }

        if(fixtA.getUserData() != null){
            contactDataFixtA = (ContactData)fixtA.getUserData();
            contactDataFixtA.pushTouchedFixtures(fixtB);
            if (contactDataFixtA.getMyColliderType().equals(Constants.COLLIDER_TYPE.ONEWAY)) {
                // if you are here, that means that the Fixture A was part of a Lance, or another one-way plateform
                //Gdx.app.debug("PreSolve", "One Way detected!");
                float posy = contactDataFixtA.peekTouchedFixtures().getBody().getPosition().y + 0.1f; // that the position y of the collided player or object + 0.1
                if (posy < contact.getWorldManifold().getPoints()[0].y) { // contact.getWorldManifold().getPoints()[0].y is where was the collision between the two objects.
                    contact.setEnabled(false);
                }
            }
        }
        if(fixtB.getUserData() != null) {
            contactDataFixtB = (ContactData) fixtB.getUserData();
            contactDataFixtB.pushTouchedFixtures(fixtA);
            // see comments above with contactDataFixtA for explaination
            if (contactDataFixtB.getMyColliderType().equals(Constants.COLLIDER_TYPE.ONEWAY)) {
                //Gdx.app.debug("PreSolve", "One Way detected!");
                float posy = contactDataFixtB.peekTouchedFixtures().getBody().getPosition().y + 0.1f;
                if (posy < contact.getWorldManifold().getPoints()[0].y) {
                    contact.setEnabled(false);
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
