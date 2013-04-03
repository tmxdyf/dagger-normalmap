package dagger.nrm;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.math.Matrix4;

public class Scene {
	public Matrix4 sceneModelMatrix;
	public Mesh sceneModel;
	public Texture mapTexture;
	public Texture normalTexture;
	public Texture noTexture;
	public Mesh lightMesh;
	public Scene() {
		mapTexture = new Texture(Gdx.files.internal("data/texture2__COLOR.png"));
		mapTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		normalTexture = new Texture(Gdx.files.internal("data/texture2__NRM.png"));
		normalTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		noTexture = new Texture(Gdx.files.internal("data/noTexture.png"));
		noTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		sceneModelMatrix = new Matrix4();
		sceneModelMatrix.idt();
		sceneModel = loadMap();
		lightMesh = loadLightball();
	}
	private static Mesh loadMap() {
		InputStream in = Gdx.files.internal("data/terrain_3_UV.obj").read();
		Mesh mesh = ObjLoaderTan.loadObj(in);
		try {

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mesh;
	}
	private static Mesh loadLightball() {
		InputStream in = Gdx.files.internal("data/light.obj").read();
		Mesh mesh = ObjLoader.loadObj(in);
		try {

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mesh.getVertexAttribute(Usage.Position).alias = "a_position";
		mesh.getVertexAttribute(Usage.Normal).alias = "a_normal";
		return mesh;
	}
}
