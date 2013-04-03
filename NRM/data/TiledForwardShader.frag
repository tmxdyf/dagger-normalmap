#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D s_texture;
uniform sampler2D u_lights;
uniform sampler2D s_lightInfo;
uniform int width;
uniform int height;
uniform vec3 u_lightSource[48];
varying vec3 N;
varying vec3 v;
varying vec2 v_texCoord;

void main() {
	// Calculate what tile the pixel is in 
	int xtile = int(gl_FragCoord.x)/(width/10);
	int ytile = int(gl_FragCoord.y)/(height/10);
	float textureRow = float(ytile*10+xtile)/128.0;
	vec4 pixel = texture2D(u_lights,vec2(0.5/64.0,textureRow));
	vec4 color = texture2D(s_texture,v_texCoord) * 1.3; //our texture was a bit dark :( 
	vec4 scene_ambient_light = vec4(0.2,0.1,0.2,1.0);
	vec4 material_diffuse = vec4(1.0);
	vec4 ambient = material_diffuse * scene_ambient_light;
	vec4 finalDiffuse = vec4(0.0);
	// loop over the lights contained in the tile
	for (int i = 0; i < int(ceil(pixel.r*255.0)); i++) {
	    // Using offset 1, look at the pixels at the given textureRow to find the lightID. (Later used to lookup in info texture.)
		vec4 tempPixel = texture2D(u_lights,vec2((float(i)+1.5)/64.0,textureRow));
		int lID = int(clamp(ceil(tempPixel.r*255.0),0.0,255.0));
		//Look at the first pixel at the row corresponding to the light ID for color and radius info.
		vec4 lightInfo = texture2D(s_lightInfo,vec2(0.5,float(lID)/128.0));
		float light_radius = lightInfo.a*255.0;
		lightInfo.a = 1.0;
		vec3 dif = u_lightSource[lID] - v;
		float len = length(dif);
		vec3 dir = dif/len;
		
		float diffuseTerm = clamp(dot(N, dir), 0.0,1.0) * clamp((light_radius-len),0.0,1.0);
		finalDiffuse += lightInfo *diffuseTerm * material_diffuse;
	}
	gl_FragColor = color*(ambient+finalDiffuse);
}
