// FRAGMENT SHADER INFORMATION
#version 400 core

in vec2 texCoords;

uniform sampler2D ourTexture;
uniform float fill;
uniform vec3 fillColour;

out vec4 FragColour;

void main(){
    vec4 colour = texture(ourTexture, texCoords);
    if(colour.a == 0) discard;

    else if(texCoords.y < fill) FragColour = vec4(fillColour, colour.a);
    else FragColour = colour;
}