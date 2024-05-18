package com.pineypiney.sonder.characters.player

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.sonder.characters.Character
import com.pineypiney.sonder.util.inventory.Inventory
import com.pineypiney.sonder.util.inventory.Items
import glm_.parseInt

class Player(parent: GameObject) : Character(parent) {

	val inventory = Inventory()

	override fun getScriptFunctions(): Map<String, (String) -> Int> {
		return super.getScriptFunctions() + ("has" to ::has)
	}

	fun has(details: String): Int{
		val parts = details.split(' ')
		return if(inventory.amount(Items.getItem(parts[1])) >= parts[0].parseInt()) 1 else 0
	}

}