package com.pineypiney.sonder.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class IsoRendererComponent(
	parent: GameObject,
	val texture: Texture,
	val ppu: Float,
	floorCenter: Vec2i,
	shader: Shader = SpriteComponent.defaultShader
) : ShaderRenderedComponent(parent, shader) {

	val vShape = makeShape(texture, ppu, floorCenter)
	override val shape = vShape.shape
	override val renderSize: Vec2 = Vec2(1f)

	override fun render(renderer: RendererI, tickDelta: Double) {
		texture.bind()
		shader.setUp(uniforms, renderer)
		vShape.bindAndDraw()
	}

	companion object {
		fun makeShape(texture: Texture, ppu: Float, center: Vec2i): VertexShape {
			val pixelSize = 1f / ppu
			val size = Vec2(texture.size) * pixelSize
			val bl = -Vec2(center) * pixelSize
			val tr = size + bl
			return SquareShape(bl, tr)
		}
	}
}