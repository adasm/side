package com.softsquare.side;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class LevelEditor extends InputManager.InputReceiver implements Screen {
	private ArrayList<Shaper> terrainMap;

	private Level level;
	private Camera camera;
	private Vector2 cameraOrigin = new Vector2(0, 0);

	public LevelEditor(Level level) {
		InputManager.add(this);
		this.level = level;
		terrainMap = level.terrain.terrainMap;
		camera = new Camera();
		camera.moveTo(cameraOrigin);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float deltaTime) {
		Globals.game.begin();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		if (drag) {
			drag();
			camera.moveTo(cameraOrigin);
		}
		camera.update(deltaTime, drag);
		renderTerrain();
		renderStartingPoint();
		if (drag)
			Globals.game.markCircle(camera, worldPos);
		Globals.game.end();
	}

	public void renderTerrain() {
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		shapeRenderer.setColor(1, 0, 1, 1);
		for (int i = 0; i < terrainMap.size(); i++) {
			shapeRenderer.line(terrainMap.get(i).getVectorList()[0].x,
					terrainMap.get(i).getVectorList()[0].y, terrainMap.get(i)
							.getVectorList()[1].x, terrainMap.get(i)
							.getVectorList()[1].y);
			shapeRenderer.line(terrainMap.get(i).getVectorList()[1].x,
					terrainMap.get(i).getVectorList()[1].y, terrainMap.get(i)
							.getVectorList()[2].x, terrainMap.get(i)
							.getVectorList()[2].y);
			shapeRenderer.line(terrainMap.get(i).getVectorList()[2].x,
					terrainMap.get(i).getVectorList()[2].y, terrainMap.get(i)
							.getVectorList()[3].x, terrainMap.get(i)
							.getVectorList()[3].y);
			shapeRenderer.line(terrainMap.get(i).getVectorList()[3].x,
					terrainMap.get(i).getVectorList()[3].y, terrainMap.get(i)
							.getVectorList()[0].x, terrainMap.get(i)
							.getVectorList()[0].y);

		}
		shapeRenderer.end();
	}
	
	public void renderStartingPoint(){
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		shapeRenderer.setColor(0,1,0,1);
		shapeRenderer.filledCircle(level.fluid.fallX, level.fluid.fallY, 20);
		shapeRenderer.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {

	}

	Vector3 target = new Vector3(0, 0, 0);
	Vector3 worldPos = new Vector3(0, 0, 0);
	int tX, tY;
	boolean drag = false;

	public void drag() {
		target = new Vector3(tX, tY, 0);
		camera.get().unproject(target);
		cameraOrigin.x += -target.x + worldPos.x;
		cameraOrigin.y += -target.y + worldPos.y;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == 1) {
			tX = screenX;
			tY = screenY;
			worldPos = new Vector3(tX, tY, 0);
			camera.get().unproject(worldPos);
			drag = true;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (drag) {
			tX = screenX;
			tY = screenY;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		drag = false;
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		Globals.zoom_max.set(Globals.zoom_max.get() - 0.1f * amount);
		return false;
	}

	@Override
	public boolean wantInput() {
		return Globals.editorMode;
	}

}
