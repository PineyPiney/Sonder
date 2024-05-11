package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.sonder.building.Building
import com.pineypiney.sonder.building.BuildingSurface
import com.pineypiney.sonder.building.PlacementRule
import com.pineypiney.sonder.building.parts.Parts.Companion.ppu
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class BuildingPart(parent: GameObject, id: String) : Component(parent, id) {

	abstract val placementRule: PlacementRule
	open val shape: SquareShape = VertexShape.footSquare

	val building get() = parent.getLineage().firstNotNullOfOrNull { it.getComponent<Building>() }

	fun setProperties(properties: Map<String, Any>){
		for((k, v) in properties){
			when(k){
				"sprite" -> setSprite(v as Texture)
				"sprites" -> setSprites((v as Map<*, *>).entries.associate { it.key as Texture to it.value as Vec3 })
				"surface" -> {
					v as Pair<*, *>
					setSurface(v.first as Vec2, v.second as Vec2)
				}
			}
		}
	}

	open fun setSprite(sprite: Texture){
		parent.components.add(SpriteComponent(parent, sprite, ppu, vShape = shape))
	}

	fun setSprites(sprites: Map<Texture, Vec3>){

		for((tex, vec) in sprites) {
			val component = SpriteComponent(GameObject("${parent.name}_sprite"), tex, ppu, vShape = shape).applied()
			parent.addChild(component.apply { parent.position = Vec3(vec.x / ppu, vec.y / ppu, vec.z) }.parent)
		}
	}

	fun setSurface(ori: Vec2, vec: Vec2){
		parent.components.add(BuildingSurface(parent, ori, vec))
	}

	fun getShape(shape: String) = when(shape){
		"foot" -> VertexShape.footSquare
		else -> VertexShape.centerSquareShape
	}
}