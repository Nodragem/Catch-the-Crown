package com.mygdx.rope.util.TrajectorySolver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 10/01/2015.
 */
public class TrajectoryPolygon implements TrajectorySolver {
    public Array<Vector2> vectors;
    public FloatArray cumulated_distances;
    public float nextStop;
    private float mySpeed = 0;
    private float currentSpeed = 0;
    private float current_distance = 0;
    private float total_distance = 0;
    private float coolDown;
    private float toNextStop;
    private boolean onStop;
    private float previousSpeed;

    public TrajectoryPolygon(Polyline poly, float speed){
        mySpeed = speed;
        onStop = false;
        currentSpeed = mySpeed;
        FloatArray vertices = new FloatArray(poly.getVertices());
        cumulated_distances = new FloatArray(2);
        vectors = new Array(1);
        for (int i = 0; i < vertices.size-3 ; i=i+2) {
            float d = (float) Math.sqrt(
                    Math.pow(vertices.get(i)-vertices.get(i+2), 2) +
                    Math.pow(vertices.get(i+1)-vertices.get(i+3), 2));
            total_distance += d/ Constants.TILES_SIZE;
            cumulated_distances.add(total_distance);
            Vector2 v = new Vector2(vertices.get(i+2) - vertices.get(i), vertices.get(i+3) - vertices.get(i+1)).nor();
            vectors.add(v);
        }
    }

    @Override
    public Vector2 getSpeedFrom(float deltaTime, float waitingTime) {

        //Gdx.app.debug("Solver", "current distance: "+current_distance+", total distance:"+total_distance);
        int currentVector = 0;
        if (!onStop) {
            for (int i =0; i < cumulated_distances.size; i++){
                if(current_distance <= cumulated_distances.get(i)){
                    currentVector = i;
                    if (currentSpeed > 0)
                        nextStop = cumulated_distances.get(i);
                    else if (currentSpeed < 0){
                        if (i == 0)
                            nextStop = 0;
                        else
                            nextStop = cumulated_distances.get(i - 1);
                    }
                    break;
                }
            }
            toNextStop = Math.abs(current_distance - nextStop);
            if(toNextStop <= Math.abs(deltaTime*currentSpeed) ) {
                current_distance = nextStop; // should be zero on next turn
                previousSpeed = currentSpeed;
                currentSpeed = 0;
                onStop = true;
                if (waitingTime == -1) {
                    if (current_distance != 0)
                        coolDown = 100000f;
                } else
                    coolDown = waitingTime;
            }

        }
        else {
            if(coolDown <= 0){
                // give the Speed back when cooldown reaches zero
                coolDown = 0;
                if (current_distance == total_distance || current_distance == 0){
                    Gdx.app.debug("R", "Depassee");
                    currentSpeed = -previousSpeed;
                }
                else{
                    currentSpeed = previousSpeed;
                }
            } else {
                coolDown -= deltaTime;
            }
            if (Math.abs(currentSpeed)>0){
                if(toNextStop < Math.abs(deltaTime*currentSpeed) ){ // actually it is the previous stop here
                    onStop = false;
                }
            }
        }


        current_distance += deltaTime*currentSpeed;

        //Gdx.app.debug("Solver", "currentVector:"+ vectors.get(currentVector));
        //return new Vector2(vectors.get(currentVector)).scl(deltaTime*mySpeed);
        return new Vector2(vectors.get(currentVector)).scl(currentSpeed);
    }
}
