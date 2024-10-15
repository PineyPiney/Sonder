package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.PlacementRule
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class Door(parent: GameObject) : BuildingPart(parent, "DOR"), InteractorComponent {

	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_SURFACE

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var passThrough: Boolean = false

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)

		if (hover && action == GLFW.GLFW_PRESS) {
			(SonderEngine.INSTANCE.game as? SonderGamePlay)?.player?.position = parent.transformComponent.worldPosition
			building?.useDoor()
			return InteractorComponent.INTERRUPT
		}

		return action
	}
}