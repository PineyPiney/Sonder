package com.pineypiney.sonder.building

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.building.parts.BuildingPart
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class ItemPlacer(parent: GameObject, val scene: SonderGamePlay) : DefaultInteractorComponent(parent, "IPC") {

	var item: GameObject? = null
		set(value) {
			field = value
			field?.let {
				move(it, scene.renderer.camera.getRay(scene.input.mouse.lastPos))
			}
		}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		item?.let {
			move(it, ray)
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if (action == GLFW.GLFW_PRESS) {
			if (InputState.control(mods)) {
				item?.getComponent<BuildingPart>()?.onPlace(scene)
				item = null
			} else {
				val copy = item?.copy()?.apply { init(); scene.add(this) }
				copy?.getComponent<BuildingPart>()?.onPlace(scene)
			}
			return INTERRUPT
		}
		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if (action == GLFW.GLFW_PRESS) {
			item?.delete()
			item = null
		}
		return action
	}

	private fun move(o: GameObject, ray: Ray) {
		val part = o.getComponent<BuildingPart>() ?: return
		val oldPlace = Vec3(o.position)
		part.placementRule(o, scene, ray)
		part.onMove(oldPlace, Vec3(o.position))
	}

	override fun shouldInteract(): Boolean {
		return item != null
	}
}