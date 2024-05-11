package com.pineypiney.sonder

import com.pineypiney.game_engine.audio.AudioSource
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowGameLogic
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import org.lwjgl.openal.AL10

abstract class SonderLogic(override val gameEngine: SonderEngine): WindowGameLogic() {

    override val renderer: SonderRenderer = SonderRenderer()
    override val window: SonderWindow get() = gameEngine.window

    var colour = Vec4(0f, 0f, 0f, 1f)

    val sounds: MutableSet<AudioSource> = mutableSetOf()

    override fun open() {
        super.open()
        AL10.alSourcePlayv(sounds.filter { it.state == AL10.AL_PAUSED }.map { it.ptr }.toIntArray())
    }

    open fun close(){
        AL10.alSourcePausev(sounds.map { it.ptr }.toIntArray())
    }

    override fun render(tickDelta: Double) {
        renderer.render(this, tickDelta)
    }

    open fun setKnM(){
        gameEngine.inputType = ControlType.KEYBOARD
        window.setCursorType(GLFW.GLFW_CURSOR_NORMAL)
    }

    open fun setGmP(){
        gameEngine.inputType = ControlType.GAMEPAD_BUTTON
        window.setCursorType(GLFW.GLFW_CURSOR_DISABLED)
    }

    override fun onInput(state: InputState, action: Int): Int {
        val type = when(state.controlType){
            ControlType.KEYBOARD, ControlType.MOUSE -> ControlType.KEYBOARD
            ControlType.GAMEPAD_BUTTON, ControlType.GAMEPAD_AXIS -> ControlType.GAMEPAD_BUTTON
        }
        if(type == ControlType.KEYBOARD && gameEngine.inputType == ControlType.GAMEPAD_BUTTON) {
            setKnM()
            return action
        }
        else if(type == ControlType.GAMEPAD_BUTTON && gameEngine.inputType == ControlType.KEYBOARD) {
            setGmP()
            return action
        }

        return super.onInput(state, action)
    }

    companion object{
        const val INTERRUPT = InteractorComponent.INTERRUPT
    }
}