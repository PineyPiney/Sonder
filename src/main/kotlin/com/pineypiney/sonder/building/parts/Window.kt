package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.sonder.building.PlacementRule

class Window(parent: GameObject): BuildingPart(parent, "WND") {
	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_WALL

	override val shape: SquareShape = VertexShape.centerSquareShape
}