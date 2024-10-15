package com.pineypiney.sonder.scenes.menu

import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.sonder.SonderEngine
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class PauseMenu(engine: SonderEngine) : SonderMenu(engine) {

	private val continueButton = TextButton("Continue", Vec2(-.4f, .5f), Vec2(.8f, .3f)) { _, _ ->
		gameEngine.openGame(false)
	}

	private val exitButton = TextButton("Exit", Vec2(-.4f, -.8f), Vec2(.8f, .3f)) { _, _ ->
		gameEngine.setMenu(StartMenu(gameEngine))
		gameEngine.openMenu()
	}

	override fun addObjects() {
		add(continueButton)
		add(exitButton)
	}

	override fun onInput(state: InputState, action: Int): Int {
		super.onInput(state, action)
		if ((state.i == GLFW.GLFW_KEY_ESCAPE || state == InputState(
				GLFW.GLFW_GAMEPAD_BUTTON_B,
				ControlType.GAMEPAD_BUTTON
			)) && action == GLFW.GLFW_PRESS
		) {
			gameEngine.openGame(false)
			return INTERRUPT
		} else return action
	}
}