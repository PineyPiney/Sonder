package com.pineypiney.sonder

import com.pineypiney.game_engine.LibrarySetUp


fun main() {
	LibrarySetUp.initLibraries()
	SonderWindow.INSTANCE.init()
	SonderEngine.INSTANCE.run()
}