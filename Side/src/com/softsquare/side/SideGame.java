package com.softsquare.side;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class SideGame extends Game {
	public static boolean isOnDesktop = false;
	public InputManager input = new InputManager();
	public Skin skin;
	public Stage stage;
	public Label stats;
	public boolean statsEnabled = true;
	public Console console;
	public LevelScreen level;
	public LevelEditor editor;
	public Globals globalInput = new Globals();

	public SideGame() {
		Globals.game = this;
	}

	@Override
	public void create() {
		if(isOnDesktop) {
			ConsoleVariables.load("cfg.txt");
			skin = new Skin();
			skin.addRegions(new TextureAtlas(new FileHandle(
					"../data/ui/skin/uiskin.atlas")));
			skin.load(new FileHandle("../data/ui/skin/uiskin.json"));
		}
		Gdx.input.setInputProcessor(input);
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		if(isOnDesktop) {
			stage = new Stage(width, height, false);
			stats = new Label("", skin);
			stats.setPosition(0, height - 10);
			stats.setAlignment(Align.left | Align.top);
			stage.addActor(stats);
			InputManager.setStage(stage);
			console = new Console();
		}		
		level = new LevelScreen(1);
		setScreen(level);
		
		InputManager.add(globalInput);
		editor = new LevelEditor(level.level);
	}

	public void begin() {
		if(isOnDesktop) {
			stats.setText("scr " + Gdx.graphics.getWidth() + "x"
					+ Gdx.graphics.getHeight() + " "
					+ Gdx.graphics.getBufferFormat().toString() + "\n" + "fps "
					+ Gdx.graphics.getFramesPerSecond() + "\n" + "ft "
					+ Gdx.graphics.getDeltaTime());
			if (Globals.editorMode)
				addStats("EDITOR_MODE_ACCESS" + "\n" + "right click to zoom");

			if (console.isEnabled())
				Gdx.input.setInputProcessor(console);
			else
				Gdx.input.setInputProcessor(input);
		}
	}

	public void addStats(String s) {
		if(isOnDesktop)
			stats.setText(stats.getText() + "\n" + s);
	}

	public void end() {
		boolean statsEnabled = isOnDesktop ? !console.isEnabled() && !Globals.hideStats.get() : false;
		float deltaTime = Gdx.graphics.getDeltaTime();
		if(isOnDesktop) {
			if (statsEnabled) {
				stats.setVisible(true);
				if (stats.getColor().a < 0.9)
					stats.getColor().a += deltaTime;
			} else {
				if (stats.getColor().a > 0.1)
					stats.getColor().a -= deltaTime;
				if (stats.getColor().a < 0.1) {
					stats.getColor().a = 0.1f;
				}
			}
			console.update(deltaTime);
			stage.act(deltaTime);
			stage.draw();
		}
	}

	public void markCircle(Camera camera, Vector3 pos) {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		shapeRenderer.setColor(1, 0.6f, 0.5f, 0.5f);
		shapeRenderer.filledCircle(pos.x, pos.y, 10);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}

	@Override
	public void dispose() {
		if(isOnDesktop)
			ConsoleVariables.save("cfg.txt");
	}

	public void switchScreens() {
		if (Globals.editorMode) {
			level.resume();
			editor.pause();
			setScreen(level);
			Globals.editorMode = false;
		} else {
			level.pause();
			editor.resume();
			setScreen(editor);
			Globals.editorMode = true;
		}
	}

}
