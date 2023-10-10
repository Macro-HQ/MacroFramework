package dev.macrohq.macroframework.pathing.node;

import net.minecraft.util.BlockPos;

public class WalkNode implements INode {
    private final BlockPos targetPos;

    public WalkNode(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }
}
