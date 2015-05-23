package com.mygdx.rope.util.InputHandler;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Geoffrey on 31/01/2015.
 */
public interface InputProfile {
    public void loadContexts(FileHandle handle);
    public void setContext(String contextName);
    public InputContext getContext();
    public InputContext getContextByName(String name);
    public Float getAimingAngle(Vector2 origin);
    public Vector2 getMovingVector();
    public boolean getButtonState(String name);
}
