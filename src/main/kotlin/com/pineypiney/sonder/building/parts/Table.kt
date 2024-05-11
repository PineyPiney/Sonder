package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.sonder.building.PlacementRule

class Table(parent: GameObject): BuildingPart(parent, "TBL") {
	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_SURFACE
}