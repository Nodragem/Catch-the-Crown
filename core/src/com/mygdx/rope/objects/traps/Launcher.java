package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.rope.objects.GameObject;

/**
 * Created by Geoffrey on 18/09/2016.
 */
public interface Launcher {
    void notifyKill(GameObject gameObject);
    void deliverProjectile();
}
