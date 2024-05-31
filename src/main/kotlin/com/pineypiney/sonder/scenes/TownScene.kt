package com.pineypiney.sonder.scenes

import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.npcs.Puppy
import com.pineypiney.sonder.environment.CherryTree
import glm_.vec3.Vec3

class TownScene(engine: SonderEngine): SonderGamePlay(engine) {

    override val size: Float = 40f
    override val floor: Float = 1.3f

    override fun addObjects() {
        super.addObjects()
        add(Puppy())

        add(CherryTree().apply { translate(Vec3(20f, 0f, 0f)) })
    }
}