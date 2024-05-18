// VERTEX SHADER INFORMATION
#version 400 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 worldPos;
out vec2 texCoords;

void main(){
	vec4 worldSpace = model * vec4(aPos, 0.0, 1.0);
	gl_Position = projection * view * worldSpace;
	worldPos = vec3(worldSpace);
	texCoords = aTexCoords;
}