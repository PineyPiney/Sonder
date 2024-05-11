package com.pineypiney.sonder.ui.dialogue

import com.pineypiney.game_engine.objects.components.ClickerComponent
import com.pineypiney.game_engine.objects.components.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.sonder.characters.Character
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class DialogueBubble(speaker: Character, speech: String): MenuItem() {

    var clicked = false

    val text = Text.makeMenuText(speech, Vec4(0f, 0f, 0f, 1f), 1f, 1f, 0.3f, Text.ALIGN_CENTER)

    override fun addChildren() {
        super.addChildren()
        addChild(text)
    }

    override fun addComponents() {
        super.addComponents()
        components.add(ColourRendererComponent(this, Vec4.fromHex(0xF2E5E1), ColourRendererComponent.menuShader, VertexShape.cornerSquareShape))
        components.add(ClickerComponent(this, { clicked = true }))
    }

    override fun init() {
        super.init()

        position = Vec3(-0.45f, -0.9f, 0f)
        scale = Vec3(.9f, .3f, 1f)

        text.position = Vec3(0f, 0f, -.1f)
    }

    fun setValues(speaker: Character, speech: String)
    {
        text.getComponent<TextRendererComponent>()!!.text.text = speech
    }
}