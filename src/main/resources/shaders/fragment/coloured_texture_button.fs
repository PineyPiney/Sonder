// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform vec4 colour;
uniform bool selected;

out vec4 FragColour;

void main(){
	vec4 texColour = texture(ourTexture, texCoords);
	if(texColour.a == 0) discard;
	vec2 c = texCoords - vec2(0.5);
	if(selected && (c.x * c.x) + (c.y * c.y) < 0.0036) FragColour = vec4(1.0);
	else FragColour = texColour * colour;
}