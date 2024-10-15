package com.pineypiney.sonder.ui.character_customisation

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.HoverComponent
import com.pineypiney.game_engine.objects.components.PostChildrenInit
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.sonder.util.MenuNode
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy

class CharacterPartComponent(parent: GameObject, val type: String, val onClick: (String) -> Unit) : Component(
	parent,
	"CHR"
), PostChildrenInit {

	override val fields: Array<Field<*>> = arrayOf()

	override fun init() {
		super.init()
		createCategories()
	}

	override fun postChildrenInit() {
		parent.children.filterIsInstance<HoverButton>().forEachIndexed(::positionHoverButton)
	}

	private fun createCategories() {

		clear()
		val menu = MenuNode.readMenuFile("menu_configs/character_menu/$type.menu")

		menu.forEachIndexed { i, category ->
			val submenu = CharacterPartSubMenu(CharacterPart.CharacterMenuSettings(category), category, onClick)
			submenu.position = Vec3(0f, 0f, .02f)
			submenu.active = false
			parent.addChild(submenu)

			val hoverButton = HoverButton(
				Texture.broke,
				{ (it.parent as HoverButton).selector?.active = true }) {
				if (parent.getComponent<DefaultInteractorComponent>()?.hover != true) (it.parent as HoverButton).selector?.active =
					false
			}
			parent.addChild(hoverButton)
			category.with("icon") { icon ->
				hoverButton.icon = TextureLoader[ResourceKey("ui/character_customisation/$icon")]
			}
			hoverButton.selector = submenu
		}
	}

	fun positionHoverButton(i: Int, button: HoverButton) {

		val buttonSize = button.scale.xy * button.renderer!!.renderSize
		val menuSize = parent.renderer!!.renderSize

		button.position = Vec3(buttonSize.x * i, menuSize.y - (buttonSize.y * .4f), .01f)
	}

	fun getSelected(): HoverButton? {
		return parent.children.filterIsInstance<HoverButton>()
			.firstOrNull { it.getComponent<HoverComponent>()?.hover == true }
	}

	private fun clear() {
		for (c in parent.children) {
			if (c is HoverButton || c is CharacterPart) {
				(c as Deleteable).delete()
			}
		}
	}
}