package com.pineypiney.sonder

import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import org.lwjgl.glfw.GLFW

class SonderWindow: Window("Sonder", 960, 540, false, false, hints) {
    override val input: Inputs = DefaultInput(this)

    fun setCursorType(type: Int){
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, type)
    }

    companion object{
        val hints = defaultHints + mapOf(
            GLFW.GLFW_CONTEXT_VERSION_MAJOR to 3,
            GLFW.GLFW_CONTEXT_VERSION_MINOR to 3,
        )

        val INSTANCE = SonderWindow()
    }
}