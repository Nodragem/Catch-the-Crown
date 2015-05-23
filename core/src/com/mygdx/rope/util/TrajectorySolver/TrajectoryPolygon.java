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
    private float mySpeed = 0;
    private float current_distance = 0;
    private float total_distance = 0;

    public TrajectoryPolygon(Polyline poly, float speed){
        mySpeed = speed;
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
    public Vector2 getSpeedFrom(float deltaTime) {
        current_distance += deltaTime*mySpeed;
        //Gdx.app.debug("Solver", "current distance: "+current_distance+", total distance:"+total_distance);
        if (current_distance>total_distance){
            mySpeed *= -1;
        }
        else if (current_distance < 0){
            mySpeed *=-1;
        }
        int currentVector = 0;
        for (int i =0; i < cumulated_distances.size; i++){
            if(current_distance <= cumulated_distances.get(i)){
                currentVector = i;
                break;
            }
        }
        //Gdx.app.debug("Solver", "currentVector:"+ vectors.get(currentVector));
        //return new Vector2(vectors.get(currentVector)).scl(deltaTime*mySpeed);
        return new Vector2(vectors.get(currentVector)).scl(mySpeed);
    }
}
