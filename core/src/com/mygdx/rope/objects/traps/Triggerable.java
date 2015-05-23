package com.mygdx.rope.objects.traps;

/**
 * Created by Geoffrey on 18/10/2014.
 */
public interface Triggerable {
    public void reset();
    public  boolean isDefaultON();
    public void triggerONActions(HubInterface hub);
    public void triggerOFFActions(HubInterface hub);
}
