package com.pineypiney.sonder

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.window.WindowedGameEngine
import com.pineypiney.sonder.scenes.SonderGameLogic
import com.pineypiney.sonder.scenes.TownScene
import com.pineypiney.sonder.scenes.menu.SonderMenu
import com.pineypiney.sonder.scenes.menu.StartMenu
import com.pineypiney.sonder.util.dialogue.DialogueUtil

class SonderEngine : WindowedGameEngine<SonderLogic>(FileResourcesLoader()) {
	override var TARGET_FPS: Int = 1000
	override val TARGET_UPS: Int = 20
	override val window: SonderWindow = SonderWindow.INSTANCE

	val dialogue = DialogueUtil()
	val counter = FPSCounter(GameObject(), 5.0) { GameEngineI.logger.info("FPS is $it") }

	var inputType = ControlType.KEYBOARD

	init {
		GameEngineI.defaultFont = "Large Font"

		FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
	}

	var menu: SonderMenu = StartMenu(this)
	var game: SonderGameLogic = TownScene(this)
	var inMenu = true

	override val activeScreen: SonderLogic get() = if (inMenu) menu else game

	override fun update(interval: Float) {
		super.update(interval)
		dialogue.update(interval, Timer.time)
	}

	override fun render(tickDelta: Double) {
		super.render(tickDelta)
		counter.preRender(tickDelta)
	}

	fun setGame(newGame: SonderGameLogic, delete: Boolean = true) {
		if (delete) game.cleanUp()
		else game.close()
		game = newGame
	}

	// Delete is set false when making a sub menu, and the current menu
	// will be brought back once the player backs out of that menu
	fun setMenu(newMenu: SonderMenu, delete: Boolean = true) {
		if (delete) menu.cleanUp()
		else menu.close()
		menu = newMenu
	}

	fun openGame(init: Boolean = true) {
		if (init) game.init()
		if (inMenu) {
			menu.close()
			inMenu = false
		}
		game.open()
	}

	fun openMenu(init: Boolean = true) {
		if (init) menu.init()
		if (!inMenu) {
			game.close()
			inMenu = true
		}
		menu.open()
	}

	companion object {
		val INSTANCE = SonderEngine()
	}
}