package com.pineypiney.sonder_test

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TransformComponent
import com.pineypiney.game_engine.util.extension_functions.rotate
import glm_.quat.Quat
import glm_.vec3.Vec3
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test

class Test {

	@Test
	fun reflection() {
		val o = GameObject()
		val c = TransformComponent(o)

		c.velocity = Vec3(2f, -.1f, 0f)
		c.transform.position = Vec3(2f, 5f, -.5f)
		c.transform.rotation = Quat(Vec3(.1f, .4f, 2f))

		val d = c.copy(o)
	}

	@Test
	fun isometricVectors(){

		val up = Vec3(0f, 1f, 0f)
		val r = Vec3(1f, 0f, 0f)

		val sqrt3 = sqrt(1f/3f)
		val sqrt2 = sqrt(.5f)
		val view = Vec3(-sqrt3)

		val texSide1 = Vec3(sqrt2, 0f, -sqrt2)
		val texSide2 = view cross texSide1

		val rUp = Vec3(texSide1 dot up, texSide2 dot up, view dot up)
		val rR = Vec3(texSide1 dot r, texSide2 dot r, view dot r)

		val side2 = view.rotate(Vec3(0f, 0f, PI*.25))
		println("vec is $side2")
	}
}