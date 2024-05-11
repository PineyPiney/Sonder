package com.pineypiney.sonder

import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.sonder.scenes.SonderGamePlay
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SonderCamera: OrthographicCamera(SonderWindow.INSTANCE) {

	val zoom get() = height

	fun updateBounds(bound: Vec2){
		val size = getSpan() * .5f
		cameraMinPos = Vec3(-bound, 5f) + Vec3(size)
		cameraMaxPos = Vec3(bound, 5f) - Vec3(size)
		setPos(cameraPos)
	}

	fun setZoom(newHeight: Float){
		height = newHeight.coerceIn(.25f, 10f)
		updateBounds(Vec2((SonderEngine.INSTANCE.game as SonderGamePlay).size, 5f))
	}

	fun increaseHeight(increase: Float){
		setZoom(height + increase)
	}
}