package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 26/06/2014.
 */
public interface HubInterface {
    public void addIntegrator(Integrable integrator);
    public void removeIntegrator(Integrable integrator);
    public void addTriggerable(Triggerable triggered); // we are doing a Observable/Observer pattern
    public void removeTriggerable(Triggerable triggered);
}
