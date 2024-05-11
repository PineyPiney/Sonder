// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;

out vec4 FragColour;

void main(){
	vec4 texColour = texture(ourTexture, texCoords);
	if(texColour.a == 0) discard;
	FragColour = texColour * colour;
}