package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.npcs.Puppy
import com.pineypiney.sonder.environment.CherryTree
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class TownScene(engine: SonderEngine): SonderGamePlay(engine) {

    private val ground: GameObject
    override val size: Float = 40f
    override val floor: Float = 1.3f

    init {
        val width = renderer.camera.getSpan().x
        val tex = TextureLoader[ResourceKey("environment/ground")]
        val texWidth = size / width
        ground = GameObject.simpleTextureGameObject(tex, SquareShape(Vec2(-size, -5), Vec2(size, (width / tex.aspectRatio) - 5), Vec2(-texWidth, 0), Vec2(texWidth, 1)))
        ground.position = Vec3(0f, 0f, -10f)
    }

    override fun addObjects() {
        super.addObjects()
        add(Puppy())

        add(CherryTree().apply { translate(Vec2(20f, 0f)) })
    }
}