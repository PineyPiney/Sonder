package com.pineypiney.sonder.util

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.SonderEngine
import java.io.InputStream

class MenuNode(val tags: Map<String, String>, val children: MutableList<MenuNode>)
{

    constructor(tags: Map<String, String>): this(tags, mutableListOf())

    fun containsKey(key: String) = tags.containsKey(key)

    fun with(key: String, predicate: (String) -> Unit)
    {
        tags[key]?.run(predicate)
    }

    operator fun get(s: String) = tags[s]

    companion object{
        private fun getParts(line: String): Map<String, String> {
            val dict = mutableMapOf<String, String>()
            val parts = line.split(':')
            for (part in parts)
            {
                val index = part.indexOf('[') + 1
                val indey = part.indexOf(']')
                val d = part.substring(index, indey)
                val f = part.substring(indey + 1).trim()
                dict[d] = f
            }
            return dict
        }

        private fun indent(s: String): Int
        {
            var i = 0
            while(s[i] == '\t')
            {
                i++
            }
            return i
        }

        fun readMenuFile(file: String) = readMenuFile(SonderEngine.INSTANCE.resourcesLoader.getStream(file)!!, file)
        fun readMenuFile(file: InputStream, name: String): List<MenuNode>{
            val list = mutableListOf<MenuNode>()
            val lines = file.readAllBytes().toString(Charsets.UTF_8).split('\n')

            val parents = mutableListOf<MenuNode>()

            for((i, line) in lines.withIndex())
            {
                if(line.trim().isEmpty()) continue

                val indent = indent(line)

                val indentStep = 1 + indent - parents.size
                if(indentStep > 1)
                {
                    GameEngineI.warn("Indent is too large on line $i in file $file")
                }
                else if(indentStep <= 0)
                {
                    if (parents.size > 0) {
                        for(x in indentStep..0) parents.removeLast()
                    }
                }


                val newNode = MenuNode(getParts(line))
                if(newNode.containsKey("file")){
                    val newNodes = readMenuFile((ResourceKey(name).parentFolder + newNode["file"]!!).key)

                    if (parents.size > 0) parents.last().children.addAll(newNodes)
                    parents.addAll(newNodes)

                    if(indent == 0)
                    {
                        list.addAll(newNodes)
                    }
                }

                else{
                    if (parents.size > 0) parents.last().children.add(newNode)
                    parents.add(newNode)

                    if(indent == 0)
                    {
                        list.add(newNode)
                    }
                }

            }
            file.close()
            return list
        }

        fun <K, V> readMenuFileDict(fileDirectory: InputStream, name: String, predicate: (MenuNode, Int) -> Pair<K, V>): Map<K, V>{
            val list = readMenuFile(fileDirectory, name)
            return list.associate { l -> predicate(l, 0) }
        }
    }
}
