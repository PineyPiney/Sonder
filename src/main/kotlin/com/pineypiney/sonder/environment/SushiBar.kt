package com.pineypiney.sonder.environment

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.TextureMapsComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.SonderEngine
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import kotlin.math.*

class SushiBar: GameObject("sushi"){

	val normalMap = TextureLoader[ResourceKey("environment/sushi_bar/sushi_normal")].apply { binding = 1 }
	val setter: SpriteComponent.() -> Unit = {
		uniforms.setFloatUniform("ambient") { .5f }
		uniforms.setVec3Uniform("lightDir") { Vec3(.9 * cos(Timer.frameTime), -.4f, .9 * sin(Timer.frameTime)).normalize() }
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
		components.add(Collider3DComponent(this, Cuboid(Vec3(-.3f, 2.15f, .3f), Quat.identity, Vec3(2.1f, 4.3f, 2.1f))))
	}

	override fun addChildren() {
		super.addChildren()
		addChild(object : GameObject("sushi bar renderer"){
			override fun addComponents() {
				super.addComponents()
				val t = this
				components.add(object : SpriteComponent(t, TextureLoader[ResourceKey("environment/sushi_bar/base")], 200f, ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]){
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
						components.add(object : SpriteComponent(t, TextureLoader[ResourceKey("environment/sushi_bar/overlay")], 200f, ShaderLoader[ResourceKey("vertex/isometric"), ResourceKey("fragment/lit_isometric")]){
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
				position = Vec3(0f, -.25f, -0f)
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