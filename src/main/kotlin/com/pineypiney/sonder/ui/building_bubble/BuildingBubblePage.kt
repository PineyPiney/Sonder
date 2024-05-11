package com.pineypiney.sonder.ui.building_bubble

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.parts.BuildingPart
import com.pineypiney.sonder.building.parts.Parts
import com.pineypiney.sonder.scenes.SonderGamePlay
import com.pineypiney.sonder.ui.phone.Phone
import com.pineypiney.sonder.ui.phone.apps.BuildingApp
import com.pineypiney.sonder.util.MenuNode
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.max

class BuildingBubblePage(parent: GameObject, val page: MenuNode): Component(parent, "BBP") {

    override val fields: Array<Field<*>> = arrayOf()

    override fun init() {
        super.init()

        addButtons()
    }

    private fun addButtons(){
        for((i, node) in page.children.withIndex()){
            if(node.children.isEmpty()){
                val properties = node.tags.mapValues { (k, v) -> processProperties(k, v) }.removeNullValues()
                val texture = properties["sprite"] as Texture

                val prefabName = node["prefab"] ?: page["prefab"] ?: "Plant"
                val prefab = Parts.parts.firstOrNull { it.name == prefabName } ?: Parts.PLANT
                val newButton = SpriteButton(texture, Phone.ppu, Vec3((i * .1f) - .28f, 0f, .1f)){

                    val new = prefab.copy()
                    new.getComponent<BuildingPart>()?.setProperties(properties)
                    new.init()
                    (SonderEngine.INSTANCE.game as? SonderGamePlay)?.let {
                        it.placer.item = new
                    }
                }

                val childrenMap = properties["sprites"] as? Map<*, *>
                if(childrenMap != null){
                    for((k, v) in childrenMap){
                        val sprite = k as Texture
                        val vec = v as Vec3
                        val component = SpriteComponent(MenuItem(), sprite, Phone.ppu, SpriteComponent.menuShader, VertexShape.cornerSquareShape).applied()
                        val shape = prefab.getComponent<BuildingPart>()!!.shape
                        val point = shape.bl
                        val parentPoint = Vec2(0f) // The bl of the SquareShape used to render the main image of the sprite component, in this case it's the corner square
                        val offset = parentPoint - point
                        val parentTex = properties["sprite"] as Texture
                        val offsetVec = (offset * (parentTex.size - sprite.size)) + Vec2(vec)
                        newButton.addChild(component.parent.apply { position = Vec3(offsetVec / Phone.ppu, .1f) })
                    }
                }

                parent.addChild(newButton)

                val scaling = 160f / max(texture.width, texture.height)
                if(scaling < 1f) newButton.scale = Vec3(scaling, scaling, 1f)
            }
            else{
                parent.addChild(SpriteButton(TextureLoader[ResourceKey("ui/${node["icon"]}")], Phone.ppu, Vec3((i * .1f) - .28f, 0f, .1f)){
                    parent.parent?.getComponent<BuildingApp>()?.openNewPage(node)
                })
            }
        }
    }

    private fun processProperties(key: String, value: String): Any?{
        return when(key){
            "sprite" -> TextureLoader[ResourceKey("edit/$value")]
            "sprites" -> getSubSprites(value)
            "surface" -> value.split(';').map { Vec2.fromString(it, false) }.let { it[0] to it[1] }
            else -> null
        }
    }

    fun getSubSprites(string: String): Map<Texture, Vec3>{
        val sprites = string.split(';')
        return sprites.associate { sprite ->

            val index = sprite.indexOf('(')
            val (tex, vec) = if (index > 0) {
                sprite.substring(0, index).trim() to Vec3.fromString(sprite.substring(index + 1).substringBeforeLast(')'))
            } else sprite.trim() to Vec3(0f)

            TextureLoader[ResourceKey("edit/$tex")] to vec
        }
    }
}