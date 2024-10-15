package com.pineypiney.sonder.util.inventory

class Inventory {
	val slots = Array(10) { ItemStack(Items.AIR, 0) }

	fun addStack(stack: ItemStack) {
		var empty = -1
		for (i in slots.indices) {
			// If the slot is empty
			if (slots[i].isEmpty()) {
				// This keeps a memory of the first empty slot
				// incase this item type is not yet in the inventory
				if (empty == -1) {
					empty = i
				}
				continue
			}
			// Add stack to stack already in inventory
			if (slots[i].item == stack.item) {
				slots[i].size += stack.size
				return
			}
		}
		// Add new stack to inventory
		if (empty != -1) slots[empty] = stack
	}

	fun amount(item: Item): Int {
		var amount = 0
		for (slot in slots) {
			if (slot.item == item) amount += slot.size
		}
		return amount
	}
}

