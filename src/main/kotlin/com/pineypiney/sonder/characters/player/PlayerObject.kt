package com.pineypiney.sonder.characters.player

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.characters.player.render.PlayerRenderer
import glm_.quat.Quat
import glm_.vec3.Vec3

class PlayerObject : GameObject() {

    override var name: String = "Player"

    var buildMode = false
    var debug = false

    val player get() = getComponent<Player>()
    var renderChild = PlayerRenderer()

    override fun addComponents() {
        super.addComponents()
        components.add(Collider3DComponent(this, Cuboid(Vec3(0f), Quat.identity, Vec3(.5f, 1.2f, .5f))))
        components.add(Player(this))
    }

    override fun addChildren() {
        addChild(renderChild)
        addChild(CollisionBox3DRenderer(this))
    }

    infix fun copyDetailsTo(other: PlayerObject){
        other.renderChild.skinTone = renderChild.skinTone
        other.renderChild.hairColour = renderChild.hairColour
        other.renderChild.hairStyle = renderChild.hairStyle
    }
}