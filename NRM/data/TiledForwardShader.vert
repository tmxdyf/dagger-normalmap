attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord;


uniform mat4 u_mvpMatrix;
uniform mat4 u_normalMatrix;
uniform mat4 u_modelViewMatrix;

varying vec3 N;
varying vec3 v;
varying vec2 v_texCoord;

void main()
{
	v = vec3(u_modelViewMatrix * a_position);
	N = normalize(vec3(u_normalMatrix * vec4(a_normal,1.0)));
	gl_Position = u_mvpMatrix * a_position;
	v_texCoord = a_texCoord;
}