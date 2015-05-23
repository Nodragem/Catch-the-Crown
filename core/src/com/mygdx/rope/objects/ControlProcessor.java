package com.mygdx.rope.objects;

import com.mygdx.rope.objects.characters.Character;

/**
 * Created by Nodragem on 05/05/2014.
 */
public interface ControlProcessor {
    public void processInputs(float deltaTime);
    public void setCharacter(Character character);
    public Character getCharacter();
}
