package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.HoverComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.textures.Texture

class HoverButton(var icon: Texture, val onEnter: (HoverComponent) -> Unit = {}, val onExit: (HoverComponent) -> Unit = {}) : MenuItem() {

    var selector: CharacterPartSubMenu? = null

    override fun addComponents() {
        super.addComponents()
        components.add(SpriteComponent(this, icon, 1200f, SpriteComponent.menuShader, VertexShape.cornerSquareShape))
        components.add(HoverComponent(this@HoverButton, onEnter, onExit))
    }
}