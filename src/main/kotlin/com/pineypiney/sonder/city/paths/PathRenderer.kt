package com.pineypiney.sonder.city.paths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4ui

class PathRenderer(parent: GameObject, var vShape: VertexShape = VertexShape.cornerCubeShape) :
	ShaderRenderedComponent(parent, pathShader) {

	override val renderSize: Vec2 = Vec2(1f)

	override val shape: Shape<*> get() = vShape.shape

	var gridMask = Vec4ui(0)
	var selected = false

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setFloatUniform("outerThickness") { .02f }
		uniforms.setFloatUniform("innerThickness") { .01f }
		uniforms.setFloatUniform("brickWidth") { .2f }
		uniforms.setFloatUniform("brickLength") { .7f }

		uniforms.setVec3Uniform("lineColour") { Vec3.fromHex(0x544844) }
		uniforms.setVec3Uniform("darkBrickColour") { Vec3.fromHex(0xAFA29E) }
		uniforms.setVec3Uniform("lightBrickColour") { Vec3.fromHex(0xD8C9C5) }
		uniforms.setFloatUniform("rgbVariance") { 0.1f }

		uniforms.setBoolUniform("selected", ::selected)

		uniforms.setVec4uiUniform("gridMask", ::gridMask)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		vShape.bindAndDraw()
	}

	companion object {
		val pathShader = ShaderLoader[ResourceKey("vertex/pass_pos_3D"), ResourceKey("fragment/path")]
	}
}