package com.mygdx.rope.objects.traps;

/**
 * Created by Geoffrey on 27/07/2014.
 */
interface Integrable {
    public float getIntegratedValue();
    public void reactToHubFeedback(boolean activated);
}
