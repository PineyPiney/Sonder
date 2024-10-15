package com.pineypiney.sonder.characters

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.Rigidbody3DComponent
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.util.Animation

abstract class Character(parent: GameObject) : Component(parent, "CHR"), UpdatingComponent {

	override val fields: Array<Field<*>> = arrayOf()

	var gravity: Boolean
		get() = parent.getComponent<Rigidbody3DComponent>()?.let { it.gravity.length2() > 0 } ?: false
		set(value) {
			parent.getComponent<Rigidbody3DComponent>()?.gravity?.y = if (value) -20f else 0f
		}

	var animation: Animation?
		get() = parent.allDescendants().firstNotNullOfOrNull { it.getComponent<AnimatedComponent>() }?.animation
		set(value) {
			value?.let { v ->
				parent.allDescendants().firstNotNullOfOrNull { it.getComponent<AnimatedComponent>() }?.animation = v
			}
		}

	fun setAnimation(name: String) = parent.getComponent<AnimatedComponent>()?.setAnimation(name)

	override fun update(interval: Float) {

	}

	open fun getScriptFunctions(): Map<String, (String) -> Int> = emptyMap()
}