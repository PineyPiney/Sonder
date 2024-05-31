// FRAGMENT SHADER INFORMATION
#version 400 core
#define tileWidth 1.0

in vec3 pos;

uniform mat4 model;
uniform float innerThickness;
uniform float outerThickness;
uniform float brickWidth;
uniform float brickLength;

uniform vec3 darkBrickColour;
uniform vec3 lightBrickColour;
uniform vec3 lineColour;

// This mask tells the shader which edges are adjacent to other paths
// So edges shouldn't be drawn there
// x is the positive x edge, y is the negative x edge,
// z is the positive z edge, w is the negative z edge.
// Bit N of the integer is for the Nth tile from this paths origin.
// 1 means there is a path and it shouldn't be drawn, 0 means there is no path and to draw the edge as normal
uniform ivec4 edgeMask;

out vec4 FragColour;

// https://stackoverflow.com/a/4275343
// Generate a Pseudorandom number from seed
float rand(int seed);

float PHI = 1.61803398874989484820459;  // Φ = Golden Ratio
float gold_noise(ivec2 xy, int seed, float mult, float offset);

// Varies the offset of the bricks in the Z axis to give a staggered effect
float getBrickOffsetZ(int id);
// Varies the width of the bricks in the X axis to give them different widths (Min .6, Max 1.4)
float getBrickStretchX(int id);
// Varies the length of the bricks in the Z axis to give them different lengths (Min .6, Max 1.4)
float getBrickStretchZ(int id);

// Calculate new brick widths
float randomiseWidths(inout int xBrickID, inout float brickXDelta);

// Calculate new brick lengths
float randomiseLengths(inout int xBrickID, inout int zBrickID, inout float brickZDelta);

bool shouldDraw(float pos, int mask);
// check if value is near to repeat.
// Returns 1.0 of value is within margin of repeat,
// Then linearly lerps 0 - 1 to outerMargin,
// Returns 0 if further than outerMargin
float check(float value, float repeat, float margin, float outerMargin);

vec3 pickColour(ivec2 brickID){
	float base = gold_noise(brickID, 27793, 0.8, -0.125);
	float redVary = gold_noise(brickID, 24683, 0.2, 0.5);
	float greenVary = gold_noise(brickID, 59561, 0.2, 0.5);
	float blueVary = gold_noise(brickID, 85091, 0.2, 0.5);

	vec3 multiplier = vec3(base + redVary, base + greenVary, base + blueVary);

	return darkBrickColour + (lightBrickColour - darkBrickColour) * multiplier;
}

void main(){

	float sizeX = model[0][0];
	float posX = sizeX * pos.x;
	float texX = posX + model[3][0];

	float sizeY = model[1][1];

	float sizeZ = model[2][2];
	float posZ = sizeZ * pos.z;
	float texZ = posZ + model[3][2];

	// Range from 0 - brickWidth, x coordinate within brick
	float brickXDelta = mod(texX, brickWidth);
	int xBrickID = int(ceil(texX / brickWidth));

	// This function modifies both its parameters
	float thisBrickWidth = randomiseWidths(xBrickID, brickXDelta);


	float zBrickOffset = getBrickOffsetZ(xBrickID);
	float brickZDelta = mod(texZ + zBrickOffset, brickLength);
	int zBrickID = int(ceil(texZ + zBrickOffset / brickLength));

	float thisBrickLength = brickLength; //randomiseLengths(xBrickID, zBrickID, brickZDelta);

	// This mask isn't neccessary because the brick lines need drawing anyway
	int xMask = (pos.x > 0.5 ? edgeMask.x : edgeMask.y);
	bool drawXEdge = shouldDraw(posZ, xMask);

	int zMask = (pos.z > 0.5 ? edgeMask.z : edgeMask.w);
	bool drawZEdge = shouldDraw(posX, zMask);


	// Take the strength of the brick line for each axis
	float checkX = check(brickXDelta, thisBrickWidth, innerThickness, outerThickness);
	if(drawXEdge) checkX = max(check(posX, sizeX, innerThickness, outerThickness), checkX);

	float checkY = check(pos.y * sizeY, sizeY, innerThickness, outerThickness);

	float checkZ = check(brickZDelta, thisBrickLength, innerThickness, outerThickness);
	if(drawZEdge) checkZ = max(check(posZ, sizeZ, innerThickness, outerThickness), checkZ);

	// The true strength is the SECOND strongest, because the first strongest
	// is always 1 because one axis is just the rendered plane

	float strength;
	if(checkX == 1.0) strength = max(checkY, checkZ);
	else if(checkY == 1.0) strength = max(checkX, checkZ);
	else strength = max(checkY, checkX);
	//strength = checkX; //TEMP

	if(strength == 1.0) FragColour = vec4(lineColour, 1.0);
	else {
		vec3 brickColour = pickColour(ivec2(xBrickID, zBrickID));
		if(strength == 0.0) FragColour = vec4(brickColour, 1.0);
		else FragColour = vec4((lineColour * strength) + (brickColour * (1.0 - strength)), 1.0);
	}
}


