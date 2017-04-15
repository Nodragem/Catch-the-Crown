package com.mygdx.rope.objects.traps;

/**
 * Created by Nodragem on 26/06/2014.
 */
public interface HubInterface {
    public void addIntegrator(Integrable integrator);
    public void removeIntegrator(Integrable integrator);
    public void addTriggerable(Triggerable triggered); // we are doing a Observable/Observer pattern

    void resetTriggerables();

    public void removeTriggerable(Triggerable triggered);
}
