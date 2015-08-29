package com.mygdx.rope.screens;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.objects.*;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.objects.characters.Player;
import com.mygdx.rope.objects.collectable.Coins;
import com.mygdx.rope.objects.collectable.Crown;
import com.mygdx.rope.objects.stationary.BlockFactory;
import com.mygdx.rope.objects.traps.TrapFactory;
import com.mygdx.rope.util.*;
import com.mygdx.rope.util.InputHandler.InputProfile;
import com.mygdx.rope.util.InputHandler.InputProfileController;
import com.mygdx.rope.util.InputHandler.InputProfileKeyboard;
import com.mygdx.rope.util.Constants.GAME_STATE;
import com.mygdx.rope.util.assetmanager.Assets;

import java.lang.String;
import java.util.Iterator;

public class GameScreen implements Screen {
	private static final boolean DEBUG_BOX2D = true;
	private TiledMap map;
    private ArrayMap <String, Player> playersList;
    public int ipauser = 0; // the player who pressed the pause button and control the pause menu.
	private OrthogonalTiledMapRenderer map_renderer;
	private OrthographicCamera camera;
	private CameraHelper cameraHelper;
    private GUILayer GUIlayer;
    public GlyphLayout glayout = new GlyphLayout();
    private GameObject cameraTarget;
    private ContactData bufferContactData;
    public Assets assetManager;
    public Viewport gameViewport;
    public Array <GameObject> objectsToRender;
    public Array <Updatable> objectsToUpdate;
    private Array<Updatable> objectsToWipeOut;
    public Array <Controller> XboxControllers;
    public ArrayMap<String, Integer> victoryTable;
    public ArrayMap<String, Integer> finalScoreTable;
	private World b2world;
    public GAME_STATE stateGame;
	private Box2DDebugRenderer b2debug;
	private SpriteBatch batch;
    private int[] foregroundLayers = { 2 };
    private int[] backgroundLayers = {0, 1};
    private Array<Vector2> playerSpawners;
    private boolean fullscreen;
    public float timer; // ms
    private float groupScore;
    private Crown theCrown;
    private int currentLevel;
    private Array<String> listLevels;
    private GAME_STATE previousStateGame;
    private int victoryThreshold;

    public GameScreen(int currentLevel, Array<String> listLevels){
        this.currentLevel = currentLevel;
        this.listLevels = listLevels;
    }

    @Override
    public void show() {
        assetManager = new Assets(); // mainly for the sounds,
        assetManager.loadGroups("sounds/soundGroups.json"); // will have a file per level
        assetManager.finishLoading();
        //Gdx.app.debug("Asset Manager", ""+assetManager.get("jump", 0));
        //enableFullScreen();
        if (gameViewport == null) {
//        //gameViewport = null;
            camera = new OrthographicCamera();
            camera.position.set(Constants.VIEW_PORT_WIDTH/2, Constants.VIEW_PORT_HEIGHT/2,0);
            camera.update();
            gameViewport = new FitViewport(Constants.VIEW_PORT_WIDTH, Constants.VIEW_PORT_HEIGHT, camera);
            //gameViewport.update(gameViewport.getScreenWidth(), gameViewport.getScreenHeight());
            gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        victoryThreshold = 2;
        victoryTable = null;
        cameraHelper = new CameraHelper();
        cameraHelper.setTarget(cameraTarget);
        GUIlayer = new GUILayer(this); // is empty (no players)
        setDebugText("DEBUG ON");
        startNewLevel(listLevels.get(currentLevel));
        if (victoryTable == null){
            victoryTable = new ArrayMap<String, Integer>(4);
            for (int i = 0; i < playersList.size; i++) {
                victoryTable.put(playersList.getKeyAt(i), 0);
            }
        }
        if(finalScoreTable == null){
            finalScoreTable = new ArrayMap<String, Integer>(4);
            for (int i = 0; i < playersList.size; i++) {
                finalScoreTable.put(playersList.getKeyAt(i), 0);
            }
        }



    }

    public void startNewLevel(String levelname){
        groupScore = 0;
        stateGame = GAME_STATE.PLAYED;
        GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_GUI);
        timer = Constants.STARTTIMER;
        objectsToRender = new Array<GameObject>();
        objectsToUpdate = new Array<Updatable>();
        objectsToWipeOut = new Array<Updatable>();
        playerSpawners = new Array<Vector2>();
        batch = new SpriteBatch();
        b2debug = new Box2DDebugRenderer();

        b2world = new World(new Vector2(0, -9.81f), true);
        b2world.setAutoClearForces(true);
        b2world.setContactListener(new MyContactListener());

        map =  new TmxMapLoader().load(levelname);
        map_renderer = new OrthogonalTiledMapRenderer(map, 1/Constants.TILES_SIZE); // is it here I tell him I want 32 pixels = 1 unit
        placeObjectsFromMap(map);
        createPlayers();
        GUIlayer.updatePlayerList(playersList);

    }

