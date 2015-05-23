package com.mygdx.rope.util.TrajectorySolver;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Geoffrey on 22/02/2015.
 */
public class MockTrajectory implements TrajectorySolver{
    Vector2 Speed;
    public MockTrajectory(){
        Speed = new Vector2(0,0);
    }

    @Override
    public Vector2 getSpeedFrom(float deltatime) {
        return Speed ;
    }
}
