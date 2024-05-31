package com.pineypiney.sonder.city.paths

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.maths.shapes.AxisAlignedCuboid
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4i

class PathRenderer(parent: GameObject): RenderedComponent(parent, pathShader) {

	val vShape = VertexShape.cornerCubeShape

	override val renderSize: Vec2 = Vec2(1f)

	override val shape: AxisAlignedCuboid get() = vShape.shape

	var edgeMask = Vec4i(0)

	init {
		parent.components.add(Collider3DComponent(parent, Cuboid(Vec3(0.5f), Quat.identity, Vec3(1.0f))))
	}

	override fun init() {
		super.init()
		parent.addChild(CollisionBox3DRenderer(parent))
	}

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setFloatUniform("outerThickness"){ .02f }
		uniforms.setFloatUniform("innerThickness"){ .01f }
		uniforms.setFloatUniform("brickWidth"){ .25f }
		uniforms.setFloatUniform("brickLength"){ 1f }

		uniforms.setVec3Uniform("darkBrickColour"){ Vec3.fromHex(0xAFA29E) }
		uniforms.setVec3Uniform("lightBrickColour"){ Vec3.fromHex(0xD8C9C5) }
		uniforms.setVec3Uniform("lineColour"){ Vec3.fromHex(0x544844) }

		uniforms.setVec4iUniform("edgeMask", ::edgeMask)
	}

	override fun render(renderer: RendererI<*>, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		vShape.bindAndDraw()
	}

	companion object{
		val pathShader = ShaderLoader[ResourceKey("vertex/pass_pos_3D"), ResourceKey("fragment/path")]
	}
}