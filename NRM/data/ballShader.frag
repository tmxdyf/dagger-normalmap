#ifdef GL_ES
precision mediump float;
#endif
uniform vec3 color;
varying float v_ttl;
void main()
{
    gl_FragColor =  vec4(color.x,color.y,color.z,1.0); 
}