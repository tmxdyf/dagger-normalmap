package dagger.nrm;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

public class Renderer implements InputProcessor{
	public ShaderProgram tfsShader,ballShader,normalShader;
	public Matrix4 mvpMatrix;
	public Matrix4 nMatrix;
	SmallFrustums frustums;
	Texture lightTexture;
	Texture lightInfoTexture;
	Color c = new Color();
	Pixmap lightPixmap = new Pixmap(64,128,Pixmap.Format.RGBA8888);
	Pixmap lightInfoPixmap = new Pixmap(1,128,Pixmap.Format.RGBA8888);
	Array<IntArray> lights2 = new Array<IntArray>();
	PerspectiveCamera cam;
	Array<Light> lights;
	public int renderMode = 0;
	Vector3 tempVec = new Vector3();
	Matrix4 modelMatrix = new Matrix4();
	int hasTexture = 1;
	OrthographicCamera uiCAM;
	SpriteBatch sb;
	Sprite on;
	Sprite off;
	Sprite ton;
	Sprite toff;
	BitmapFont font;
	public static final String[] shaderType = {"Normal Mapping Off","Normal Mapping On"};
	public Renderer() {
		lights = new Array<Light>();
		Pixmap.setBlending(Pixmap.Blending.None);
		for (int i = 0; i < 100; i++) {
			lights2.add(new IntArray());
		}
		lightTexture = new Texture(64,128,Pixmap.Format.RGBA8888);
		lightTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		lightInfoTexture = new Texture(1,128,Pixmap.Format.RGBA8888);
		lightInfoTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		frustums = new SmallFrustums(10,10);
		tfsShader = new ShaderProgram(Gdx.files.internal("data/TiledForwardShader.vert").readString(), 
				Gdx.files.internal("data/TiledForwardShader.frag").readString());
		if (!tfsShader.isCompiled())
			throw new GdxRuntimeException("Couldn't compile shader: "
					+ tfsShader.getLog());

		normalShader = new ShaderProgram(Gdx.files.internal("data/normalMapShader.vert").readString(), 
				Gdx.files.internal("data/normalMapShader.frag").readString());
		if (!normalShader.isCompiled())
			throw new GdxRuntimeException("Couldn't compile shader: "
					+ normalShader.getLog());

		ballShader  = new ShaderProgram(Gdx.files.internal("data/ballShader.vert").readString(), 
				Gdx.files.internal("data/ballShader.frag").readString());
		if (!ballShader.isCompiled())
			throw new GdxRuntimeException("Couldn't compile shader: "
					+ ballShader.getLog());

		mvpMatrix = new Matrix4();
		nMatrix = new Matrix4();

		sb  = new SpriteBatch();
		on = new Sprite(new Texture(Gdx.files.internal("data/on.png")));
		off = new Sprite(new Texture(Gdx.files.internal("data/off.png")));
		ton = new Sprite(new Texture(Gdx.files.internal("data/ton.png")));
		toff = new Sprite(new Texture(Gdx.files.internal("data/toff.png")));
		uiCAM = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = 0.5f;
		cam.far = 30;
		cam.position.set(0,15,0);
		cam.direction.set(0,-1,0);
		cam.up.set(0,0,-1);
		cam.update();
		lights.add(new Light(0,1.5f,-7,1,1,1));
		lights.get(0).radius = 7;
		lights.get(0).stationary = true;
		lights.add(new Light(0,1.5f,MathUtils.random(-3f,3f),0,0,1));
		lights.add(new Light(0,1.5f,MathUtils.random(-3f,3f),1,0,0));
		lights.add(new Light(0,1.5f,MathUtils.random(-3f,3f),0,1,0));
		font = new BitmapFont();
		font.setScale(2f);
	}

