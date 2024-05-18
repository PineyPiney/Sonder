package com.pineypiney.sonder.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider2DComponent
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class UtilObjects {

    companion object{
        fun barrier2D(bl: Vec2, size: Vec2): GameObject{
            return object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(Collider2DComponent(this, Rect2D(bl, size)))
                }

                override fun addChildren() {
                    super.addChildren()
                    addChild(CollisionBox2DRenderer(this))
                }
            }
        }

        fun barrier3D(bl: Vec3, size: Vec3, rotation: Quat = Quat.identity): GameObject{
            return object : GameObject(){
                override fun addComponents() {
                    super.addComponents()
                    components.add(Collider3DComponent(this, Cuboid(bl, rotation, size)))
                }

                override fun addChildren() {
                    super.addChildren()
                    addChild(CollisionBox3DRenderer(this))
                }
            }
        }
    }
}