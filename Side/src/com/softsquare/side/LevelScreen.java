package com.softsquare.side;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class LevelScreen implements Screen {
	private Camera camera;
	public Level level;
	private int levelId;
	private Box2DDebugRenderer renderer;
	private boolean render = true, started = false;

	public LevelScreen(int id) {
		this.levelId = id;
	}

	@Override
	public void show() {
		if (!started) {
			camera = new Camera();
			level = new Level(levelId, camera);
			level.generate();
			renderer = new Box2DDebugRenderer();
			renderer.setDrawJoints(false);
			started = true;
			Logger.logSuccess("Loaded level" + level);
		}
	}

	@Override
	public void render(float deltaTime) {
		Globals.game.begin();
		Gdx.gl.glClearColor(0.5f, 0.7f, 1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		level.update(deltaTime);
		// game.addStats("c " + level.fluid.getCenterPosition().toString());
		// renderer.render(level.world, camera.get().combined);
		System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());
		level.render();
		Globals.game.end();

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
		if(SideGame.isOnDesktop)
			level.terrain.saveCurrentTerrain();
	}

	@Override
	public void resume() {
		if(SideGame.isOnDesktop)
			level.terrain.loadCurrentTerrain();
	}

	@Override
	public void dispose() {
		level.world.dispose();
		renderer.dispose();
	}
}