	public void render(Scene s) {
		// Update the position of each light source
		for (Light l : lights) {
			l.update();
		}
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		updateCam();
		// Builds the subfrustums.
		frustums.setFrustums(cam);
		// Initiates the light textures and calculates tile intersection.
		makeLightTexture();
		if (renderMode == 0) {
			tfsShader.begin();
			lightTexture.bind(0);
			if(hasTexture == 1){
				s.mapTexture.bind(1);
			} else {
				s.noTexture.bind(1);
			}
			lightInfoTexture.bind(2);
			mvpMatrix.set(cam.combined);
			mvpMatrix.mul(s.sceneModelMatrix);
			nMatrix.set(cam.view);
			nMatrix.mul(s.sceneModelMatrix);
			tfsShader.setUniformMatrix("u_modelViewMatrix", nMatrix);
			nMatrix.inv();
			nMatrix.tra();
			tfsShader.setUniformi("width", Gdx.graphics.getWidth());
			tfsShader.setUniformi("height", Gdx.graphics.getHeight());
			tfsShader.setUniformi("u_lights", 0);
			tfsShader.setUniformi("s_texture", 1);
			tfsShader.setUniformi("s_lightInfo",2);
			tfsShader.setUniformMatrix("u_normalMatrix", nMatrix);
			tfsShader.setUniformMatrix("u_mvpMatrix", mvpMatrix);
			tfsShader.setUniform3fv("u_lightSource",getViewSpacePositions(), 0,lights.size * 3);
			s.sceneModel.render(tfsShader, GL20.GL_TRIANGLES);
			tfsShader.end();
		} else if (renderMode == 1) {
			normalShader.begin();
			lightTexture.bind(0);
			
			if(hasTexture == 1){
				s.mapTexture.bind(1);
			} else {
				s.noTexture.bind(1);
			}
			
			lightInfoTexture.bind(2);
			s.normalTexture.bind(3);
			mvpMatrix.set(cam.combined);
			mvpMatrix.mul(s.sceneModelMatrix);
			nMatrix.set(cam.view);
			nMatrix.mul(s.sceneModelMatrix);
			normalShader.setUniformMatrix("u_modelViewMatrix", nMatrix);
			nMatrix.inv();
			nMatrix.tra();
			normalShader.setUniformi("width", Gdx.graphics.getWidth());
			normalShader.setUniformi("height", Gdx.graphics.getHeight());
			normalShader.setUniformi("u_lights", 0);
			normalShader.setUniformi("s_texture", 1);
			normalShader.setUniformi("s_lightInfo",2);
			normalShader.setUniformi("u_nrmMap",3);
			normalShader.setUniformMatrix("u_normalMatrix", nMatrix);
			normalShader.setUniformMatrix("u_mvpMatrix", mvpMatrix);
			normalShader.setUniform3fv("u_lightSource",getViewSpacePositions(), 0,lights.size * 3);
			s.sceneModel.render(normalShader, GL20.GL_TRIANGLES);
			normalShader.end();
		}
		renderLights(s);
		
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		uiCAM.position.set(Gdx.graphics.getWidth()/2,Gdx.graphics.getHeight()/2,0);
		uiCAM.viewportHeight = Gdx.graphics.getHeight();
		uiCAM.viewportWidth = Gdx.graphics.getWidth();
		uiCAM.update();
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		sb.setProjectionMatrix(uiCAM.combined);
		sb.begin();
		ton.setPosition(0,0);
		ton.draw(sb);
		toff.setPosition(Gdx.graphics.getWidth()-toff.getWidth(), 0);
		toff.draw(sb);
		on.setPosition(0,Gdx.graphics.getHeight()-on.getHeight());
		on.draw(sb);
		off.setPosition(Gdx.graphics.getWidth()-off.getWidth(), Gdx.graphics.getHeight()-off.getHeight());
		off.draw(sb);
		font.draw(sb, "FPS:" + Gdx.graphics.getFramesPerSecond() + " WITH " + lights.size + " LIGHTS USING " + shaderType[renderMode] , Gdx.graphics.getWidth()/2-300, 40);
		sb.end();
	}


