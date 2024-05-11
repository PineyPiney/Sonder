// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec3 colour;
uniform float fill;

out vec4 FragColour;

void main(){
	if(texCoords.y > fill) discard;
	vec4 texColour = texture(ourTexture, texCoords);
	if(texColour.a == 0) discard;
	FragColour = vec4(colour, texColour.a);
}