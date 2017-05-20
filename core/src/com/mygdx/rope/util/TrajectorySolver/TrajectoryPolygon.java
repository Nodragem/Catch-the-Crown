package com.mygdx.rope.util.TrajectorySolver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 10/01/2015.
 */
public class TrajectoryPolygon implements TrajectorySolver {
    public Array<Vector2> vectors;
    public FloatArray cumulated_distances;
    public Constants.PLATFORM_STATE platformState;
    private float mySpeed = 0;
    private float currentSpeed = 0;
    private float current_distance = 0;
    private float total_distance = 0;
    private float coolDown;
    private float previousSpeed;
    private float correctionSpeed;
    private float fromPreviousStop;
    private float previousStop;
    public float nextStop;
    private float toNextStop;
    private Vector2 last_position;

    public TrajectoryPolygon(Polyline poly, float speed, Vector2 position){
        mySpeed = speed; // not really useful
        last_position = position;
        platformState = Constants.PLATFORM_STATE.FINAL_STOP;
        currentSpeed = 0;
        previousSpeed = -speed; // because we start at an extremity of the path and
        // the algo will reverse the previous speed to get the current speed after the initial stopping time
        FloatArray vertices = new FloatArray(poly.getVertices());
        cumulated_distances = new FloatArray(2);
        vectors = new Array(1);
        for (int i = 0; i < vertices.size-3 ; i=i+2) {
            float d = (float) Math.sqrt(
                    Math.pow(vertices.get(i)-vertices.get(i+2), 2) +
                    Math.pow(vertices.get(i+1)-vertices.get(i+3), 2));
            total_distance += d/ Constants.TILES_SIZE;
            cumulated_distances.add(total_distance); // starts with the first distance (not with zero)
            Vector2 v = new Vector2(vertices.get(i+2) - vertices.get(i), vertices.get(i+3) - vertices.get(i+1)).nor();
            vectors.add(v);
        }
        Gdx.app.debug("Trajectories: ", "vectors: "+vectors);
        Gdx.app.debug("Trajectories: ", "distances: "+cumulated_distances);
    }

    @Override
    public Vector2 getSpeedFrom(float deltaTime, float waitingTime, boolean looping, Body body) {
        // we add a little hack to compensate for frame drop,
        if (deltaTime > 0.05)
        {
            body.setTransform(last_position, 0f);
            return new Vector2(0, 0);
        }
        last_position = body.getPosition();
        //Gdx.app.debug("Solver", "current distance: "+current_distance+", total distance:"+total_distance);
        switch(platformState){
            case MOVING:
                //updateNextStop();
                // if the distance from the next Stop is smaller than the last object displacement
                // (it means that the current displacement would pass over the stop position),
                // we stop the object, pin it to the stop position and update the stop flag:
                toNextStop = Math.abs(current_distance - nextStop);
                if(toNextStop <= Math.abs(deltaTime*currentSpeed) ) { // if we going to go over the target point
                    correctionSpeed = (nextStop - current_distance)/deltaTime;
                    platformState = Constants.PLATFORM_STATE.GOING_TO_STOP;
                    //initCoolDown(waitingTime);
                }
                break;
            case GOING_TO_STOP:
                current_distance = nextStop; // should be zero on next turn
                previousSpeed = currentSpeed;
                currentSpeed = 0;
                if (almostEqual(current_distance, total_distance) || almostEqual(current_distance, 0))
                    platformState = Constants.PLATFORM_STATE.FINAL_STOP;
                else
                    platformState = Constants.PLATFORM_STATE.STOPPED;
                initCoolDown(waitingTime);
                break;
            case LEAVING_STOP: // it just started to move again, but it is still close to the stop position:
                // we don't want the object to be pinned to the stop position it just made,
                // so we do not remove the flag "onStop" until it does not leave
                // the zone of influence of the stop position:
                if (Math.abs(currentSpeed)>0){
                    // note that the nextStop is actually the previous Stop position here (maybe we could rename it):
                    fromPreviousStop = Math.abs(current_distance - previousStop);
                    if(fromPreviousStop > Math.abs(deltaTime*currentSpeed) ){ // actually it is the previous stop here
                        platformState = Constants.PLATFORM_STATE.MOVING;
                    }
                }
                break;
            case STOPPED: // Here the object in on a Stop position (not moving)
                //  when the cooldown reaches zero, we re-attribute the Speed that the object had before to be stopped:
                if (coolDown <= 0) {
                    leaveStopState();
                } else {
//                    Gdx.app.debug("R", "Cool Down: "+coolDown);
                    coolDown -= deltaTime;
                }
                break;
            case FINAL_STOP:
                //  when the cooldown reaches zero, we re-attribute the Speed that the object had before to be stopped:
                if(looping) {
                    if (coolDown <= 0) {
                        leaveStopState();
                    } else {
//                    Gdx.app.debug("R", "Cool Down: "+coolDown);
                        coolDown -= deltaTime;
                    }
                    break;
                }
        }

        if(platformState!= Constants.PLATFORM_STATE.GOING_TO_STOP)
            current_distance += deltaTime*currentSpeed;
        else
            current_distance += deltaTime*correctionSpeed;

        int currentVector = 0;
        for (int i =0; i < cumulated_distances.size; i++) {
            if (current_distance <= cumulated_distances.get(i)) {
                currentVector = i;
                break;
            }
        }

        //Gdx.app.debug("Solver", "currentVector:"+ vectors.get(currentVector));
        //return new Vector2(vectors.get(currentVector)).scl(deltaTime*mySpeed);
        if(platformState!= Constants.PLATFORM_STATE.GOING_TO_STOP)
            return new Vector2(vectors.get(currentVector)).scl(currentSpeed);
        else
            return new Vector2(vectors.get(currentVector)).scl(correctionSpeed);
    }

    public void leaveStopState() {
        coolDown = 0;
        platformState = Constants.PLATFORM_STATE.LEAVING_STOP;
        // if we are at an extremity of the path, then we reverse the previous speed:
        if (almostEqual(current_distance, total_distance) || almostEqual(current_distance, 0)) {
//                        Gdx.app.debug("R", "Depassee");
            currentSpeed = -previousSpeed;
        } else {
            currentSpeed = previousSpeed;
        }
        // will define the next Stop position, according to if the moving object is going forward or backward:
        updateStops();
    }

    @Override
    public Constants.PLATFORM_STATE getTrajectoryState() {
        return platformState;
    }

    public void initCoolDown(float waitingTime) {
        // we set up how long the object will wait on the stop, with a cooldown:
        if (waitingTime == -1) {
            if (current_distance != 0) // avoid to just have it waiting even before having moved
                coolDown = 100000f; // an ugly way to block the object to its destination
        } else
            coolDown = waitingTime;
    }


    private void updateStops() {
        previousStop = nextStop;
        for (int i =0; i < cumulated_distances.size; i++){
            if(current_distance+currentSpeed < cumulated_distances.get(i)) {
                // we simulate that the block has moved to avoid the problems we have with equality
                if (currentSpeed > 0) // if going forward
                    nextStop = cumulated_distances.get(i);
                else if (currentSpeed < 0){ // if going backward
                    if (i == 0)
                        nextStop = 0;
                    else
                        nextStop = cumulated_distances.get(i - 1);
                }
                break;
            }
        }
    }

    private boolean almostEqual(float v1, float v2){
        return (Math.abs(v1 - v2) < 0.00001);
    }
}
