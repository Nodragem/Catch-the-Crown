package com.mygdx.rope.screens;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.rope.util.InputHandler.InputProfile;

/**
 * Created by Geoffrey on 29/08/2015.
 */
public interface Window {
    void update(float deltaTime);
    void render(float deltaTime);
    boolean executeSelectedAction();
    void selectNextAction();
    void selectPreviousAction();
    void selectAction(int index);
    void openWindow(InputProfile inputProfile, Window previousWindow);
    void callBackWindow();
    int getActivePlayer();
    void setPreviousWindow(Window window);
    Window getPreviousWindow();
    void requestClosing();
    boolean isClosed();
}
