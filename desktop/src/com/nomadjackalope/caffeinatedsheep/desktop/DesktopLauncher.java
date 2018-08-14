package com.nomadjackalope.caffeinatedsheep.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.nomadjackalope.caffeinatedsheep.CaffeinatedSheep;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Caffeinated Sheep";
		config.width = 1080;
		config.height = 608;
		new LwjglApplication(new CaffeinatedSheep(), config);
	}
}
