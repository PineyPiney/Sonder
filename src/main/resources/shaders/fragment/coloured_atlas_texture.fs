// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;
uniform vec2 origin;
uniform vec2 size;

out vec4 FragColour;

void main(){
	vec4 texColour = texture(ourTexture, origin + (size * texCoords));
	if(texColour.a <= 0.2) discard;
	FragColour = texColour * colour;
}