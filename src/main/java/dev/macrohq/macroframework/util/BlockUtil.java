package dev.macrohq.macroframework.util;

import dev.macrohq.macroframework.pathing.AStarPathfinder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class BlockUtil {
    public static List<BlockPos> neighbourGenerator(BlockPos mainBlock, int size) {
        return neighbourGenerator(mainBlock, size, size, size);
    }

    public static List<BlockPos> neighbourGenerator(BlockPos mainBlock, int x, int y, int z) {
        return neighbourGenerator(mainBlock, -x, x, -y, y, -z, z);
    }

    public static List<BlockPos> neighbourGenerator(BlockPos mainBlock, int fromX, int toX, int fromY, int toY, int fromZ, int toZ) {
        List<BlockPos> neighbours = new ArrayList<>();
        for (int x : IntStream.range(fromX, toX + 1).toArray()) {
            for (int y : IntStream.range(fromY, toY + 1).toArray()) {
                for (int z : IntStream.range(fromZ, toZ + 1).toArray()) {
                    neighbours.add(new BlockPos(mainBlock.getX() + x, mainBlock.getY() + y, mainBlock.getZ() + z));
                }
            }
        }
        return neighbours;
    }

    public static boolean walkableWithoutJump(BlockPos pos) {
        Block block = Ref.world().getBlockState(pos).getBlock();
        return block instanceof BlockStairs || block instanceof BlockSlab;
    }

    public static boolean blocksBetweenValid(BlockPos startPos, BlockPos endPos) {
        List<BlockPos> blocks = bresenham(toVec3(startPos).addVector(0.0, 0.4, 0.0), toVec3(endPos).addVector(0.0, 0.4, 0.0));
        int blockFail = 0;
        int lastBlockY = blocks.get(0).getY();
        boolean lastFullBlock = Ref.world().isBlockFullCube(blocks.get(0));
        boolean isLastBlockAir = Ref.world().isAirBlock(blocks.get(0));
        blocks.remove(blocks.get(0));
        for (BlockPos block : blocks) {
            if (!new AStarPathfinder.Node(block, null).isWalkable() && !Ref.world().isAirBlock(block)) {
                return false;
            }
            if (isLastBlockAir && Ref.world().isBlockFullCube(block) && !walkableWithoutJump(block)) return false;
            if (lastFullBlock && Ref.world().isBlockFullCube(block) && block.getY() > lastBlockY) return false;
            if (Ref.world().isAirBlock(block)) blockFail++;
            else blockFail = 0;
            if (blockFail > 3) return false;
            lastBlockY = block.getY();
            lastFullBlock = Ref.world().isBlockFullCube(block);
            isLastBlockAir = Ref.world().isAirBlock(block);
        }
        return true;
    }

    public static List<BlockPos> bresenham(Vec3 start, Vec3 end) {
        List<BlockPos> blocks = new ArrayList<>(Collections.singletonList(toBlockPos(start)));
        double x1 = MathHelper.floor_double(end.xCoord);
        double y1 = MathHelper.floor_double(end.yCoord);
        double z1 = MathHelper.floor_double(end.zCoord);
        double x0 = MathHelper.floor_double(start.xCoord);
        double y0 = MathHelper.floor_double(start.yCoord);
        double z0 = MathHelper.floor_double(start.zCoord);
        for (int i = 0; i < 200; i++) {
            if (x0 == x1 && y0 == y1 && z0 == z1) {
                blocks.add(toBlockPos(end));
                return blocks;
            }
            boolean hasNewX = true;
            boolean hasNewY = true;
            boolean hasNewZ = true;
            double newX = 999.0;
            double newY = 999.0;
            double newZ = 999.0;
            if (x1 > x0) {
                newX = x0 + 1.0;
            } else if (x1 < x0) {
                newX = x0 + 0.0;
            } else {
                hasNewX = false;
            }
            if (y1 > y0) {
                newY = y0 + 1.0;
            } else if (y1 < y0) {
                newY = y0 + 0.0;
            } else {
                hasNewY = false;
            }
            if (z1 > z0) {
                newZ = z0 + 1.0;
            } else if (z1 < z0) {
                newZ = z0 + 0.0;
            } else {
                hasNewZ = false;
            }
            double stepX = 999.0;
            double stepY = 999.0;
            double stepZ = 999.0;
            double dx = end.xCoord - start.xCoord;
            double dy = end.yCoord - start.yCoord;
            double dz = end.zCoord - start.zCoord;
            if (hasNewX) stepX = (newX - start.xCoord) / dx;
            if (hasNewY) stepY = (newY - start.yCoord) / dy;
            if (hasNewZ) stepZ = (newZ - start.zCoord) / dz;
            if (stepX == -0.0) stepX = -1.0E-4;
            if (stepY == -0.0) stepY = -1.0E-4;
            if (stepZ == -0.0) stepZ = -1.0E-4;
            EnumFacing enumfacing;
            if (stepX < stepY && stepX < stepZ) {
                if (x1 > x0) enumfacing = EnumFacing.WEST;
                else enumfacing = EnumFacing.EAST;
                start = new Vec3(newX, start.yCoord + dy * stepX, start.zCoord + dz * stepX);
            } else if (stepY < stepZ) {
                if (y1 > y0) enumfacing = EnumFacing.DOWN;
                else enumfacing = EnumFacing.UP;
                start = new Vec3(start.xCoord + dx * stepY, newY, start.zCoord + dz * stepY);
            } else {
                if (z1 > z0)
                    enumfacing = EnumFacing.NORTH;
                else enumfacing = EnumFacing.SOUTH;
                start = new Vec3(start.xCoord + dx * stepZ, start.yCoord + dy * stepZ, newZ);
            }
            x0 = MathHelper.floor_double(start.xCoord);
            if (enumfacing == EnumFacing.EAST)
                x0 = x0 - 1;
            y0 = MathHelper.floor_double(start.yCoord);
            if (enumfacing == EnumFacing.UP)
                y0 = y0 - 1;
            z0 = MathHelper.floor_double(start.zCoord);
            if (enumfacing == EnumFacing.SOUTH)
                z0 = z0 - 1;
            blocks.add(new BlockPos(x0, y0, z0));
        }
        return blocks;
    }

    public static BlockPos toBlockPos(Vec3 vec3) {
        return new BlockPos(vec3.xCoord + 0.5, vec3.yCoord + 0.5, vec3.zCoord + 0.5);
    }

    public static Vec3 toVec3(BlockPos blockPos) {
        return new Vec3(blockPos);
    }
}
