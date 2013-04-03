attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec3 a_tangent;
attribute vec2 a_texCoord;

uniform mat4 u_mvpMatrix;
uniform mat4 u_modelViewMatrix;
uniform mat4 u_normalMatrix;

varying vec2 v_texCoord;
varying vec3 viewSpacePos;
varying vec3 viewSpaceNormal;
varying vec3 viewSpaceTangent;

void main(void)
{
	viewSpacePos = vec3(u_modelViewMatrix * a_position);
    viewSpaceNormal = vec3(u_normalMatrix * vec4(a_normal,1.0));
    viewSpaceTangent = vec3(u_normalMatrix * vec4(a_tangent,1.0));
    gl_Position = u_mvpMatrix * a_position;
    v_texCoord = a_texCoord;
}