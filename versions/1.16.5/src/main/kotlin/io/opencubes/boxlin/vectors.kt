package io.opencubes.boxlin

import net.minecraft.dispenser.IPosition
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.*

operator fun Vector2f.unaryMinus(): Vector2f = Vector2f(-x, -y)
operator fun Vector3f.unaryMinus(): Vector3f = Vector3f(-x(), -y(), -z())
operator fun Vector3d.unaryMinus(): Vector3d = Vector3d(-x, -y, -z)
operator fun Vector3i.unaryMinus(): Vector3i = Vector3i(-x, -y, -z)
operator fun BlockPos.unaryMinus(): BlockPos = BlockPos(-x, -y, -z)
operator fun Vector4f.unaryMinus(): Vector4f = Vector4f(-x(), -y(), -z(), -w())
private operator fun IPosition.unaryMinus(): IPosition = object : IPosition {
  override fun x(): Double = -this@unaryMinus.x()
  override fun y(): Double = -this@unaryMinus.y()
  override fun z(): Double = -this@unaryMinus.z()
}

// Plus

// BlockPos
operator fun BlockPos.plus(other: BlockPos): BlockPos = BlockPos(x + other.x, y + other.y, z + other.z)
operator fun BlockPos.plus(other: Int): BlockPos = BlockPos(x + other, y + other, z + other)
operator fun BlockPos.plus(other: Vector3i): BlockPos = BlockPos(other)
operator fun BlockPos.plus(other: Vector3d): BlockPos = BlockPos(other)
operator fun BlockPos.plus(other: IPosition): BlockPos = BlockPos(other)
operator fun Int.plus(other: BlockPos): BlockPos = other.plus(this)
// Vector3i
operator fun Vector3i.plus(other: Int): Vector3i = Vector3i(x + other, y + other, z + other)
operator fun Int.plus(other: Vector3i): Vector3i = other.plus(this)
operator fun Vector3i.plus(other: BlockPos): Vector3i = Vector3i(x + other.x, y + other.y, z + other.z)
// Vector3d
operator fun Vector3d.plus(other: Double): Vector3d = Vector3d(x + other, y + other, z + other)
operator fun Double.plus(other: Vector3d): Vector3d = other.plus(this)
operator fun Vector3d.plus(other: IPosition): Vector3d = Vector3d(x + other.x(), y + other.y(), z + other.z())
// Vector3f
operator fun Vector3f.plus(other: Float): Vector3f = Vector3f(x() + other, y() + other, z() + other)
operator fun Float.plus(other: Vector3f): Vector3f = other.plus(this)
operator fun Vector3f.plus(other: IPosition): Vector3f = Vector3f(x() + other.x().toFloat(), y() + other.y().toFloat(), z() + other.z().toFloat())
// Vector2f
operator fun Vector2f.plus(other: Float): Vector2f = Vector2f(x + other, y + other)
operator fun Float.plus(other: Vector2f): Vector2f = other.plus(this)
// Vector4f
operator fun Vector4f.plus(other: Float): Vector4f = Vector4f(x() + other, y() + other, z() + other, w() + other)
operator fun Float.plus(other: Vector4f): Vector4f = other.plus(this)
// Matrix4f
operator fun Matrix4f.plus(other: Matrix4f): Matrix4f = copy().apply { add(other) }

// Minus

// BlockPos
operator fun BlockPos.minus(other: BlockPos): BlockPos = BlockPos(x - other.x, y - other.y, z - other.z)
operator fun BlockPos.minus(other: Int): BlockPos = BlockPos(x - other, y - other, z - other)
operator fun BlockPos.minus(other: Vector3i): BlockPos = BlockPos(-other)
operator fun BlockPos.minus(other: Vector3d): BlockPos = BlockPos(-other)
operator fun BlockPos.minus(other: IPosition): BlockPos = BlockPos(-other)
operator fun Int.minus(other: BlockPos): BlockPos = BlockPos(this - other.x, this - other.y, this - other.z)
// Vector3i
operator fun Vector3i.minus(other: Int): Vector3i = Vector3i(x - other, y - other, z - other)
operator fun Int.minus(other: Vector3i): Vector3i = Vector3i(this - other.x, this - other.y, this - other.z)
operator fun Vector3i.minus(other: BlockPos): Vector3i = Vector3i(x - other.x, y - other.y, z - other.z)
// Vector3d
operator fun Vector3d.minus(other: Double): Vector3d = Vector3d(x - other, y - other, z - other)
operator fun Double.minus(other: Vector3d): Vector3d = other.minus(this)
operator fun Vector3d.minus(other: IPosition): Vector3d = Vector3d(x - other.x(), y - other.y(), z - other.z())
// Vector3f
operator fun Vector3f.minus(other: Float): Vector3f = Vector3f(x() - other, y() - other, z() - other)
operator fun Float.minus(other: Vector3f): Vector3f = other.minus(this)
operator fun Vector3f.minus(other: IPosition): Vector3f = Vector3f(x() - other.x().toFloat(), y() - other.y().toFloat(), z() - other.z().toFloat())
// Vector2f
operator fun Vector2f.minus(other: Float): Vector2f = Vector2f(x - other, y - other)
operator fun Float.minus(other: Vector2f): Vector2f = other.minus(this)
// Vector4f
operator fun Vector4f.minus(other: Float): Vector4f = Vector4f(x() - other, y() - other, z() - other, w() - other)
operator fun Float.minus(other: Vector4f): Vector4f = other.minus(this)

