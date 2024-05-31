package com.pineypiney.sonder.characters.player.render

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.wrap
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor

class PlayerRenderer : GameObject("player_renderer") {

	var flipY = false
	var frames = 9
	var invFrames = 1f / frames
	var scaledAngle = 0f
	var animationSpeed = 1f

	var hair = object : GameObject() {

		override var name: String = "hair"

		val texture = ColouredSpriteComponent(this, TextureLoader[ResourceKey("characters/player/hair/hair1")], 600f, Vec4(0.2f, 0.08f, 0.02f, 1f), playerShader)

		override fun addComponents() {
			super.addComponents()
			components.add(texture)
		}
	}

	var skinTone
		get() = Vec3(getComponent<ColouredSpriteComponent>()!!.tint())
		set(value) {
			getComponent<ColouredSpriteComponent>()!!.tint = { Vec4(value, 1f) }
		}
	var hairColour
		get() = Vec3(hair.getComponent<ColouredSpriteComponent>()!!.tint())
		set(value) {
			hair.getComponent<ColouredSpriteComponent>()!!.tint = { Vec4(value, 1f) }
		}
	var hairStyle
		get() = hair.getComponent<SpriteComponent>()!!.texture.fileLocation.removePrefix("characters\\player\\hair\\")
			.substringBeforeLast('.')
		set(value) {
			hair.getComponent<SpriteComponent>()!!.texture = TextureLoader[ResourceKey(value)]
		}

	override fun addComponents() {
		super.addComponents()
		components.add(object : ColouredSpriteComponent(this@PlayerRenderer, TextureLoader[ResourceKey("characters/player/piccolo/walk/walk_right")], 200f, Vec4(1f, 1f, 1f, 1f), playerShader){
			override fun setUniforms() {
				super.setUniforms()
				uniforms.setVec2Uniform("origin"){ Vec2(invFrames * ((floor(Timer.frameTime * animationSpeed * frames) % frames) + if(flipY) 1 else 0), 0f) }
				uniforms.setVec2Uniform("size"){ Vec2(if(flipY) -invFrames else invFrames, 1f) }
			}
		})
	}

	override fun addChildren() {
		//addChild(hair)
	}

	override fun init() {
		super.init()
		rotation = SonderGamePlay.isometricRotation
		scale = Vec3(invFrames, 1f, 1f)
		hair.position = Vec3(0f, 2f, .1f)
	}

	fun updateAnimation(velocity: Vec3): Walk{
		val speed = velocity.length()
		if(speed > .1f) {
			val velocityAngle = velocity.run { atan2(-z, x) }
			scaledAngle = velocityAngle / PI.toFloat()
			return walkMap.minBy { (a, _, _, w) -> abs((scaledAngle - a).wrap(-1f, 1f) * w) }
		}
		else{
			return idleMap.minBy { (a, _, _, w) -> abs((scaledAngle - a).wrap(-1f, 1f) * w) }
		}
	}

	companion object{
		val playerShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/coloured_atlas_texture")]

		val walkMap = listOf(
			Walk(.75f, "walk/walk_up", 9, 2f),
			Walk(.25f, "walk/walk_right", 9, 1f),
			Walk(-.25f, "walk/walk_down", 9, 2f),
			Walk(-.75f, "walk/walk_left", 9, 1f),
		)

		val idleMap = listOf(
			Walk(.75f, "idle/idle_up", 4, 2f),
			Walk(.25f, "idle/idle_right", 4, 1f),
			Walk(-.25f, "idle/idle_down", 4, 2f),
			Walk(-.75f, "idle/idle_right", 4, 1f, 1f, true),
		)
	}

	data class Walk(val dir: Float, val tex: Texture, val frames: Int, val invWeight: Float, val speed: Float = 1f, val mirror: Boolean = false){
		constructor(dir: Float, tex: String, frames: Int, invWeight: Float, speed: Float = 1f, mirror: Boolean = false): this(dir, TextureLoader[ResourceKey("characters/player/piccolo/$tex")], frames, invWeight, speed, mirror)
	}
}