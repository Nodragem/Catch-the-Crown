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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;

import java.util.Locale;

/**
 * Created by Nodragem on 22/06/2014.
 */
public class TrapFactory {
    GameScreen gameScreen;
    float units;
    ArrayMap <String, HubInterface> listHubs; // Logic Nodes are called HUBs here
    HubInterface currentHub; // Logic Nodes are called HUBs here

    public TrapFactory(GameScreen gameScreen){
        this.gameScreen = gameScreen;
        units = Constants.TILES_SIZE;
        listHubs = new ArrayMap<String, HubInterface>();
    }

    public void extractInteractiveObjectsFrom(MapLayer mapLayer){
        Array <RectangleMapObject> interactiveObject;
        interactiveObject = mapLayer.getObjects().getByType(RectangleMapObject.class);
        for (RectangleMapObject obj: interactiveObject ) {
            String nameObject = obj.getProperties().get("Object", String.class);
            String typeConnection = obj.getProperties().get("type", String.class);
            String HubID = obj.getProperties().get("Parent", String.class);
            if (typeConnection.equals("Triggerable")){
                addTriggerable(mapLayer, obj, nameObject, HubID);
            }
            else if (typeConnection.equals("Integrator")) {
                addIntegrator(mapLayer, obj, nameObject, HubID);
            }
        }
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

    private void addIntegrator( MapLayer mapLayer, RectangleMapObject rectangleObj, String nameObject, String HubID) {
        Integrable newInteractiveObject = null;
        Rectangle rectangle = convertToUnits(rectangleObj.getRectangle());
        Gdx.app.debug("TrapFactory.addIntegrator()", "nameObject to add:"+ nameObject);
        Constants.INTEGRATOR_TYPE typeObject = Constants.INTEGRATOR_TYPE.valueOf(nameObject.toUpperCase(Locale.ENGLISH));
        if (typeObject != null) {
            switch (typeObject) {
                case SIMPLESWITCH:
                    float rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class);

                    int weight = Integer.parseInt(rectangleObj.getProperties().get("weight", String.class));
                    String SwitcherType = null;
                    if (rectangleObj.getProperties().get("SwitcherType", String.class) != null)
                        SwitcherType = rectangleObj.getProperties().get("SwitcherType", String.class);

                    JsonReader jsonreader = new JsonReader();
                    FileHandle handle = Gdx.files.internal("object_types.json");
                    JsonValue info = jsonreader.parse(handle);
                    info = info.get("switcher_types");
                    Gdx.app.debug("TrapFactory", "Check of switcher_type: "+ info);
                    if (SwitcherType != null)
                        info = info.get(SwitcherType);
                    else
                        info = info.get("totem_switch");
                    float offset_y_all = info.getFloat("offsety_all", 0);

                    Vector2 correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation - 90) + offset_y_all * MathUtils.sinDeg(-rotation), 1 + MathUtils.sinDeg(-rotation-90) + offset_y_all * MathUtils.cosDeg(-rotation));
                    String texture_name = info.getString("texture", null);
                    Gdx.app.debug("TrapFactory", "Check of switcher_type.simple: "+ info);
                    newInteractiveObject = new SimpleSwitcher(gameScreen, rectangle.getPosition(new Vector2()).add(correctionPos), rectangle.getSize(new Vector2()) , -rotation*MathUtils.degreesToRadians, texture_name, info, weight);
                    break;
                case HOLDINGSWITCH:
                    break;
            }
            currentHub = listHubs.get(HubID);
            if (newInteractiveObject != null && currentHub != null) {
                currentHub.addIntegrator(newInteractiveObject);
                Gdx.app.debug("TrapFactory.addIntegrator()", nameObject+" added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Integrable " + typeObject.toString() + " not found!");
        }
        else {
            Gdx.app.debug("TrapFactory", "Type Integrable " + typeObject.toString() + " not found!");
        }
    }

    private void addTriggerable( MapLayer mapLayer, RectangleMapObject rectangleObj, String nameObject, String HubID){
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
                    rotation =0;
                    if (rectangleObj.getProperties().get("rotation", Float.class) != null)
                        rotation = rectangleObj.getProperties().get("rotation", Float.class);
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
                            rectangle.getSize(new Vector2()) , -rotation*MathUtils.degreesToRadians, intervalShoot, reloadTime, impulse, nbpool, defaultON, type_projectile );
                    break;
                case PLATFORM:
                    Gdx.app.debug("FactoryTrap", "PLATFORM" );
                    Gdx.app.debug("FactoryTrap", "RotationTiled "+ rectangleObj.getProperties().get("Rotation", String.class)+"; myRotation "+0.0f );
                    String pathID = rectangleObj.getProperties().get("Path", String.class);
                    float rotationSpeed =0;
                    if (rectangleObj.getProperties().get("rotationSpeed", String.class) != null)
                        rotationSpeed = Float.parseFloat(rectangleObj.getProperties().get("rotationSpeed", String.class));
                    MapObject path = mapLayer.getObjects().get(pathID);
                    defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    boolean alwaysVisible = Boolean.valueOf(rectangleObj.getProperties().get("alwaysVisible", "true", String.class));
                    String textureName = rectangleObj.getProperties().get("texture", "wood", String.class);
                    newInteractiveObject = new MovingPlatform(gameScreen, rectangle.getPosition(new Vector2()), rectangle.getSize(new Vector2()), path, rotationSpeed, defaultON, alwaysVisible, textureName);
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
    }

    private Rectangle convertToUnits(Rectangle rectangle) {
        return new Rectangle(rectangle.x/ units, rectangle.y/ units, rectangle.width/units, rectangle.height/units); // seems to be the position
    }


}
