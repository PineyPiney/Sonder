package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.sonder.building.PlacementRule

class Pond(parent: GameObject) : BuildingPart(parent, "PND") {

	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_FLOOR
}