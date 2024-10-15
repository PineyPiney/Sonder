package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.JoystickInteractableSelector
import com.pineypiney.sonder.util.MenuNode
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy
import kotlin.math.min

class CharacterPartSubMenu(
	val settings: CharacterPart.CharacterMenuSettings,
	val category: MenuNode,
	val onClick: (String) -> Unit = {}
) : MenuItem() {

	val columns = category["columns"]?.toInt() ?: settings.columns
	val rows = category["rows"]?.toInt() ?: settings.rows

	val selector = JoystickInteractableSelector { children.mapNotNull { it.getComponent<ButtonComponent>() } }

	override fun addChildren() {
		super.addChildren()
		createButtons()
	}

	override fun init() {
		super.init()

		val buttons = children.filter { it.hasComponent<ButtonComponent>() }
		buttons.forEachIndexed { i, button -> positionButton(button, i) }
	}

	private fun createButtons() {
		val buttonSpaces = columns * rows
		val numbuttons = min(buttonSpaces, category.children.size)

		val defaultButton: (String, Float) -> CharacterPartButton = getButton(category["button_prefab"] ?: "ColourButton")
		val defaultMessage = category["message"] ?: settings.defaultMessage

		for (j in 0..<numbuttons) {
			val buttonPrefab = defaultButton
			val buttonInfo = category.children[j]
			val button = buttonPrefab((buttonInfo["name"] ?: "Unnamed") + " Button", parent!!.getComponent<SpriteComponent>()!!.sprite.pixelsPerUnit)

			buttonInfo.tags.forEach(button::setBProperty)

			var message = buttonInfo["message"] ?: buttonInfo[defaultMessage] ?: ""

			// $ signs are references to other tags
			while (message.startsWith('$')) message = buttonInfo[message.substring(1)]!!

			button.value = message
			button.onClick = onClick

			addChild(button)
		}
	}

	private fun positionButton(button: GameObject, i: Int) {

		val column = i % columns
		val row = i / columns

		val buttonSize = button.scale.xy * button.renderer!!.renderSize
		val menuSize = parent!!.renderer!!.renderSize
		val padX = (menuSize.x - (settings.width * (columns - 1) + buttonSize.x)) / 2f
		val padY = (menuSize.y - (settings.height * (rows - 1) + buttonSize.y)) / 2f

		button.position =
			Vec3(padX + column * settings.width, menuSize.y - (padY + buttonSize.y + row * settings.height), 0f)
	}

	private fun getButton(string: String): (String, Float) -> CharacterPartButton =
		when (string) {
			"IconButton" -> ::IconButton
			"ColourButton" -> ::ColourButton
			else -> ::CharacterPartButton
		}
}