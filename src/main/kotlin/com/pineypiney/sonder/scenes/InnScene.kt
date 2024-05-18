package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.Building
import com.pineypiney.sonder.environment.SushiBar
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class InnScene(engine: SonderEngine): SonderGamePlay(engine) {

    override val size: Float = 20f
    override val floor: Float = .1f

    val house = Building.make(Vec2(-4f, 0f), Vec2(8f, 6f)).apply {
        position = Vec3(10f, -4.9f, 0f)
    }

    val sushi = SushiBar()

    override fun addObjects() {
        super.addObjects()
        add(sushi)
    }

    override fun init() {
        super.init()
        colour = Vec4.fromHex(0xDAD3B4)
    }
}