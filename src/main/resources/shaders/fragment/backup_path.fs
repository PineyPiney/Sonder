// FRAGMENT SHADER INFORMATION
#version 400 core
#define tileWidth 1.0

in vec3 pos;

uniform mat4 model;
uniform float innerThickness;
uniform float outerThickness;
uniform float brickWidth;
uniform float brickLength;

uniform vec3 lineColour;
uniform vec3 darkBrickColour;
uniform vec3 lightBrickColour;
uniform float rgbVariance;

uniform bool selected;

// This mask tells the shader which edges are adjacent to other paths
// So edges shouldn't be drawn there
// x is the positive x edge, y is the negative x edge,
// z is the positive z edge, w is the negative z edge.
// Bit N of the integer is for the Nth tile from this paths origin.
// 1 means there is a path and it shouldn't be drawn, 0 means there is no path and to draw the edge as normal
uniform uvec4 xzEdgeMask;
// This is another mask for the 4 corners
// From smallest to larger bit,
// -x, -z
// -x, +z
// +x, -z
// +x, +z
uniform uint yEdgeMask;

out vec4 FragColour;

// https://stackoverflow.com/a/4275343
// Generate a Pseudorandom number from seed
float rand(int seed);

float vec2Noise(ivec2 xy, int seed, float mult, float offset);

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

bool shouldDraw(float pos, uint mask);
// check if value is near to repeat.
// Returns 1.0 of value is within margin of repeat,
// Then linearly lerps 0 - 1 to outerMargin,
// Returns 0 if further than outerMargin
float check(float value, float repeat, float margin, float outerMargin);

vec3 pickColour(ivec2 brickID);

void main(){

	float sizeX = length(model[0]);
	float sizeY = length(model[1]);
	float sizeZ = length(model[2]);

	float posX = sizeX * pos.x;
	float posY = sizeY * pos.y;
	float posZ = sizeZ * pos.z;

	float checkXEdges = check(posX, sizeX, innerThickness, outerThickness);
	float checkYEdges = check(posY, sizeY, innerThickness, outerThickness);
	float checkZEdges = check(posZ, sizeZ, innerThickness, outerThickness);

	// If checkY == 1.0 then the top surface is being drawn, otherwise it's a side
	bool drawingSides = checkYEdges < 1.0;

	if(drawingSides){
		// If this is a side being rendered and it's within inner thickness of the visible X face
		// then clamp the position to halfway inside the innerThickness so that the face
		// calculates the correct brickID
		if(posX + innerThickness > sizeX) posX = sizeX - (innerThickness * 0.5f);
	}

	float texX = posX + model[3][0];
	float texZ = posZ + model[3][2];


	// Range from 0 - brickWidth, x coordinate within brick
	float brickXDelta = mod(texX, brickWidth);
	int xBrickID = int(ceil(texX / brickWidth));

	// This function modifies both its parameters
	float thisBrickWidth = randomiseWidths(xBrickID, brickXDelta);


	float zBrickOffset = getBrickOffsetZ(xBrickID) * brickLength;
	texZ += zBrickOffset;
	float brickZDelta = mod(texZ, brickLength);

	int zBrickID = int(ceil(texZ / brickLength));
	float thisBrickLength = brickLength; //randomiseLengths(xBrickID, zBrickID, brickZDelta);

	// This mask isn't neccessary because the brick lines need drawing anyway
	uint xMask = (pos.x > 0.5 ? xzEdgeMask.x : xzEdgeMask.y);
	uint zMask = (pos.z > 0.5 ? xzEdgeMask.z : xzEdgeMask.w);

	bool drawXEdge = shouldDraw(posZ, xMask);
	bool drawZEdge = shouldDraw(posX, zMask);


	bool drawYEdge = false;
	if(drawingSides) {
		// -- = 0 / +- = 1 / -+ = 2 / ++ = 3
		int corner = (int(pos.x > 0.5) << 1) + int(pos.z > 0.5);
		drawYEdge = ((yEdgeMask >> corner) & 1u) == 0u;
	}

	// This is for the y edge of the path,

	// Only draw the y line if x and z edges are being drawn
	//
	bool drawYLine = drawYEdge;

	// Take the strength of the brick line for each axis
	float checkBrickX = check(brickXDelta, thisBrickWidth, innerThickness, outerThickness);
	float checkBrickZ = check(brickZDelta, thisBrickLength, innerThickness, outerThickness);

	float checkX = drawXEdge ? max(checkXEdges, checkBrickX) : checkBrickX;
	float checkZ = drawZEdge ? max(checkZEdges, checkBrickZ) : checkBrickZ;


	// The true strength is the SECOND strongest, because the first strongest
	// is always 1 because one axis is just the rendered plane
	float strength;
	if(!drawingSides) strength = max(checkX, checkZ);
	else if(checkXEdges > 0.0 && checkZEdges > 0.0 && !drawYEdge) strength = checkYEdges;
	else if(checkZ == 1.0) strength = max(checkX, checkYEdges);
	else if(checkX == 1.0) strength = max(checkYEdges, checkZ);


	if(strength == 1.0) FragColour = vec4(lineColour, 1.0);
	else {
		vec3 brickColour = pickColour(ivec2(xBrickID, zBrickID));
		if(strength == 0.0) FragColour = vec4(brickColour, 1.0);
		else FragColour = vec4((lineColour * strength) + (brickColour * (1.0 - strength)), 1.0);
	}

	if(selected) FragColour *= 1.2f;
}


