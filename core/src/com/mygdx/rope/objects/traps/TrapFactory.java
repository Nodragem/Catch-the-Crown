package com.mygdx.rope.objects.traps;

import com.badlogic.gdx.Gdx;
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
        // the HUB object are circleObjects and the Path are lineObjects
        // so that we are sure we are not iterating through them.
        for (RectangleMapObject obj: interactiveObject ) {

            newObject = (GameObject) addInteractiveObject(mapLayer, obj);

            Integer ownID = obj.getProperties().get("id", Integer.class);
            Integer parentID = Integer.parseInt(obj.getProperties().get("Parent", "-1", String.class));
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
            boolean silent = Boolean.parseBoolean(ellipse.getProperties().get("isSilent", "true", String.class));
            listHubs.put(ellipse.getProperties().get("ID", String.class), new SimpleHub(gameScreen, silent));
        }
    }

    public ArrayMap<String, HubInterface> getListHubs() {
        return listHubs;
    }

    private Triggerable addInteractiveObject(MapLayer mapLayer, RectangleMapObject rectangleObj) {
        /* -------------------------------------------------------------------------------------------------
        *  About: correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
        *  Tiled map give the coordinate of the top-left corner of its object and libGDX translate those coor-
        *  dinates to bottom-left corner origin just by adding +1 on the y-axis. That simplistic method do
        *  not take in account rotation.
        *  Thus we have to add 1 on y-axis to inverse the libGDX translation, and then we apply our own
        *  translation taking in account the rotation.
        *  Moreover, note that a rotation r in Tiled is a rotation -r in libGDX.
        *
        *  WARNING: not custom properties in Tiled starts with Uppercase in the GUI, but not here.
        *  ------------------------------------------------------------------------------------------------- */
        Triggerable newInteractiveObject = null;
        float rotation;
        Vector2 correctionPos;
        String objectDataID = "";
        String TypeObject = rectangleObj.getProperties().get("type", String.class);
        String HubInID = rectangleObj.getProperties().get("Hub_in", null, String.class);
        String HubOutID = rectangleObj.getProperties().get("Hub_out", null, String.class);
        Rectangle rectangle = convertToUnits(rectangleObj.getRectangle());
        Gdx.app.debug("TrapFactory.addInteractiveObject()", "Type of Object to add:" + TypeObject );
        Constants.MAPOBJECT_TYPE TYPEOBJECT = Constants.MAPOBJECT_TYPE.valueOf(TypeObject.toUpperCase(Locale.ENGLISH));

        if (TYPEOBJECT != null) {
            switch (TYPEOBJECT) {
                case SWITCH:
                    rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class); // degree
                    int weight = Integer.parseInt(rectangleObj.getProperties().get("weight", String.class)); // all custom properties are strings in TileMap Editor
                    objectDataID = rectangleObj.getProperties().get("Subtype", "0", String.class);
                    boolean isEnabledByDefault = rectangleObj.getProperties().get("isEnabledByDefault", true, Boolean.class);

                    JsonValue objectInfo = null;
                    if (objectDataID != null && gameScreen.getObjectDataBase().has(objectDataID))
                        objectInfo = gameScreen.getObjectDataBase().get(objectDataID);
                    else
                        objectInfo = gameScreen.getObjectDataBase().get("totem_switch");

                    float text_offsety = objectInfo.getFloat("texture_offsety", 0);
                    correctionPos = new Vector2(
                            // FIXME: Check whether it can be/need to be implemented for other objects
                            //  usual correction as see below in TRAP || correction for the offset
                            // -----------------------------------||-------------------------------------------|
                            0 + MathUtils.cosDeg(-rotation - 90) + text_offsety * MathUtils.sinDeg(-rotation),
                            1 + MathUtils.sinDeg(-rotation-90) - text_offsety * MathUtils.cosDeg(-rotation)
                    );

                    newInteractiveObject = new SimpleSwitcher(gameScreen,
                            rectangle.getPosition(new Vector2()).add(correctionPos), rectangle.getSize(new Vector2()),
                            -rotation, objectDataID, weight, isEnabledByDefault);
                    break;
                case TRAP:
                    rotation = rectangleObj.getProperties().get("rotation", 0f, Float.class);
                    float intervalOFF =0;
                    if (rectangleObj.getProperties().get("intervalOFF", String.class) != null)
                        intervalOFF = Float.parseFloat(rectangleObj.getProperties().get("intervalOFF", String.class));
                    float intervalON =0;
                    if (rectangleObj.getProperties().get("intervalON", String.class) != null)
                        intervalON = Float.parseFloat(rectangleObj.getProperties().get("intervalON", String.class));
                    boolean defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    Gdx.app.debug("FactoryTrap", "RotationTiled Float: "+ rectangleObj.getProperties().get("rotation", Float.class)+"; myRotation "+rotation );

                    correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
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
                    rotation = Float.parseFloat(rectangleObj.getProperties().get("rotation", "0", String.class));
                    correctionPos = new Vector2(0+ MathUtils.cosDeg(-rotation-90),1+ MathUtils.sinDeg(-rotation-90));
                    Gdx.app.debug("FactoryTrap", "PLATFORM" );
                    Gdx.app.debug("FactoryTrap", "RotationTiled "+ rectangleObj.getProperties().get("Rotation", String.class)+"; myRotation "+0.0f );
                    String pathID = rectangleObj.getProperties().get("Path", String.class);
                    objectDataID = rectangleObj.getProperties().get("Subtype", String.class);
                    float rotationSpeed = Float.parseFloat(rectangleObj.getProperties().get("rotationSpeed", "0", String.class));
                    float waitingTime = Float.parseFloat(rectangleObj.getProperties().get("waitingTime", "0", String.class));
                    MapObject path = mapLayer.getObjects().get(pathID);
                    defaultON = Boolean.valueOf(rectangleObj.getProperties().get("defaultON", "true", String.class));
                    boolean alwaysVisible = Boolean.valueOf(rectangleObj.getProperties().get("alwaysVisible", "true", String.class));
//                    String color_texture = rectangleObj.getProperties().get("texture", "wood", String.class);
                    newInteractiveObject = new MovingPlatform(gameScreen,
                            rectangle.getPosition(new Vector2()).add(correctionPos),
                            rectangle.getSize(new Vector2()),-rotation, path, rotationSpeed, waitingTime,
                            defaultON, alwaysVisible, objectDataID);
                    break;
            }
            currentHub = listHubs.get(HubOutID);
            if (newInteractiveObject != null && newInteractiveObject instanceof Integrable && currentHub != null) {
                currentHub.addIntegrator((Integrable) newInteractiveObject);
                Gdx.app.debug("TrapFactory.addIntegrator()", TypeObject + " of Subtype " + objectDataID +
                        " added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Integrable " + TYPEOBJECT.toString() + " not found!");
            // now integrator can be triggered:
            currentHub = listHubs.get(HubInID);
            if (newInteractiveObject != null && newInteractiveObject instanceof Triggerable && currentHub != null) {
                currentHub.addTriggerable((Triggerable) newInteractiveObject);
                Gdx.app.debug("TrapFactory.addTriggerable()", TypeObject +" of Subtype " + objectDataID +
                        " added to "+listHubs.getKey(currentHub, true));
            }
            else
                Gdx.app.debug("TrapFactory", "Type Triggerable " + TYPEOBJECT.toString() +
                        " found! but weird problem in switch case...");
        }
        else {
            Gdx.app.debug("TrapFactory", "Type Integrable " + TYPEOBJECT.toString() + " not found!");
        }
        return newInteractiveObject;
    }

    private Rectangle convertToUnits(Rectangle rectangle) {
        return new Rectangle(rectangle.x/ units, rectangle.y/ units, rectangle.width/units, rectangle.height/units); // seems to be the position
    }


}
