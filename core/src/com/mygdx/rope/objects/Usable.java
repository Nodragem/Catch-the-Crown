package com.mygdx.rope.objects;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Geoffrey on 15/02/2015.
 */
public interface Usable {
    public void use(GameObject obj, Vector2 position, Vector2 impulse);
}
