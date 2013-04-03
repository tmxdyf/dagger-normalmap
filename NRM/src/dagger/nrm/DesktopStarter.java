package dagger.nrm;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopStarter {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "NRM";
		cfg.useGL20 = true; 
		cfg.width = 1280;
		cfg.height = 720;
		
		new LwjglApplication(new Main(), cfg);
	}
}
