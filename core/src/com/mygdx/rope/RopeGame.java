package com.mygdx.rope;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mygdx.rope.screens.GameScreen;


public class RopeGame extends Game {


	@Override
	public void create() {		
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		setScreen(new GameScreen());

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
