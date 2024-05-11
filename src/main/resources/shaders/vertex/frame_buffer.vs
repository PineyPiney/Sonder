// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform float z;

out vec2 texCoords;

void main(){
	gl_Position = vec4(aPos, z, 1.0);
	texCoords = aTexCoords;
}