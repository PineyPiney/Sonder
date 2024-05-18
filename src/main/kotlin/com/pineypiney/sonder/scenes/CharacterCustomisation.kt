package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.components.HoverComponent
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.player.PlayerObject
import com.pineypiney.sonder.ui.GamePadKeyLabel
import com.pineypiney.sonder.ui.character_customisation.CharacterPart
import com.pineypiney.sonder.ui.character_customisation.CharacterPartComponent
import com.pineypiney.sonder.ui.character_customisation.CharacterPartSubMenu
import com.pineypiney.sonder.ui.character_customisation.HoverButton
import glm_.parseInt
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class CharacterCustomisation(engine: SonderEngine, val parent: SonderGameLogic) : SonderGameLogic(engine) {

    override val player = PlayerObject()

    val parts get() = gameObjects.getAllComponentInstances<CharacterPartComponent>().toList()

    override fun addObjects() {
        add(player)
        add(CharacterPart("skin_tones", Vec2(-0.9f, 0.3f), Vec2(0.9f)){ player.renderChild.skinTone = (Vec3.fromHex(it.parseInt(16))) })
        add(CharacterPart("hair_colours", Vec2(0.4f, 0.3f), Vec2(0.9f)){ player.renderChild.hairColour = (Vec3.fromHex(it.parseInt(16))) })
        add(CharacterPart("hair_styles", Vec2(0.4f, -0.4f), Vec2(0.9f)){ player.renderChild.hairStyle = "characters/player/hair/$it" })
        when(gameEngine.inputType){
            ControlType.KEYBOARD -> add(
                TextButton("Done", Vec2(0.5f, -0.9f), Vec2(0.4f, 0.2f)){
                    saveAndExit()
                }
            )
            ControlType.GAMEPAD_BUTTON -> {
                val label = GamePadKeyLabel(input.gamepad.connectedGamepads.first(), ControlType.GAMEPAD_BUTTON, GLFW.GLFW_GAMEPAD_BUTTON_X, "Save")
                label.position = Vec3(.5f, -.9f, 0f)
                add(label)
            }
            else -> {}
        }
    }

    override fun init() {
        super.init()
        parent.player copyDetailsTo player
        player.player?.gravity = false
    }

    override fun open() {
        super.open()
        if(gameEngine.inputType == ControlType.GAMEPAD_BUTTON){
            selectPart(0)
        }
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == INTERRUPT) return INTERRUPT

        when(state.controlType) {
            ControlType.GAMEPAD_AXIS -> {
                when (state.i) {
                    GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER -> if (action == 1) selectPart(-1)
                    GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER -> if (action == 1) selectPart(1)
                    GLFW.GLFW_GAMEPAD_AXIS_LEFT_X, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y -> {
                        if (action != 0) {
                            val d = input.gamepad.connectedGamepads.first().leftJoystick.normalize()
                            if (d != Vec2()) selectButton(d)
                        }
                    }
                }
            }

            ControlType.GAMEPAD_BUTTON -> {
                if(action == 1) {
                    when (state.i) {
                        GLFW.GLFW_GAMEPAD_BUTTON_X -> saveAndExit()
                        in GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP..GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT -> {
                            val d = input.gamepad.connectedGamepads.first().dPad.normalize()
                            if (d != Vec2()) selectButton(d)
                        }
                    }
                }
            }
            else -> {}
        }

        return action
    }

    fun saveAndExit(){
        gameEngine.setGame(parent)
        player copyDetailsTo parent.player
        gameEngine.openGame(false)
    }

    fun selectPart(d: Int) {
        val parts = this.parts
        if (parts.isNotEmpty()) {
            val selectedPart = parts.firstOrNull { it.parent.getComponent<HoverComponent>()?.shouldUpdate() ?: false }
            if (selectedPart == null) {
                selectPart(parts.first())
            }
            else if (parts.size > 1) {

                val i = parts.indexOf(selectedPart)
                val nextPart = parts[(i + parts.size + d) % parts.size]
                selectedPart.getSelected()?.getComponent<HoverComponent>()?.let{ unselectCategory(it) }
                selectPart(nextPart)
            }
        }
    }

    fun selectPart(part: CharacterPartComponent){
        selectCategory(part.parent.children.filterIsInstance<HoverButton>().first().getComponent<HoverComponent>()!!)
    }

    fun selectCategory(part: HoverComponent){
        part.hover = true
        part.onEnter(part)
        part.parent.parent!!.children.filterIsInstance<CharacterPartSubMenu>().first().selector.selectFirstButton()
    }
    fun unselectCategory(part: HoverComponent){
        part.hover = false
        part.onExit(part)
    }
    fun selectButton(d: Vec2){
        val subMenu = parts.first {
            it.parent.getComponent<HoverComponent>()?.shouldUpdate() == true
        }.parent.children.filterIsInstance<CharacterPartSubMenu>().first()
        subMenu.selector.selectButton(d)
    }
}