    private void createPlayers(){
        JsonReader jsonreader = new JsonReader();
        FileHandle handle = Gdx.files.local("preferences.json");
        if (!handle.exists())
            handle = Gdx.files.internal("preference/preferences.json");
        JsonValue root = jsonreader.parse(handle);
        int nb_players = root.getInt("nb_players", 4);
        XboxControllers = Controllers.getControllers();
        int nb_controllers = XboxControllers.size;
        int nb_keyboard = 1;
        playersList = new ArrayMap<String, Player>(XboxControllers.size);
        // loop over player list:
        int nb_created_player = 0;
        for (JsonValue item:root.get("players")){
            if (nb_created_player >= nb_players)
                break;
            Gdx.app.debug("GameScreen", "player: " + item.get("name"));
            boolean keyboard = item.getString("input", "controller").equals("keyboard");
            InputProfile inputProfil = null;
            if (keyboard & nb_keyboard > 0){
                inputProfil = new InputProfileKeyboard(Gdx.files.internal("preference/profileKeyboard.xml"), camera);
                nb_keyboard -= 1;
            }
            else if (nb_controllers > 0){
                inputProfil = new InputProfileController(Gdx.files.internal("preference/profileController.xml"),
                        XboxControllers.get(nb_controllers-1));
                nb_controllers -= 1;
            }
            if (inputProfil == null)
                break;
            inputProfil.setContext("Game");
            Player player = new Player(item.getString("name", "Player "+(nb_created_player+1)),
                    new Character(this, new Vector2(15,15), item.getString("color", "purple")), inputProfil, this);
            playersList.put(player.getName(), player);
            nb_created_player +=1;
        }

        for (int i = 0; i < playersList.size; i++) {
            playersList.getValueAt(i).getCharacter().setPosition(getSpawnPosition()); // spawners are place by the placeObjectsFromMap
        }
    }

    public ArrayMap<String, Player> getPlayersList() {
        return playersList;
    }

