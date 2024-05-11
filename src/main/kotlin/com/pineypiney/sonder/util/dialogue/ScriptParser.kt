package com.pineypiney.sonder.util.dialogue

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.get
import com.pineypiney.sonder.SonderLogic
import com.pineypiney.sonder.characters.Character
import glm_.i
import glm_.intValue
import glm_.s
import java.io.BufferedReader
import java.io.FileReader

class ScriptParser(val reader: BufferedReader, val scene: SonderLogic) {

    var index: Short = 0
    val loadedLines = mutableListOf<String>()
    val bracketPairs = mutableMapOf<Short, Short>()

    var reachedEnd = false

    constructor(file: String, scene: SonderLogic): this(BufferedReader(FileReader("src/main/resources/dialogue/$file.dlg")), scene)

    fun processNextLine(): Boolean
    {
        val line = getNextLine()
        if(line == "end") return false

        if (line.startsWith("if(")) processIf(line)
        else if (line.startsWith("set ")) processVariable(line)
        else if (line.contains(':')) processSpeech(line)

        index++
        return true
    }

    fun processIf(line: String)
    {
        val query = line.substring(3, line.indexOf(')'))
        val parts = query.split(' ')
        // if the result of the if statement is false skip to the end of the if statement
        if (!queryIf(parts)) index = bracketPairs[index] ?: index
    }

    fun queryIf(parts: List<String>): Boolean
    {
        if (parts.size == 1)
        {
            // If there is just one part then it is a boolean check
            return if (parts[0][0] == '!') DialogueUtil.getVariable(parts[0].substring(1)) == 0
            else DialogueUtil.getVariable(parts[0]) > 0
        }
        if (parts[0].startsWith('@'))
        {
            val c = scene.gameObjects.get<GameObject>(parts[0].substring(1))?.getComponent<Character>() ?: return false
            val func = c.getScriptFunctions()[parts[1]] ?: return false

            // If the function returns 0 then return false
            return func(parts.subList(2, parts.size).joinToString(" ")) != 0
        }
        return false
    }
    fun processVariable(line: String)
    {
        val parts = line.split(' ')
        DialogueUtil.dialogueVariables[parts[1]] = parts[2].intValue()
    }

    fun processSpeech(line: String)
    {
        val parts = line.split(':')
        val speaker: String = parts[0].trim()
        val speech: String = parts[1][(parts[1].indexOf('"') + 1)..<(parts[1].lastIndexOf('"'))]
        DialogueUtil.setDialogue(speaker, speech)
    }

    fun loadLines()
    {
        val lines = mutableListOf<String>()
        val brackpairs = mutableMapOf<Short, Short>()
        var bracketDepth = 0
        val openBrackets = mutableListOf<Short>()
        do
        {
            val newLine = readLine()
            if (newLine == null)
            {
                reachedEnd = true
                break
            }
            for (i in newLine.indices)
            {
                val c = newLine[i]

                if (c == '{')
                {
                    bracketDepth++
                    openBrackets.add(lines.size.s)
                }
                else if (c == '}')
                {
                    bracketDepth--
                    brackpairs[openBrackets.last()] = lines.size.s
                    openBrackets.removeLast()
                }
            }
            lines.add(newLine)
        }
        while (bracketDepth > 0)

        loadedLines.clear()
        loadedLines.addAll(lines)
        bracketPairs.clear()
        bracketPairs.putAll(brackpairs)
        index = 0
    }

    fun getNextLine(): String
    {
        if (index >= loadedLines.size) loadLines()

        if (reachedEnd) return "end"

        return loadedLines[index.i]
    }

    fun readLine(): String?
    {
        var line = ""
        while(line == "")
        {
            line = reader.readLine() ?: return null

            line = line.trim()
            if (line.contains('#')) line = line[0, line.indexOf('#')].trim()
        }
        return line
    }

    fun jumpToLine(line: Int)
    {

    }
}