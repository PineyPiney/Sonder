package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.Building
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.roundToInt

class InnScene(engine: SonderEngine): SonderGamePlay(engine) {

    val background: GameObject
    val foreground: GameObject
    override val size: Float
    override val floor: Float = .1f

    val sea: GameObject

    init {
        val tex = TextureLoader[ResourceKey("environment/inn/inn")]
        val xBound = tex.aspectRatio * 5f
        size = xBound
        val shape = SquareShape(Vec2(-xBound, -5f), Vec2(xBound, 5f))
        background = GameObject.simpleTextureGameObject(tex, shape)
        background.position = Vec3(0f, 0f, -10f)
        foreground = GameObject.simpleTextureGameObject(TextureLoader[ResourceKey("environment/inn/foreground")], shape)
        foreground.position = Vec3(0f, 0f, 1f)

        sea = object : GameObject(){

            val water0 = TextureLoader[ResourceKey("environment/inn/water_0")]
            val water1 = TextureLoader[ResourceKey("environment/inn/water_1")]

            override fun addComponents() {
                super.addComponents()
                val o = this
                components.add(object : MeshedTextureComponent(o, water0, vShape = shape), UpdatingComponent{

                    override fun update(interval: Float) {
                        when((Timer.time * 3.0).roundToInt() % 3){
                            0 -> visible = false
                            1 -> {
                                visible = true
                                texture = water0
                            }
                            2 -> texture = water1
                        }
                    }
                })
            }
        }
    }

    val house = Building.make(Vec2(-4f, 0f), Vec2(8f, 6f)).apply {
        position = Vec3(10f, -4.9f, 0f)
    }

    override fun addObjects() {
        super.addObjects()
        add(background, foreground, sea)
        add(house)
    }

    override fun init() {
        super.init()
        colour = Vec4.fromHex(0xDAD3B4)
    }
}