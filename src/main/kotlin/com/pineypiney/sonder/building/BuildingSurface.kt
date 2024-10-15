package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.maths.shapes.Rect3D
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class BuildingSurface(parent: GameObject, val origin: Vec3, val size: Vec2) : Component(parent, "BDS") {
	override val fields: Array<Field<*>> = arrayOf(
		Vec3Field("ori", ::origin) { origin.put(it) },
		Vec2Field("vec", ::size) { size.put(it) },
	)

	val rect = Rect3D(origin, Vec3(size.x, 0f, 0f), Vec3(0f, 0f, size.y))
}