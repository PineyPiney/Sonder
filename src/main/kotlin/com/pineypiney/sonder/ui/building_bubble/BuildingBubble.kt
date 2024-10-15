package com.pineypiney.sonder.ui.building_bubble

import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.sonder.ui.character_customisation.CharacterPart

class BuildingBubble : MenuItem() {

	val sprite = SpriteComponent(this, CharacterPart.backgroundTexture, ppu, SpriteComponent.menuShader)

	override fun addComponents() {
		super.addComponents()
		components.add(sprite)
		components.add(BuildingBubbleComponent(this))
	}

	companion object {
		val ppu = 2000f
	}
}