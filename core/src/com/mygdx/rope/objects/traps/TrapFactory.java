package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.util.Constants;

import java.util.Locale;

/**
 * Created by Nodragem on 22/06/2014.
 */
public class TrapFactory {
    GameScreenTournament gameScreen;
    float units;
    ArrayMap <String, HubInterface> listHubs; // Logic Nodes are called HUBs here
    HubInterface currentHub; // Logic Nodes are called HUBs here

    public TrapFactory(GameScreenTournament gameScreen){
        this.gameScreen = gameScreen;
        units = Constants.TILES_SIZE;
        listHubs = new ArrayMap<String, HubInterface>();
    }

    public ObjectMap<GameObject, Integer> extractInteractiveObjectsFrom(MapLayer mapLayer){
        Array <RectangleMapObject> interactiveObject;
        interactiveObject = mapLayer.getObjects().getByType(RectangleMapObject.class);
        GameObject newObject = null;
        ObjectMap <GameObject, Integer> toParent = new ObjectMap();
        for (RectangleMapObject obj: interactiveObject ) {
            String nameObject = obj.getProperties().get("Object", String.class);
            Integer ownID = obj.getProperties().get("id", Integer.class);
            Gdx.app.debug("Trap Factory: ", "Build "+nameObject+" "+ownID);
            String typeConnection = obj.getProperties().get("type", String.class);
            String HubInID = obj.getProperties().get("Hub_in", null, String.class);
            String HubOutID = obj.getProperties().get("Hub_out", null, String.class);
            Integer parentID = Integer.parseInt(obj.getProperties().get("Parent", "-1", String.class));

            if (typeConnection.equals("Triggerable")){
               newObject = (GameObject) addTriggerable(mapLayer, obj, nameObject, HubInID);
            }
            else if (typeConnection.equals("Integrator")) {
                newObject = (GameObject) addIntegrator(mapLayer, obj, nameObject, HubInID, HubOutID);
            }
            if (newObject != null){
                newObject.setID(ownID);
                Gdx.app.debug("TrapFactory: ", "Add Parent Step 1, own ID: "+ownID);
                if(parentID != -1) {
                    toParent.put(newObject, parentID);
                }

            }
        }
        return toParent;

    }

    public void extractLogicNodeFrom(MapLayer mapLayer){ // Logic Nodes are called HUBs here
        Array<EllipseMapObject> HubsDescription;
        HubsDescription = mapLayer.getObjects().getByType(EllipseMapObject.class);
        for(EllipseMapObject ellipse: HubsDescription){
            listHubs.put(ellipse.getProperties().get("ID", String.class), new SimpleHub(gameScreen));
        }
    }

    public ArrayMap<String, HubInterface> getListHubs() {
        return listHubs;
    }

