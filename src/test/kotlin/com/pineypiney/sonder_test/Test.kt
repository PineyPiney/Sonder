package com.pineypiney.sonder_test

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TransformComponent
import glm_.quat.Quat
import glm_.vec3.Vec3
import kotlin.test.Test

class Test {

	@Test
	fun reflection() {
		val o = GameObject()
		val c = TransformComponent(o)

		c.velocity = Vec3(2f, -.1f, 0f)
		c.transform.position = Vec3(2f, 5f, -.5f)
		c.transform.rotation = Quat(Vec3(.1f, .4f, 2f))
		c.depth = 2

		val d = c.copy(o)
	}
}