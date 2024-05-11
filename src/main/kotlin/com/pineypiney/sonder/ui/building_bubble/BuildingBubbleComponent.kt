package com.pineypiney.sonder.ui.building_bubble

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class BuildingBubbleComponent(parent: GameObject): DefaultInteractorComponent(parent, "BBB") {

    var clickPos: Vec2? = null


    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        val size = Vec2(parent.transformComponent.worldScale) * renderSize
        return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition) - (size * .5f), size)
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        when(action) {
            GLFW.GLFW_PRESS -> {
                val size = Vec2(parent.transformComponent.worldScale) * renderSize
                val pos = Vec2(parent.transformComponent.worldPosition) - (size * .5f)
                if (cursorPos.isWithin(Vec2(pos.x, pos.y + (size.y * .85f)), Vec2(size.x, size.y * .15f))) {
                    clickPos = cursorPos - Vec2(parent.position)
                }
            }
            GLFW.GLFW_RELEASE -> clickPos = null
        }
        return super.onPrimary(window, action, mods, cursorPos)
    }

    override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
        super.onCursorMove(window, cursorPos, cursorDelta, ray)
        clickPos?.let{
            val v = Vec2(1f) - (Vec2(parent.transformComponent.worldScale) * renderSize * .5f)
            parent.position = Vec3((cursorPos - it).coerceIn(v), 0f)
        }
    }
}