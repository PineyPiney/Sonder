package com.pineypiney.sonder.environment

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.GameClickerComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.characters.player.Player
import com.pineypiney.sonder.characters.player.PlayerObject
import com.pineypiney.sonder.util.inventory.ItemStack
import com.pineypiney.sonder.util.inventory.Items

class CherryTree: GameObject() {

    override fun addComponents() {
        super.addComponents()
        components.add(SpriteComponent(this, treeTexture, 250f))
        components.add(GameClickerComponent(this, { objects?.get<PlayerObject>()?.getComponent<Player>()?.inventory?.addStack(ItemStack(Items.WOOD, 5)) }))
    }

    override fun addChildren() {
        super.addChildren()
        addChild(CollisionBox2DRenderer(this))
    }

    companion object{
        val treeTexture = TextureLoader[ResourceKey("environment/cherry_tree")]
    }
}