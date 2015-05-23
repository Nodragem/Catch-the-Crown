package com.mygdx.rope.objects.traps;

import com.mygdx.rope.util.ContactData;

/**
 * Created by Geoffrey on 27/07/2014.
 */
interface Integrable {
    public float getIntegratedValue();
    public void reactToHubFeedback(boolean activated);
}
