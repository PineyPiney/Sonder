package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.sonder.building.PlacementRule

class Plant(parent: GameObject) : BuildingPart(parent, "PNT") {
	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_SURFACE
}