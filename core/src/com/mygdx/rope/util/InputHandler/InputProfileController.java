package com.mygdx.rope.util.InputHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.XmlReader;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Geoffrey on 31/01/2015.
 *
 * The context of such an InputProfileController need to have the entries:
 * Jump, Attack1, Attack2, PickUp, MovingAxis_X, MovingAxis_Y, AimingAxis_X, AimingAxis_Y
 */
public class InputProfileController implements ControllerListener, InputProfile {
    private ArrayMap<String, InputContext> contexts;
    private ArrayMap<String, Integer> axisCode;
    private Controller controller; // the controller he's polling should be the same controller he's listenning to
    private InputContext context;
    private Vector2 movingAxisState;
    private Vector2 aimingAxisState;


    public InputProfileController(FileHandle handle, Controller controller) {
        this.controller = controller;
        axisCode = new ArrayMap<String, Integer>();
        movingAxisState = new Vector2(0,0);
        aimingAxisState = new Vector2(0,0);
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

    public InputContext getContext() {return context; }

    public InputContext getContextByName(String name) {
        return contexts.get(name);
    }

    @Override
    public Float getAimingAngle(Vector2 origin) {
        movingAxisState.set( controller.getAxis(axisCode.get("MovingAxis_X")),
                -controller.getAxis(axisCode.get("MovingAxis_Y"))
        );
        aimingAxisState.set(controller.getAxis(axisCode.get("AimingAxis_X")),
                -controller.getAxis(axisCode.get("AimingAxis_Y"))
        );
        Float angle = 0.0f; //  the angle computation change according tothe user inputs;
        if (aimingAxisState.len() > 0.2){ // to put it first (and to keep it independant to the running) allows to move the aiming while running
            angle = aimingAxisState.angle() * MathUtils.degreesToRadians;
        }
        else if (movingAxisState.len() > 0.2){
            angle = movingAxisState.angle() * MathUtils.degreesToRadians;
        }
        else {
            angle = null;
        }
        return angle; //  in radians
    }

    @Override
    public Vector2 getMovingVector() {
        movingAxisState.set( controller.getAxis(axisCode.get("MovingAxis_X")),
                -controller.getAxis(axisCode.get("MovingAxis_Y"))
        );
        return movingAxisState;
    }

    @Override
    public boolean getButtonState(String name) {
        Integer keycode = context.getButtonCode(name);
        if (keycode != null) {
            return controller.getButton(keycode);
        }
        return false;
    }


    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (context != null) {
            return context.buttonDown(buttonCode);
        }

        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
