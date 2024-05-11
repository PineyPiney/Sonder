package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class PlacementRule(val rule: (obj: GameObject, scene: SonderGamePlay, position: Vec2) -> Unit) {

	operator fun invoke(obj: GameObject, scene: SonderGamePlay, position: Vec2) = rule(obj, scene, position)

	companion object {
		val PLACE_ON_SURFACE = PlacementRule { obj, scene, position ->
			val house = scene.getHoveredBuilding() ?: return@PlacementRule

			val surfaces = house.getAllSurfaces().filter { it.parent != obj }
			val distances = surfaces.associateWith { it.line vectorTo (position - Vec2(it.parent.transformComponent.worldPosition))  }
			val (surface, distance) = distances.minBy { it.value.length2() }
			if(obj.parent != surface.parent) surface.parent.addChild(obj)
			obj.position = Vec3((position - Vec2(surface.parent.transformComponent.worldPosition)) - distance, .1f)
		}

		val PLACE_ON_WALL = PlacementRule { obj, scene, position ->
			val house = scene.getHoveredBuilding() ?: return@PlacementRule

			val bounds = obj.renderer?.renderSize?.times(.5f) ?: Vec2(.5f)
			if(obj.parent != house.parent) house.parent.addChild(obj)
			obj.position = Vec3((position - Vec2(house.parent.transformComponent.worldPosition)).coerceIn(house.origin + bounds, house.origin + house.size - bounds), .1f)
		}
	}
}