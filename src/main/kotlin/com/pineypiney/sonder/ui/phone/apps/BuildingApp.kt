package com.pineypiney.sonder.ui.phone.apps

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.UpdatingComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.SpriteButton
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.sonder.ui.building_bubble.BuildingBubblePage
import com.pineypiney.sonder.ui.phone.Phone
import com.pineypiney.sonder.util.MenuNode
import glm_.quat.Quat
import glm_.vec3.Vec3
import java.util.*
import kotlin.math.PI

class BuildingApp(parent: GameObject, phone: Phone) : App(parent, phone), UpdatingComponent {

	override val orientation: PhoneOrientation = PhoneOrientation.HORIZONTAL
	override val colour: Vec3 = Vec3.fromHex(0xF2E5E1)

	var page = object : MenuItem() {
		override fun addComponents() {
			super.addComponents()
			components.add(
				BuildingBubblePage(
					this,
					MenuNode(
						emptyMap(),
						MenuNode.readMenuFile("menu_configs/building_menu/building.menu").toMutableList()
					)
				)
			)
		}
	}

	val history = LinkedList<MenuNode>()
	val future = LinkedList<MenuNode>()

	val backButton = SpriteButton(
		"Back Button",
		TextureLoader[ResourceKey("ui/building/back_arrow")],
		Phone.ppu,
		Vec3(-.27f, .09f, .1f)
	) { _, _ -> back() }
	val frwdButton = SpriteButton(
		"Forward Button",
		TextureLoader[ResourceKey("ui/building/forward_arrow")],
		Phone.ppu,
		Vec3(-.22f, .09f, .1f)
	) { _, _ -> forward() }

	override fun endOpen() {
		super.endOpen()
		parent.addAndInitChild(page, backButton, frwdButton)
		parent.rotation = Quat(Vec3(0f, 0f, PI * -.5))
		background.parent.rotate(Vec3(0f, 0f, .5 * PI))
	}

	override fun startClose() {
		super.startClose()
		parent.removeAndDeleteChild(page, backButton, frwdButton)
	}

	fun back() {
		if (history.isNotEmpty()) {
			future.push(page.getComponent<BuildingBubblePage>()!!.page)
			openPage(history.pop())
		}
	}

	fun forward() {
		if (future.isNotEmpty()) {
			history.push(page.getComponent<BuildingBubblePage>()!!.page)
			openPage(future.pop())
		}
	}

	fun openNewPage(page: MenuNode) {
		future.clear()
		history.push(this.page.getComponent<BuildingBubblePage>()!!.page)
		openPage(page)
	}

	fun openPage(node: MenuNode) {
		parent.removeAndDeleteChild(page)

		page = object : MenuItem() {
			override fun addComponents() {
				super.addComponents()
				components.add(BuildingBubblePage(this, node))
			}
		}
		parent.addChild(page)
		page.init()
	}
}