package com.pineypiney.sonder.city.paths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.maths.shapes.Shape
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4ui

class PathRendererBackUp(parent: GameObject, val vShape: VertexShape = VertexShape.cornerCubeShape) :
	ShaderRenderedComponent(parent, pathShader), InteractorComponent {

	override val renderSize: Vec2 = Vec2(1f)

	override val shape: Shape<*> get() = vShape.shape

	var xzEdgeMask = Vec4ui(0)
	var yEdgeMask = 0u

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var importance: Int = 0

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

		uniforms.setBoolUniform("selected") { hover }

		uniforms.setVec4uiUniform("xzEdgeMask", ::xzEdgeMask)
		uniforms.setUIntUniform("yEdgeMask", ::yEdgeMask)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		vShape.bindAndDraw()
	}

	override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
		return parent.getComponent<BoxCollider3DComponent>()?.transformedShape?.intersectedBy(ray)?.isNotEmpty()
			?: false
	}

	companion object {
		val pathShader = ShaderLoader[ResourceKey("vertex/pass_pos_3D"), ResourceKey("fragment/path")]
	}
}