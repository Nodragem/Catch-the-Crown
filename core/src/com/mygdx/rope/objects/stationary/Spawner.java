package com.mygdx.rope.objects.stationary;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Nodragem on 14/06/2014.
 */
public class Spawner {

    private Array<Vector2> SpawnArray;

    public Spawner(){
        SpawnArray = new Array<Vector2>();
    }

    public Array<Vector2> getSpawnArray() {
        return SpawnArray;
    }

    public Vector2 getSpawnPosition(){
        return SpawnArray.random();
    }

}
