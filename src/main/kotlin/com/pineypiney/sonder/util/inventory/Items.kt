package com.pineypiney.sonder.util.inventory

class Items {
    companion object {

        val AIR = Item("air");
        val WOOD = Item("wood");

        val items get() = listOf(
            WOOD,
        )

        fun getItem(name: String): Item
        {
            return items.firstOrNull { it.name == name } ?: AIR;
        }
    }
}
