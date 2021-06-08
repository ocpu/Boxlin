package io.opencubes.boxlin

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3i

operator fun BlockPos.plus(other: BlockPos): BlockPos = add(other.x, other.y, other.z)
operator fun BlockPos.plus(other: Int): BlockPos = add(other, other, other)
operator fun BlockPos.plus(other: Vector3i): BlockPos = add(other)
operator fun Int.plus(other: BlockPos): BlockPos = other.add(this, this, this)
operator fun Vector3i.plus(other: BlockPos): Vector3i = Vector3i(x + other.x, y + other.y, z + other.z)

operator fun BlockPos.minus(other: BlockPos): BlockPos = add(-other.x, -other.y, -other.z)
operator fun BlockPos.minus(other: Int): BlockPos = add(other, other, other)
operator fun BlockPos.minus(other: Vector3i): BlockPos = subtract(other)
operator fun Int.minus(other: BlockPos): BlockPos = other.add(-this, -this, -this)
operator fun Vector3i.minus(other: BlockPos): Vector3i = Vector3i(x - other.x, y - other.y, z - other.z)

operator fun BlockPos.div(other: BlockPos) = BlockPos(x / other.x, y / other.y, z / other.z)
operator fun BlockPos.div(other: Int) = BlockPos(x / other, y / other, z / other)
operator fun BlockPos.div(other: Vector3i) = BlockPos(x / other.x, y / other.y, z / other.z)
operator fun Int.div(other: BlockPos): BlockPos = other.div(this)
operator fun Vector3i.div(other: BlockPos): Vector3i = Vector3i(x / other.x, y / other.y, z / other.z)

operator fun BlockPos.times(other: BlockPos) = BlockPos(x * other.x, y * other.y, z * other.z)
operator fun BlockPos.times(other: Int) = BlockPos(x * other, y * other, z * other)
operator fun BlockPos.times(other: Vector3i) = BlockPos(x * other.x, y * other.y, z * other.z)
operator fun Int.times(other: BlockPos): BlockPos = other.times(this)
operator fun Vector3i.times(other: BlockPos): Vector3i = Vector3i(x * other.x, y * other.y, z * other.z)
