package dev.macrohq.macroframework.pathing.node;

import net.minecraft.util.BlockPos;

public class EtherwarpNode implements INode {
    private final BlockPos standingPos;
    private final BlockPos targetPos;

    public EtherwarpNode(BlockPos standingPos, BlockPos targetPos) {
        this.standingPos = standingPos;
        this.targetPos = targetPos;
    }

    public BlockPos getStandingPos() {
        return standingPos;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }
}
