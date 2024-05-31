// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform vec3 ambient;
uniform vec3 lightDir;
uniform vec3 lightColour;

uniform sampler2D ourTexture;
uniform sampler2D normalMap;
uniform float alpha;

out vec4 FragColour;

void main(){

	vec4 tex = texture(ourTexture, texCoords);
	float a = tex.a * alpha;
	if(tex.a < .2 || a < .01) discard;

	vec3 normal = normalize(vec3(texture(normalMap, texCoords)));
	float diff = max(-dot(normal, lightDir), 0.0);

	FragColour = vec4(vec3(tex) * (ambient + (diff * lightColour)), a);
}