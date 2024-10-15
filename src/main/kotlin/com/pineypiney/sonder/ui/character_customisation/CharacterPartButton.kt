package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.menu_items.AbstractButton
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

open class CharacterPartButton(name: String, val ppu: Float) : AbstractButton(name) {

	var icon: Texture = Texture.broke

	var value: String = ""
	var onClick: (String) -> Unit = {}

	override val action = { _: ButtonComponent, _: Vec2 -> onClick(value) }

	open fun setBProperty(property: String, value: String) {
		when (property) {
			"icon" -> icon = TextureLoader[ResourceKey("ui/character_customisation/$value")]
		}
	}
}