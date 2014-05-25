package com.softsquare.side;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Terrain {
	private World world;
	// public ArrayList<Vector2> terrainMap = new ArrayList<Vector2>();
	public Body[] terrainBodies = new Body[Globals.terrainSize.get().intValue()];
	public ArrayList<Shaper> terrainMap = new ArrayList<Shaper>();

	private Level level;

	public Terrain(World world, Level level) {
		this.world = world;
		this.level = level;
	}

	public void generate() {
		final float scale = SideGame.isOnDesktop ? 0.45f : 0.5f;
		int size = SideGame.isOnDesktop ? Globals.terrainSize.get().intValue() : 64;
		float step = SideGame.isOnDesktop ? Globals.terrainStep.get().intValue() : 16;
		float tabX[] = new float[size];
		float tabY[] = new float[size];
		float tabYf[] = new float[size];
		float tabY2[] = new float[size];
		float tabY2f[] = new float[size];
		float tabY3[] = new float[size];
		float tabY3f[] = new float[size];
		for (int i = 0; i < size; i++) {
			tabX[i] = ((i - size / 2) * step);
			tabY[i] = (float) ((Math.sin(i * 256.0 / size) + 1.0f) * 50);
			tabY[i] = (float) ((tabY[i] * Math.max(0.05, Math.random()) * 1.2f + 300 * Math
					.abs(Math.sin(i / (size * 0.06)))) + (Math.random() > (1.0 - Globals.terrainSharpness
					.get().floatValue()) ? Math.random() * 300.0f : 0));

			tabY2[i] = (float) ((Math.sin(i * 256.0 / size) + 3.0f) * 2000);
			tabY2[i] = (float) ((tabY2[i] * Math.max(0.05, Math.random())
					* 1.2f + 100 * Math.abs(Math.sin(i / (size * 0.02)))) + (Math
					.random() > (1.0 - Globals.terrainSharpness.get()
					.floatValue()) ? Math.random() * 100.0f : 0));
			if (tabY2[i] < tabY[i] + 64)
				tabY2[i] = 999;

			tabY3[i] = (float) ((Math.sin(i * 256.0 / size) + 6.0f) * 3000);
			tabY3[i] = (float) ((tabY3[i] * Math.max(0.05, Math.random())
					* 1.2f + 1000 * Math.abs(Math.sin(i / (size * 0.06)))));
			if (tabY3[i] < tabY2[i])
				tabY3[i] = -999;

			if (tabY3[i] - tabY2[i] < 50) {
				tabY2[i] = tabY3[i] = -999;
			}
		}

		int width = Globals.terrainSmoothKernel.get().intValue();
		int kernel = 2 * width + 1;
		float coef[] = new float[kernel];
		int index = 0;
		for (int i = -width; i <= width; i++) {
			coef[index++] = (float) (Math.exp(-i * i / (2.0 * width * width)) / (Math
					.sqrt(2 * Math.PI) * width));
		}

		// Smooth Y
		for (int o = 0; o < Globals.terrainSmooth.get().intValue(); o++) {
			for (int i = 0; i < size; i++) {
				float s = 0;
				int c = 0;
				for (int j = (i - width < 0) ? 0 : i - width; j <= i + width
						&& j < size; j++)
					s += tabY[j] * coef[c++];
				tabYf[i] = s;
			}
			for (int i = 0; i < size; i++)
				tabY[i] = tabYf[i];
		}

		// Smooth Y2
		for (int o = 0; o < Globals.terrainSmooth.get().intValue() * 3; o++) {
			for (int i = 0; i < size; i++) {
				float s = 0;
				int c = 0;
				for (int j = (i - width < 0) ? 0 : i - width; j <= i + width
						&& j < size; j++)
					s += tabY2[j] * coef[c++];
				tabY2f[i] = s;
			}
			for (int i = 0; i < size; i++)
				tabY2[i] = tabY2f[i];
		}

		// Smooth Y3
		for (int o = 0; o < Globals.terrainSmooth.get().intValue() * 3; o++) {
			for (int i = 0; i < size; i++) {
				float s = 0;
				int c = 0;
				for (int j = (i - width < 0) ? 0 : i - width; j <= i + width
						&& j < size; j++)
					s += tabY3[j] * coef[c++];
				tabY3f[i] = s;
			}
			for (int i = 0; i < size; i++)
				tabY3[i] = tabY3f[i];
		}

		for (int _x = 0; _x < size - 1; _x++) {
			float y = tabY[_x];
			float y2 = tabY[_x + 1];
			float x = tabX[_x] - 1;
			float x2 = tabX[_x + 1];
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.position.set(0, 0);
			groundBodyDef.angle = 0;
			Body groundBody = world.createBody(groundBodyDef);
			PolygonShape groundShape = new PolygonShape();
			Vector2[] vertices = new Vector2[4];
			vertices[0] = new Vector2(x2 * scale, y2 * scale);
			vertices[1] = new Vector2(x * scale, y * scale);
			vertices[2] = new Vector2(x * scale, -1.0f * scale);
			vertices[3] = new Vector2(x2 * scale, -1.0f * scale);
			groundShape.set(vertices);
			groundBody.createFixture(groundShape, 0f);
			
			groundShape.getVertex(0, vertices[0]);
			groundShape.getVertex(1, vertices[1]);
			groundShape.getVertex(2, vertices[2]);
			groundShape.getVertex(3, vertices[3]);
			
			Shaper shape = new Shaper(vertices.length);
			shape.set(vertices);
			terrainMap.add(shape);
		}
		if (false) {
			for (int _x = 0; _x < size - 1; _x++) {
				if (tabY2[_x] > tabY[_x] + 64 && tabY3[_x] > tabY2[_x]
						&& tabY2[_x + 1] > tabY[_x + 1] + 64
						&& tabY3[_x + 1] > tabY2[_x + 1]) {
					BodyDef groundBodyDef = new BodyDef();
					groundBodyDef.position.set(0, 0);
					Body groundBody = world.createBody(groundBodyDef);
					PolygonShape groundShape = new PolygonShape();
					Vector2[] vertices = new Vector2[4];
					vertices[0] = new Vector2(tabX[_x], tabY2[_x]);
					vertices[1] = new Vector2(tabX[_x], tabY3[_x + 1]);
					vertices[2] = new Vector2(tabX[_x + 1], tabY3[_x + 1]);
					vertices[3] = new Vector2(tabX[_x + 1], tabY2[_x]);
					Shaper shape = new Shaper(vertices.length);
					shape.set(vertices);
					groundShape.set(vertices);
					groundBody.createFixture(groundShape, 0f);
				}
			}
		}
		Logger.logSuccess("Generated terrain " + size + "x" + step);
	}

	public void saveCurrentTerrain() {
		FileHandle file = Gdx.files.getFileHandle("../data/levels/level"
				+ level.levelId, FileType.Local);
		try {
			PrintWriter saver = new PrintWriter(file.file());
			saver.println(Globals.terrainSize.get().intValue());
			for (int i = 0; i < terrainMap.size(); i++) {
				saver.print(terrainMap.get(i).getCount() + " ");
				for (int j = 0; j < terrainMap.get(i).getCount(); j++) {
					saver.print(terrainMap.get(i).getVectorList()[j].x + " "
							+ terrainMap.get(i).getVectorList()[j].y + " ");
				}
				saver.println();
			}
			saver.close();
			Logger.logSuccess("Saved terrain");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public void render(Camera camera) {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glDisable(GL10.GL_BLEND);
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.FilledTriangle);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1);
		for (int i = 0; i < terrainMap.size(); i++) {
			shapeRenderer.filledTriangle(terrainMap.get(i).getVectorList()[0].x, terrainMap.get(i).getVectorList()[0].y, 
										terrainMap.get(i).getVectorList()[1].x, terrainMap.get(i).getVectorList()[1].y, 
										terrainMap.get(i).getVectorList()[2].x, terrainMap.get(i).getVectorList()[2].y);
			shapeRenderer.filledTriangle(terrainMap.get(i).getVectorList()[0].x, terrainMap.get(i).getVectorList()[0].y, 
					terrainMap.get(i).getVectorList()[2].x, terrainMap.get(i).getVectorList()[2].y, 
					terrainMap.get(i).getVectorList()[3].x, terrainMap.get(i).getVectorList()[3].y);
		}
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.FilledRectangle);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
		shapeRenderer.filledRect(-5000, 0, 100000, -1000);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
	}

	public void loadCurrentTerrain() {
		terrainMap.clear();
		UniversalParser parser = new UniversalParser("../data/levels/level"
				+ level.levelId);
		int size;
		if (!parser.isEOFReached())
			size = Integer.parseInt(parser.next());
		while (!parser.isEOFReached()) {
			String tempCount = parser.next();
			int count = 0;
			if (!tempCount.isEmpty()) {
				count = Integer.parseInt(tempCount);
			}
			Shaper shape = new Shaper(count);
			for (int i = 0; i < count; i++) {
				String x = parser.next();
				String y = parser.next();
				if (!x.isEmpty() || !y.isEmpty()) {
					shape.add(new Vector2(Float.parseFloat(x), Float
							.parseFloat(y)));
				}
			}
			if(count != 0) terrainMap.add(shape);
		}
		System.out.println(terrainMap.size());
		Logger.logSuccess("Terrain loaded");
	}

	public void reload(ArrayList<Shaper> terrainMap) {
		int _x = 0;
		for (int i = 0; i < terrainMap.size(); i++) {
			BodyDef groundBodyDef = new BodyDef();
			groundBodyDef.position.set(0, 0);
			Body groundBody = world.createBody(groundBodyDef);
			terrainBodies[_x] = groundBody;
			PolygonShape groundShape = new PolygonShape();
			groundShape.set(terrainMap.get(i).getVectorList());
			groundBody.createFixture(groundShape, 0f);
			_x++;

		}
	}
}
