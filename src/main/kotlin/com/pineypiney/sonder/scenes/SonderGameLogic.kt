package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.SonderLogic
import com.pineypiney.sonder.characters.player.RenderedPlayer
import com.pineypiney.sonder.scenes.menu.PauseMenu
import org.lwjgl.glfw.GLFW

abstract class SonderGameLogic(engine: SonderEngine): SonderLogic(engine) {

    abstract val player: RenderedPlayer

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == INTERRUPT) return INTERRUPT

        when(action){
            GLFW.GLFW_PRESS -> {
                when(state.removeMods()){
                    InputState(GLFW.GLFW_KEY_ESCAPE), InputState(GLFW.GLFW_GAMEPAD_BUTTON_START, ControlType.GAMEPAD_BUTTON) -> {
                        gameEngine.setMenu(PauseMenu(gameEngine))
                        gameEngine.openMenu()
                        return INTERRUPT
                    }
                }
            }
        }

        return action
    }
}