// https://stackoverflow.com/a/4275343
float rand(int seed){
	return fract(sin(seed * 78.233) * 43758.5453);
}

float vec2Noise(ivec2 xy, int seed, float mult, float offset){
	return (rand(xy.x * seed + xy.y * seed ^ 86243) - offset) * mult;
}

// Varies the offset of the bricks in the Z axis to give a staggered effect
float getBrickOffsetZ(int id){
	if(id % 2 == 0) {
		float val = rand(id) * 0.3;
		 return val > 0.05 ? val : 0.0;
	}
	else return 0.5 + (rand(id) * 0.3);
}

// Varies the width of the bricks in the X axis to give them different widths (Min .6, Max 1.4)
float getBrickStretchX(int id){
	float brickPerTile = tileWidth / brickWidth;
	if(fract(brickPerTile) == 0.0 && id % int(brickPerTile) == 0) return 0.0;
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

bool shouldDraw(float pos, uint mask){
	int tilId = int(floor(pos) / tileWidth);
	bool drawEdge = ((mask >> tilId) & 1u) == 0u;

	// If the edge is not supposed to be drawn for this tile
	if(!drawEdge){
		// How far into the tile is the pixel
		float tileZDelta = mod(pos, tileWidth);
		// If it is just neighbouring a tile that SHOULD be drawn then draw it anyway,
		// This helps to round off corners
		if(tileZDelta < innerThickness){
			drawEdge = ((mask >> (tilId - 1) & 1u) == 0u);
		}
		else if(tileZDelta > (tileWidth - innerThickness)){
			drawEdge = ((mask >> (tilId + 1) & 1u) == 0u);
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

vec3 pickColour(ivec2 brickID){
	float range = rgbVariance * 2.0f;
	float base = vec2Noise(brickID, 27793, 1.0 - range, rgbVariance / (range - 1.0));
	float redVary = vec2Noise(brickID, 24683, range, 0.5);
	float greenVary = vec2Noise(brickID, 59561, range, 0.5);
	float blueVary = vec2Noise(brickID, 85091, range, 0.5);

	vec3 multiplier = vec3(base + redVary, base + greenVary, base + blueVary);

	return darkBrickColour + (lightBrickColour - darkBrickColour) * multiplier;
}
