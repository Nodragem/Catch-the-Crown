package com.mygdx.rope.util.TrajectorySolver;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.rope.util.Constants;

/**
 * Created by Geoffrey on 10/01/2015.
 */
public interface TrajectorySolver {
    public Vector2 getSpeedFrom(float deltatime, float waitingTime, boolean looping);
    void initCoolDown(float waitingTime);
    void leaveStopState();
    Constants.PLATFORM_STATE getTrajectoryState();
}
