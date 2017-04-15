package com.mygdx.rope.screens;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.rope.RopeGame;
import com.mygdx.rope.objects.*;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.objects.characters.Player;
import com.mygdx.rope.objects.collectable.Coins;
import com.mygdx.rope.objects.collectable.Crown;
import com.mygdx.rope.objects.stationary.BlockFactory;
import com.mygdx.rope.objects.traps.TrapFactory;
import com.mygdx.rope.objects.weapon.AttackManager;
import com.mygdx.rope.util.*;
import com.mygdx.rope.util.InputHandler.InputProfile;
import com.mygdx.rope.util.Constants.GAME_STATE;
import com.mygdx.rope.util.assetmanager.Assets;

import java.lang.String;
import java.util.Iterator;

public class GameScreenTournament implements Screen {
    private final RopeGame game;
    private TiledMap map;
    private ArrayMap <String, Player> playersList;
    public int ipauser = 0; // the player who pressed the pause button and control the pause menu.
	private OrthogonalTiledMapRenderer map_renderer;
	private OrthographicCamera camera;
	private CameraHelper cameraHelper;
    private GUILayer GUIlayer;
    private GameObject cameraTarget;
    private ContactData bufferContactData;
    public Assets assetManager;
    public Viewport gameViewport;
    public Array <Renderable> objectsToRender;
    public Array <Updatable> objectsToUpdate;
    private Array<Updatable> objectsToWipeOut;
    public Array <Controller> XboxControllers;
    public ArrayMap<String, Integer> victoryTable;
    public ArrayMap<String, Integer> totalScoreTable;
	private World b2world;
    public GAME_STATE stateGame;
	private Box2DDebugRenderer b2debug;
	private SpriteBatch batch;
    private int[] foregroundLayers = { 2 };
    private int[] backgroundLayers = {0, 1};
    private Array<Vector2> playerSpawners;
    //private boolean fullscreen;
    public float timer; // ms
    private float groupScore;
    private Crown theCrown;
    private int currentLevel;
    private Array<String> listLevels;
    private GAME_STATE previousStateGame;
    private int victoryThreshold;
    private boolean randomLevel;
    public JsonValue objectDataBase;
    private Character currentWinner;
    protected ObjectMap<String, Animation> animationGoldenPrabbit;
    protected ObjectMap<String, Animation> animationBufferPrabbit;
    protected ObjectMap<String, Animation> animationGoldenWeapon;
    protected ObjectMap<String, Animation> animationBufferWeapon;
    private Character goldenPrabbit;
    private boolean isDebugMode;
    private boolean timeOutMusic;
    private Music musicTimer;

