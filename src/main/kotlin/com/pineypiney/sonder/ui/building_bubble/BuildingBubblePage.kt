package com.pineypiney.sonder.ui.building_bubble

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.removeNullValues
import com.pineypiney.sonder.SonderEngine
import com.pineypiney.sonder.building.parts.BuildingPart
import com.pineypiney.sonder.building.parts.Parts
import com.pineypiney.sonder.scenes.SonderGamePlay
import com.pineypiney.sonder.ui.phone.Phone
import com.pineypiney.sonder.ui.phone.apps.BuildingApp
import com.pineypiney.sonder.util.MenuNode
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C
import kotlin.math.max

class BuildingBubblePage(parent: GameObject, val page: MenuNode) : Component(parent, "BBP") {

	override val fields: Array<Field<*>> = arrayOf()

	override fun init() {
		super.init()

		addButtons()
	}

	private fun addButtons() {
		for ((i, node) in page.children.withIndex()) {
			if (node.children.isEmpty()) {
				val prefabName = node["prefab"] ?: page["prefab"] ?: "Plant"
				val prefab = Parts.parts.firstOrNull { it.name == prefabName } ?: Parts.PLANT

				val folder = node["folder"] ?: page["folder"] ?: ""
				val properties = node.tags.mapValues { (k, v) -> processProperties(folder, k, v) }.removeNullValues()
				val texture = properties["sprite"] as? Texture ?: renderPrefabToTexture(prefab.copy())

				val newButton = SpriteButton(texture, Phone.ppu, Vec3((i * .1f) - .28f, 0f, .1f)) { _, _ ->

					val new = prefab.copy()
					new.getComponent<BuildingPart>()?.setProperties(properties)
					new.init()
					(SonderEngine.INSTANCE.game as? SonderGamePlay)?.let {
						it.placer.item = new
					}
				}

				val childrenMap = properties["sprites"] as? Map<*, *>
				if (childrenMap != null) {
					for ((k, v) in childrenMap) {
						val sprite = k as Texture
						val vec = v as Vec3
						val component = SpriteComponent(
							MenuItem(),
							sprite,
							Phone.ppu,
							SpriteComponent.menuShader,
							VertexShape.cornerSquareShape
						).applied()
						val shape = prefab.getComponent<BuildingPart>()!!.shape
						val point = shape.bl
						val parentPoint =
							Vec2(0f) // The bl of the SquareShape used to render the main image of the sprite component, in this case it's the corner square
						val offset = parentPoint - point
						val parentTex = properties["sprite"] as Texture
						val offsetVec = (offset * (parentTex.size - sprite.size)) + Vec2(vec)
						newButton.addChild(component.parent.apply { position = Vec3(offsetVec / Phone.ppu, .1f) })
					}
				}

				parent.addChild(newButton)

				val scaling = 160f / max(texture.width, texture.height)
				if (scaling < 1f) newButton.scale = Vec3(scaling, scaling, 1f)
			} else {
				parent.addChild(
					SpriteButton(
						TextureLoader[ResourceKey("ui/${node["icon"]}")],
						Phone.ppu,
						Vec3((i * .1f) - .28f, 0f, .1f)
					) { _, _ ->
						parent.parent?.getComponent<BuildingApp>()?.openNewPage(node)
					})
			}
		}
	}

	private fun processProperties(folder: String, key: String, value: String): Any? {
		val folderS = if (folder.isEmpty()) "" else "$folder/"
		return when (key) {
			"sprite" -> TextureLoader[ResourceKey(folderS + value)]
			"sprites" -> getSubSprites(folderS, value)
			"surface" -> value.split(';').let { Vec3.fromString(it[0], false) to Vec2.fromString(it[1], false) }
			else -> null
		}
	}

	fun getSubSprites(folder: String, string: String): Map<Texture, Vec3> {
		val sprites = string.split(';')
		return sprites.associate { sprite ->

			val index = sprite.indexOf('(')
			val (tex, vec) = if (index > 0) {
				sprite.substring(0, index).trim() to Vec3.fromString(
					sprite.substring(index + 1).substringBeforeLast(')')
				)
			} else sprite.trim() to Vec3(0f)

			TextureLoader[ResourceKey("$folder$tex")] to vec
		}
	}

	private fun renderPrefabToTexture(prefab: GameObject): Texture {
		prefab.init()
		prefab.position = Vec3(0f, 0f, -.3f)
		prefab.rotation = SonderGamePlay.isometricRotation.inverse()

		val buffer = FrameBuffer(128, 128, GL11C.GL_RGBA)
		buffer.generate()
		buffer.bind()
		GLFunc.viewportO = Vec2i(128)
		GLFunc.depthTest = true
		partRenderer.clear()
		for (d in prefab.allActiveDescendants().sortedBy { it.position.z }) d.getComponent<RenderedComponentI>()
			?.render(partRenderer, 0.0)
		FrameBuffer.unbind()

		return Texture("${prefab.name} render", buffer.TCB)
	}

	companion object {
		val partRenderer = object : RendererI {
			override val viewPos: Vec3 = Vec3(39.2f, 40, 39.2)
			override val view: Mat4 =
				Mat4(1f);//glm.lookAt(viewPos, viewPos - Vec3(sqrt(0.33333334f)), Vec3(0f, 1f, 0f))
			override val projection: Mat4 = glm.ortho(-1f, 1f, -1f, 1f)
			override val guiProjection: Mat4 = projection
			override val viewportSize: Vec2i = Vec2i(128)
			override val aspectRatio: Float = 1f

			override fun init() {}
			override fun delete() {}
		}
	}
}