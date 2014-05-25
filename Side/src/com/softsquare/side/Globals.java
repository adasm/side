package com.softsquare.side;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.softsquare.side.ConsoleVariables.VariableBoolean;
import com.softsquare.side.ConsoleVariables.VariableDouble;
import com.softsquare.side.ConsoleVariables.VariableInt;

public class Globals extends InputManager.InputReceiver {
	//SideGame
	public static SideGame 			game;
	public static boolean 			editorMode = false;
	public static VariableBoolean 	hideStats = new VariableBoolean("main", "hideStats", false);
	//Physics
	public static VariableDouble 	gravity = new VariableDouble("physics", "gravity", 9.82, 1, 100);
	public static VariableDouble 	time = new VariableDouble("physics", "time", 1, 0.00001f, 2000000);
	//Fluid
	public static VariableInt 		fluidStepHz = new VariableInt("fluid", "stepHz", 120, 1, 2000000);
	public static VariableInt 		fluidNumParticles = new VariableInt("fluid", "numParticles", 512, 0, 2000000);
	public static VariableDouble 	fluidMass = new VariableDouble("fluid", "mass", 10.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidGravityScale = new VariableDouble("fluid", "gravityScale", 1, 0, 1);
	public static VariableDouble 	fluidForceToCenter = new VariableDouble("fluid", "forceToCenter", 1, -100, 100);
	public static VariableDouble 	fluidParticleRadius = new VariableDouble("fluid", "particleRadius", 1.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidIdealRad = new VariableDouble("fluid", "idealRad", 5.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidViscosity = new VariableDouble("fluid", "viscosity", 0.004f, 0.00001f, 2000000);
	public static VariableDouble 	fluidTargetPressure = new VariableDouble("fluid", "targetPressure", 2.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidFriction = new VariableDouble("fluid", "friction", 0.1f, 0.00001f, 2000000);
	public static VariableDouble 	fluidRestitution = new VariableDouble("fluid", "restitution", 0.4f, 0.00001f, 2000000);
	public static VariableBoolean 	fluidSleepingAllowed = new VariableBoolean("fluid", "sleepingAllowed", false);
	public static VariableBoolean 	fluidDrawMassCenter = new VariableBoolean("fluid", "drawMassCenter", false);
	public static VariableBoolean 	fluidDrawDragLine = new VariableBoolean("fluid", "drawDragLine", true);
	public static VariableDouble 	fluidAlphaStart = new VariableDouble("fluid", "alphaStart", 0.1f, 0, 1);
	public static VariableDouble 	fluidAlphaEnd = new VariableDouble("fluid", "alphaEnd", 0.8f, 0, 1);
	public static VariableDouble 	fluidAlphaStep = new VariableDouble("fluid", "optimizedRendering", 0.1f, 0, 1);
	public static VariableBoolean 	fluidAlphaBlendingEnabled = new VariableBoolean("fluid", "alphaBlendingEnabled", true);
	public static VariableBoolean 	fluidOptimizedRendering = new VariableBoolean("fluid", "optimizedRendering", false);
	public static VariableDouble 	fluidOptimizedParticleRadius = new VariableDouble("fluid", "optimizedParticleRadius", 1, 0.00001f, 2000000);
	public static VariableInt 		fluidOptimizedParticleStep = new VariableInt("fluid", "optimizedParticleStep", 8, 3, 64);
	public static VariableDouble 	fluidFallWidth = new VariableDouble("fluid", "fallWidth", 300.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidFallHeight = new VariableDouble("fluid", "fallHeight", 100.0f, 0.00001f, 2000000);
	public static VariableDouble 	fluidDragCoef = new VariableDouble("fluid", "dragCoef", 100.0f, 0.00001f, 2000000);
	public static VariableBoolean 	fluidExpRadius = new VariableBoolean("fluid", "expRadius", false);
	public static VariableDouble 	fluidMinY = new VariableDouble("fluid", "minY", 0, -2000000, 2000000);
	public static VariableBoolean 	fluidComputeFluidMinMax = new VariableBoolean("fluid", "computeFluidMinMax", true);
	public static VariableDouble 	fluidDragRadius = new VariableDouble("fluid", "dragRadius", 96, 1, 2000000);
	public static VariableBoolean 	fluidRespawn = new VariableBoolean("fluid", "respawn", true);
	public static VariableBoolean 	fluidRespawnAtCenter = new VariableBoolean("fluid", "respawnAtCenter", true);
	public static VariableBoolean 	fluidRandomOnRespawn = new VariableBoolean("fluid", "randomOnRespawn", true);
	public static VariableInt 		fluidHashWidth = new VariableInt("fluid", "hashWidth", 32, 1, 2000000);
	public static VariableInt 		fluidHashHeight = new VariableInt("fluid", "hashHeight", 32, 1, 2000000);
	//Camera
	public static VariableBoolean 	zoom_dynamic = new VariableBoolean("camera", "zoomDynamic", true);
	public static VariableDouble 	zoom_start = new VariableDouble("camera", "zoomStart", 0.10, 0.05, 10.00);
	public static VariableDouble 	zoom_static = new VariableDouble("camera", "zoom_static", 2, 0.05, 10.00);
	public static VariableDouble 	zoom_min = new VariableDouble("camera", "zoomMin", 4, 0.005, 10.00);
	public static VariableDouble 	zoom_max = new VariableDouble("camera", "zoomMax", 10, 0.005, 1000.00);
	public static VariableDouble 	zoom_coef = new VariableDouble("camera", "zoomCoef", 0.5f, 0.00, 10.00);
	public static VariableDouble 	speed = new VariableDouble("camera", "speed", 5.0, 0.00, 100.00);
	public static VariableBoolean 	cameraSmooth = new VariableBoolean("camera", "smooth", true);
	//Terrain
	public static VariableInt 		terrainSize = new VariableInt("terrain", "size", 4096, 0, 2000000);
	public static VariableDouble 	terrainStep = new VariableDouble("terrain", "step", 3.0, 0.00, 1000.00);
	public static VariableInt 		terrainSmooth = new VariableInt("terrain", "smooth", 3, 2, 2000000);
	public static VariableInt 		terrainSmoothKernel = new VariableInt("terrain", "smoothKernel", 3, 0, 2000000);
	public static VariableDouble 	terrainSharpness = new VariableDouble("terrain", "sharpness", 0.9, 0.0, 1.0);
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.F1) {
			game.switchScreens();
		}
		if (keycode == Keys.ESCAPE) {
			Gdx.app.exit();
		}
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
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean wantInput() {
		return true;
	}
	
}