    private Integrable addIntegrator(MapLayer mapLayer, RectangleMapObject rectangleObj, String nameObject, String HubInID, String HubOutID) {
        Integrable newInteractiveObject = null;
        Rectangle rectangle = convertToUnits(rectangleObj.getRectangle());
        Gdx.app.debug("TrapFactory.addIntegrator()", "nameObject to add:"+ nameObject);
        Constants.INTEGRATOR_TYPE typeObject = Constants.INTEGRATOR_TYPE.valueOf(nameObject.toUpperCase(Locale.ENGLISH));
        if (typeObject != null) {
            switch (typeObject) {
                case SIMPLESWITCH:
                    float rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class); // degree
                    int weight = Integer.parseInt(rectangleObj.getProperties().get("weight", String.class)); // all custom properties are strings in TileMap Editor
                    String SwitcherType = rectangleObj.getProperties().get("SwitcherType", "0", String.class);
                    boolean isEnabledByDefault = rectangleObj.getProperties().get("isEnabledByDefault", true, Boolean.class);

                    JsonReader jsonreader = new JsonReader();
                    FileHandle handle = Gdx.files.internal("object_types.json");
                    JsonValue info_type = jsonreader.parse(handle);
                    info_type = info_type.get("switcher_types");
                    Gdx.app.debug("TrapFactory", "Check of switcher_type: "+ info_type);
                    if (SwitcherType != null)
                        info_type = info_type.get(SwitcherType);
                    else
                        info_type = info_type.get("totem_switch");

                    float offset_y_all = info_type.getFloat("offsety_all", 0);
                    Vector2 correctionPos = new Vector2(
                            //  usual correction as see below in SPIKES || correction for the offset
                            // -----------------------------------||-------------------------------------------|
                            0 + MathUtils.cosDeg(-rotation - 90) + offset_y_all * MathUtils.sinDeg(-rotation),
                            1 + MathUtils.sinDeg(-rotation-90) - offset_y_all * MathUtils.cosDeg(-rotation)
                    );

                    Gdx.app.debug("TrapFactory", "Check of switcher_type.simple: "+ info_type);
                    newInteractiveObject = new SimpleSwitcher(gameScreen,
                            rectangle.getPosition(new Vector2()).add(correctionPos), rectangle.getSize(new Vector2()),
                            -rotation, info_type, weight, isEnabledByDefault);
//                    newInteractiveObject = new SimpleSwitcher(gameScreen,
//                            rectangle.getPosition(new Vector2()).add(correctionPos), rectangle.getSize(new Vector2()),
//                            -rotation*MathUtils.degreesToRadians, texture_name, info, weight, isEnabledByDefault);
                    break;
                case HOLDINGSWITCH:
                    break;
            }
            currentHub = listHubs.get(HubOutID);
            if (newInteractiveObject != null && currentHub != null) {
                currentHub.addIntegrator(newInteractiveObject);
                Gdx.app.debug("TrapFactory.addIntegrator()", nameObject+" added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Integrable " + typeObject.toString() + " not found!");
            // now integrator can be triggered:
            currentHub = listHubs.get(HubInID);
            if (newInteractiveObject != null && newInteractiveObject instanceof Triggerable && currentHub != null) {
                currentHub.addTriggerable((Triggerable) newInteractiveObject);
                Gdx.app.debug("TrapFactory.addTriggerable()", nameObject+" added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Triggerable " + typeObject.toString() + " found! but weird problem in switch case...");
        }
        else {
            Gdx.app.debug("TrapFactory", "Type Integrable " + typeObject.toString() + " not found!");
        }
        return newInteractiveObject;
    }

    private Triggerable addTriggerable(MapLayer mapLayer, RectangleMapObject rectangleObj, String nameObject, String HubID){
        Triggerable newInteractiveObject = null;
        float rotation;
        float intervalON;
        boolean defaultON;
        Vector2 correctionPos;
        Rectangle rectangle = convertToUnits(rectangleObj.getRectangle());
        Gdx.app.debug("TrapFactory.addTriggerable()", "nameObject to add:"+ nameObject);
        Constants.TRIGGERABLE_TYPE typeObject = Constants.TRIGGERABLE_TYPE.valueOf(nameObject.toUpperCase(Locale.ENGLISH));
        if (typeObject != null) {
            switch (typeObject) {
                case SPIKES:
                    //float rotation = Float.parseFloat(rectangleObj.getProperties().get("myRotation", String.class));
                    //float rotation = rectangleObj.getProperties().get("rotation", Float.class);
                    rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class);
                    float intervalOFF =0;
                    if (rectangleObj.getProperties().get("intervalOFF", String.class) != null)
                        intervalOFF = Float.parseFloat(rectangleObj.getProperties().get("intervalOFF", String.class));
                    intervalON =0;
                    if (rectangleObj.getProperties().get("intervalON", String.class) != null)
                        intervalON = Float.parseFloat(rectangleObj.getProperties().get("intervalON", String.class));
                    defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    //Gdx.app.debug("FactoryTrap", "RotationTiled String: "+ rectangleObj.getProperties().get("rotation", String.class)+"; myRotation: "+rotation );
                    Gdx.app.debug("FactoryTrap", "RotationTiled Float: "+ rectangleObj.getProperties().get("rotation", Float.class)+"; myRotation "+rotation );
                    /* Tiled map give the coordinate of the top-left corner of its object and libGDX translate those coordinates
                    *   to bottom-left corner origin just by adding +1 on the y-axis. That simplistic method do not take in account rotation.
                    *  Thus we have to add 1 on y-axis to inverse the libGDX translation, and then we apply our own translation taking in account the rotation
                    *  Moreover, note that a rotation r in Tiled is a rotation -r in libGDX. */
                    correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
                    //Vector2 correctionPos =  new Vector2(0,0);
                    newInteractiveObject = new Spikes(gameScreen, rectangle.getPosition(new Vector2()).add(correctionPos), rectangle.getSize(new Vector2()) , -rotation, "spikes", intervalON, intervalOFF, defaultON);
                    break;
                case LAUNCHER:
                    rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class);
                    float intervalShoot =0;
                    if (rectangleObj.getProperties().get("intervalShoot", String.class) != null)
                        intervalShoot = Float.parseFloat(rectangleObj.getProperties().get("intervalShoot", String.class));
                    float reloadTime =0;
                    if (rectangleObj.getProperties().get("reloadTime", String.class) != null)
                        reloadTime = Float.parseFloat(rectangleObj.getProperties().get("reloadTime", String.class));
                    float impulse =0;
                    if (rectangleObj.getProperties().get("impulse", String.class) != null)
                        impulse = Float.parseFloat(rectangleObj.getProperties().get("impulse", String.class));
                    int nbpool =0;
                    if (rectangleObj.getProperties().get("pool", String.class) != null)
                        nbpool = Integer.parseInt(rectangleObj.getProperties().get("pool", String.class));
                    String type_projectile = null;
                    if (rectangleObj.getProperties().get("Child", String.class) != null)
                        type_projectile = rectangleObj.getProperties().get("Child", String.class);

                    correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
                    defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    newInteractiveObject = new SimpleLauncher(gameScreen,rectangle.getPosition(new Vector2()).add(correctionPos),
                            rectangle.getSize(new Vector2()) , -rotation, intervalShoot, reloadTime, impulse, nbpool, defaultON, type_projectile );
                    break;
                case PLATFORM:
                    rotation =0;
                    if (rectangleObj.getProperties().get("rotation", Float.class) != null)
                        rotation = rectangleObj.getProperties().get("rotation", Float.class);
                    correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
                    Gdx.app.debug("FactoryTrap", "PLATFORM" );
                    Gdx.app.debug("FactoryTrap", "RotationTiled "+ rectangleObj.getProperties().get("Rotation", String.class)+"; myRotation "+0.0f );
                    String pathID = rectangleObj.getProperties().get("Path", String.class);
                    float rotationSpeed =0;
                    if (rectangleObj.getProperties().get("rotationSpeed", String.class) != null)
                        rotationSpeed = Float.parseFloat(rectangleObj.getProperties().get("rotationSpeed", String.class));
                    float waitingTime = Float.parseFloat(rectangleObj.getProperties().get("Wait", String.class));
                    MapObject path = mapLayer.getObjects().get(pathID);
                    defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    boolean alwaysVisible = Boolean.valueOf(rectangleObj.getProperties().get("alwaysVisible", "true", String.class));
                    String textureName = rectangleObj.getProperties().get("texture", "wood", String.class);
                    newInteractiveObject = new MovingPlatform(gameScreen, rectangle.getPosition(new Vector2()).add(correctionPos),
                            rectangle.getSize(new Vector2()),-rotation, path, rotationSpeed, waitingTime, defaultON, alwaysVisible, textureName);
                    break;
            }
            currentHub = listHubs.get(HubID);
            if (newInteractiveObject != null && currentHub != null) {
                currentHub.addTriggerable(newInteractiveObject);
                Gdx.app.debug("TrapFactory.addTriggerable()", nameObject+" added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Triggerable " + typeObject.toString() + " found! but weird problem in switch case...");
        }
        else {
            Gdx.app.debug("TrapFactory", "Type Triggerable " + typeObject.toString() + " not found!");
        }
        return newInteractiveObject;
    }

    private Rectangle convertToUnits(Rectangle rectangle) {
        return new Rectangle(rectangle.x/ units, rectangle.y/ units, rectangle.width/units, rectangle.height/units); // seems to be the position
    }


}