    private void placeObjectsFromMap(TiledMap map) {
        ObjectMap<GameObject, Integer> toParent = new ObjectMap<GameObject, Integer>();
        BlockFactory mapBodyManager = new BlockFactory(b2world, null, 1); // tiles of 32 pixels/32 = 1 unit
        mapBodyManager.createPhysics(map, "Collision");

        TrapFactory trapFactory = new TrapFactory(this);
        MapLayer trapLayer = map.getLayers().get("InteractiveObjects");
        trapFactory.extractLogicNodeFrom(trapLayer);
        toParent.putAll(trapFactory.extractInteractiveObjectsFrom(map.getLayers().get("MovingPlatforms")));
        toParent.putAll(trapFactory.extractInteractiveObjectsFrom(trapLayer));


        // create Parent relationships
        for (ObjectMap.Entry<GameObject, Integer> gameObjectIntegerEntry : toParent) {
            for (Updatable updatable : this.objectsToUpdate) {
                Gdx.app.debug("TrapFactory: ", "Add Parent  Step 1.5, Parent ID: " + gameObjectIntegerEntry.value);
                if (updatable instanceof GameObject) {
                    GameObject iterObject = (GameObject) updatable;
                    Gdx.app.debug("TrapFactory: ", "Add Parent  Step 2: " + iterObject + "\n ID: " + iterObject.ID);
                    if (iterObject.ID != null && iterObject.ID.equals(gameObjectIntegerEntry.value)) {
                        try {
                            gameObjectIntegerEntry.key.setParent(iterObject);
                            Gdx.app.debug("TrapFactory: ", "Add Parent  Step 3");
                            break;
                        } catch (Exception e) {
                            Gdx.app.debug("TrapFactory: ", "Cannot attach an object to its parent...");
                        }
                    }
                }
            }
        }


        // the coins need the factory and its list of Hubs:
        Array<Coins> coingroups = new Array<Coins>(10);
        for (int k = 1; k < 10; k++) { // we limit the number of coin groups to 10
            TiledMapTileLayer coinslayer = (TiledMapTileLayer) map.getLayers().get("collectable_"+k);
            if (coinslayer == null)
                break;
            coingroups.add(new Coins(this, new Vector2(0,0), coinslayer, trapFactory.getListHubs()));
        }
        // Let's spawn the crown oand the Spawners:
        MapObjects objects = map.getLayers().get("Entities").getObjects();
        initObjectPositions(objects, "Spawner", playerSpawners);
        Array<Vector2> posCrown = new Array<Vector2>(1);
        initObjectPositions(objects,"Crown", posCrown);
        theCrown = new Crown(this, posCrown.first(), coingroups);

    }

    public Vector2 getSpawnPosition() {
        return playerSpawners.random();
    }

    private void initObjectPositions(MapObjects objects, String name, Array<Vector2> output) {
        Iterator<MapObject> objectIt = objects.iterator();
        while(objectIt.hasNext()) {
            MapObject object = objectIt.next();
            Gdx.app.debug("initObj", name+" detected? "+ object.getName() );
            if (object instanceof RectangleMapObject && object.getName().equals(name)) {
                Gdx.app.debug("initObj", name+" detected");
                RectangleMapObject rectangle = (RectangleMapObject) object;
                Vector2 pos = rectangle.getRectangle().getPosition(new Vector2(0,0)).scl(1/Constants.TILES_SIZE);
                output.add(pos);
            }
        }
    }

    @Override
	public void render(float deltaTime) {
        gameViewport.apply();
        handleDebugInput(deltaTime);
        this.update(deltaTime); // never forget!!
        Gdx.gl.glClearColor(0x64 / 255.0f, 0x95 / 255.0f, 0xed / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined); // what is the change?
        map_renderer.setView(camera); // this do the same as batch.setProjectionMatrix
        map_renderer.render(backgroundLayers);
        batch.begin();
        for (GameObject obj : objectsToRender) {
                obj.render(batch);
        }
        batch.end();
        map_renderer.render(foregroundLayers);
        if (DEBUG_BOX2D)
            b2debug.render(b2world, camera.combined); // again the same as setProjectionMatrix
        GUIlayer.renderUI(batch, deltaTime, DEBUG_BOX2D);

	}

