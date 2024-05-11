package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.util.maths.shapes.Line2D
import glm_.vec2.Vec2

class BuildingSurface(parent: GameObject, val origin: Vec2, val vector: Vec2) : Component(parent, "BDS") {
	override val fields: Array<Field<*>> = arrayOf(
		Vec2Field("ori", ::origin){ origin.put(it) },
		Vec2Field("vec", ::vector){ vector.put(it) },
	)

	val line = Line2D(origin, origin + vector)
}