package com.mygdx.rope.objects;

import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.rope.objects.characters.Character;

/**
 * Created by Geoffrey on 24/09/2016.
 */
public interface Carriable {
    void setCarrier(Character carrier);
    Character getCarrier();
    Body getBody();
}