    public void update(float deltaTime){
        if(previousStateGame != stateGame){ // on change of state
            switch (stateGame){
                case PLAYED:
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_GUI);
                    break;
                case PAUSED:
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_PAUSE);
                    break;
                case ROUND_END:
                    proceedToEndOfRoundAction();
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_END);
                    break;
                case TOURNAMENT_END: // we don't know yet
                    break;
            }
        }
        groupScore = 0;
        for (int i = 0; i < playersList.size; i++) {
            // players update is done after objects update, cause they need to know the state of their character to process the input.
            playersList.getValueAt(i).update(deltaTime);
            groupScore += playersList.getValueAt(i).getScore();
        }
        if (this.stateGame == GAME_STATE.PAUSED){
            return;
        }
        // Check if end of the round:
        timer -= deltaTime;
        if (timer < 0)
            setStateGame(GAME_STATE.ROUND_END);
        // update the scene and objects:
        setDebugText("");
		cameraHelper.update(deltaTime);
		cameraHelper.applyTo(camera);

        if(theCrown.getCarrier() != null){
            groupScore += theCrown.getCrownGoldValue();
        }
        // b2world.step() can go before Player, but maybe better here,
        // cause Player changes the physics of characters and characters listen to their physical states to know their states
        b2world.step(deltaTime, 8, 3);
        for (Updatable updatable:objectsToUpdate) {
             if (updatable.update(deltaTime)){ // when an object is dead is give back False
                 objectsToWipeOut.add(updatable);
             }
        }

        for(Updatable updatable:objectsToWipeOut){
            if (updatable instanceof GameObject) {
                GameObject obj = (GameObject) updatable;
                Gdx.app.debug("WipeOut", "remove item ID "+obj.myRenderID+" Class: "+obj.getClass());
                Gdx.app.debug("WipeOut", "list of fixture A"+ obj.body.getFixtureList());
                cleanAllFixtureInstances(obj);
                objectsToRender.removeValue(obj, true);
                objectsToUpdate.removeValue(obj, true);
                getB2World().destroyBody(obj.getBody());
            }
            else {
                objectsToUpdate.removeValue(updatable, true);
            }
        }
        objectsToWipeOut.clear();

	}

    private void proceedToEndOfRoundAction() {
    // we dont want to get the score table in order highest score to lowest score, we want to keep it in player order.
//        ArrayMap<String, Integer> UnorderedScoreTable = getScoreTableOfCurrentRound(false);
//        for (int i = 0; i < finalScoreTable.size; i++) {
//            finalScoreTable.setValue(i, finalScoreTable.getValueAt(i)+UnorderedScoreTable.getValueAt(i));
//        }
        ArrayMap<String, Integer> OrderedScoreTable = getScoreTableOfCurrentRound(true);
        for (ObjectMap.Entry<String, Integer> entry : OrderedScoreTable) {
            finalScoreTable.put(entry.key, finalScoreTable.get(entry.key) + entry.value);
        }

        ArrayMap<String, Integer> previousVictoryTable = new ArrayMap<String, Integer>(victoryTable);
        if(getNumberOfWinners(OrderedScoreTable) == 1) { // we may have more condition for accepting  winner
            int currentVictoryOfWinner = victoryTable.get(OrderedScoreTable.getKeyAt(0));
            victoryTable.put(OrderedScoreTable.getKeyAt(0), currentVictoryOfWinner+1);
            if ((currentVictoryOfWinner + 1) >= victoryThreshold)
                setStateGame(GAME_STATE.TOURNAMENT_END);
        }
        Gdx.app.debug("Victory Table", victoryTable+"");
        int rank = 0;
        ArrayMap<String, Integer> rankTable = new ArrayMap<String, Integer>(4);
        float score_buffer = 0;
        for (int i = 0; i < OrderedScoreTable.size; i++) {
            if (OrderedScoreTable.getValueAt(i) != score_buffer)
                rank += 1;
            rankTable.put(OrderedScoreTable.getKeyAt(i), rank);
            score_buffer = OrderedScoreTable.getValueAt(i);
        }
        GUIlayer.loadTheScoreTable(previousVictoryTable, OrderedScoreTable, rankTable);

    }

    private void cleanAllFixtureInstances(GameObject obj) {
        for (Fixture fixtA:obj.body.getFixtureList()) {
            Gdx.app.debug("WipeOut", "current fixture A" + fixtA);
            if (fixtA.getUserData() != null) {
                bufferContactData = (ContactData) fixtA.getUserData();
                bufferContactData.deepFlush();
            }
        }
//            if(fixtA.getUserData()!=null) {
//                bufferContactData = (ContactData) fixtA.getUserData();
//                Gdx.app.debug("WipeOut", "list of fixture B "+ bufferContactData.getTouchedFixtures());
//                for (Fixture fixtB : bufferContactData.getTouchedFixtures()) {
//                    Gdx.app.debug("WipeOut", "current fixture B"+ fixtB);
//                    if(fixtB.getUserData()!=null) {
//                        ContactData d = (ContactData) fixtB.getUserData(); // maybe continue to use the same buffer
//                        d.removeTouchedFixtures(fixtA);
//                        //bufferContactData.flush();
//                        Gdx.app.debug("WipeOut", "I removed the fixture"+fixtA+" of the body "+ fixtA.getBody() +" of that object: "+ fixtA.getBody().getUserData());
//                        Gdx.app.debug("WipeOut", "it was touching the fixture"+fixtB+" of the body "+ fixtB.getBody() +" of that object: "+ fixtB.getBody().getUserData());
//                    }
//                }
//            }
//        }
    }

    public String getWinnerName(){
        return getWinner().getName();
    }

    public Player getWinner(){ // we use this function during the real-time game, it is fast, and the danger of having a draw is not important
        Player winner = playersList.getValueAt(0);
        for (int i = 0; i < playersList.size; i++) {
            Player buffer = playersList.getValueAt(i);
            if (buffer.score >= winner.score)
                winner = buffer;
        }
        return winner;
    }

    public String getMaxName(ArrayMap<String, Integer> map) {
        ObjectMap.Entry<String, Integer> maxEntry = null;
        for (ObjectMap.Entry<String, Integer> entry : map) {
            if (maxEntry == null || entry.value.compareTo(maxEntry.value) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry.key;
    }
    public int getMaxIndex(ArrayMap<String, Integer> map) {
        int index_max = 0;
        for (int i = 0; i < map.size; i++) {
            if (map.getValueAt(i) > map.getValueAt(index_max)) {
                index_max = i;
            }
        }
        return index_max;
    }

    public int getNumberOfWinners(ArrayMap<String, Integer>scoreTable){
        int nb_winner = 1;
        for (int i = 1; i < scoreTable.size; i++) {
            if(scoreTable.getValueAt(i).equals(scoreTable.getValueAt(i-1) ))
                nb_winner +=1;
            else
                break;
        }
        return nb_winner;
    }

    public ArrayMap<String, Integer> getScoreTableOfCurrentRound(boolean ordered){
        ArrayMap <String, Integer> tableScore = new ArrayMap<String, Integer>(4);
        if(ordered) {
            ArrayMap<String, Player> bufferPlayers = new ArrayMap<String, Player>(playersList);
            for (int i = 0; i < playersList.size; i++) {
                Player winner = bufferPlayers.getValueAt(0);
                for (int j = 0; j < bufferPlayers.size; j++) {
                    Player buffer = bufferPlayers.getValueAt(j);
                    if (buffer.score > winner.score)
                        winner = buffer;
                }
                bufferPlayers.removeValue(winner, true);
                tableScore.put(winner.getName(), (int) winner.getScore());
            }
        }
        else {
            for (ObjectMap.Entry<String, Player> playerEntry : playersList) {
                tableScore.put(playerEntry.key, (int) playerEntry.value.getScore());
            }
        }
    return tableScore;
    }

    public World getB2World() {
        return b2world;
    }

    public Array<GameObject> getObjectsToRender() {
        return objectsToRender;
    }

    public Array<Updatable> getObjectsToUpdate() {
        return objectsToUpdate;
    }

    @Override
	public void resize(int width, int height) {
		camera.viewportWidth = width*16 /height;
		camera.update();
        gameViewport.update(width, height);
        GUIlayer.resize(width, height);
	}

	@Override
	public void hide() {
		dispose();

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		map.dispose();
		map_renderer.dispose();
        GUIlayer.dispose();
        assetManager.dispose();

	}
	
	private void handleDebugInput(float deltaTime) {
        if (Gdx.app.getType() != ApplicationType.Desktop) return;
        float camSpeed = 5 * deltaTime;
        float camZoom = 1 * deltaTime;
        float accFactor = 5;
        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) camSpeed *= accFactor;
        if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
            if (Gdx.input.isKeyPressed(Keys.LEFT)) cameraHelper.addPosition(-camSpeed, 0);
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) cameraHelper.addPosition(camSpeed, 0);
            if (Gdx.input.isKeyPressed(Keys.UP)) cameraHelper.addPosition(0, camSpeed);
            if (Gdx.input.isKeyPressed(Keys.DOWN)) cameraHelper.addPosition(0, -camSpeed);
            if (Gdx.input.isKeyPressed(Keys.O)) cameraHelper.setPosition(0, 0);
            if (Gdx.input.isKeyPressed(Keys.PAGE_UP)) cameraHelper.addZoom(camZoom);
            if (Gdx.input.isKeyPressed(Keys.PAGE_DOWN)) cameraHelper.addZoom(-camZoom);
            if (Gdx.input.isKeyPressed(Keys.END)) cameraHelper.setZoom(1);
            if (Gdx.input.isKeyPressed(Keys.ENTER)) cameraHelper.setTarget(cameraHelper.hasTarget() ? null : cameraTarget);
            if (Gdx.input.isKeyPressed(Keys.R)) startNewLevel(listLevels.get(currentLevel));
            if (Gdx.input.isKeyPressed(Keys.F)) enableFullScreen();
            if (Gdx.input.isKeyPressed(Keys.P)) stateGame = (stateGame == GAME_STATE.PAUSED)?GAME_STATE.PLAYED : GAME_STATE.PAUSED;
            if (Gdx.input.isKeyPressed(Keys.T)) this.timer -= 10;
            // should be replaced with an actionListenner (push request instead of pulling)
            // cause for now the game can pause and unpaused if we keep pressing.
        }
    }

    private void enableFullScreen() {
        if (fullscreen) {
            Gdx.graphics.setDisplayMode(640, 320, false);
            fullscreen = false;
        } else {
            Graphics.DisplayMode desktopDisplayMode = Gdx.graphics.getDesktopDisplayMode();
            Gdx.graphics.setDisplayMode(desktopDisplayMode.width, desktopDisplayMode.height, true);
            fullscreen = true;
        }
    }

    public void setDebugText(String debugText) {
        if(GUIlayer != null)
            GUIlayer.debugText = debugText;
    }

    public void addDebugText(String s) {
        if(GUIlayer != null)
            GUIlayer.debugText += s;
    }

    public String getDebugText() {
        if(GUIlayer != null)
            return GUIlayer.debugText;
        else
            return "";
    }

    public float getCrownGoldValue(){
        return theCrown.getCrownGoldValue();
    }

    public float getGroupScore() {
        return groupScore;
    }

    public GAME_STATE getStateGame() {
        return stateGame;
    }

    public GAME_STATE getPreviousStateGame() {
        return previousStateGame;
    }

    public boolean setStateGame(GAME_STATE stateGame) {
        if (this.stateGame != stateGame) {
            this.previousStateGame = this.stateGame;
            this.stateGame = stateGame;
            return true;
        }

        return false;
    }
    public int getVictoryThreshold() {
        return victoryThreshold;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setIndexPauser(String name) {
        this.ipauser = playersList.indexOfKey(name);
    }

    public int getIndexPauser() {
        return ipauser;
    }

    public Window openPauseWindow(boolean b, String name) {
        this.ipauser = playersList.indexOfKey(name);
        return GUIlayer.openPauseWindow(b, ipauser);
    }

    public String getCurrentLevel() {
        return listLevels.get(currentLevel);
    }
}
