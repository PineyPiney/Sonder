package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import glm_.parseInt
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ColourButton(name: String, ppu: Float) : CharacterPartButton(name, ppu) {

	var colour = Vec3(1f)

	init {
		icon = TextureLoader[ResourceKey("ui/character_customisation/colour")]
	}

	override fun addComponents() {
		super.addComponents()
		components.add(
			ColouredSpriteComponent(
				this,
				Sprite(icon, ppu),
				{ Vec4(colour, 1f) },
				buttonShader
			) {
				uniforms.setBoolUniform("selected") { getComponent<ButtonComponent>()!!.hover }
			})
	}

	override fun setBProperty(property: String, value: String) {
		super.setBProperty(property, value)
		when (property) {
			"colour" -> setColour(value)
		}
	}

	fun setColour(colourString: String) {
		colour = Vec3.fromHex(colourString.parseInt(16))
	}

	companion object {
		val buttonShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/coloured_texture_button")]
	}
}