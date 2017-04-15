package com.mygdx.rope;


import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.*;
import com.mygdx.rope.screens.GameScreenTournament;
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
	public void create() { // TODO: I think here we will provide the lastly played set of levels, for the quick start option

		FileHandle handle = Gdx.files.local("preferences.json");
		if (!handle.exists())
			handle = Gdx.files.internal("preference/preferences.json");
		JsonReader jsonreader = new JsonReader();
		JsonValue root = jsonreader.parse(handle);
		JsonValue res_array = root.getChild("resolution");
		int width = res_array.asInt();
		res_array = res_array.next;
		int height = res_array.asInt();
		this.fullscreen = root.getBoolean("full_screen", true);
		Gdx.graphics.setDisplayMode(width, height, this.fullscreen);

		batch = new SpriteBatch();
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		levels = new Array<String>(1);
		updateLevelSelection(Constants.TOURNAMENT_LEVEL_PATH, null, false);
		randomSelectionLevel = false;
        camera = new OrthographicCamera();
		createProfiles();
		menuScreen = new MenuScreen(this);
		setScreen(menuScreen);


	}
	public void toggleFullScreen() {
		if (fullscreen) {
			Gdx.graphics.setDisplayMode(640, 320, false);
			fullscreen = false;
		} else {
			Graphics.DisplayMode desktopDisplayMode = Gdx.graphics.getDesktopDisplayMode();
			Gdx.graphics.setDisplayMode(desktopDisplayMode.width, desktopDisplayMode.height, true);
			fullscreen = true;
		}
	}

	public void updateLevelSelection(String levelPath, BooleanArray levelSelected, boolean isRandom) {
		randomSelectionLevel = isRandom;
		JsonReader reader = new JsonReader();
		JsonValue levelInfo = reader.parse(Gdx.files.internal(levelPath + "/Levels.json") );
		Gdx.app.debug("Path to level: ", ""+levelPath + "/Levels.json");
		String[] levelNames = levelInfo.get("levels").asStringArray();
		BooleanArray levelBlocked = new BooleanArray(levelInfo.get("unblocked").asBooleanArray());
		if (levelSelected == null) { // if there is no selected level, it takes the lastly saved selection
            levelSelected = new BooleanArray(levelInfo.get("selected").asBooleanArray());
            isRandom = levelInfo.get("random").asBoolean();
        }
		if (levelNames != null  && levelBlocked != null && levelBlocked.size == levelNames.length && levelNames.length == levelSelected.size){
			levels.clear();
			for (int i = 0; i < levelSelected.size; i++) {
				if (levelSelected.get(i) && levelBlocked.get(i))
					levels.add(levelPath+"/"+levelNames[i]+".tmx");
				else
					Gdx.app.error("RopeGame", "You manage to select an unlocked level !?");
				Gdx.app.debug("888", levelPath+"/"+levelNames[i]+".tmx");
			}
		}
		else{
			Gdx.app.error("RopeGame", "Check if the list of selected levels is the same length as the list of levels.");
		}
	}

	public void saveLevelSelection(String levelPath, BooleanArray levelSelected, boolean isRandom) {
        // TODO: need to create an object/class LevelSelection with {isRandom, levelSelected, levelBlocked} as a mirror of the Json File.
    }



	public void startTournament() {
		setScreen(new GameScreenTournament(this));
	}

	public void createProfiles(){
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
