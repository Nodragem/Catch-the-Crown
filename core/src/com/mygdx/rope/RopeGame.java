package com.mygdx.rope;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.screens.MainMenuWindow;
import com.mygdx.rope.util.Constants;


public class RopeGame extends Game {


	@Override
	public void create() {		
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		//setScreen(new MainMenuWindow());
		Array levels = new Array<String>(1);
		levels.add(Constants.LEVEL_01);
		setScreen(new GameScreen(0, levels));

	}

	@Override
	public void dispose() {
		super.dispose();
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
}
