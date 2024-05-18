package com.pineypiney.sonder.characters

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.util.Animation
import glm_.vec3.Vec3

abstract class Character(parent: GameObject): Component(parent, "CHR"), UpdatingComponent{

    var gravity = true

    var animation: Animation?
        get() = parent.allDescendants().firstNotNullOfOrNull { it.getComponent<AnimatedComponent>() }?.animation
        set(value) { value?.let { v -> parent.allDescendants().firstNotNullOfOrNull { it.getComponent<AnimatedComponent>() }?.animation = v } }

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("gvt", ::gravity){ gravity = it }
    )

    fun setAnimation(name: String) = parent.getComponent<AnimatedComponent>()?.setAnimation(name)

    override fun update(interval: Float) {
        if(gravity) parent.velocity = parent.velocity + (Vec3(0f, -20f * interval, 0f))
        move(parent.velocity * interval)
    }

    fun move(walk: Vec3){
        val collider = parent.getComponent<Collider3DComponent>() ?: return
        parent.translate(collider.checkAllCollisions(walk))
    }

    open fun getScriptFunctions(): Map<String, (String) -> Int> = emptyMap()
}