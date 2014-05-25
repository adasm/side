package com.softsquare.side;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Side (c) 2013";
		cfg.useGL20 = false;
		cfg.width = 1280;
		cfg.height = 720;
		cfg.vSyncEnabled = false;
		cfg.fullscreen = false;
		SideGame.isOnDesktop = true;
		new LwjglApplication(new SideGame(), cfg);
	}
}
