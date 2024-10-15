package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.resources.textures.Sprite
import glm_.vec2.Vec2

class IconButton(name: String, ppu: Float) : CharacterPartButton(name, ppu) {

	override fun addComponents() {
		super.addComponents()
		components.add(SpriteComponent(this, Sprite(icon, ppu, Vec2()), SpriteComponent.menuShader))
	}
}