// FRAGMENT SHADER INFORMATION
#version 400 core
#define TILE_WIDTH 1.0
#define PATH_HEIGHT 0.2

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

// This mask tells the shader where the tiles are in this chunk and the neighbouring chunks
// First two UInts are this chunk by row from 0,0 to 7,7
// Third and fourth are the neighbouring chunks, clockwise from -1, -1
uniform uvec4 gridMask;

out vec4 FragColour;

// https://stackoverflow.com/a/4275343
// Generate a Pseudorandom number from seed
float rand(int seed);

float vec2Noise(ivec2 xy, int seed, float mult, float offset);

bool checkTileMake(ivec2 tile);

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

	vec2 chunkPos = pos.xz * TILE_WIDTH;
	ivec2 tile = ivec2(int(ceil(chunkPos.x)) - 1, int(ceil(chunkPos.y)) - 1);
	vec2 tilePos = chunkPos - tile;

	// If checkY == 1.0 then the top surface is being drawn, otherwise it's a side
	bool drawingSides = pos.y < PATH_HEIGHT;

	float texX = pos.x + model[3][0];
	float texZ = pos.z + model[3][2];

	// Range from 0 - brickWidth, x coordinate within brick
	float brickXDelta = mod(texX, brickWidth);
	// This renders the X face properly, needed because it should be rendered as part of the brick behind it, not the brick in front
	if(brickXDelta == 0.0) brickXDelta = brickWidth;

	int xBrickID = int(ceil(texX / brickWidth));
	// This function modifies both its parameters
	float thisBrickWidth = randomiseWidths(xBrickID, brickXDelta);


	float zBrickOffset = getBrickOffsetZ(xBrickID) * brickLength;
	texZ += zBrickOffset;
	float brickZDelta = mod(texZ, brickLength);

	int zBrickID = int(ceil(texZ / brickLength));
	float thisBrickLength = brickLength; //randomiseLengths(xBrickID, zBrickID, brickZDelta);

    int xDir = tilePos.x > (TILE_WIDTH * 0.5) ? 1 : -1;
    bool drawXNeighbour = checkTileMake(tile + ivec2(xDir, 0));
    int zDir = tilePos.y > (TILE_WIDTH * 0.5) ? 1 : -1;
    bool drawZNeighbour = checkTileMake(tile + ivec2(0, zDir));
	bool drawXZNeighbour = checkTileMake(tile + ivec2(xDir, zDir));

	// Draw the edges between tiles if the next tile in the direction is not being drawn
	// Or if this is a concave corner between two tiles
	bool drawXEdge = !drawXNeighbour || (drawingSides && drawXZNeighbour);
	bool drawZEdge = !drawZNeighbour || (drawingSides && drawXZNeighbour);

	// Take the strength of the tile line for each axis
	float checkTileX = check(tilePos.x, TILE_WIDTH, innerThickness, outerThickness);
	float checkTileY = check(pos.y, PATH_HEIGHT, innerThickness, outerThickness);
	float checkTileZ = check(tilePos.y, TILE_WIDTH, innerThickness, outerThickness);

	// Take the strength of the brick line for each axis
	float checkBrickX = check(brickXDelta, thisBrickWidth, innerThickness, outerThickness);
	float checkBrickZ = check(brickZDelta, thisBrickLength, innerThickness, outerThickness);

	float checkX = drawXEdge ? max(checkTileX, checkBrickX) : checkBrickX;
	float checkZ = drawZEdge ? max(checkTileZ, checkBrickZ) : checkBrickZ;

	// The true strength is the SECOND strongest, because the first strongest
	// is always 1 because one axis is just the rendered plane
	float strength;
	if(!drawingSides) strength = max(checkX, checkZ);
	else if(checkZ == 1.0) strength = max(checkX, checkTileY);
	else if(checkX == 1.0) strength = max(checkTileY, checkZ);
	else {
		// This is for debugging, it should never be reached
		FragColour = vec4(1.0, 0.0, 0.0, 1.0);
		return;
	}

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

/*
	Tile Mask Layout
	z[18] z[17] z[16] z[15] z[14] z[13] z[12] z[11] z[10] z[09]
	z[19] y[31] y[30] y[29] y[28] y[27] y[26] y[25] y[24] z[08]
	z[20] y[23] y[22] y[21] y[20] y[19] y[18] y[17] y[16] z[07]
	z[21] y[15] y[14] y[13] y[12] y[11] y[10] y[09] y[08] z[06]
	z[22] y[07] y[06] y[05] y[04] y[03] y[02] y[01] y[00] z[05]
	z[23] x[31] x[30] x[29] x[28] x[27] x[26] x[25] x[24] z[04]
	z[24] x[23] x[22] x[21] x[20] x[19] x[18] x[17] x[16] z[03]
	z[25] x[15] x[14] x[13] x[12] x[11] x[10] x[09] x[08] z[02]
	z[26] x[07] x[06] x[05] x[04] x[03] x[02] x[01] x[00] z[01]
	z[27] w[07] w[06] w[05] w[04] w[03] w[02] w[01] w[00] z[00]
*/
bool checkTileMake(ivec2 tile){
	// Check edges
	// z[27 - 18]
	if(tile.x == -1) return ((gridMask.z >> (26 - tile.y)) & 1u) == 1u;
	// z[17 - 9]
	if(tile.y == 8) return ((gridMask.z >> (17 - tile.x)) & 1u) == 1u;
	// z[8 - 0]
	if(tile.x == 8) return ((gridMask.z >> (tile.y + 1)) & 1u) == 1u;
	// w[7 - 0]
	if(tile.y == -1) return ((gridMask.w >> (tile.x)) & 1u) == 1u;

	if(tile.y >= 4) return ((gridMask.y >> (((tile.y - 4) << 3) + 7 - tile.x)) & 1u) == 1u;
	else return ((gridMask.x >> (((tile.y) << 3) + 7 - tile.x)) & 1u) == 1u;
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
	float brickPerTile = TILE_WIDTH / brickWidth;
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
	int tilId = int(floor(pos) / TILE_WIDTH);
	bool drawEdge = ((mask >> tilId) & 1u) == 0u;

	// If the edge is not supposed to be drawn for this tile
	if(!drawEdge){
		// How far into the tile is the pixel
		float tileZDelta = mod(pos, TILE_WIDTH);
		// If it is just neighbouring a tile that SHOULD be drawn then draw it anyway,
		// This helps to round off corners
		if(tileZDelta < innerThickness){
			drawEdge = ((mask >> (tilId - 1) & 1u) == 0u);
		}
		else if(tileZDelta > (TILE_WIDTH - innerThickness)){
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
