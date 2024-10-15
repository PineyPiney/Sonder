package com.pineypiney.sonder.scenes

import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.npcs.Puppy
import com.pineypiney.sonder.environment.CherryTree
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class TownScene(engine: SonderEngine) : SonderGamePlay(engine) {

	override val size: Vec2 = Vec2(40f)
	override val floor: Float = 1.3f

	override fun addObjects() {
		super.addObjects()
		add(Puppy())

		add(CherryTree().apply { translate(Vec3(20f, 0f, 0f)) })
	}
}