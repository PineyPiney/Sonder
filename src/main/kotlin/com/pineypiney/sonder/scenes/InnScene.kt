package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.Building
import com.pineypiney.sonder.city.buildings.House1
import com.pineypiney.sonder.city.buildings.SushiBar
import com.pineypiney.sonder.city.paths.PathRenderer
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i

class InnScene(engine: SonderEngine): SonderGamePlay(engine) {

    override val size: Float = 20f
    override val floor: Float = 0f

    override fun init() {
        super.init()
        colour = Vec4.fromHex(0x7B937A, 0f)
    }

    val house = Building.make(Vec2(-4f, 0f), Vec2(8f, 6f)).apply {
        position = Vec3(10f, -4.9f, 0f)
    }

    val path = PathRenderer(GameObject("path")).applied().apply { edgeMask = Vec4i(0, 0, 0, 8) }.parent
    val path1 = PathRenderer(GameObject("path1")).applied().apply { edgeMask = Vec4i(1, 0, 1, 0) }.parent
    val path2 = PathRenderer(GameObject("path2")).applied().apply { edgeMask = Vec4i(0, 1, 0, 0) }.parent

    val sushi = SushiBar().apply { position = Vec3(-2.5f, 0f, -2.5f) }
    val house1 = House1().apply { position = Vec3(10f, 0f, -10f) }

    override fun addObjects() {
        super.addObjects()
        add(sushi, path.apply { position = Vec3(-4f, 0f, -1f); scale = Vec3(4f, 0.2f, 1f) },
            path1.apply { position = Vec3(-1f, 0f, -4f); scale = Vec3(1f, 0.2f, 3f)},
            path2.apply { position= Vec3(0f, 0f, -4f); scale = Vec3(4f, .2f, 1f) })
        //add(ColourRendererComponent(GameObject("floor"), Vec3.fromHex(0x454C44), ColourRendererComponent.shader3D, SquareShape3D(Vec3(-1f, 0f, -1f), Vec3(2f, 0f, 0f), Vec3(0f, 0f, 2f))).applied().parent.apply { scale = Vec3(size) })
    }
}