package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.roundToInt

class PlacementRule(val rule: (obj: GameObject, scene: SonderGamePlay, ray: Ray) -> Unit) {

	operator fun invoke(obj: GameObject, scene: SonderGamePlay, ray: Ray) = rule(obj, scene, ray)

	companion object {
		val PLACE_ON_SURFACE = PlacementRule { obj, scene, ray ->
			val house = scene.getHoveredBuilding() ?: return@PlacementRule

			val surfaces = house.getAllSurfaces().filter { it.parent != obj }
			val intersections =
				surfaces.associateWith { ((it.rect transformedBy it.parent.worldModel) intersectedBy ray).firstOrNull() }
					.removeNullValues()
			val (surface, distance) = intersections.minByOrNull { (it.value - ray.rayOrigin).length2() }
				?: return@PlacementRule
			if (obj.parent != surface.parent) surface.parent.addChild(obj)
			obj.position = surface.rect.origin + distance
		}

		val PLACE_ON_WALL = PlacementRule { obj, scene, ray ->

			val position = Vec2(ray.rayOrigin)
			val house = scene.getHoveredBuilding() ?: return@PlacementRule

			val bounds = obj.renderer?.renderSize?.times(.5f) ?: Vec2(.5f)
			if (obj.parent != house.parent) house.parent.addChild(obj)
			obj.position = Vec3(
				(position - Vec2(house.parent.transformComponent.worldPosition)).coerceIn(
					house.origin + bounds,
					house.origin + house.size - bounds
				), .1f
			)
		}

		val PLACE_ON_FLOOR = PlacementRule { obj, scene, ray ->
			val rayMult = ray.rayOrigin.y / ray.direction.y
			val pos = ray.rayOrigin - (ray.direction * rayMult)

			if (obj.objects == null) scene.add(obj)
			obj.position = pos
		}

		val PLACE_TILED = PlacementRule { obj, scene, ray ->
			val rayMult = ray.rayOrigin.y / ray.direction.y
			val pos = ray.rayOrigin - (ray.direction * rayMult)

			if (obj.objects == null) scene.add(obj)
			obj.position = Vec3((pos.x - .5f).roundToInt(), 0f, (pos.z - .5f).roundToInt())
		}
	}
}