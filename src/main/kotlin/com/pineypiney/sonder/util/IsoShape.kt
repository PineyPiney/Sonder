package com.pineypiney.sonder.util

import com.pineypiney.game_engine.objects.util.shapes.IndicesShape
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import com.pineypiney.game_engine.util.maths.sin30
import com.pineypiney.game_engine.util.maths.sin60
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class IsoShape(val texture: Texture, point: Vec2i, x: Float, y: Float, z: Float, ppu: Float): IndicesShape(createVertices(texture, point, x, y, z, ppu), intArrayOf(3, 2), indices) {

	override val shape: Shape
		get() = Rect2D(Vec2(0), Vec2(1))

	companion object {
		val indices = IntArray(18){
			val i  = it % 3
			val j = it / 3
			when(i){
				0 -> 0
				1 -> j + 1
				2 -> ((j + 1) % 6) + 1
				else -> 0
			}
		}
		fun createVertices(texture: Texture, point: Vec2i, x: Float, y: Float, z: Float, ppu: Float): FloatArray{
			val list = mutableListOf<Float>()

			val hx = x * .5f
			val hz = z * .5f

			val bottom = Vec3(hx, 0f, hz)
			val center = Vec3(hx, y, hz)
			val top = Vec3(-hx, y, -hz)
			val topLeft = Vec3(-hx, y, hz)
			val topRight = Vec3(hx, y, -hz)
			val bottomLeft = Vec3(-hx, 0f, hz)
			val bottomRight = Vec3(hx, 0f, -hz)

			val centerTexture = Vec2(point) / texture.size


			val mult = Vec2(ppu) / texture.size
			val xVec = mult * x * Vec2(-sin60, sin30)
			val yVec = Vec2(0f, mult.y * y)
			val zVec = mult * z * Vec2(sin60, sin30)

			val bottomTexture = centerTexture - yVec
			val topTexture = centerTexture + (xVec + zVec)
			val topLeftTexture = centerTexture + xVec
			val topRightTexture = centerTexture + zVec
			val bottomLeftTexture = topLeftTexture - yVec
			val bottomRightTexture = topRightTexture - yVec

			list.addAll(createVertex(center, centerTexture))
			list.addAll(createVertex(top, topTexture))
			list.addAll(createVertex(topRight, topRightTexture))
			list.addAll(createVertex(bottomRight, bottomRightTexture))
			list.addAll(createVertex(bottom, bottomTexture))
			list.addAll(createVertex(bottomLeft, bottomLeftTexture))
			list.addAll(createVertex(topLeft, topLeftTexture))

			return list.toFloatArray()
		}
		
		fun createVertex(pos: Vec3, tex: Vec2): List<Float>{
			return listOf(pos.x, pos.y, pos.z, tex.x, tex.y)
		}
	}
}