	public void makeLightTexture() {
		/* Calculates what lights are affecting what tiles.
		 * This is done by dividing the camera frustum. 20 planes,
		 * 10 rows and 10 columns to serve as the limits for 100
		 * small frustums (Meaning screen is divided into a 10x10 grid)
		 */
		for (int i = 0; i < 100; i++) {
			lights2.get(i).clear();
		}
		for (int i = 0; i < lights.size; i++) {
			Light l = lights.get(i); 
			frustums.checkFrustums(l.position, l.radius, lights2, i);
		}
		/* Creates a texture containing the color and radius
		 * information about all light sources. Position could
		 * be added here, but for this example it is not due to 
		 * limitations in precision.
		 */
		for (int i = 0; i < lights.size; i++) {
			Light l = lights.get(i);
			c.set(l.color.x,l.color.y,l.color.z,l.radius/255);
			lightInfoPixmap.setColor(c);
			lightInfoPixmap.drawPixel(0,i);
		}
		lightInfoTexture.draw(lightInfoPixmap, 0,0);
		/* Creates a texture that contains a list of
		 * light sources that are affecting each specific
		 * tile. The row in the texture is decided by:
		 * yTile*10+xTile and the following pixels on that
		 * row are used to represent the ID of the light 
		 * sources.  
		 */
		for (int row = 0; row < 100; row++) {
			int col = 0;
			float r=0;
			r = lights2.get(row).size;
			c.set(r/255,0,0,0);
			lightPixmap.setColor(c);
			lightPixmap.drawPixel(col, row);
			col++;
			for (int i = 0; i < lights2.get(row).size; i++) {
				int j = (lights2.get(row).get(i));
				c.set(((float)j)/255, 0, 0, 0);	
				lightPixmap.setColor(c);
				lightPixmap.drawPixel(col, row);
				col++;
			}		
		}
		lightTexture.draw(lightPixmap, 0, 0);
	}
	
	/* Calculate viewspace positions for all light sources
	 * as light computations are done in view space.
	 */
	public float[] getViewSpacePositions(){

		float position[] = new float[lights.size*3];
		int i = 0;
		for(int k=0;k<lights.size;k++){
			tempVec.set(lights.get(k).position);
			tempVec.mul(cam.view);
			position[i++] = tempVec.x;
			position[i++] = tempVec.y;
			position[i++] = tempVec.z;
		}
		return position;
	}
	
	/* Render the light sources using a very simple
	 * shader that just renders a sphere with the 
	 * given color. 
	 */
	public void renderLights(Scene s) {
		ballShader.begin();
		for (Light l : lights) {
			mvpMatrix.set(cam.combined);
			modelMatrix.setToTranslation(l.position).scale(0.3f,0.3f,0.3f);
			mvpMatrix.mul(modelMatrix);
			ballShader.setUniformMatrix("u_mvpMatrix", mvpMatrix);
			ballShader.setUniformf("color", l.color);
			s.lightMesh.render(ballShader, GL20.GL_TRIANGLES);
		}
		ballShader.end();		
	}
	
	/* Updates the camera to allow movement using WASD.
	 * Camera direction can be changed by moving the mouse
	 * (see mouseMoved()).
	 */
	public void updateCam() {
		Gdx.input.setCursorCatched(false);
		Vector3 camVec = new Vector3();
		if (Gdx.input.isKeyPressed(51)) {
			cam.position.add(cam.direction);
		}
		if (Gdx.input.isKeyPressed(47)) {
			cam.position.sub(cam.direction);
		}
		if (Gdx.input.isKeyPressed(32)) {
			camVec.set(cam.direction.x,0,cam.direction.z);
			camVec.crs(cam.up);
			camVec.nor();
			cam.position.add(camVec);
		}
		if (Gdx.input.isKeyPressed(29)) {
			camVec.set(cam.direction.x,0,cam.direction.z);
			camVec.crs(cam.up);
			camVec.nor();
			cam.position.sub(camVec);
		}
		if (Gdx.input.isKeyPressed(62)) {
			cam.position.add(0,1,0);
		}
		cam.update();
	}

	public boolean keyDown(int arg0) {
		return false;
	}

	/* Changes the direction of the camera according to
	 * mouse movement. 
	 */
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int arg2, int arg3) {
		y = Gdx.graphics.getHeight()-y;
		tempVec.set(x,y,0);
		uiCAM.project(tempVec);
		float fx = tempVec.x;
		float fy = tempVec.y;
		if (ton.getBoundingRectangle().contains(fx, fy)) {
			hasTexture = 1;
		} else if (toff.getBoundingRectangle().contains(fx,fy)) {
			hasTexture = 0;
		} else if (on.getBoundingRectangle().contains(fx,fy)) {
			renderMode = 1;
		} else if (off.getBoundingRectangle().contains(fx,fy)){
			renderMode = 0;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return false;
	}


}
