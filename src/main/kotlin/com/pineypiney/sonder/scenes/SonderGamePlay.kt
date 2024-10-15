package com.pineypiney.sonder.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.SonderWindow
import com.pineypiney.sonder.building.Building
import com.pineypiney.sonder.building.ItemPlacer
import com.pineypiney.sonder.characters.player.PlayerObject
import com.pineypiney.sonder.ui.GamePadKeyLabel
import com.pineypiney.sonder.ui.phone.Phone
import com.pineypiney.sonder.util.UtilObjects
import com.pineypiney.sonder.util.dialogue.DialogueUtil
import glm_.func.common.abs
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

abstract class SonderGamePlay(engine: SonderEngine) : SonderGameLogic(engine) {

	abstract val size: Vec2
	abstract val floor: Float

	var freedom = false
	override val player: PlayerObject = PlayerObject()

	//val buildMenu = BuildingBubble().apply { active = false }
	val placer by lazy { ItemPlacer(GameObject("Item Placer"), this).applied() }
	val grid =
		GameObject.simpleRenderedGameObject(ShaderLoader[ResourceKey("vertex/world_pos"), ResourceKey("fragment/grid")]) {
			uniforms.setFloatUniform("cellSize") { .5f }
			uniforms.setFloatUniform("thickness") { .005f }
			uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 0f, .6f) }
		}.apply { name = "Grid"; active = false }
	val phone = object : MenuItem("Phone") {

		override fun addComponents() {
			super.addComponents()
			components.add(Phone(this, this@SonderGamePlay))
		}
	}

	override fun addObjects() {
		val s1 = size + Vec2(1f)
		val s05 = size + Vec2(.5f)
		val barriers = object : GameObject("Barriers") {
			override fun addChildren() {
				addChild(
					UtilObjects.barrier3D("Floor Barrier", Vec3(0f, -.5f, 0f), Vec3(2f * s1.x, 1f, 2f * s1.y)),
					UtilObjects.barrier3D("-Z Barrier", Vec3(0f, 2.5f, -s05.y), Vec3(2f * s1.x, 5f, 1f)),
					UtilObjects.barrier3D("+Z Barrier", Vec3(0f, 2.5f, s05.y), Vec3(2f * s1.x, 5f, 1f)),
					UtilObjects.barrier3D("-X Barrier", Vec3(-s05.x, 2.5f, -0f), Vec3(1f, 5f, size.y * 2f)),
					UtilObjects.barrier3D("+X Barrier", Vec3(s05.x, 2.5f, -0f), Vec3(1f, 5f, size.y * 2f)),
				)
			}
		}

		add(player, barriers)
		grid.rotation = Quat(Vec3(PI * .5f, 0f, 0f))
		grid.scale = Vec3(size.x * 2, size.y * 2f, 1f)
		add(placer.parent, grid, phone)

		if (gameEngine.inputType == ControlType.GAMEPAD_BUTTON) {
			val label = GamePadKeyLabel(
				input.gamepad.connectedGamepads.first(),
				ControlType.GAMEPAD_BUTTON,
				GLFW.GLFW_GAMEPAD_BUTTON_Y,
				"Edit Character"
			)
			label.position = Vec3(.5f, -.9f, 0f)
			add(label)
		}
	}

	override fun init() {
		super.init()
		trap()
		colour = Vec4(0f, 1f, 0f, 1f)
		renderer.camera.setPos(Vec3(renderer.camera.y))
	}

	override fun render(tickDelta: Double) {

		val (_, texture, frames, _, speed, flip) = player.renderChild.updateAnimation(player.velocity)
		player.renderChild.flipY = flip

		if (frames != player.renderChild.frames) {
			player.renderChild.frames = frames
			player.renderChild.invFrames = 1f / frames
			player.renderChild.animationSpeed = speed
			player.renderChild.scale = Vec3(player.renderChild.invFrames, 1f, 1f)
		}
		player.renderChild.getComponent<ColouredSpriteComponent>()?.sprite = Sprite(texture, 200f)

		super.render(tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if (super.onInput(state, action) == INTERRUPT) return INTERRUPT

		when (action) {
			GLFW.GLFW_PRESS -> {
				when (state.removeMods()) {
					InputState('K'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_Y, ControlType.GAMEPAD_BUTTON) -> {
						gameEngine.setGame(CharacterCustomisation(gameEngine, this), false)
						gameEngine.openGame()
						return INTERRUPT
					}

					InputState('Q'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT, ControlType.GAMEPAD_BUTTON) -> {
						phone.getComponent<Phone>()?.let { if (it.open) it.close() else it.open() }
					}

					InputState('B'), InputState(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, ControlType.GAMEPAD_BUTTON) -> {
						setBuildMode(!player.buildMode)
					}

					InputState(' '), InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) -> {
						if (player.getComponent<BoxCollider3DComponent>()?.isGrounded() == true &&
							!player.buildMode
						) player.velocity = Vec3(player.velocity.x, 9f, player.velocity.z)
					}

					InputState('R') -> {
						player.position = Vec3(-35f, 0f, 0f)
						player.velocity = Vec3(0f)
					}

					InputState('G') -> {
						DialogueUtil.setDialogue(player.player!!, "Hello my namem is PHILMNIP!")
					}

					InputState('F') -> if (freedom) trap() else free()
				}
			}
		}

		return action
	}

	override fun onScroll(scrollDelta: Vec2): Int {
		val r = super.onScroll(scrollDelta)
		if (r == INTERRUPT) return r

		if (player.buildMode) renderer.camera.increaseHeight(-2f * scrollDelta.y)
		return 0
	}

	override fun update(interval: Float, input: Inputs) {
		super.update(interval, input)

		val move = Vec2()
		if (gameEngine.inputType == ControlType.KEYBOARD) {
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_W) == 1) move += Vec2(0, 1)
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_S) == 1) move += Vec2(0, -1)

			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_D) == 1) move += Vec2(1, 0)
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_A) == 1) move += Vec2(-1, 0)
			if (move.length2() > 1f) move.normalizeAssign()
			move *= (1f + GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT))
		} else {
			val pad = input.gamepad.connectedGamepads.first()
			if (player.buildMode) {
				val y = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y]
				if (y.abs > pad.deadzone.x) move += Vec2(0, -y)
			}
			val x = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X]
			if (x.abs > pad.deadzone.x) move += Vec2(x, 0)
			move *= (1.5f + pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER] * .5f)
		}

		//if(player.buildMode) {
		//    renderer.camera.translate(Vec2(move * interval * renderer.camera.zoom))
		//}
		//else {
		//    player.velocity2D = Vec2(move.x * 4f, player.velocity2D.y)
		//    renderer.camera.setPos(player.position - (renderer.camera.cameraFront * 5f))
		//}

		if (freedom) {
			renderer.camera.translate(renderer.camera.cameraFront * move.y + renderer.camera.cameraRight * move.x)
		} else {
			// move.x(1, -1) + move.y(-1, -1)
			val v = Vec2(move.x - move.y, -(move.x + move.y)) * 2f
			player.velocity = Vec3(v.x, player.velocity.y, v.y)
			val h = renderer.camera.cameraPos.y - player.position.y
			renderer.camera.setPos(player.position + Vec3(h))
		}
	}

	override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
		super.onCursorMove(cursorPos, cursorDelta)

		if (freedom) {
			renderer.apply {
				camera.cameraYaw += cursorDelta.x * 18
				camera.cameraPitch = (camera.cameraPitch + cursorDelta.y * 18).coerceIn(-89.99, 89.99)

				camera.updateCameraVectors()
			}
		}
	}

	fun setBuildMode(value: Boolean) {
		player.buildMode = value
		player.velocity.x = 0f
		//buildMenu.active = value
		grid.active = value
		if (!value) renderer.camera.setZoom(10f)
	}

	fun getHoveredBuilding(): Building? {
		val buildings = gameObjects.getAllComponentInstances<Building>(0)
		return buildings.firstOrNull {
			val scale = Vec2(it.parent.transformComponent.worldScale)
			Vec2(renderer.camera.screenToWorld(input.mouse.lastPos)).isWithin(
				Vec2(it.parent.transformComponent.worldPosition) + (it.origin * scale),
				it.size * scale
			)
		}
	}

	fun free() {
		freedom = true
		SonderWindow.INSTANCE.setCursorType(GLFW.GLFW_CURSOR_DISABLED)
	}

	fun trap() {
		freedom = false
		SonderWindow.INSTANCE.setCursorType(GLFW.GLFW_CURSOR_NORMAL)

		renderer.camera.cameraYaw = -135.0
		renderer.camera.cameraPitch = -Math.toDegrees(atan2(1.0, sqrt(2.0)))
		renderer.camera.updateCameraVectors()
	}

	override fun updateAspectRatio(window: WindowI) {
		super.updateAspectRatio(window)

		renderer.camera.updateBounds(Vec2(size + Vec2(renderer.camera.y)))
	}

	companion object {
		private val m_isometricRotation = Quat(Vec3(atan(-1f / sqrt(2f)), PI * .25f, 0f))
		val isometricRotation get() = Quat(m_isometricRotation)
	}
}