package dagger.nrm;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public class Main implements ApplicationListener {
	Scene scene;
	Renderer renderer;
	public void create() {		
		scene = new Scene();
		renderer = new Renderer();		
		Gdx.input.setInputProcessor(renderer);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void render() {		
		Gdx.app.log("Log", Gdx.graphics.getFramesPerSecond() + " FPS WITH " + renderer.lights.size + " LIGHTS USING RENDERMODE " + renderer.renderMode);
		renderer.render(scene);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
