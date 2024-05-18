package com.pineypiney.sonder.characters.npcs

import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.Collider2DComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.game_objects.GameObject2D
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.Character
import glm_.func.common.abs
import glm_.vec2.Vec2

class Puppy: GameObject2D() {

    override var name: String = "Puppy"

    override fun addComponents() {
        super.addComponents()
        components.add(Collider2DComponent(this, Rect2D(Vec2(-0.5f, -1f), Vec2(1f, 2f))))
        components.add(SpriteComponent(this, TextureLoader[ResourceKey("characters/puppy/idle/Untitled_Artwork-1")], 600f))

        val idle = Animation("idle", 6f, "characters/puppy/idle", (1..4).map { "Untitled_Artwork-$it" }, "characters/puppy/idle")
        components.add(AnimatedComponent(this, idle, listOf(
            idle,
            Animation("talk", 6f, "characters/puppy/talk", (1..5).map { "Untitled_Artwork-$it" }, "characters/puppy/talk"),
            Animation("walk", 6f, "characters/puppy/walk", (1..8).map { "Untitled_Artwork-$it" }, "characters/puppy/walk")
        )))
        components.add(object : Character(this){

            override fun update(interval: Float) {
                super.update(interval)

                val player = SonderEngine.INSTANCE.game.player

                setAnimation(
                    when{
                        player.velocity.x != 0f -> "walk"
                        player.position.x.abs < 1f -> "talk"
                        else -> "idle"
                    }
                )
            }
        })
    }
}