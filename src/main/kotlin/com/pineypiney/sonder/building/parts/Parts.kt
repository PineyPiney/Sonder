package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import glm_.quat.Quat
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class Parts {

	companion object {
		val ppu = 200f

		val CHAIR = createPart("Chair") {
			arrayOf(
				Plant(it),
			)
		}
		val PLANT = createPart("Plant") {
			arrayOf(
				Plant(it),
			)
		}
		val DIY_TABLE = createPart("Table") {
			arrayOf(
				Table(it),
			)
		}

		val DOOR = createPart("Door") {
			arrayOf(
				Door(it),
			)
		}
		val WALL = createPart("Wall") {
			arrayOf(
				Plant(it),
			)
		}
		val WINDOW = createPart("Window") {
			arrayOf(
				Window(it),
			)
		}
		val POND = createPart("Pond") {
			arrayOf(
				Pond(it)
			)
		}

		val PATH = createPart("Path") {
			arrayOf(
				Path(it, Vec2i(0, 0)),
				BoxCollider3DComponent(it, Cuboid(Vec3(0.5f), Quat.identity, Vec3(1.0f)))
			)
		}

		val parts = Parts::class.java.declaredFields.map { it.get(null) }.filterIsInstance<GameObject>()

		fun createPart(name: String, createComponents: (GameObject) -> Array<Component>): GameObject {
			val o = GameObject(name)
			o.components.addAll(createComponents(o))
			return o
		}
	}
}