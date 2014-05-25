package com.softsquare.side;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;

public class Level extends InputManager.InputReceiver  {
	public Camera camera;
	public World world;
	public Terrain terrain;
	public Fluid fluid;
	public Vector2 cameraOrigin = new Vector2();
	public boolean cameraChase = true;
	public boolean resetFluid = true;
	public int levelId;
	RayHandler rayHandler;
	PointLight light;

	public Level(int id, Camera camera) {
		this.levelId = id;
		this.camera = camera;
		InputManager.add(this);
	}

	public void generate() {
		world = new World(new Vector2(0, -5 * Globals.gravity.get().floatValue()), false);
		rayHandler = new RayHandler(world, 50, 50, 50);
		rayHandler.setBlur(true);
		rayHandler.setBlurNum(16);
		rayHandler.setAmbientLight(1, 1, 1, 0.5f);
		RayHandler.setColorPrecisionHighp();
		RayHandler.setGammaCorrection(true);
		
		terrain = new Terrain(world, this);
		terrain.generate();
		//terrain.saveCurrentTerrain();
		//terrain.loadCurrentTerrain();
		//terrain.reload(terrain.terrainMap);
		fluid = new Fluid(world, rayHandler, camera);
		}

	public void update(float deltaTime) {
		if(resetFluid) {
			fluid.init(0, 150);
			resetFluid = false;
		}
		float dt = 1 / 60.0f * Globals.time.get().floatValue();
		world.step(dt, 4, 2);
		fluid.update(dt);
		camera.moveTo(cameraChase ? fluid.getCameraCenterPosition() : cameraOrigin);
		camera.update(deltaTime, drag);
		if (drag)
			drag();
	}

	public void saveCurrentState() {
		FileHandle file = Gdx.files.getFileHandle("../data/levels/current_state", FileType.Local);
		try {
			PrintWriter saver = new PrintWriter(file.file());
			if (fluid != null)
				saver.println(fluid.getCenterPosition().x + " " + fluid.getCenterPosition().y);
			saver.close();
			Logger.logSuccess("Saved current state");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadCurrentState() {
		UniversalParser parser = new UniversalParser("../data/levels/current_state");
		if (!parser.isEOFReached()) {
			String x = parser.next();
			String y = parser.next();
			Vector2 fluidCenter = new Vector2(Float.parseFloat(x), Float.parseFloat(y));
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.C) {
			cameraChase = !cameraChase;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) { return false; }
	@Override
	public boolean keyTyped(char character)  { return false; }

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
		if (!cameraChase && button == 1) {
			tX = screenX;
			tY = screenY;
			worldPos = new Vector3(tX, tY, 0);
			camera.get().unproject(worldPos);
			drag = true;
		}
		else if (button == 2) {
			resetFluid = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		drag = false;
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
	
	public void renderLights() {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		rayHandler.update();
		rayHandler.setAmbientLight(1, 1, 1, 0.5f);
		rayHandler.setCombinedMatrix(camera.get().combined);
		rayHandler.setShadows(false);
		rayHandler.render();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	public void render() {
		//renderLights();
		if(drag)
			Globals.game.markCircle(camera, worldPos);
		terrain.render(camera);
		fluid.render();
		
		
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override
	public boolean scrolled(int amount) {
		Globals.zoom_max.set(Globals.zoom_max.get() - 0.1f * amount);
		return false;
	}

	@Override
	public boolean wantInput() {
		return !Globals.editorMode;
	}
}