// https://stackoverflow.com/a/4275343
float rand(int seed){
	return fract(sin(seed * 78.233) * 43758.5453);
}

float gold_noise(ivec2 xy, int seed, float mult, float offset){
	return (rand(xy.x * seed + xy.y * seed ^ 86243) - offset) * mult;
}

// Varies the offset of the bricks in the Z axis to give a staggered effect
float getBrickOffsetZ(int id){
	return (id % 2 == 0) ? rand(id) * 0.3 : 0.5 + (rand(id) * 0.3);
}

// Varies the width of the bricks in the X axis to give them different widths (Min .6, Max 1.4)
float getBrickStretchX(int id){
	return brickWidth * ((0.4 * rand(id)) - 0.2);
}

// Varies the length of the bricks in the Z axis to give them different lengths (Min .6, Max 1.4)
float getBrickStretchZ(int id){
	return brickLength * ((0.3 * rand(id)) - 0.15);
}

// Calculate new brick widths
float randomiseWidths(inout int xBrickID, inout float brickXDelta){

	float lowerXBoundary = getBrickStretchX(xBrickID - 1);
	float upperXBoundary = getBrickStretchX(xBrickID);

	if(brickXDelta < lowerXBoundary){
		xBrickID--;
		upperXBoundary = lowerXBoundary;
		lowerXBoundary = getBrickStretchX(xBrickID - 1);
		brickXDelta += brickWidth;
	}
	else if(brickXDelta > (brickWidth + upperXBoundary)){
		lowerXBoundary = upperXBoundary;
		upperXBoundary = getBrickStretchX(xBrickID++);
		brickXDelta -= brickWidth;
	}

	// Transforms the range to between lowerBoundary - (thisBrickWidth + upperBoundary)
	brickXDelta -= lowerXBoundary;
	return brickWidth + upperXBoundary - lowerXBoundary;
}

float randomiseLengths(inout int xBrickID, inout int zBrickID, inout float brickZDelta){

	float lowerZBoundary = getBrickStretchZ(xBrickID * (zBrickID - 1));
	float upperZBoundary = getBrickStretchZ(xBrickID * zBrickID);
	if(brickZDelta < lowerZBoundary){
		zBrickID--;
		upperZBoundary = lowerZBoundary;
		lowerZBoundary = getBrickStretchZ(xBrickID * (zBrickID - 1));
		brickZDelta += brickLength;
	}
	else if(brickZDelta > (brickLength + upperZBoundary)){
		lowerZBoundary = upperZBoundary;
		upperZBoundary = getBrickStretchZ(xBrickID * zBrickID++);
		brickZDelta -= brickLength;
	}

	float newBrickLength = brickLength + upperZBoundary - lowerZBoundary;
	// Transforms the range to between lowerBoundary - (thisBrickWidth + upperBoundary)
	float zBrickOffset = getBrickOffsetZ(xBrickID);
	brickZDelta = mod(brickZDelta + zBrickOffset - lowerZBoundary, newBrickLength);

	return newBrickLength;
}

bool shouldDraw(float pos, int mask){
	// This mask isn't neccessary because the brick lines need drawing anyway
	int tilId = int(floor(pos) / tileWidth);
	bool drawEdge = ((mask >> tilId) & 1) == 0;

	if(!drawEdge){
		float tileZDelta = mod(pos, tileWidth);
		if(tileZDelta < innerThickness){
			drawEdge = ((mask >> (tilId - 1) & 1) == 0);
		}
		else if(tileZDelta > (tileWidth - innerThickness)){
			drawEdge = ((mask >> (tilId + 1) & 1) == 0);
		}
	}
	return drawEdge;
}

float check(float value, float repeat, float margin, float outerMargin){
	float distance = (repeat * 0.5) - abs((repeat * 0.5) - value);
	if(distance < margin) return 1.0;
	else if(distance < outerMargin) return (outerMargin - distance) / (outerMargin - margin);
	else return 0.0;

}
