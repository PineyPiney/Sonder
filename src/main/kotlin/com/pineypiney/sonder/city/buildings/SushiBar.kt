package com.pineypiney.sonder.city.buildings

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.TextureMapsComponent
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.extension_functions.lerp
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.player.PlayerObject
import com.pineypiney.sonder.scenes.SonderGamePlay
import com.pineypiney.sonder.scenes.SushiBarScene
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.*

class SushiBar : Building("sushi") {

	fun angle() = Timer.frameTime.f * .5f // SonderEngine.INSTANCE.input.mouse.lastPos.angle()
	fun colour(angle: Float) = Vec3.fromHex(0xE5741D).lerp(Vec3.fromHex(0xE5E39B), angle)

	val normalMap = TextureLoader[ResourceKey("city/buildings/sushi_bar/sushi_normal")].apply { binding = 1 }
	val setter: SpriteComponent.() -> Unit = {
		uniforms.setVec3Uniform("ambient") {
			val ca = cos(angle())
			val modifier = max(ca * .1f, -.05f)
			val str = .2f + modifier
			colour(max(ca, 0f)) * str
		}
		uniforms.setVec3Uniform("lightDir") {
			val a = angle()
			val x = -sin(a) * sqrt(.5f)
			val y = -cos(a)
			Vec3(x, y, -x).normalize()
		}
		uniforms.setVec3Uniform("lightColour") {
			val a = cos(angle())
			// Vary between 0 -- 1.5 between a = -.3 -- 1
			colour(max(a, 0f)) * sqrt(max(0f, (a + .3f) * 1.73f))
		}
		uniforms.setFloatUniform("alpha", ::getAlpha)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(BoxCollider3DComponent(this, Cuboid(Vec3(0f, 2.15f, 0f), Quat.identity, Vec3(2.1f, 4.3f, 2.1f))))
		components.add(object : DefaultInteractorComponent(this@SushiBar, "DOR") {
			override fun shouldInteract(): Boolean {
				return (PlayerObject.INSTANCE.position + Vec3(-1.05f, -.6f, -.5f) - position).length2() < .7f
			}

			override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
				if (super.onInput(window, input, action, cursorPos) == INTERRUPT) return INTERRUPT
				else if (input.c == 'E' && action == 1) {
					(SonderEngine.INSTANCE.game as? SonderGamePlay)?.let {
						it.gameEngine.setGame(SushiBarScene(it.gameEngine, it), false)
						it.gameEngine.openGame()
					}
					return INTERRUPT
				}
				return action
			}
		})
	}

	override fun addChildren() {
		super.addChildren()
		addChild(object : GameObject("sushi bar renderer") {
			override fun addComponents() {
				super.addComponents()
				val t = this
				components.add(object : SpriteComponent(
					t,
					TextureLoader[ResourceKey("city/buildings/sushi_bar/base")],
					200f,
					ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]
				) {
					override fun setUniforms() {
						super.setUniforms()
						setter()
					}
				})
				components.add(TextureMapsComponent(this, mapOf("normalMap" to normalMap)))
			}

			override fun addChildren() {
				super.addChildren()
				addChild(object : GameObject("sushi overlay") {
					override fun init() {
						super.init()
						position = Vec3(0f, 0f, .1f)
					}

					override fun addComponents() {
						super.addComponents()
						val t = this
						components.add(object : SpriteComponent(
							t,
							TextureLoader[ResourceKey("city/buildings/sushi_bar/overlay")],
							200f,
							ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]
						) {
							override fun setUniforms() {
								super.setUniforms()
								setter()
							}
						})
						components.add(TextureMapsComponent(this, mapOf("normalMap" to normalMap)))
					}
				})
			}

			override fun init() {
				super.init()
				position = Vec3(-0.25f, 1.9f, -0.3f)
				rotation = SonderGamePlay.isometricRotation
			}
		})
		addChild(CollisionBox3DRenderer(this))
	}
}