// Div

// BlockPos
operator fun BlockPos.div(other: BlockPos): BlockPos = BlockPos(x / other.x, y / other.y, z / other.z)
operator fun BlockPos.div(other: Int): BlockPos = BlockPos(x / other, y / other, z / other)
operator fun BlockPos.div(other: Vector3i): BlockPos = BlockPos(x / other.x, y / other.y, z / other.z)
operator fun BlockPos.div(other: Vector3d): BlockPos = BlockPos(x / other.x, y / other.y, z / other.z)
operator fun BlockPos.div(other: IPosition): BlockPos = BlockPos(x / other.x(), y / other.y(), z / other.z())
operator fun Int.div(other: BlockPos): BlockPos = BlockPos(this / other.x, this / other.y, this / other.z)
// Vector3i
operator fun Vector3i.div(other: Int): Vector3i = Vector3i(x / other, y / other, z / other)
operator fun Int.div(other: Vector3i): Vector3i = Vector3i(this / other.x, this / other.y, this / other.z)
operator fun Vector3i.div(other: BlockPos): Vector3i = Vector3i(x / other.x, y / other.y, z / other.z)
// Vector3d
operator fun Vector3d.div(other: Double): Vector3d = Vector3d(x / other, y / other, z / other)
operator fun Double.div(other: Vector3d): Vector3d = other.div(this)
operator fun Vector3d.div(other: IPosition): Vector3d = Vector3d(x / other.x(), y / other.y(), z / other.z())
// Vector3f
operator fun Vector3f.div(other: Float): Vector3f = Vector3f(x() / other, y() / other, z() / other)
operator fun Float.div(other: Vector3f): Vector3f = other.div(this)
operator fun Vector3f.div(other: IPosition): Vector3f = Vector3f(x() / other.x().toFloat(), y() / other.y().toFloat(), z() / other.z().toFloat())
// Vector2f
operator fun Vector2f.div(other: Float): Vector2f = Vector2f(x / other, y / other)
operator fun Float.div(other: Vector2f): Vector2f = other.div(this)
// Vector4f
operator fun Vector4f.div(other: Float): Vector4f = Vector4f(x() / other, y() / other, z() / other, w() / other)
operator fun Float.div(other: Vector4f): Vector4f = other.div(this)

// Times

// BlockPos
operator fun BlockPos.times(other: BlockPos): BlockPos = BlockPos(x * other.x, y * other.y, z * other.z)
operator fun BlockPos.times(other: Int): BlockPos = BlockPos(x * other, y * other, z * other)
operator fun BlockPos.times(other: Vector3i): BlockPos = BlockPos(x * other.x, y * other.y, z * other.z)
operator fun BlockPos.times(other: Vector3d): BlockPos = BlockPos(x * other.x, y * other.y, z * other.z)
operator fun BlockPos.times(other: IPosition): BlockPos = BlockPos(x * other.x(), y * other.y(), z * other.z())
operator fun Int.times(other: BlockPos): BlockPos = other.times(this)
// Vector3i
operator fun Vector3i.times(other: Int): Vector3i = Vector3i(x * other, y * other, z * other)
operator fun Int.times(other: Vector3i): Vector3i = other.times(this)
operator fun Vector3i.times(other: BlockPos): Vector3i = Vector3i(x * other.x, y * other.y, z * other.z)
// Vector3d
operator fun Vector3d.times(other: Double): Vector3d = Vector3d(x * other, y * other, z * other)
operator fun Double.times(other: Vector3d): Vector3d = other.times(this)
operator fun Vector3d.times(other: IPosition): Vector3d = Vector3d(x * other.x(), y * other.y(), z * other.z())
// Vector3f
operator fun Vector3f.times(other: Float): Vector3f = Vector3f(x() * other, y() * other, z() * other)
operator fun Float.times(other: Vector3f): Vector3f = other.times(this)
operator fun Vector3f.times(other: IPosition): Vector3f = Vector3f(x() * other.x().toFloat(), y() * other.y().toFloat(), z() * other.z().toFloat())
// Vector2f
operator fun Vector2f.times(other: Float): Vector2f = Vector2f(x * other, y * other)
operator fun Float.times(other: Vector2f): Vector2f = other.times(this)
// Vector4f
operator fun Vector4f.times(other: Float): Vector4f = Vector4f(x() * other, y() * other, z() * other, w() * other)
operator fun Float.times(other: Vector4f): Vector4f = other.times(this)
// Matrix4f
operator fun Matrix4f.times(other: Matrix4f): Matrix4f = copy().apply { multiply(other) }
operator fun Matrix4f.times(other: Float): Matrix4f = copy().apply { multiply(other) }
