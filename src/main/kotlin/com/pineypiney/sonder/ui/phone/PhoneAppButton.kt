package com.pineypiney.sonder.ui.phone

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import kotlin.math.floor

class PhoneAppButton(val icon: Int, val action: (ButtonComponent, Vec2) -> Unit) : MenuItem() {

	override fun addComponents() {
		super.addComponents()

		val bl = BUTTON_OFFSET + (BUTTON_SIZE * Vec2(icon % 4, -floor(icon * .25f)))
		components.add(
			SpriteComponent(
				this,
				Sprite(BUTTON_TEXTURE, Phone.ppu, Vec2(.5f), BUTTON_OFFSET, BUTTON_SIZE),
				SpriteComponent.menuShader
			)
		)
		components.add(ButtonComponent(this, action))
	}

	companion object {
		val BUTTON_TEXTURE = TextureLoader[ResourceKey("ui/phone/apps_map")]
		val BUTTON_SIZE = Vec2(113f, 109f) / BUTTON_TEXTURE.size
		val BUTTON_OFFSET = Vec2(0f, 353) / BUTTON_TEXTURE.size
	}
}