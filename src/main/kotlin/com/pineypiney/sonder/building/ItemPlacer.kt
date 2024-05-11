package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.building.parts.BuildingPart
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class ItemPlacer(parent: GameObject, val scene: SonderGamePlay) : DefaultInteractorComponent(parent, "IPC") {

	var item: GameObject? = null
		set(value) {
			field = value
			field?.let {
				place(it, scene.input.mouse.lastPos)
			}
		}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		item?.let{
			place(it, cursorPos)
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if(action == GLFW.GLFW_PRESS){
			item = null
			return INTERRUPT
		}
		return action
	}

	private fun place(o: GameObject, c: Vec2){
		val part = o.getComponent<BuildingPart>() ?: return
		val gameSpace = scene.renderer.camera.screenToWorld(c)
		part.placementRule(o, scene, gameSpace)
	}

	override fun shouldUpdate(): Boolean {
		return item != null
	}
}