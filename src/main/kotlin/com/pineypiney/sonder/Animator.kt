package com.pineypiney.sonder

import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.apps.animator.ObjectAnimator
import com.pineypiney.sonder.characters.Character
import com.pineypiney.sonder.characters.player.RenderedPlayer


fun main() {
    LibrarySetUp.initLibraries()
    ObjectAnimator.run({ RenderedPlayer() }){ it.getComponent<Character>()?.gravity = false }
}