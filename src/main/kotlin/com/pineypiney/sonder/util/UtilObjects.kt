package com.pineypiney.sonder.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBoxRenderer
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2

class UtilObjects {

    companion object{
        fun barrier(bl: Vec2, size: Vec2): GameObject{
            return object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(ColliderComponent(this, Rect2D(bl, size)))
                }

                override fun addChildren() {
                    super.addChildren()
                    addChild(CollisionBoxRenderer(this))
                }
            }
        }
    }
}