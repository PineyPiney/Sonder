// FRAGMENT SHADER INFORMATION
#version 400 core
#define NUM_COLOURS 4

in vec2 texCoords;

uniform vec3 colours[NUM_COLOURS];
uniform float levels[NUM_COLOURS - 1];
uniform float blendSpace;

out vec4 FragColour;

int getSpace(){

	for(int i = 0; i < NUM_COLOURS - 1; i++){
		if(levels[i] > texCoords.y) return i;
	}
	return NUM_COLOURS - 1;
}

vec3 blendColours(vec3 one, vec3 two, float delta){
	return (one * (1.0 - delta)) + (two * delta);
}

void main(){
	int space = getSpace();
	float f = texCoords.y;

	float bottom = space == 0 ? 0.0 : levels[space - 1];
	float top = space == NUM_COLOURS - 1 ? 1.0 : levels[space];

	float bottomDist = f - bottom;
	if(space != 0 && bottomDist < blendSpace) {
		FragColour = vec4(blendColours(colours[space - 1], colours[space], 0.5 + (bottomDist / (2.0 * blendSpace))), 1.0);
		return;
	}

	float topDist = top - f;
	if(space != NUM_COLOURS - 1 && topDist < blendSpace) {
		FragColour = vec4(blendColours(colours[space + 1], colours[space], 0.5 + (topDist / (2.0 * blendSpace))), 1.0);
		return;
	}

	FragColour = vec4(colours[space], 1.0);
}
