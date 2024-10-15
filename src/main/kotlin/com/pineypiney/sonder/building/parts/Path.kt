package com.pineypiney.sonder.building.parts

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.colliders.CompoundCollider3DComponent
import com.pineypiney.game_engine.objects.util.shapes.IndicesShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.manhattan
import com.pineypiney.game_engine.util.maths.shapes.*
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.sonder.building.PlacementRule
import com.pineypiney.sonder.city.paths.PathRenderer
import com.pineypiney.sonder.scenes.SonderGamePlay
import com.pineypiney.sonder.util.BitMap
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2ui
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xz
import glm_.vec4.swizzle.zw
import unsigned.toUint
import kotlin.math.min
import kotlin.math.roundToInt

class Path(parent: GameObject) : BuildingPart(parent, "PTH"), InteractorComponent {

	constructor(parent: GameObject, vararg tiles: Vec2i) : this(parent) {
		addParts(*tiles.toSet().toTypedArray())
		refreshRenderers()
	}

	constructor(parent: GameObject, vararg parts: Rect) : this(parent) {
		addParts(*parts.flatMap { getPartsFromRect(it) }.toTypedArray())
		refreshRenderers()
	}

	override val fields: Array<Field<*>> = arrayOf(
		Field("bmp", ::DefaultFieldEditor, ::bitMap, { bitMap = it }, { "" }, { c, s -> mutableMapOf() }) { map ->
			val copy = LinkedHashMap<Vec2i, BitMap>(map.size)
			for ((c, m) in map) copy[Vec2i(c)] = BitMap(m)
			copy
		}
	)
	override val placementRule: PlacementRule = PlacementRule.PLACE_TILED

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var importance: Int = 0

	var bitMap = mutableMapOf<Vec2i, BitMap>()

	init {
		parent.components.add(CompoundCollider3DComponent(parent, CompoundShape3D(mutableSetOf())))
	}

	override fun init() {
		super.init()
		parent.deleteAllChildren()
		refreshRenderers()
		splitIntoChunks()
	}

	fun clearParts() {
		bitMap.clear()
		parent.deleteAllChildren()
	}

	fun addParts(vararg parts: Vec2i) {
		for (tile in parts) {
			val tilePos = parent.transformComponent.worldPosition.xz + tile
			val chunk = getChunk(tilePos)
			val chunkPos = getChunkPos(tilePos)

			val currentMask = bitMap[chunk]
			if (currentMask != null) currentMask orAssign chunkPos
			else bitMap[chunk] = BitMap(chunkPos)
		}
	}

	fun addParts(vararg parts: Rect) {
		addParts(*parts.flatMap { getPartsFromRect(it) }.toTypedArray())
	}

	infix fun consume(other: Path) {
		for ((c, m) in other.bitMap) {
			val map = bitMap[c]
			if (map != null) map orAssign m
			else bitMap[c] = m
		}
	}

	override fun onMove(oldLocation: Vec3, newLocation: Vec3) {
		super.onMove(oldLocation, newLocation)
		val mask = getChunkPathMask()
		val rects = generateRects(mask)
	}

	override fun onPlace(scene: SonderGamePlay) {
		super.onPlace(scene)
		val tilePos = Vec2i(parent.position.xz)
		val allPaths = scene.gameObjects.getAllComponentInstances<Path>().filter { path -> path != this }
		val paths = allPaths.filter {
			val tiles = it.getAllTiles()
			tiles.any { tile -> (tile - tilePos).manhattan() <= 1 }
		}

		if (paths.isEmpty()) {
			parent.position = Vec3(0f)
			clearParts()
			addParts(tilePos)
			refreshRenderers()
		} else {
			val path = paths.first()
			path.addParts(tilePos)
			if (paths.size > 1) {
				for (i in 1..<paths.size) {
					path consume paths[i]
					scene.remove(paths[i].parent)
					paths[i].parent.delete()
				}
			}
			path.refreshRenderers()
			parent.delete()
		}
	}

	fun getChunkPos(worldPos: Vec2 = parent.position.xz): Vec2i =
		Vec2i(worldPos.x.toInt().mod(CHUNK_SIZE), worldPos.y.toInt().mod(CHUNK_SIZE))

	fun getChunk(worldPos: Vec2 = parent.position.xz): Vec2i {
		return Vec2i((worldPos - getChunkPos(worldPos)) * INV_CHUNK_SIZE)
	}

	fun getPathsInChunk(): Set<Path> {
		val chunk = getChunk()
		val objects = parent.objects ?: return emptySet()
		return objects.getAllComponentInstances<Path>(parent.layer).filter { it.getChunk() == chunk && it != this }
			.toSet()
	}

	fun getPathMask(): BitMap {
		val chunkPos = Vec2i(getChunkPos())
		val scale = Vec2i(parent.scale.xz)

		return BitMap(Rect(chunkPos, scale))
	}

