package com.pineypiney.sonder.city.buildings

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.TextureMapsComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.extension_functions.lerp
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.SonderEngine
import glm_.f
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import kotlin.math.*

class SushiBar: GameObject("sushi"){

	fun angle() = Timer.frameTime.f * .5f // SonderEngine.INSTANCE.input.mouse.lastPos.angle()
	fun colour(angle: Float) = Vec3.fromHex(0xE5741D).lerp(Vec3.fromHex(0xE5E39B), angle)

	val normalMap = TextureLoader[ResourceKey("city/buildings/sushi_bar/sushi_normal")].apply { binding = 1 }
	val setter: SpriteComponent.() -> Unit = {
		uniforms.setVec3Uniform("ambient") {
			val ca = cos(angle())
			val modifier = max(ca, -.5f)
			val str = .35f + (.5f * modifier)
			colour(max(ca, 0f)) * str
		}
		uniforms.setVec3Uniform("lightDir") {
			val a = angle()
			val x = -sin(a) * sqrt(.5f)
			val y = -cos(angle())
			Vec3(x, y, -x).normalize()
		}
		uniforms.setVec3Uniform("lightColour") {
			val a = cos(angle())
			// Vary between 0 -- 1 between a = -.3 -- 1
			colour(a) * max(0f, sqrt((a + .3f) * .77f))
		}
		uniforms.setFloatUniform("alpha", ::getAlpha)
	}

	val leftEdge by lazy {
		getComponent<Collider3DComponent>()?.transformedBox?.let { it.center + (it.size * Vec3(-.5f, -.5f, .5f)) } ?: Vec3()
	}
	val rightEdge by lazy {
		getComponent<Collider3DComponent>()?.transformedBox?.let { it.center + (it.size * Vec3(.5f, -.5f, -.5f)) } ?: Vec3()
	}

	override fun addComponents() {
		super.addComponents()
		components.add(Collider3DComponent(this, Cuboid(Vec3(0f, 2.15f, 0f), Quat.identity, Vec3(2.1f, 4.3f, 2.1f))))
	}

	override fun addChildren() {
		super.addChildren()
		addChild(object : GameObject("sushi bar renderer"){
			override fun addComponents() {
				super.addComponents()
				val t = this
				components.add(object : SpriteComponent(t, TextureLoader[ResourceKey("city/buildings/sushi_bar/base")], 200f, ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]){
					override fun setUniforms() {
						super.setUniforms()
						setter()
					}
				})
				components.add(TextureMapsComponent(this, mapOf("normalMap" to normalMap)))
			}

			override fun addChildren() {
				super.addChildren()
				addChild(object : GameObject("sushi overlay"){
					override fun init() {
						super.init()
						position = Vec3(0f, 0f, .1f)
					}
					override fun addComponents() {
						super.addComponents()
						val t = this
						components.add(object : SpriteComponent(t, TextureLoader[ResourceKey("city/buildings/sushi_bar/overlay")], 200f, ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]){
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
				rotation = Quat(Vec3(atan(-1f / sqrt(2f)), PI * .25f, 0f))
			}
		})
		addChild(CollisionBox3DRenderer(this))
	}

	fun getAlpha(): Float{
		val playerObject = SonderEngine.INSTANCE.game.player
		val playerBox = playerObject.getComponent<Collider3DComponent>()?.transformedBox ?: Cuboid(playerObject.transformComponent.worldPosition, Quat.identity, Vec3(0f))
		val a = PI.toFloat() * .25f
		val left = leftEdge.xz.rotate(a)
		val right = rightEdge.xz.rotate(a)
		val playerLeft = playerBox.run { center + (size * Vec3(-.5f, .5f, .5f)) }.xz.rotate(a)
		val playerRight = playerBox.run { center + (size * Vec3(.5f, .5f, -.5f)) }.xz.rotate(a)

		return if(playerLeft.y < left.y){
			if(playerRight.x < left.x) max(0f, left.x - playerRight.x)
			else if(playerLeft.x > right.x) min(1f, playerLeft.x - right.x)
			else 0f
		}
		else{
			if(playerRight.x < left.x) min(1f, (left - playerRight).length())
			else if(playerLeft.x > right.x) min(1f, (playerLeft - right).length())
			else 1f//min(1f, playerLeft.y - left.y)
		}
	}
}