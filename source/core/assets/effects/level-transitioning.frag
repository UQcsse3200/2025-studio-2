#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
	#define PRECISION
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform float u_progress;
uniform float u_smoothness;
uniform float u_centerX;
uniform float u_centerY;

void main() {
	vec3 rgb = texture2D(u_texture0, v_texCoords).xyz;
	float d = distance(v_texCoords, vec2(u_centerX, u_centerY));

	float inner = 1.0 - u_progress - u_smoothness / 2.0;
	float outer = inner + u_smoothness;

	float factor = smoothstep(inner, outer, d);
	rgb = mix(rgb, vec3(0.0, 0.0, 0.0), factor);

	gl_FragColor = vec4(rgb, 1.0);
}