	fun getChunkPathMask(): BitMap {
		val map = BitMap()
		for (path in getPathsInChunk()) {
			map orAssign path.getPathMask()
		}

		return map
	}

	fun getAllTiles(): Set<Vec2i> {
		return bitMap.flatMap { (c, m) ->
			m.allTrue().map { it + (c * 8) }
		}.toSet()
	}

	fun generateRects(bitmap: BitMap): List<Rect> {
		val mask = BitMap(bitmap)
		val list = mutableListOf<Rect>()
		var range: IntRange = -1..-1
		for (x in 0..7) {
			for (y in 0..7) {
				val bit = mask.check(x, y)
				if (bit) {
					if (range.first == -1) {
						// Starting new column
						range = y..-1
					}
					if (y == 7) {
						// Finish size of column to top of chunk
						range = range.first..7
					}
				} else {
					if (range.first != -1) {
						// Finish size of column to y
						range = range.first..<y
					}
				}
				if (range.last != -1) {
					var boxX = x
					while (boxX < 7) {
						val column = range.all { bitmap.check(boxX + 1, it) }
						if (column) boxX++
						// This is the end of the box
						else break
					}
					val rect = Rect(x, range.first, boxX + 1 - x, range.last + 1 - range.first)
					mask andNot rect
					list.add(rect)
					range = -1..-1
				}
			}
		}
		return list
	}

	fun generateMesh(rects: List<Rect>): VertexShape {
		val verts = mutableListOf<Float>()
		val indices = mutableListOf<Int>()
		for ((index, rec) in rects.withIndex()) {
			val o = Vec2(rec.origin)
			val p = o + Vec2(rec.size)
			val i = index * 8
			verts.addAll(
				o.x, 0f, o.y,
				p.x, 0f, o.y,
				p.x, 0f, p.y,
				o.x, 0f, p.y,
				o.x, 0.2f, o.y,
				p.x, 0.2f, o.y,
				p.x, 0.2f, p.y,
				o.x, 0.2f, p.y,
			)
			indices.addAll(
				// Z Face
				i + 3, i + 7, i + 6,
				i + 6, i + 2, i + 3,
				// X Face
				i + 2, i + 6, i + 5,
				i + 5, i + 1, i + 2,
				// Y Face
				i + 4, i + 5, i + 6,
				i + 6, i + 7, i + 4
			)
		}
		return object : IndicesShape(verts.toFloatArray(), intArrayOf(3), indices.toIntArray()) {
			override val shape: Shape2D = Rect2D(Vec2(), Vec2(8f))
		}
	}

	fun generateColliderShape(list: List<Rect>, chunk: Vec2i): Set<Shape3D> {
		val pos = Vec2(chunk * 8)
		return list.map {
			val size = Vec2(it.size)
			val center = pos + Vec2(it.origin) + (size * .5f)
			Cuboid(Vec3(center.x, 0.1f, center.y), Quat.identity, Vec3(size.x, .2f, size.y))
		}.toSet()
	}

	fun refreshRenderers() {
		val renderers = parent.children.mapNotNull { it.getComponent<PathRenderer>() }
		val shape = parent.getComponent<CompoundCollider3DComponent>()?.shape
		val meshes = bitMap.mapValues {
			val rects = generateRects(it.value)
			shape?.shapes?.addAll(generateColliderShape(rects, it.key))
			generateMesh(rects)
		}
		for ((chunk, mesh) in meshes) {
			val map = bitMap[chunk]!!
			val chunkPos = Vec3(chunk.x * 8f, 0f, chunk.y * 8f)
			val renderChild: PathRenderer = renderers.firstOrNull { it.parent.position == chunkPos } ?: PathRenderer(
				GameObject("Path Renderer"),
				mesh
			).applied().also {
				it.parent.position = Vec3(chunk.x * 8f, 0f, chunk.y * 8f)
				parent.addChild(it.parent)
				it.init()
				it.selected = hover
			}
			renderChild.vShape = mesh
			renderChild.gridMask.x = map.getmap().toLong().toUint()
			renderChild.gridMask.y = (map.getmap() shr 32).toLong().toUint()
		}
		val borderChunks = bitMap.keys.flatMap { c -> (0..8).map { i -> c + Vec2i((i % 3) - 1, (i / 3) - 1) } }.toSet()
		for (bc in borderChunks) {
			val worldPos = Vec3(bc.x * 8, 0f, bc.y * 8)
			parent.children.firstOrNull { it.position == worldPos }?.getComponent<PathRenderer>()?.let {
				it.gridMask.zw = getBorderMask(bc)
			}
		}
	}

