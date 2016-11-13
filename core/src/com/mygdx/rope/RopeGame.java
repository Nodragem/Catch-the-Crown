package com.mygdx.rope;


import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.rope.objects.characters.*;
import com.mygdx.rope.screens.DefaultWindow;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.screens.MainMenuWindow;
import com.mygdx.rope.screens.MenuScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.InputHandler.InputProfile;
import com.mygdx.rope.util.InputHandler.InputProfileController;
import com.mygdx.rope.util.InputHandler.InputProfileKeyboard;


public class RopeGame extends Game {

	private MenuScreen menuScreen;
	private boolean fullscreen;
	private SpriteBatch batch;
	public Array<String> levels;
	public OrthographicCamera camera;
	public boolean randomSelectionLevel;
	private ArrayMap<String, InputProfile> inputProfiles;
	private Array<Controller> XboxControllers;
	private ArrayMap<String, String> colorProfiles;


	@Override
	public void create() {
//		enableFullScreen();
		batch = new SpriteBatch();
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		levels = new Array<String>(1);
		//levels.add(Constants.LEVEL_01);
        Array<Boolean> selectionLevels = new Array<Boolean>(2);
        selectionLevels.add(true); //selectionLevels.add(true);selectionLevels.add(true);
		retrieveLevelFrom(Constants.TOURNAMENT_LEVEL_PATH, selectionLevels);
		randomSelectionLevel = false;
        camera = new OrthographicCamera();
		createProfiles();
		menuScreen = new MenuScreen(this);
		setScreen(menuScreen);


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

	private void retrieveLevelFrom(String levelPath, Array<Boolean> selected) {
		JsonReader reader = new JsonReader();
//		JsonValue root = reader.parse(Gdx.files.internal(levelPath + "/progressionLevels.json") );
		JsonValue root = reader.parse(Gdx.files.internal(levelPath + "/testLevels.json") );
        Gdx.app.debug("Path to level: ", ""+levelPath + "/testLevels.json");
        String[] levelNames = root.get("levels").asStringArray();
        boolean[] levelBlocked = root.get("unblocked").asBooleanArray();
        if (levelNames != null  && levelBlocked != null && levelBlocked.length == levelNames.length && levelNames.length == selected.size){
            for (int i = 0; i < selected.size; i++) {
                if (selected.get(i) && levelBlocked[i])
                    levels.add(levelPath+"/"+levelNames[i]+".tmx");
				Gdx.app.debug("888", levelPath+"/"+levelNames[i]+".tmx");
            }
        }
	}

	public void startTournament() {
		setScreen(new GameScreenTournament(this));
	}

	private void createProfiles(){
		JsonReader jsonreader = new JsonReader();
		FileHandle handle = Gdx.files.local("preferences.json");
		if (!handle.exists())
			handle = Gdx.files.internal("preference/preferences.json");
		JsonValue root = jsonreader.parse(handle);
		int nb_players = root.getInt("nb_players", 4);
		XboxControllers = Controllers.getControllers();
		int nb_controllers = XboxControllers.size;
		int nb_keyboard = 1;
		inputProfiles = new ArrayMap<String, InputProfile>(Math.min(nb_controllers+nb_keyboard, 4));
		colorProfiles = new ArrayMap<String, String>(Math.min(nb_controllers+nb_keyboard, 4));

		int nb_created_player = 0;
		for (JsonValue item:root.get("players")){
			if (nb_created_player >= nb_players)
				break;
			Gdx.app.debug("GameScreenTournament", "player: " + item.get("name"));
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
			String name = item.getString("name", "Player " + (nb_created_player + 1));
			inputProfiles.put(name, inputProfil);
			colorProfiles.put(name, item.getString("color", "purple"));

		}


	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public Array<String> getLevels() {
		return levels;
	}

	public ArrayMap<String, InputProfile> getInputProfiles() {
		return inputProfiles;
	}
	public ArrayMap<String,String> getColorProfiles() {
		return colorProfiles;
	}

	public boolean isRandomSelectionLevel() {
		return randomSelectionLevel;
	}

	@Override
	public void dispose() {
		batch.dispose();
		super.dispose();
	}

	public void requestExit(){
		Gdx.app.exit();
	}

	@Override
	public void render() {
		super.render();

	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.pause();
	}


	public void toMainMenu() {

		setScreen(menuScreen);

	}
}
