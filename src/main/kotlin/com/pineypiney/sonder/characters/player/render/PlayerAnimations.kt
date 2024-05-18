package com.pineypiney.sonder.characters.player.render

import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.f
import kotlin.math.PI

class PlayerAnimations {
    companion object{
        private fun getTextures(root: String, vararg textures: String): List<Texture>{
            return textures.map { TextureLoader[ResourceKey("$root/$it")] }
        }

        val playerIdleGen = Animation("idleGen", 20f, "characters/player/body/idle", (1..22).map { "frame_" + "%02d".format(it) }, "characters/player/gen/idle_gen")
        val playerWobbleGen = Animation("wobbleGen", (1..22).associateWith { Animation.KeyFrame(mutableMapOf("RND.txr" to "characters/player/body/idle/IMG_" + "%02d".format(it), "C2D.rtn" to Component.FloatField.float2String((it * 0.095238 * PI).f))) }.toMutableMap(), 12f, 21, "characters/player/gen/wobble_gen")
        val playerWalkGen = Animation("walk", 6f, "characters/player/body/walk", (0..6).map { "IMG_${2991 + it}" }, "characters/player/gen/walk_gen")
        val playerIdle = Animation("characters/player/idle")
        val playerWobble = Animation("characters/player/wobble")
        val playerWalk = Animation("characters/player/walk")
    }
}