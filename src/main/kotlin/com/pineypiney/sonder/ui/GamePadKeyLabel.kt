package com.pineypiney.sonder.ui

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.GamePad
import com.pineypiney.sonder.SonderWindow
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.swizzle.xy
import glm_.vec4.swizzle.zw

class GamePadKeyLabel(val controller: GamePad, val type: ControlType, val button: Int, val desc: String) : MenuItem() {

	val icon: MenuItem
	val text: GameObject

	init {
		val width = 1f / SonderWindow.INSTANCE.aspectRatio

		icon = object : MenuItem() {

			override fun addComponents() {
				super.addComponents()
				val (texture, bounds) = controller.getButtonIcon(type, button)
				components.add(
					MeshedTextureComponent(
						this,
						texture,
						SpriteComponent.menuShader,
						SquareShape(Vec2(0f, -.5f), Vec2(1f, .5f), bounds.xy, bounds.xy + bounds.zw)
					)
				)
			}

			override fun init() {
				super.init()
				scale = Vec3(width * .2f, 1f, 1f)
			}
		}

		text = Text.makeMenuText(desc, alignment = Text.ALIGN_CENTER_LEFT)
	}

	override fun addChildren() {
		super.addChildren()
		addChild(icon, text)
	}

	override fun init() {
		super.init()
		scale = Vec3(.5f, .1f, 1f)
		text.position = Vec3(icon.scale.x * 1.25f, 0f, 0f)
	}
}