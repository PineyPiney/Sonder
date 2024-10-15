package com.pineypiney.sonder.characters.npcs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.GameClickerComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.sonder.characters.Character
import com.pineypiney.sonder.util.dialogue.DialogueUtil
import glm_.vec2.Vec2

class Jonathon : GameObject() {

	override var name: String = "Guide"

	override fun addComponents() {
		super.addComponents()
		components.add(Collider2DComponent(this, Rect2D(Vec2(-1.5f, -1.8f), Vec2(3f, 3.6f))))
		components.add(SpriteComponent(this, TextureLoader[ResourceKey("characters/red_panda/idle_0")], 400f))
		val idle = Animation("idle", 6f, "characters/red_panda", (0..4).map { "idle_$it" }, "characters/jonathon/idle")
		components.add(
			AnimatedComponent(
				this, idle, listOf(
					idle,
					Animation(
						"sitting_idle",
						6f,
						"characters/red_panda",
						(0..2).map { "sitting_idle_$it" },
						"characters/jonathon/sitting_idle"
					),
					Animation(
						"sitting_talk",
						6f,
						"characters/red_panda",
						(0..2).map { "sitting_talking_$it" },
						"characters/jonathon/sitting_talking"
					),
					Animation(
						"talking",
						6f,
						"characters/red_panda",
						(0..2).map { "talking_$it" },
						"characters/jonathon/talking"
					),
					Animation(
						"walking",
						6f,
						"characters/red_panda",
						(0..3).map { "walking_$it" },
						"characters/jonathon/walking"
					),
				)
			)
		)

		components.add(object : Character(this) {})
		components.add(GameClickerComponent(this, { DialogueUtil.setScript("Guide Introduction") }))
	}

	override fun addChildren() {
		addChild(CollisionBox2DRenderer(this))
	}
}