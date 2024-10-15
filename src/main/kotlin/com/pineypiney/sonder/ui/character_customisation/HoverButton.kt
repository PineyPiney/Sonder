package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.HoverComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2

class HoverButton(
	var icon: Texture,
	val onEnter: (HoverComponent) -> Unit = {},
	val onExit: (HoverComponent) -> Unit = {}
) : MenuItem() {

	var selector: CharacterPartSubMenu? = null

	override fun addComponents() {
		super.addComponents()
		components.add(SpriteComponent(this, Sprite(icon, 1200f, Vec2(0f)), SpriteComponent.menuShader))
		components.add(HoverComponent(this@HoverButton, onEnter, onExit))
	}
}