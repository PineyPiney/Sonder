package com.pineypiney.sonder.util.dialogue

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.characters.Character
import com.pineypiney.sonder.ui.dialogue.DialogueBubble

class DialogueUtil {

	fun update(interval: Float, time: Double) {
		if (scriptParser != null) {
			if (currentDialogue?.clicked != false) {
				if (!scriptParser!!.processNextLine()) {
					// It has reached the end of the script
					scriptParser = null
					clearDialogue()
				}
			}
		} else if (currentDialogue?.clicked == true) {
			clearDialogue()
		}
	}

	companion object {

		val scene get() = SonderEngine.INSTANCE.activeScreen

		val dialogueCanvas: GameObject?
			get() {
				return scene.gameObjects.get("DialogueCanvas")
			}


		var scriptParser: ScriptParser? = null

		val currentDialogue: DialogueBubble?
			get() {
				return scene.gameObjects.get<DialogueBubble>()
			}

		fun setDialogue(speaker: Character, dialogue: String) {
			clearDialogue()
			val newDialogue = DialogueBubble(speaker, dialogue)
			//newDialogue.setValues(speaker, dialogue)
			newDialogue.init()
			scene.add(newDialogue)
		}

		fun setDialogue(speaker: String, dialogue: String) {
			val npc = scene.gameObjects.get<GameObject>(speaker)?.getComponent<Character>()
			if (npc != null) setDialogue(npc, dialogue)
		}

		fun setScript(file: String) {
			scriptParser = ScriptParser(file, scene)
		}

		fun clearDialogue() {
			currentDialogue?.delete()
		}

		val dialogueVariables = mutableMapOf<String, Int>()

		fun getVariable(name: String): Int {
			return dialogueVariables[name] ?: 0
		}
	}
}