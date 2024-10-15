package com.pineypiney.sonder

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.Debug
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C

class SonderRenderer : BufferedGameRenderer<SonderLogic>() {

	override val window: SonderWindow = SonderWindow.INSTANCE
	override val camera: SonderCamera = SonderCamera()

	override val view = I
	override val projection = I
	override val guiProjection = I

	val guiBuffer = FrameBuffer(0, 0, GL11C.GL_RGBA)

	// DEBUGGING
	val debugMap = (0..4).associateWith { 0 }.toMutableMap()

	override fun init() {
		super.init()
		GLFunc.depthTest = true
		GLFunc.blend = true
		GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)

		guiBuffer.setSize(window.framebufferSize)
		screenUniforms.setIntUniform("effects") { 0 }
		screenUniforms.setFloatUniform("z") { .1f }
	}


	override fun render(game: SonderLogic, tickDelta: Double) {
		val d = Debug().start()
		camera.getView(view)
		camera.getProjection(projection)

		GLFunc.clearColour = game.colour
		clear()//clearFrameBuffer()

		GLFunc.depthTest = true
		d.add() //0

		renderLayer(game, tickDelta, 0) { position.z }

		d.add() //1

		d.add() //2

		clearFrameBuffer(guiBuffer)
		renderLayer(game, tickDelta, 1) { -(position - camera.cameraPos).length2() }

		d.add() //3

		FrameBuffer.unbind()
		GLFunc.depthTest = false
		GLFunc.viewportO = window.framebufferSize

		screenShader.setUp(screenUniforms, this)
		//buffer.draw()
		guiBuffer.draw()

		d.add() //4
		for ((i, t) in d.differences().withIndex()) if (t > 1.0) debugMap[i] = debugMap[i]!! + 1
		if (debugMap.values.sum() > 512) {
			GameEngineI.logger.debug("Each stage has count\n" + debugMap.entries.joinToString { (i, c) -> "$i -> $c" })
			for (i in 0..4) debugMap[i] = 0
		}
	}

	fun renderLayer(game: SonderLogic, tickDelta: Double, layer: Int, sort: GameObject.() -> Float) {
		for (o in game.gameObjects[layer].flatMap { it.allActiveDescendants() }.sortedBy { it.sort() }) {
			val renderedComponents = o.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
			val preRendererComponent = o.components.filterIsInstance<PreRenderComponent>()
			if (renderedComponents.isEmpty()) {
				// If there are no visible Components only PreRender components that should be PreRendered anyway
				for (c in preRendererComponent) if (!c.whenVisible) c.preRender(tickDelta)
			} else {
				for (c in preRendererComponent) c.preRender(tickDelta)
				for (c in renderedComponents) c.render(this, tickDelta)
			}
		}
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		super.updateAspectRatio(window, objects)
		guiBuffer.setSize(window.framebufferSize)
		GLFunc.viewportO = window.size
		val w = window.aspectRatio
		glm.ortho(-w, w, -1f, 1f, guiProjection)
	}

	override fun deleteFrameBuffers() {
		super.deleteFrameBuffers()
		guiBuffer.delete()
	}

	companion object {
		val skyShader = ShaderLoader[ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/sky_gradient")]
	}
}

/*
        0.70710677 0.0 -0.70710677 -0.0
        -0.40824828 0.81649655 -0.40824828 -0.65319824
        0.57735026 0.57735026 0.57735026 -68.35827
        0.0 0.0 0.0 1.0

 */