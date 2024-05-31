package com.pineypiney.sonder.scenes.menu

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.util.JoystickInteractableSelector
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.SonderLogic
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

abstract class SonderMenu(engine: SonderEngine) : SonderLogic(engine){

	var selector = JoystickInteractableSelector{ gameObjects.getAllComponentInstances<ButtonComponent>() }
	init {
		colour = Vec4(0.2f, .5f, .8f, 1f)
	}

	override fun open() {
		super.open()
		if(gameEngine.inputType == ControlType.GAMEPAD_BUTTON) selector.selectFirstButton()
	}

	override fun setGmP() {
		super.setGmP()
		selector.selectFirstButton()
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == INTERRUPT) return INTERRUPT

		val d = when(state.controlType){
			ControlType.GAMEPAD_BUTTON -> {
				if (action == 1) {
					when (state.i) {
						in GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP..GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT -> input.gamepad.connectedGamepads.first().dPad
						else -> Vec2(0, 0)
					}
				}
				else Vec2(0, 0)
			}
			ControlType.GAMEPAD_AXIS -> {
				if(action != 0) {
					when (state.i) {
						GLFW.GLFW_GAMEPAD_AXIS_LEFT_X, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y -> input.gamepad.connectedGamepads.first().leftJoystick.normalize()
						else -> Vec2(0, 0)
					}
				}
				else Vec2(0, 0)
			}
			else -> Vec2(0, 0)
		}

		if (d != Vec2(0, 0)) {
			selector.selectButton(d)
		}

		return action
	}
}