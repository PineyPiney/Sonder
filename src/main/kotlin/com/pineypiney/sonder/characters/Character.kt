package com.pineypiney.sonder.characters

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.util.Animation
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class Character(parent: GameObject): Component(parent, "CHR"), UpdatingComponent{

    var gravity = true

    var animation: Animation
        get() = parent.getComponent<AnimatedComponent>()!!.animation
        set(value) { parent.getComponent<AnimatedComponent>()?.animation = value }

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("gvt", ::gravity){ gravity = it }
    )

    fun setAnimation(name: String) = parent.getComponent<AnimatedComponent>()?.setAnimation(name)

    override fun update(interval: Float) {
        if(gravity) parent.velocity = parent.velocity + (Vec3(0f, -20f * interval, 0f))
        move(parent.velocity * interval)
    }

    fun move(walk: Vec3){
        val collider = parent.getComponent<ColliderComponent>() ?: return
        parent.translate(Vec3(collider.checkAllCollisions(Vec2(walk))))
    }

    open fun getScriptFunctions(): Map<String, (String) -> Int> = emptyMap()
}