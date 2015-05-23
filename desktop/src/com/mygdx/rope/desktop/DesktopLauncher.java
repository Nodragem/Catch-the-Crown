package com.mygdx.rope.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.rope.RopeGame;

public class DesktopLauncher {
    private final boolean RELOAD_TEXTURE = false;
	public static void main (String[] arg) {
//        if (RELOAD_TEXTURE) {
//            Settings settings = new Settings();
//            settings.maxWidth = 512;
//            settings.maxHeight = 512;
//
//            TexturePacker2.process("..\\raw-asset\\topack", "..\\core\\assets", "texture_objects.pack");
//        }

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Catch the Crown!!";
        cfg.height = 640;
        cfg.width = 1280;
		new LwjglApplication(new RopeGame(), cfg);
	}
}
