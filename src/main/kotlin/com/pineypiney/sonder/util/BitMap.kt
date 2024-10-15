package com.pineypiney.sonder.util

import com.pineypiney.sonder.building.parts.Path
import glm_.pow
import glm_.vec2.Vec2i
import java.math.BigInteger

class BitMap(private var map: ULong = 0uL) {

	constructor(x: Int, y: Int) : this() {
		orAssign(x, y)
	}

	constructor(point: Vec2i) : this() {
		orAssign(point.x, point.y)
	}

	constructor(rect: Path.Rect) : this(createBitMap(rect))

	constructor(bitMap: BitMap) : this(bitMap.map)

	infix fun or(value: ULong): ULong {
		return map or value
	}

	infix fun orAssign(value: ULong) {
		map = map or value
	}

	infix fun orAssign(value: BitMap) {
		map = map or value.map
	}

	infix fun orAssign(point: Vec2i) {
		orAssign(point.x, point.y)
	}

	fun orAssign(x: Int, y: Int) {
		orAssign((0x80uL shr x) * (BigInteger.valueOf(256).pow(y)).toLong().toULong())
	}

	infix fun andNot(rect: Path.Rect) {
		val mask = createBitMap(rect).inv()
		map = map and mask
	}

	fun check(x: Int, y: Int): Boolean {
		val bitShift = (y shl 3) or (7 - x)
		val shift = map shr bitShift
		return shift and 1uL == 1uL
	}

	infix fun check(point: Vec2i): Boolean {
		return (map shr ((point.y shl 3) and point.x)) and 1uL == 1uL
	}

	fun allTrue(): Set<Vec2i> {
		val set = mutableSetOf<Vec2i>()
		for (x in 0..7) for (y in 0..7) if (check(x, y)) set.add(Vec2i(x, y))
		return set
	}

	fun getmap() = map

	override fun toString(): String {
		return map.toString(2).padStart(64, '0').chunked(8).joinToString("\n")
	}

	companion object {
		fun createBitMap(rect: Path.Rect): ULong {
			val xMask = ((2 pow rect.size.x) - 1).toULong() shl (8 - (rect.origin.x + rect.size.x))
			val yMask = (((BigInteger.valueOf(256)
				.pow(rect.size.y) - BigInteger.valueOf(1)).div(BigInteger.valueOf(255))) * BigInteger.valueOf(256)
				.pow(rect.origin.y)).toLong().toULong()
			return xMask * yMask
		}
	}
}