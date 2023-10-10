package dev.macrohq.macroframework.dsl

import dev.macrohq.macroframework.util.BlockUtil
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

fun BlockPos.toVec3() = BlockUtil.toVec3(this)
fun Vec3.toBlockPos() = BlockUtil.toBlockPos(this)