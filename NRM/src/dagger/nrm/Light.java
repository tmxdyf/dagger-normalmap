package dagger.nrm;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Light {
	public Vector3 color;
	public Vector3 position;
	public float radius;
	float deg;
	float speed;
	public boolean stationary;
	public Light(float x, float y, float z, float r, float g, float b) {
		radius = 3;
		color = new Vector3(r,g,b);
		position = new Vector3(x,y,z);
		deg = MathUtils.random(0,360f);
		speed = MathUtils.random(3f,4f);
	}
	public void update() {
		if (stationary)
			return;
		deg += 1 % 360;
		position.x = MathUtils.sin(deg*MathUtils.degreesToRadians)*speed;
	}
}
