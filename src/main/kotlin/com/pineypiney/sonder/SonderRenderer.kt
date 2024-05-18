package com.pineypiney.sonder

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.PreRenderComponent
import com.pineypiney.game_engine.objects.components.RenderedComponentI
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.Debug
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class SonderRenderer: BufferedGameRenderer<SonderLogic>() {

    override val window: SonderWindow = SonderWindow.INSTANCE
    override val camera: SonderCamera = SonderCamera()

    override val view = I
    override val projection = I
    override val guiProjection = I

    val guiBuffer = FrameBuffer(0, 0, GL11C.GL_RGBA)

    // DEBUGGING
    val debugMap = (0..4).associateWith { 0 }.toMutableMap()

    override fun init() {
        super.init()
        GLFunc.depthTest = true
        GLFunc.blend = true
        GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)

        guiBuffer.setSize(window.framebufferSize)
        screenUniforms.setIntUniform("effects") { 0 }
    }


    override fun render(game: SonderLogic, tickDelta: Double) {
        val d = Debug().start()
        camera.getView(view)
        camera.getProjection(projection)

        GLFunc.clearColour = game.colour
        clearFrameBuffer()

        drawSky()

        GLFunc.depthTest = true
        d.add() //0

        renderLayer(game, tickDelta, 0) { position.z }

        d.add() //1

        GLFunc.clearColour = Vec4(0f)
        clearFrameBuffer(guiBuffer)

        screenShader.setUp(screenUniforms, this)
        screenShader.setFloat("z", .1f)

        d.add() //2

        renderLayer(game, tickDelta, 1){ -(position - camera.cameraPos).length2() }

        d.add() //3

        GLFunc.depthTest = false
        GLFunc.viewportO = window.framebufferSize
        FrameBuffer.unbind()
        clear()

        screenShader.setUp(screenUniforms, this)
        buffer.draw()
        guiBuffer.draw()

        d.add() //4
        for((i, t) in d.differences().withIndex()) if(t > 1.0) debugMap[i] = debugMap[i]!! + 1
        if(debugMap.values.sum() > 512) {
            GameEngineI.logger.debug("Each stage has count\n" + debugMap.entries.joinToString{ (i, c) -> "$i -> $c" })
            for(i in 0..4) debugMap[i] = 0
        }
    }

    fun renderLayer(game: SonderLogic, tickDelta: Double, layer: Int, sort: GameObject.() -> Float){
        for(o in game.gameObjects[layer].flatMap { it.allActiveDescendants() }.sortedBy { it.sort() }) {
            val renderedComponents = o.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
            val preRendererComponent = o.components.filterIsInstance<PreRenderComponent>()
            if(renderedComponents.isEmpty()){
                // If there are no visible Components only PreRender components that should be PreRendered anyway
                for(c in preRendererComponent) if(!c.whenVisible) c.preRender(tickDelta)
            }
            else {
                for(c in preRendererComponent) c.preRender(tickDelta)
                for(c in renderedComponents) c.render(this, tickDelta)
            }
        }
    }

    fun drawSky(){

        skyShader.use()
        skyShader.setVec3("colours[0]", Vec3.fromHex(0x23465E))
        skyShader.setVec3("colours[1]", Vec3.fromHex(0x476E89))
        skyShader.setVec3("colours[2]", Vec3.fromHex(0x6B99B8))
        skyShader.setVec3("colours[3]", Vec3.fromHex(0xA4C6DD))
        skyShader.setFloat("levels[0]", 0.15f)
        skyShader.setFloat("levels[1]", 0.5f)
        skyShader.setFloat("levels[2]", 0.85f)
        skyShader.setFloat("blendSpace", 0.15f)
        VertexShape.screenQuadShape.bindAndDraw()

    }

    override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
        super.updateAspectRatio(window, objects)
        guiBuffer.setSize(window.framebufferSize)
        GLFunc.viewportO = window.size
        val w = window.aspectRatio
        glm.ortho(-w, w, -1f, 1f, guiProjection)
    }

    override fun deleteFrameBuffers() {
        super.deleteFrameBuffers()
        guiBuffer.delete()
    }

    companion object{
        val skyShader = ShaderLoader[ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/sky_gradient")]
    }
}