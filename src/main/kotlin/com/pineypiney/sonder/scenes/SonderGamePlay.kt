package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.Building
import com.pineypiney.sonder.building.ItemPlacer
import com.pineypiney.sonder.characters.player.RenderedPlayer
import com.pineypiney.sonder.ui.GamePadKeyLabel
import com.pineypiney.sonder.ui.phone.Phone
import com.pineypiney.sonder.util.UtilObjects
import com.pineypiney.sonder.util.dialogue.DialogueUtil
import glm_.func.common.abs
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

abstract class SonderGamePlay(engine: SonderEngine): SonderGameLogic(engine) {

    abstract val size: Float
    abstract val floor: Float

    override val player: RenderedPlayer = RenderedPlayer()

    //val buildMenu = BuildingBubble().apply { active = false }
    val placer by lazy { ItemPlacer(GameObject(), this).applied() }
    val grid = GameObject.simpleRenderedGameObject(ShaderLoader[ResourceKey("vertex/world_pos"), ResourceKey("fragment/grid")]){
        uniforms.setFloatUniform("cellSize"){ .5f }
        uniforms.setFloatUniform("thickness"){ .005f }
        uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 0f, .6f) }
    }.apply { active = false }
    val phone = object : MenuItem(){

        override fun addComponents() {
            super.addComponents()
            components.add(Phone(this, this@SonderGamePlay))
        }
    }

    override fun addObjects() {
        val barriers = arrayOf(
            UtilObjects.barrier(Vec2(-(size + 1f), -5f), Vec2(1f, 10f)),
            UtilObjects.barrier(Vec2(size, -5f), Vec2(1f, 10f)),
            UtilObjects.barrier(Vec2(-size, -10f), Vec2(size * 2f, 5 + floor)),
        )

        add(player, *barriers)
        grid.scale = Vec3(size * 2, 10f, 1f)
        add(placer.parent, grid, phone)

        if(gameEngine.inputType == ControlType.GAMEPAD_BUTTON) {
            val label = GamePadKeyLabel(
                input.gamepad.connectedGamepads.first(),
                ControlType.GAMEPAD_BUTTON,
                GLFW.GLFW_GAMEPAD_BUTTON_Y,
                "Edit Character"
            )
            label.position = Vec3(.5f, -.9f, 0f)
            add(label)
        }
    }

    override fun render(tickDelta: Double) {
        if(player.velocity2D.x.abs > 0.1f) {
            if (player.character!!.animation.name == "idle") {
                player.character!!.setAnimation("walk")
            }
        }
        else if(player.character!!.animation.name == "walk"){
            player.character!!.setAnimation("idle")
        }

        super.render(tickDelta)
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == INTERRUPT) return INTERRUPT

        when(action){
            GLFW.GLFW_PRESS -> {
                when (state.removeMods()) {
                    InputState('K'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_Y, ControlType.GAMEPAD_BUTTON) -> {
                        gameEngine.setGame(CharacterCustomisation(gameEngine, this), false)
                        gameEngine.openGame()
                        return INTERRUPT
                    }

                    InputState('Q'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, ControlType.GAMEPAD_BUTTON) -> {
                        phone.getComponent<Phone>()?.let { if(it.open) it.close() else it.open() }
                    }

                    InputState('B'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, ControlType.GAMEPAD_BUTTON) -> {
                        setBuildMode(!player.buildMode)
                    }

                    InputState(' '), InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) -> {
                        if (player.getComponent<ColliderComponent>()
                                ?.isGrounded() == true && !player.buildMode
                        ) player.velocity2D = Vec2(player.velocity2D.x, 9f)
                    }

                    InputState('R') -> {
                        player.position2D = Vec2(-35f, 0f)
                        player.velocity2D = Vec2(0f)
                    }

                    InputState('G') -> {
                        DialogueUtil.setDialogue(player.character!!, "Hello my namem is PHILMNIP!")
                    }
                }
            }
        }

        return action
    }

    override fun onScroll(scrollDelta: Vec2): Int {
        val r = super.onScroll(scrollDelta)
        if(r == INTERRUPT) return r

        if(player.buildMode) renderer.camera.increaseHeight(-2f * scrollDelta.y)
        return 0
    }

    override fun update(interval: Float, input: Inputs) {
        super.update(interval, input)

        val move = Vec2()
        if(gameEngine.inputType == ControlType.KEYBOARD) {
            if (player.buildMode) {
                if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_W) == 1) move += Vec2(0, 1)
                if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_S) == 1) move += Vec2(0, -1)
            }
            if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_D) == 1) move += Vec2(1, 0)
            if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_A) == 1) move += Vec2(-1, 0)
            move *= (1f + GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT))
        }
        else {
            val pad = input.gamepad.connectedGamepads.first()
            if (player.buildMode) {
                val y = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y]
                if(y.abs > pad.deadzone.x) move += Vec2(0, -y)
            }
            val x = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X]
            if(x.abs > pad.deadzone.x) move += Vec2(x, 0)
            move *= (1.5f + pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER] * .5f)
        }

        if(player.buildMode) {
            renderer.camera.translate(Vec2(move * interval * renderer.camera.zoom))
        }
        else {
            player.velocity2D = Vec2(move.x * 4f, player.velocity2D.y)
            renderer.camera.setPos(Vec3(player.position2D))
        }
    }

    fun setBuildMode(value: Boolean){
        player.buildMode = value
        player.velocity.x = 0f
        //buildMenu.active = value
        grid.active = value
        if(!value) renderer.camera.setZoom(10f)
    }

    fun getHoveredBuilding(): Building?{
        val buildings = gameObjects.getAllComponentInstances<Building>(0)
        return buildings.firstOrNull {
            val scale = Vec2(it.parent.transformComponent.worldScale)
            renderer.camera.screenToWorld(input.mouse.lastPos).isWithin(Vec2(it.parent.transformComponent.worldPosition) + (it.origin * scale), it.size * scale)
        }
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)

        renderer.camera.updateBounds(Vec2(size, 5f))
    }
}