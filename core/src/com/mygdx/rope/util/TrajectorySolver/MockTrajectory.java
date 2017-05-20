package com.mygdx.rope.util.TrajectorySolver;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 22/02/2015.
 */
public class MockTrajectory implements TrajectorySolver{
    Vector2 Speed;

    @Override
    public Constants.PLATFORM_STATE getTrajectoryState() {
        return Constants.PLATFORM_STATE.STOPPED;
    }

    public MockTrajectory(){
        Speed = new Vector2(0,0);
    }

    @Override
    public Vector2 getSpeedFrom(float deltatime, float waitingTime, boolean looping, Body body)
    {
        return Speed ;
    }

    @Override
    public void initCoolDown(float waitingTime) {}

    public void leaveStopState() {}
}