	fun getBorderMask(chunk: Vec2i): Vec2ui {
		val bl = if (bitMap[chunk + Vec2i(-1, -1)]?.check(7, 7) == true) 0x8ffffff else 0
		val left: Int = bitMap[chunk + Vec2i(-1, 0)]?.let { m ->
			(0..7).sumOf { r ->
				val c = m.check(7, r); if (c) 1 shl (26 - r) else 0
			}
		} ?: 0
		val tl = if (bitMap[chunk + Vec2i(-1, 1)]?.check(7, 0) == true) 0x4ffff else 0
		val top: Int = bitMap[chunk + Vec2i(0, 1)]?.let { m ->
			(0..7).sumOf { r ->
				val c = m.check(r, 0); if (c) 1 shl (17 - r) else 0
			}
		} ?: 0
		val tr = if (bitMap[chunk + Vec2i(1, 1)]?.check(0, 0) == true) 0x2ff else 0
		val right: Int = bitMap[chunk + Vec2i(1, 0)]?.let { m ->
			(0..7).sumOf { r ->
				val c = m.check(0, r); if (c) 1 shl (1 + r) else 0
			}
		} ?: 0
		val br = if (bitMap[chunk + Vec2i(1, -1)]?.check(0, 7) == true) 1 else 0
		val bottom: Int = bitMap[chunk + Vec2i(0, -1)]?.let { m ->
			(0..7).sumOf { r ->
				val c = m.check(r, 7); if (c) 1 shl r else 0
			}
		} ?: 0
		return Vec2ui(bl or left or tl or top or tr or right or br, bottom)
	}

	fun splitIntoChunks() {
		val cp = getChunkPos()
		val s = Vec2i(parent.scale.xz)
		val farCorner = cp + s
		val chunks = Vec2i(Vec2(farCorner - Vec2i(1)) * INV_CHUNK_SIZE)

		// Split the path so that it doesn't cross any chunks
		if (chunks.x > 0 || chunks.y > 0) {
			for (x in 0..chunks.x) {
				for (y in 0..chunks.y) {
					if (x == 0 && y == 0) {
						/*
						parent.scale = Vec3(min(8 - cp.x, s.x), parent.scale.y, min(8 - cp.y, s.y))
						if(chunks.x > 0){
							renderer.xzEdgeMask.x = renderer.xzEdgeMask.x or (2 pow parent.scale.z.toInt()).toUint() - 1
							renderer.yEdgeMask = renderer.yEdgeMask or 12u
						}
						if(chunks.y > 0){
							renderer.xzEdgeMask.z = renderer.xzEdgeMask.z or (2 pow parent.scale.x.toInt()).toUint() - 1
							renderer.yEdgeMask = renderer.yEdgeMask or 10u
						}

						 */
					} else {
						val chunkFarCorner = farCorner - (Vec2i(x, y) * CHUNK_SIZE)
						val newPath = parent.copy()
						newPath.position = Vec3(
							if (x == 0) parent.position.x else parent.position.x - cp.x + (x * CHUNK_SIZE),
							parent.position.y,
							if (y == 0) parent.position.z else parent.position.z - cp.y + (y * CHUNK_SIZE)
						)
						newPath.scale = Vec3(
							if (x == 0) min(CHUNK_SIZE - cp.x, s.x) else min(chunkFarCorner.x, CHUNK_SIZE),
							parent.scale.y,
							if (y == 0) min(CHUNK_SIZE - cp.y, s.y) else min(chunkFarCorner.y, CHUNK_SIZE)
						)
						parent.objects?.addObject(newPath)
						val path = newPath.getComponent<Path>() ?: continue
					}
				}
			}
		}
	}

	override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
		return parent.getComponent<Collider3DComponent>()?.transformedShape?.intersectedBy(ray)?.isNotEmpty() ?: false
	}

	override fun onCursorEnter(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorEnter(window, cursorPos, cursorDelta, ray)
		for (c in parent.children) c.getComponent<PathRenderer>()?.selected = true
	}

	override fun onCursorExit(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorExit(window, cursorPos, cursorDelta, ray)
		for (c in parent.children) c.getComponent<PathRenderer>()?.selected = false
	}

	data class Rect(val origin: Vec2i, val size: Vec2i) {
		constructor(ox: Int, oy: Int, sx: Int, sy: Int) : this(Vec2i(ox, oy), Vec2i(sx, sy))
	}

	companion object {
		const val CHUNK_SIZE = 8
		const val INV_CHUNK_SIZE = 0.125f

		val PLACE_PATH = PlacementRule { obj, scene, ray ->
			val rayMult = ray.rayOrigin.y / ray.direction.y
			val pos = ray.rayOrigin - (ray.direction * rayMult)

			if (obj.objects == null) scene.add(obj)
			obj.position = Vec3((pos.x - .5f).roundToInt(), 0f, (pos.z - .5f).roundToInt())
		}

		fun getPartsFromRect(rect: Rect): List<Vec2i> {
			return List((rect.size.x * rect.size.y)) { i ->
				Vec2i(rect.origin) + Vec2i(
					i.mod(rect.size.x),
					(i / rect.size.x)
				)
			}
		}
	}
}