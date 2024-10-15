package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.rendering.IsoRendererComponent
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class SushiBarScene(engine: SonderEngine, val parent: SonderGamePlay) : SonderGamePlay(engine) {
	override val size: Vec2 = Vec2(1.05f)
	override val floor: Float = 0f


	override fun addObjects() {
		super.addObjects()

		add(object : GameObject("background renderer") {
			override fun addChildren() {
				super.addChildren()

				addChild(
					IsoRendererComponent(
						GameObject("walls"),
						TextureLoader[ResourceKey("$texLoc/walls")],
						200f,
						Vec2i(309, 175)
					).applied().parent.apply { position = Vec3(0f, 0f, -10f) })
				addChild(
					IsoRendererComponent(
						GameObject("decor"),
						TextureLoader[ResourceKey("$texLoc/decor")],
						200f,
						Vec2i(309, 175)
					).applied().parent.apply { position = Vec3(0f, 0f, -9.9f) })
				addChild(
					IsoRendererComponent(
						GameObject("bar"),
						TextureLoader[ResourceKey("$texLoc/bar")],
						200f,
						Vec2i(309, 175)
					).applied().parent.apply { position = Vec3(0f, 0f, -.4f) })
				addChild(
					IsoRendererComponent(
						GameObject("table"),
						TextureLoader[ResourceKey("$texLoc/table")],
						200f,
						Vec2i(309, 175)
					).applied().parent.apply { position = Vec3(0f, 0f, .25f) })
			}

			override fun init() {
				super.init()
				rotation = isometricRotation
			}
		})
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (super.onInput(state, action) == INTERRUPT) return INTERRUPT
		if (state.c == 'E' && action == 1 && (player.position - Vec3(.5f, .6f, -.5f)).length2() < 0.3f) {
			close()
			gameEngine.setGame(parent)
			gameEngine.openGame(false)
			return INTERRUPT
		}
		return action
	}

	companion object {
		val texLoc = "city/buildings/sushi_bar/inside"
	}
}