package com.pineypiney.sonder.ui.phone

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.ui.phone.apps.App
import com.pineypiney.sonder.ui.phone.apps.BuildingApp
import com.pineypiney.sonder.ui.phone.apps.PhoneOrientation
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.floor

class PhoneScreen(parent: GameObject, val phone: Phone) : Component(parent, "PNS"), UpdatingComponent {
	override val fields: Array<Field<*>> = arrayOf()

	var app: App? = null

	val homeScreen = object : MenuItem() {
		override fun addChildren() {
			super.addChildren()

			val parentSize = this.parent?.renderer?.renderSize ?: Vec2(1f)
			val buttons = Array(7) {
				PhoneAppButton(
					it,
					if (it == 2) { _, _ -> openApp(::BuildingApp) } else { _, _ -> }).apply {
					position = Vec3(
						((.191f * (it % 4)) - .387f) * parentSize.x,
						(.569f - (.191f * floor(it * .25f))) * parentSize.x,
						.1f
					)
				}
			}
			addChild(*buttons)
		}
	}

	init {
		parent.components.add(
			SpriteComponent(
				parent,
				TextureLoader[ResourceKey("ui/phone/tight_screen")],
				Phone.ppu,
				ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture")]
			)
		)
	}

	override fun init() {
		super.init()
		val case = parent.parent?.getComponent<Phone>()?.case?.renderer?.renderSize ?: Vec2(1f)
		parent.position = Vec3(Vec2(-.098f, -.022) * case, 0f)
		parent.addChild(homeScreen)
	}

	fun openApp(app: (GameObject, Phone) -> App) {

		val a: App
		val o = GameObject().apply {
			position = Vec3(0f, 0f, .1f)
			a = app(this, phone)
			components.add(a)
			init()
		}

		this.app = a
		parent.addChild(o)
		a.startOpen()
	}

	// Called when the app has finished opening and completely controls the screen
	fun onAppOpened() {
		homeScreen.active = false
	}

	fun closeApp() {
		homeScreen.active = true

		app?.startClose()
		phone.orientation = PhoneOrientation.VERTICAL
	}

	// Called when the app has finished closing, used to delete the app
	fun onAppClosed() {
		app?.parent?.delete()
		parent.removeChild(app?.parent)
		app = null
	}

	override fun update(interval: Float) {

	}
}