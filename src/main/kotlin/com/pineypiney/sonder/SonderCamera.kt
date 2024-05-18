package com.pineypiney.sonder

import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SonderCamera: OrthographicCamera(SonderWindow.INSTANCE) {

	val z = 10f

	fun updateBounds(bound: Vec2){
		cameraMinPos = Vec3(-bound.x, z)
		cameraMaxPos = Vec3(bound.x, z)
		setPos(cameraPos)
	}

	fun setZoom(newHeight: Float){
		height = newHeight.coerceIn(.25f, 10f)
	}

	fun increaseHeight(increase: Float){
		setZoom(height + increase)
	}
}