// FRAGMENT SHADER INFORMATION
#version 400 core

out vec4 FragColour;

uniform float cellSize;
uniform float thickness;
uniform vec4 colour;

in vec3 worldPos;

void main(){

	float outerThickness = thickness * 2.0;
	vec2 cellPos = vec2(mod(worldPos.x, cellSize), mod(worldPos.y, cellSize));
	float borderDist = min(abs(cellPos.x - (cellSize * 0.5)), abs(cellPos.y - (cellSize * 0.5)));

	if(borderDist > outerThickness) discard;
	else if(borderDist > thickness) FragColour = vec4(colour.x, colour.y, colour.z, colour.a * (2.0 - (borderDist / thickness)));
	else FragColour = colour;
}