    public GameScreenTournament(RopeGame ropeGame){
        timeOutMusic = false;
        isDebugMode = false;
        this.game = ropeGame;
        batch = ropeGame.getBatch();
        victoryThreshold = 2;
        this.listLevels = ropeGame.getLevels();
        this.randomLevel = ropeGame.isRandomSelectionLevel();
        if (randomLevel)
            this.currentLevel = MathUtils.random(listLevels.size-1);
        else
            this.currentLevel = 0;
        this.camera = ropeGame.camera;
        objectDataBase = new JsonReader().parse(Gdx.files.internal("object_types.json"));

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
            camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            camera.update();
            gameViewport = new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);
            //gameViewport.update(gameViewport.getScreenWidth(), gameViewport.getScreenHeight());
            gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        musicTimer = assetManager.get("stressful_timer_m", 0);
        musicTimer.setLooping(false);
        cameraHelper = new CameraHelper();
        cameraHelper.setTarget(cameraTarget);
        GUIlayer = new GUILayer(this); // is empty (no players)
        setDebugText("DEBUG ON");
        startNewLevel(listLevels.get(currentLevel));
        initScore();

    }

    public void startNewLevel(String levelname){
        groupScore = 0;
        if(musicTimer.isPlaying())
            musicTimer.stop();

        GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_GUI);
        timer = Constants.STARTTIMER;
        objectsToRender = new Array<Renderable>();
        objectsToUpdate = new Array<Updatable>();
        objectsToWipeOut = new Array<Updatable>();
        playerSpawners = new Array<Vector2>();

        b2debug = new Box2DDebugRenderer();

        b2world = new World(new Vector2(0, -9.81f), true);
        b2world.setAutoClearForces(true);
        b2world.setContactListener(new CollisionManager());

        map =  new TmxMapLoader().load(levelname);
        map_renderer = new OrthogonalTiledMapRenderer(map, 1/Constants.TILES_SIZE); // is it here I tell him I want 32 pixels = 1 unit
        placeObjectsFromMap(map);
        createPlayers();
        GUIlayer.updatePlayerList(playersList);
        setStateGame(GAME_STATE.PLAYED);

        // Here we load the texture of the Golden Prabbit
        // to avoid to reprogram all the animation importing, we simply create a prabbit:
        Character GoldPrabbit = new Character(this, new Vector2(15,15), "character", "yellow");
        GoldPrabbit.setWeapon(new AttackManager(this, "lance", "attack_layer", "yellow"));
        animationGoldenPrabbit = GoldPrabbit.getAnimationSet();
        animationGoldenWeapon = GoldPrabbit.getWeapon().getAnimationSet();
        goldenPrabbit = null;
        GoldPrabbit.setTodispose(true);
        GoldPrabbit = null;

    }

    public void initScore(){
        victoryTable = new ArrayMap<String, Integer>(4);
        for (int i = 0; i < playersList.size; i++) {
            victoryTable.put(playersList.getKeyAt(i), 0);
        }

        totalScoreTable = new ArrayMap<String, Integer>(4);
        for (int i = 0; i < playersList.size; i++) {
            totalScoreTable.put(playersList.getKeyAt(i), 0);
        }
    }

    private void createPlayers() {
        ArrayMap<String, InputProfile> inputProfiles = game.getInputProfiles();
        ArrayMap<String, String> colorProfiles = game.getColorProfiles();
        playersList = new ArrayMap<String, Player>(colorProfiles.size);
        for (int i = 0; i < inputProfiles.size; i++) {
            Player player = new Player(
                    inputProfiles.getKeyAt(i),
                    new Character(this, new Vector2(15,15),
                            "character", colorProfiles.getValueAt(i)),
                    inputProfiles.getValueAt(i), this);
            player.getCharacter().setWeapon(
                    new AttackManager(this, "lance", "attack_layer",
                    colorProfiles.getValueAt(i)));
            player.getCharacter().setPosition(getSpawnPosition()); // spawners are place by the placeObjectsFromMap
            playersList.put(player.getName(), player);
        }
    }


    public void toNextLevel(){
        if (stateGame == GAME_STATE.TOURNAMENT_END){
            initScore();
            this.currentLevel = 0;
        }
        if (randomLevel)
            this.currentLevel = MathUtils.random(listLevels.size-1);
        else
            this.currentLevel = (currentLevel + 1) % listLevels.size;
        startNewLevel(listLevels.get(currentLevel));
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
                            gameObjectIntegerEntry.key.setParentBody(iterObject.getBody(), true);
                            Gdx.app.debug("TrapFactory: ", "Add Parent  Step 3");
                            break;
                        } catch (Exception e) {
                            Gdx.app.debug("TrapFactory: ", "Cannot attach an object to its parentBody...");
                        }
                    }
                }
            }
        }
        // now that all the parent relationship are created it is a good time to reset all the swicth/platform
        // for that they spread their default state to their children
        trapFactory.resetHubs();


        // the coins need the factory and its list of Hubs:
        Array<Coins> coingroups = new Array<Coins>(10);
        for (int k = 1; k < 10; k++) { // we limit the number of coin groups to 10
            TiledMapTileLayer coinslayer = (TiledMapTileLayer) map.getLayers().get("collectable_"+k);
            if (coinslayer == null)
                break;
            coingroups.add(new Coins(this, new Vector2(0,0), coinslayer, trapFactory.getListHubs()));
        }
        // Let's spawn the crown oand the Spawners:
        MapObjects objects = map.getLayers().get("Spawners").getObjects();
        initObjectPositions(objects, "Player", playerSpawners);
        Array<Vector2> posCrown = new Array<Vector2>(1);
        initObjectPositions(objects, "Crown", posCrown);
        theCrown = new Crown(this, posCrown.first(), coingroups);

    }

    public Vector2 getSpawnPosition() {
        return playerSpawners.random();
    }

    private void initObjectPositions(MapObjects objects, String name, Array<Vector2> output) {
        Iterator<MapObject> objectIt = objects.iterator();
        while(objectIt.hasNext()) {
            MapObject object = objectIt.next();
            String subtype = object.getProperties().get("Subtype", String.class);
            Gdx.app.debug("initObj", name+" detected? "+ subtype );
            if (object instanceof RectangleMapObject && subtype.equals(name)) {
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
        for (Renderable obj : objectsToRender) {
                obj.render(batch);
        }
        batch.end();
        map_renderer.render(foregroundLayers);
        if (isDebugMode)
            b2debug.render(b2world, camera.combined); // again the same as setProjectionMatrix
        GUIlayer.renderUI(deltaTime, isDebugMode);

	}

    public void update(float deltaTime){


        groupScore = 0;
        for (int i = 0; i < playersList.size; i++) {
            // players update is done after objects update, cause they need to know the state of their character to process the input.
            playersList.getValueAt(i).update(deltaTime);
            groupScore += playersList.getValueAt(i).getScore();
        }
        if (this.stateGame != GAME_STATE.PLAYED){
            return;
        }
        if (timer < 30 & !timeOutMusic){
            musicTimer.play();
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

        // check whether there is a golden Prabbit
        currentWinner = getWinner().getCharacter();
        if (goldenPrabbit == null){
            if(isTheWinnerGolden(currentWinner.getPlayer())) {
                animationBufferPrabbit = currentWinner.getAnimationSet();
                animationBufferWeapon = currentWinner.getWeapon().getAnimationSet();
                currentWinner.setAnimationSet(animationGoldenPrabbit);
                currentWinner.getWeapon().setAnimationSet(animationGoldenWeapon);
                goldenPrabbit = currentWinner;
                goldenPrabbit.goToGoldenState(true);
                makeAnnouncement(Constants.ANNOUNCEMENT.GOLDEN_PRABBIT, goldenPrabbit.getPlayer().getName(), "");
            }
        } else {
            if(!isTheWinnerGolden(currentWinner.getPlayer()))
            {
                goldenPrabbit.setAnimationSet(animationBufferPrabbit);
                goldenPrabbit.getWeapon().setAnimationSet(animationBufferWeapon);
                goldenPrabbit.goToGoldenState(false);
                goldenPrabbit = null;
            }
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
                if (updatable instanceof Renderable)
                    objectsToRender.removeValue((Renderable) updatable, true);
            }
        }
        objectsToWipeOut.clear();

	}

    private void proceedToEndOfRoundAction() {
    // we dont want to get the score table in order highest score to lowest score, we want to keep it in player order.
//        ArrayMap<String, Integer> UnorderedScoreTable = getScoreTableOfCurrentRound(false);
//        for (int i = 0; i < totalScoreTable.size; i++) {
//            totalScoreTable.setValue(i, totalScoreTable.getValueAt(i)+UnorderedScoreTable.getValueAt(i));
//        }
        ArrayMap<String, Integer> OrderedScoreTable = getScoreTableOfCurrentRound(true);
        for (ObjectMap.Entry<String, Integer> entry : OrderedScoreTable) {
            totalScoreTable.put(entry.key, totalScoreTable.get(entry.key) + entry.value);
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
        GUIlayer.openScoreWindow(previousVictoryTable, OrderedScoreTable, rankTable);

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

    public boolean isTheWinnerGolden(Player winner){
        /* if the current winner, without the crown, does not have more money than a looser
           with the crown, the current winner is not gold
           You should provide the winner */
        boolean isWinnerGold = true;
        float netWalletWinner = 0;
        float potentialWalletCandidate = 0;
        ArrayMap<String, Player> loosers = new ArrayMap<String, Player>(playersList);
        loosers.removeValue(winner, true);
        for (int i = 0; i < loosers.size; i++) {
            Player candidate = loosers.getValueAt(i);
            netWalletWinner = winner.hasTheCrown()?
                    winner.getScore()-getCrownGoldValue():winner.getScore();
            potentialWalletCandidate = candidate.hasTheCrown()?
                    candidate.getScore():candidate.getScore()+getCrownGoldValue();
            if (netWalletWinner <= potentialWalletCandidate)
                isWinnerGold = false;
        }

        return isWinnerGold;
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

    public Array<Renderable> getObjectsToRender() {
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
            if (Gdx.input.isKeyJustPressed(Keys.R)) startNewLevel(listLevels.get(currentLevel));
            // if (Gdx.input.isKeyPressed(Keys.F)) game.toggleFullScreen(); // WARNING: A bug in libgdx makes the game loses the controller after passing in fullscreen
            if (Gdx.input.isKeyPressed(Keys.P)) stateGame = (stateGame == GAME_STATE.PAUSED)?GAME_STATE.PLAYED : GAME_STATE.PAUSED;
            if (Gdx.input.isKeyPressed(Keys.T)) this.timer -= 10;
            if (Gdx.input.isKeyJustPressed(Keys.B)) this.isDebugMode = !isDebugMode;
            // should be replaced with an actionListenner (push request instead of pulling)
            // cause for now the game can pause and unpaused if we keep pressing.
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

    public void makeAnnouncement(Constants.ANNOUNCEMENT announcement, String arg1, String arg2){
        GUIlayer.makeAnnouncement(announcement, arg1, arg2);
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
            switch (this.stateGame){
                case PLAYED:
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_GUI);
                    for (ObjectMap.Entry<String, Player> stringPlayerEntry : playersList) {
                        stringPlayerEntry.value.inputCoolDown = 1f;
                        stringPlayerEntry.value.getInputProfile().setContext("Game");
                    }
                    if(previousStateGame==GAME_STATE.PAUSED & !musicTimer.isPlaying() & timeOutMusic)
                        musicTimer.play();
                    break;
                case PAUSED:
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_PAUSE);
                    if(previousStateGame==GAME_STATE.PLAYED & musicTimer.isPlaying())
                        musicTimer.pause();
                    break;
                case ROUND_END:
                    if(musicTimer.isPlaying())
                        musicTimer.stop();
                    proceedToEndOfRoundAction();
                    GUIlayer.setGUIstate(Constants.GUI_STATE.DISPLAY_END);
                    break;
                case TOURNAMENT_END: // we don't know yet
                    break;
            }
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

    public Window openPauseWindow(String name) {
        return GUIlayer.openPauseWindow(playersList.get(name).getInputProfile(), playersList.indexOfKey(name));
    }

    public String getCurrentLevel() {
        return listLevels.get(currentLevel);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void toMainMenu() {
        game.toMainMenu();
    }

    public JsonValue getObjectDataBase() {
        return objectDataBase.get("game_objects");
    }
}
