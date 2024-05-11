package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component

class Parts {

	companion object {
		val ppu = 400f

		val CHAIR = createPart("Chair") { arrayOf(
			Plant(it),
		)}
		val PLANT = createPart("Plant") { arrayOf(
			Plant(it),
		)}
		val DIY_TABLE = createPart("Table") { arrayOf(
			Table(it),
		)}

		val DOOR = createPart("Door") { arrayOf(
			Door(it),
		)}
		val WALL = createPart("Wall") { arrayOf(
			Plant(it),
		)}
		val WINDOW = createPart("Window") { arrayOf(
			Window(it),
		)}

		val parts = Parts::class.java.declaredFields.map { it.get(null) }.filterIsInstance<GameObject>()

		fun createPart(name: String, createComponents: (GameObject) -> Array<Component>): GameObject{
			val o = GameObject(name)
			o.components.addAll(createComponents(o))
			return o
		}
	}
}