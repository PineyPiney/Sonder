package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.PlacementRule
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class Door(parent: GameObject): BuildingPart(parent, "DOR"), InteractorComponent {

	override val fields: Array<Field<*>> = arrayOf()
	override val placementRule: PlacementRule = PlacementRule.PLACE_ON_SURFACE

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var importance: Int = 0

	override fun checkHover(ray: Ray, screenPos: Vec2): Boolean{
		val gamePos = Vec2(ray.rayOrigin)
		val renderer = parent.getComponent<RenderedComponent>()
		if(renderer != null){
			val shape = renderer.shape as? Rect2D
			if(shape != null){
				val unitSize = Vec2(parent.transformComponent.worldScale) * renderer.renderSize
				return gamePos.isWithin(Vec2(parent.transformComponent.worldPosition) + (shape.origin * unitSize), unitSize * shape.size)
			}
		}
		return gamePos.isWithin(Vec2(parent.transformComponent.worldPosition), Vec2(parent.transformComponent.worldScale) * renderSize)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)

		if(hover && action == GLFW.GLFW_PRESS){
			(SonderEngine.INSTANCE.game as? SonderGamePlay)?.player?.position = parent.transformComponent.worldPosition
			building?.useDoor()
			return InteractorComponent.INTERRUPT
		}

		return action
	}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {}
	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {}
	override fun onScroll(window: WindowI, scrollDelta: Vec2): Int = 0
	override fun onType(window: WindowI, char: Char): Int = 0
	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int = 0
}