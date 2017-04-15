package com.mygdx.rope.util.InputHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.XmlReader;

public class InputProfileKeyboard implements InputProfile, InputProcessor {
    private ArrayMap<String, InputContext> contexts;
    private InputContext context;
    private ArrayMap<String, Integer> axisCode;
    private Vector2 movingAxisState;
    private Vector3 originPoint;
    private Camera camera;
    private Vector2 mousePoint;

    public InputProfileKeyboard(FileHandle handle, Camera camera) {
        axisCode = new ArrayMap<String, Integer>();
        this.camera = camera;
        originPoint = new Vector3();
        movingAxisState = new Vector2(0,0);
        contexts = new ArrayMap<String, InputContext>();
        context = null;
        loadContexts(handle);
    }

    public void loadContexts(FileHandle handle) {
        try {
            Gdx.app.debug("InputProfile", "Reading file " + handle.path());
            InputStream inputStream = handle.read();
            InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
            XmlReader reader = new XmlReader();

            XmlReader.Element root = reader.parse(streamReader);
            // read the contexts list
            XmlReader.Element contextList = root.getChildByName("contexts");
            int numContexts = contextList != null ? contextList.getChildCount() : 0;
            for (int i = 0; i < numContexts; ++i) {
                XmlReader.Element contextElement = contextList.getChild(i);
                InputContext context = new InputContext();
                context.load(contextElement);
                contexts.put(context.getName(), context);
            }
            // read the states list
            XmlReader.Element statesList = root.getChildByName("states");
            int numStates = statesList != null ? statesList.getChildCount() : 0;
            for (int i = 0; i < numStates; ++i) {
                XmlReader.Element stateElement = statesList.getChild(i);
                String stateName = stateElement.getAttribute("name");
                XmlReader.Element keyElement = stateElement.getChildByName("key");
                if (keyElement != null) {
                    //int keycode = Keys.valueOf(); (pas mal, keep that for keyboard)
                    axisCode.put(stateName, keyElement.getInt("code", 0));
                }
            }
        }
        catch (Exception e) {
            Gdx.app.error("InputProfile", "error loading file " + handle.path() + " " + e.getMessage());
        }
    }

    public void setContext(String contextName) {
        context = contexts.get(contextName);
    }

    public InputContext getContext() {
        return context;
    }

    public InputContext getContextByName(String name) {
        return contexts.get(name);
    }

    @Override
    public Float getAimingAngle(Vector2 origin) {
        // we project origin point on the screen coordinate
        camera.project(originPoint.set(origin.x, origin.y, 0));
        // we take the mouse position, switch the y-axis and we centered it on the "origin" position in order to get a pretty cos/sin
        mousePoint = new Vector2(Gdx.input.getX() - originPoint.x, Gdx.graphics.getHeight() - Gdx.input.getY() - originPoint.y);
        return mousePoint.angleRad();
    }

    @Override
    public Vector2 getMovingVector() {
        movingAxisState.set(
                (Gdx.input.isKeyPressed(axisCode.get("Right"))?1:0) + (Gdx.input.isKeyPressed(axisCode.get("Left"))?-1:0),
                (Gdx.input.isKeyPressed(axisCode.get("Up"))?1:0) + (Gdx.input.isKeyPressed(axisCode.get("Down"))?-1:0)
        );
        return movingAxisState;
    }

    @Override
    public boolean getButtonState(String name) {
        if (name.equals("LongAttack1")) // that's really dirty bu in the same time, who would change the attack button?
            return Gdx.input.isButtonPressed(0);
        if (name.equals("ShortAttack"))
            return Gdx.input.isButtonPressed(1);
        if (name.equals("PickUp"))
            return Gdx.input.isButtonPressed(2);
        Integer keycode = context.getButtonCode(name);
        if (keycode != null) {
            return Gdx.input.isKeyPressed(keycode);
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (context != null) {
            return context.buttonDown(keycode);
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}