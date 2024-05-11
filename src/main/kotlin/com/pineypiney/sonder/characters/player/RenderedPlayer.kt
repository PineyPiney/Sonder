package com.pineypiney.sonder.characters.player

import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.components.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.game_objects.GameObject2D
import com.pineypiney.game_engine.objects.util.collision.CollisionBoxRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.sonder.characters.Character
import com.pineypiney.sonder.util.inventory.Inventory
import com.pineypiney.sonder.util.inventory.Items
import glm_.parseInt
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class RenderedPlayer : GameObject2D() {

    override var name: String = "Player"

    var buildMode = false
    var debug = false

    val character get() = getComponent<Character>()

    var skinTone
        get() = Vec3(getComponent<ColouredSpriteComponent>()!!.tint())
        set(value) { getComponent<ColouredSpriteComponent>()!!.tint = { Vec4(value, 1f) } }
    var hairColour
        get() = Vec3(hair.getComponent<ColouredSpriteComponent>()!!.tint())
        set(value) { hair.getComponent<ColouredSpriteComponent>()!!.tint = { Vec4(value, 1f) } }
    var hairStyle
        get() = hair.getComponent<SpriteComponent>()!!.texture.fileLocation.removePrefix("characters\\player\\hair\\").substringBeforeLast('.')
        set(value) { hair.getComponent<SpriteComponent>()!!.texture = TextureLoader[ResourceKey(value)]}

    var hair = object : GameObject2D(){

        override var name: String = "hair"

        val texture = ColouredSpriteComponent(this, TextureLoader[ResourceKey("characters/player/hair/hair1")], 600f, Vec4(0.2f, 0.08f, 0.02f, 1f), playerShader)

        override fun addComponents() {
            super.addComponents()
            components.add(texture)
        }
    }

    val inventory = Inventory()

    override fun addComponents() {
        super.addComponents()
        components.add(ColliderComponent(this, Rect2D(Vec2(-1f, -2f), Vec2(1.6f, 4f))))
        components.add(ColouredSpriteComponent(this, TextureLoader[ResourceKey("characters/player/body/idle/IMG_2233")], 600f, Vec4(0.28f, 0.105f, 0.035f, 1f), playerShader))
        components.add(AnimatedComponent(this, PlayerAnimations.playerWalk, listOf(PlayerAnimations.playerWalk, PlayerAnimations.playerIdle, PlayerAnimations.playerWobble)))
        components.add(object : Character(this){
            override fun getScriptFunctions(): Map<String, (String) -> Int> {
                return super.getScriptFunctions() + ("has" to this@RenderedPlayer::has)
            }
        })
    }

    override fun addChildren() {
        addChild(hair)
        addChild(CollisionBoxRenderer(this))
    }

    override fun init() {
        super.init()

        scale = Vec3(.3f)
        hair.position = Vec3(0f, 2f, .1f)
    }

    infix fun copyDetailsTo(other: RenderedPlayer){
        other.skinTone = skinTone
        other.hairColour = hairColour
        other.hairStyle = hairStyle
    }

    fun has(details: String): Int{
        val parts = details.split(' ')
        return if(inventory.amount(Items.getItem(parts[1])) >= parts[0].parseInt()) 1 else 0
    }

    companion object{
        val playerShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/coloured_texture")]
    }
}