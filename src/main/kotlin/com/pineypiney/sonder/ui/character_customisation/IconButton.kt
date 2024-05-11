package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape

class IconButton(ppu: Float) : CharacterPartButton(ppu) {

    override fun addComponents() {
        super.addComponents()
        components.add(SpriteComponent(this, icon, ppu, SpriteComponent.menuShader, VertexShape.cornerSquareShape))
    }
}