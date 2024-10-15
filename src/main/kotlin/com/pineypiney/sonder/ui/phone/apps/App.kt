package com.pineypiney.sonder.ui.phone.apps

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.ui.phone.Phone
import glm_.and
import glm_.b
import glm_.f
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

abstract class App(parent: GameObject, val phone: Phone) : DefaultInteractorComponent(parent, "APP"),
	UpdatingComponent {

	override val fields: Array<Field<*>> = arrayOf()
	abstract val orientation: PhoneOrientation
	abstract val colour: Vec3

	/** The set bit determines the open/close state of the app.
	 * - 0 - Unopened
	 * - 1 - Opening
	 * - 2 - Open
	 * - 4 - Closing
	 * - 8 - Closed
	 */
	var state = 0.b
	var appOpenTime = Float.MAX_VALUE

	val background = object : SpriteComponent(
		MenuItem(),
		phone.screen.parent.getComponent<SpriteComponent>()!!.sprite.texture,
		Phone.ppu,
		ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/phone/app_background")]
	) {
		override fun setUniforms() {
			super.setUniforms()
			uniforms.setVec3Uniform("colour", ::colour)
			uniforms.setFloatUniform("fill", ::fill)
		}
	}.applied()

	override fun init() {
		super.init()
		parent.addChild(background.parent)
	}

	open fun startOpen() {
		phone.orientation = orientation
		state = 1
		appOpenTime = Timer.time.f
	}

	open fun endOpen() {
		state = 2
		phone.screen.onAppOpened()
	}

	open fun startClose() {
		state = 4
		appOpenTime = -Timer.time.f
	}

	open fun endClose() {
		state = 8
		phone.screen.onAppClosed()
	}

	override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
		if (super.onInput(window, input, action, cursorPos) == INTERRUPT) return INTERRUPT
		if (action == 1) {
			when (input) {
				InputState(GLFW.GLFW_KEY_ESCAPE), InputState(
					GLFW.GLFW_GAMEPAD_BUTTON_B,
					ControlType.GAMEPAD_BUTTON
				) -> {
					if (state and 2 > 0) {
						phone.screen.closeApp()
						return INTERRUPT
					}
				}
			}
		}
		return action
	}

	override fun update(interval: Float) {
		if (state and 1 > 0 && fill() == 1f) endOpen()
		else if (state and 4 > 0 && fill() == 0f) endClose()
	}

	override fun shouldInteract(): Boolean {
		return true
	}

	fun fill(): Float {
		return (if (appOpenTime > 0) (Timer.frameTime - appOpenTime) * Phone.orientationSpeed else 1.0 - ((Timer.frameTime + appOpenTime) * Phone.orientationSpeed)).coerceIn(
			0.0,
			1.0
		).toFloat()
	}
}