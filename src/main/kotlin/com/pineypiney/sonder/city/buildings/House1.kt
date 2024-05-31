package com.pineypiney.sonder.city.buildings

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Collider3DComponent
import com.pineypiney.game_engine.objects.components.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.RenderedComponentI
import com.pineypiney.game_engine.objects.util.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.util.IsoShape
import glm_.quat.Quat
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class House1: GameObject("house1"){

	val setter: RenderedComponentI.() -> Unit = {
		//uniforms.setFloatUniform("ambient") { .5f }
		//uniforms.setVec3Uniform("lightDir") { Vec3(.9 * cos(Timer.frameTime), -.4f, .9 * sin(Timer.frameTime)).normalize() }
		uniforms.setFloatUniform("alpha", ::getAlpha)
	}

	val leftEdge by lazy {
		getComponent<Collider3DComponent>()?.transformedBox?.let { it.center + (it.size * Vec3(-.5f, -.5f, .5f)) } ?: Vec3()
	}
	val rightEdge by lazy {
		getComponent<Collider3DComponent>()?.transformedBox?.let { it.center + (it.size * Vec3(.5f, -.5f, -.5f)) } ?: Vec3()
	}

	override fun addComponents() {
		super.addComponents()
		components.add(Collider3DComponent(this, Cuboid(Vec3(0f, 1.9f, 0f), Quat.identity, Vec3(1.6f, 3.8f, 3.8f))))
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

	fun getAlpha(): Float{
		val playerObject = SonderEngine.INSTANCE.game.player
		val playerBox = playerObject.getComponent<Collider3DComponent>()?.transformedBox ?: Cuboid(playerObject.transformComponent.worldPosition, Quat.identity, Vec3(0f))
		val a = PI.toFloat() * .25f
		val left = leftEdge.xz.rotate(a)
		val right = rightEdge.xz.rotate(a)
		val playerLeft = playerBox.run { center + (size * Vec3(-.5f, .5f, .5f)) }.xz.rotate(a)
		val playerRight = playerBox.run { center + (size * Vec3(.5f, .5f, -.5f)) }.xz.rotate(a)

		return if(playerLeft.y < left.y){
			if(playerRight.x < left.x) max(0f, left.x - playerRight.x)
			else if(playerLeft.x > right.x) min(1f, playerLeft.x - right.x)
			else 0f
		}
		else{
			if(playerRight.x < left.x) min(1f, (left - playerRight).length())
			else if(playerLeft.x > right.x) min(1f, (playerLeft - right).length())
			else 1f//min(1f, playerLeft.y - left.y)
		}
	}

	fun renderer(name: String, texName: String, textureCenterPoint: Vec2i, x: Float, y: Float, z: Float, ppu: Float): GameObject{
		return object : GameObject(name){
			override fun addComponents() {
				super.addComponents()
				components.add(mesh(this, texName, textureCenterPoint, x, y, z, ppu))
			}
		}
	}

	fun mesh(o: GameObject, texName: String, textureCenterPoint: Vec2i, x: Float, y: Float, z: Float, ppu: Float): MeshedTextureComponent{
		val tex = TextureLoader[ResourceKey("city/buildings/house1/$texName")]
		return object : MeshedTextureComponent(o, tex, ShaderLoader[ResourceKey("vertex/isometric_shape"), ResourceKey("fragment/isometric_shape")], IsoShape(tex, textureCenterPoint, x, y, z, ppu)){
			override fun setUniforms() {
				super.setUniforms()
				setter()
			}
		}
	}
}