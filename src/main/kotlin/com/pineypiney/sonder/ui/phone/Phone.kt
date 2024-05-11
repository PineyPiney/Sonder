package com.pineypiney.sonder.ui.phone

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.serp
import com.pineypiney.sonder.SonderWindow
import com.pineypiney.sonder.scenes.SonderGamePlay
import com.pineypiney.sonder.ui.phone.apps.PhoneOrientation
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.PI

class Phone(parent: GameObject, val game: SonderGamePlay): Component(parent, "PHN"), PreRenderComponent, UpdatingAspectRatioComponent {

	override val fields: Array<Field<*>> = arrayOf()
	override val whenVisible: Boolean = false

	// true: opening
	// false: closing
	// null: stationary
	var open: Boolean = false; private set
	// varies from 0 to 1
	var openStage = 0f

	var orientation = PhoneOrientation.VERTICAL
	var orientationStage = 0f

	val case = object : MenuItem(){
		init {
			position = Vec3(0f, 0f, -.1f)
		}
		override fun addComponents() {
			super.addComponents()
			components.add(SpriteComponent(this, TextureLoader[ResourceKey("ui/phone/case")], ppu, SpriteComponent.menuShader))
		}
	}
	val screen = PhoneScreen(MenuItem(), this@Phone).applied()

	override fun init() {
		super.init()

		parent.addChild(case)
		parent.addChild(screen.parent)

	}

	fun open(){
		open = true
		parent.active = true
	}

	fun close(){
		open = false
	}

	override fun preRender(tickDelta: Double) {
		val interval = Timer.frameDelta.f
		if(interval <= 0) return
		var change = false
		when(open){
			true -> {
				if(openStage < 1.0) {
					openStage = (openStage + (interval * openSpeed)).coerceIn(0f, 1f)
					change = true
				}
			}
			false -> {
				if(openStage > 0.0) {
					openStage = (openStage - (interval * openSpeed)).coerceIn(0f, 1f)
					change = true
					if(openStage == 0f) parent.active = false
				}
			}
		}
		if(openStage == 1f){
			when(orientation){
				PhoneOrientation.HORIZONTAL -> {
					if(orientationStage < 1.0) {
						orientationStage = (orientationStage + (interval * orientationSpeed)).coerceIn(0f, 1f)
						change = true
					}
				}
				PhoneOrientation.VERTICAL -> {
					if(orientationStage > 0.0) {
						orientationStage = (orientationStage - (interval * orientationSpeed)).coerceIn(0f, 1f)
						change = true
					}
				}
			}
		}

		if(change) position()
	}

	fun position(){

		if(orientationStage == 0f) {
			parent.position = Vec3((.43f * case.renderer!!.renderSize.x) - SonderWindow.INSTANCE.aspectRatio, (((openStage.serp() * .8f) - .4f) * case.renderer!!.renderSize.y) - 1f, parent.position.z)
			parent.rotation = Quat(1f, 0f, 0f, 0f)
			parent.scale = Vec3(1f, 1f, 1f)
		}
		else {
			parent.position = Vec3((Vec2(.43f, .4f) * case.renderer!!.renderSize - Vec2(SonderWindow.INSTANCE.aspectRatio, 1f)) * (1.0 - orientationStage), parent.position.z)
			parent.rotation = Quat(Vec3(0f, 0f, (orientationStage * PI * .5).f))
			parent.scale = Vec3(Vec2(1f + (1.4f * orientationStage)), 1f)
		}
	}

	override fun updateAspectRatio(renderer: RendererI<*>) {
		position()
	}

	companion object {
		val ppu = 1600f
		val openSpeed = 5f
		var orientationSpeed = 1.5f
	}
}