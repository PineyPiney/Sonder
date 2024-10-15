package com.pineypiney.sonder.city.buildings

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.rendering.IsoShape
import glm_.quat.Quat
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class House1 : Building("house1") {

	val setter: ShaderRenderedComponent.() -> Unit = {
		//uniforms.setFloatUniform("ambient") { .5f }
		//uniforms.setVec3Uniform("lightDir") { Vec3(.9 * cos(Timer.frameTime), -.4f, .9 * sin(Timer.frameTime)).normalize() }
		uniforms.setFloatUniform("alpha", ::getAlpha)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(BoxCollider3DComponent(this, Cuboid(Vec3(0f, 1.9f, 0f), Quat.identity, Vec3(1.6f, 3.8f, 3.8f))))
	}

	override fun addChildren() {
		super.addChildren()
		val r = renderer("house1 renderer", "base", Vec2i(500, 1409), 8f, 19f, 19f, 73f)
		r.scale = Vec3(.5f)
		addChild(r)

		val balcony = renderer("house1 balcony", "balcony", Vec2i(511, 110), 9f, 1.7f, 4.5f, 73f)
		balcony.position = Vec3(0f, 8.1f, 11.5f)

		val deck = renderer("house1 deck", "deck", Vec2i(244, 598), 3.5f, 8.2f, 14.5f, 73f)
		deck.position = Vec3(5.75f, 0f, -3.25f)
		r.addChild(deck, balcony)

		addChild(CollisionBox3DRenderer(this))
	}

	fun renderer(
		name: String,
		texName: String,
		textureCenterPoint: Vec2i,
		x: Float,
		y: Float,
		z: Float,
		ppu: Float
	): GameObject {
		return object : GameObject(name) {
			override fun addComponents() {
				super.addComponents()
				components.add(mesh(this, texName, textureCenterPoint, x, y, z, ppu))
			}
		}
	}

	fun mesh(
		o: GameObject,
		texName: String,
		textureCenterPoint: Vec2i,
		x: Float,
		y: Float,
		z: Float,
		ppu: Float
	): MeshedTextureComponent {
		val tex = TextureLoader[ResourceKey("city/buildings/house1/$texName")]
		return object : MeshedTextureComponent(
			o,
			tex,
			ShaderLoader[ResourceKey("vertex/isometric_shape"), ResourceKey("fragment/isometric_shape")],
			IsoShape(tex, textureCenterPoint, x, y, z, ppu)
		) {
			override fun setUniforms() {
				super.setUniforms()
				setter()
			}
		}
	}
}