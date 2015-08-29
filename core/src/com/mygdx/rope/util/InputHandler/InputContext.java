package com.mygdx.rope.util.InputHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;


public class InputContext {
    private String name;
    private ArrayMap<String, Integer> keyStates;
    private ArrayMap<Integer, String> keyActions;
    //private Controller controller;

    private ObjectSet<InputActionListener> listeners;

    public InputContext() {
        keyStates = new ArrayMap<String, Integer>();
        keyActions = new ArrayMap<Integer, String>();
        //this.controller = controller;
        listeners = new ObjectSet<InputActionListener>();
    }

    public void load(Element contextElement) {
        keyActions.clear();
        keyStates.clear();

        try {
            name = contextElement.getAttribute("name");

            Element statesElement = contextElement.getChildByName("states");
            int numStates = statesElement != null ? statesElement.getChildCount() : 0;

            for (int i = 0; i < numStates; ++i) {
                Element stateElement = statesElement.getChild(i);
                String stateName = stateElement.getAttribute("name");

                Element keyElement = stateElement.getChildByName("key");

                if (keyElement != null) {
                    //int keycode = Keys.valueOf(keyElement.getAttribute("code"));
                    keyStates.put(stateName, keyElement.getInt("code", 0));
                }
            }

            Element actionsElement = contextElement.getChildByName("actions");
            int numActions = actionsElement != null ? actionsElement.getChildCount() : 0;

            for (int i = 0; i < numActions; ++i) {
                Element actionElement = actionsElement.getChild(i);
                String actionName = actionElement.getAttribute("name");

                Element keyElement = actionElement.getChildByName("key");

                if (keyElement != null) {
                    //int keycode = Keys.valueOf(keyElement.getAttribute("code"));
                    int keycode = keyElement.getInt("code", 0);
                    keyActions.put(keycode, actionName);
                }
            }
        }
        catch (Exception e) {
            Gdx.app.error("InputContext", "Error loading context element");
        }
    }

    public void addListener(InputActionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(InputActionListener listener) {
        listeners.remove(listener);
    }

    public Integer getButtonCode(String state) {
        if(keyStates.containsKey(state))
            return keyStates.get(state);
        else
            return null;
    }

    public String getName() {
        return name;
    }

    public boolean buttonDown(int keycode) {
        boolean processed = false;

        String action = keyActions.get(keycode);

        if (action != null) {
            for (InputActionListener listener : listeners) {
                processed = listener.OnAction(action);

                if (processed) {
                    break;
                }
            }
        }

        return processed;
    }
}