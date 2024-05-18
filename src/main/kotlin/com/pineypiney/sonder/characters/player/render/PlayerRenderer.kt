package com.pineypiney.sonder.characters.player.render

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.game_objects.GameObject2D
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
import kotlin.math.floor

class PlayerRenderer : GameObject("player_renderer") {

	var flipY = false

	var hair = object : GameObject2D() {

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
		components.add(object : ColouredSpriteComponent(this@PlayerRenderer, TextureLoader[ResourceKey("characters/player/kitty/walk/walk_right")], 200f, Vec4(1f, 1f, 1f, 1f), playerShader){
			override fun setUniforms() {
				super.setUniforms()
				uniforms.setVec2Uniform("origin"){ Vec2(.25f * ((floor(Timer.frameTime * 4) % 4) + if(flipY) 1 else 0), 0f) }
				uniforms.setVec2Uniform("size"){ Vec2(if(flipY) -.25f else .25f, 1f) }
			}
		})
	}

	override fun addChildren() {
		//addChild(hair)
	}

	override fun init() {
		super.init()
		rotation = SonderGamePlay.isometricRotation
		scale = Vec3(.25f, 1f, 1f)
		hair.position = Vec3(0f, 2f, .1f)
	}

	fun getAnimation(velocityAngle: Float): Pair<Texture, Boolean>{
		val scaledAngle = velocityAngle / PI.toFloat()
		return walkMap.minBy { (a, _) -> abs((scaledAngle - a).wrap(-1f, 1f)) }.value
	}

	companion object{
		val playerShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/coloured_atlas_texture")]

		val walkMap = mapOf(
			.25f to (TextureLoader[ResourceKey("characters/player/kitty/walk/walk_right")] to false),
			0f to (TextureLoader[ResourceKey("characters/player/kitty/walk/walk_right_down")] to false),
			-.5f to (TextureLoader[ResourceKey("characters/player/kitty/walk/walk_right_down")] to true),
			-.75f to (TextureLoader[ResourceKey("characters/player/kitty/walk/walk_right")] to true),
		)
	}
}