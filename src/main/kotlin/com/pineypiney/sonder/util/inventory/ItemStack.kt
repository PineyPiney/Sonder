package com.pineypiney.sonder.util.inventory

class ItemStack(val item: Item, var size: Int) {

    fun isEmpty(): Boolean {
        return item == Items.AIR || size == 0;
    }

    override fun toString(): String {
        return "ItemStack[$item, $size]"
    }

}
