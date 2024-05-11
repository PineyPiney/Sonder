package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.HoverComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.util.MenuNode
import glm_.vec2.Vec2

class CharacterPart(val type: String, origin: Vec2, size: Vec2, val onClick: (String) -> Unit) : MenuItem() {

    override var name: String = "Character Part"

    init {
        os(origin, size)
    }

    override fun addComponents() {
        super.addComponents()
        components.add(SpriteComponent(this, backgroundTexture, 1200f, SpriteComponent.menuShader, VertexShape.cornerSquareShape))
        components.add(HoverComponent(this, {}){
            for(b in children.filterIsInstance<HoverButton>()){
                if(b.getComponent<DefaultInteractorComponent>()?.hover != true) b.selector?.active = false
            }
        })
        components.add(CharacterPartComponent(this, type, onClick))
    }

    companion object{
        val backgroundTexture = TextureLoader[ResourceKey("ui/character_customisation/colour_background")]
    }

    class CharacterMenuSettings{

        val columns: Int
        val rows: Int
        val width: Float
        val height: Float
        val defaultMessage: String

        constructor(columns: Int, rows: Int, width: Float, height: Float, defaultMessage: String){
            this.columns = columns
            this.rows = rows
            this.defaultMessage = defaultMessage
            this.width = width
            this.height = height
        }

        constructor(category: MenuNode){
            columns = category["columns"]?.toInt() ?: 7
            rows = category["rows"]?.toInt() ?: 2
            width = category["width"]?.toFloat() ?: 0.14f
            height = category["height"]?.toFloat() ?: 0.18f
            defaultMessage = category["message"] ?: ""
        }
    }
}