package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.sonder.util.UtilObjects
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Building(parent: GameObject, val origin: Vec2, val size: Vec2) : Component(parent, "BLD") {
	override val fields: Array<Field<*>> = arrayOf(
		Vec2Field("ori", ::origin) { origin.put(it) },
		Vec2Field("siz", ::size) { size.put(it) },
	)

	val shape = SquareShape(origin, origin + size, Vec2(0f), size)

	val inside = GameObject("Inside").apply {
		components.addAll(
			BuildingSurface(this, Vec3(this@Building.origin), Vec2(this@Building.size.x, 0f)),
			MeshedTextureComponent(this, TextureLoader[ResourceKey("edit/walls/inside/wooden")], vShape = shape)
		)
		addChild(
			UtilObjects.barrier2D(Vec2(origin.x - 1f, origin.y), Vec2(1f, size.y)),
			UtilObjects.barrier2D(Vec2(origin.x + size.x, origin.y), Vec2(1f, size.y)),
			UtilObjects.barrier2D(Vec2(origin.x - 1f, origin.y + size.y), Vec2(size.x + 2f, 1f))
		)
		active = false
	}
	val outside = GameObject("Outside").apply {
		components.addAll(
			BuildingSurface(this, Vec3(this@Building.origin), Vec2(this@Building.size.x, 0f)),
			MeshedTextureComponent(this, TextureLoader[ResourceKey("edit/walls/outside/wooden")], vShape = shape)
		)
	}

	override fun init() {
		super.init()
		parent.addChild(inside, outside)
	}

	fun useDoor() {
		if (inside.active) {
			inside.active = false
			outside.active = true
		} else {
			inside.active = true
			outside.active = false
		}
	}

	fun getAllSurfaces() = parent.allActiveDescendants().mapNotNull { it.getComponent<BuildingSurface>() }

	companion object {
		fun make(origin: Vec2, size: Vec2): GameObject {
			return object : GameObject("Building") {
				override fun addComponents() {
					super.addComponents()
					components.add(Building(this, origin, size))
					//components.add(ColourRendererComponent(this, Vec4(0.6f, 0f, 0f, .2f), vShape = SquareShape(origin, origin + size, Vec2(0f), size)))
				}
			}
		}
	}
}