package com.pineypiney.sonder.city.buildings

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.SonderEngine
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

open class Building(name: String) : GameObject(name) {

	val right = Vec3(sqrt(.5f), 0f, -sqrt(.5f))

	val leftEdge by lazy {
		getComponent<BoxCollider3DComponent>()?.transformedShape?.let { shape ->
			right * shape.projectToNormal(right).min()
		} ?: Vec3()
	}
	val rightEdge by lazy {
		getComponent<BoxCollider3DComponent>()?.transformedShape?.let { shape ->
			right * shape.projectToNormal(right).max()
		} ?: Vec3()
	}

	fun getAlpha(): Float {
		val playerObject = SonderEngine.INSTANCE.game.player
		val playerBox = playerObject.getComponent<BoxCollider3DComponent>()?.transformedShape
			?: Cuboid(playerObject.transformComponent.worldPosition, Quat.identity, Vec3(0f))
		val a = PI.toFloat() * .25f
		val left = leftEdge.xz.rotate(a)
		val right = rightEdge.xz.rotate(a)
		val playerLeft = (this.right * playerBox.projectToNormal(this.right).min()).xz.rotate(a)
		val playerRight = (this.right * playerBox.projectToNormal(this.right).max()).xz.rotate(a)

		return if (playerLeft.y < left.y) {
			if (playerRight.x < left.x) max(0f, left.x - playerRight.x)
			else if (playerLeft.x > right.x) min(1f, playerLeft.x - right.x)
			else 0f
		} else {
			if (playerRight.x < left.x) min(1f, (left - playerRight).length())
			else if (playerLeft.x > right.x) min(1f, (playerLeft - right).length())
			else 1f//min(1f, playerLeft.y - left.y)
		}
	}
}