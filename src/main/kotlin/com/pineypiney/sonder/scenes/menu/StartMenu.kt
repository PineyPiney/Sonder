package com.pineypiney.sonder.scenes.menu

import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.scenes.InnScene
import glm_.vec2.Vec2

class StartMenu(engine: SonderEngine) : SonderMenu(engine) {

	private val startButton = TextButton("Start!", Vec2(-.9f, .2f), Vec2(1.8f, .6f)) { _, _ ->
		gameEngine.setGame(InnScene(gameEngine))
		gameEngine.openGame()
	}

	private val quitButton = TextButton("Quit Game", Vec2(-.9f, -.8f), Vec2(1.8f, .6f)) { _, _ ->
		window.shouldClose = true
	}

	override fun addObjects() {
		add(startButton)
		add(quitButton)
	}
}