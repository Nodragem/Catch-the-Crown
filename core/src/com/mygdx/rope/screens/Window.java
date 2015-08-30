package com.mygdx.rope.screens;

/**
 * Created by Geoffrey on 29/08/2015.
 */
public interface Window {
    void update(float deltaTime);
    boolean executeSelectedAction();
    void selectNextAction();
    void selectPreviousAction();
    void selectAction(int index);
    void openWindow(int iplayer);
    int getActivePlayer();
    void closeWindow();
}