package dev.macrohq.macroframework.pathing;

import dev.macrohq.macroframework.util.AngleUtil;
import dev.macrohq.macroframework.util.BlockUtil;
import dev.macrohq.macroframework.util.Ref;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class AStarPathfinder {
    private final Node startNode;
    private final Node endNode;
    private final List<Node> openNodes = new ArrayList<>();
    private final List<Node> closedNodes = new ArrayList<>();

    public AStarPathfinder(BlockPos startPos, BlockPos endPos) {
        startNode = new Node(startPos, null);
        endNode = new Node(endPos, null);
    }

    public List<BlockPos> findPath(int iterations) {
        startNode.calculateCost(endNode);
        openNodes.add(startNode);
        for (int i = 0; i <= iterations; i++) {
            Node currentNode = openNodes.stream().min(Comparator.comparingDouble(Node::getFCost)).orElse(null);
            if(currentNode == null) return new ArrayList<>();
            if (currentNode.position == endNode.position) return reconstructPath(currentNode);
            openNodes.remove(currentNode);
            closedNodes.add(currentNode);
            for (Node node : currentNode.getNeighbours()) {
                node.calculateCost(endNode);
                if (node.isNotIn(openNodes) && node.isNotIn(closedNodes)) openNodes.add(node);
            }
        }
        return new ArrayList<>();
    }

    private List<BlockPos> reconstructPath(Node end) {
        List<BlockPos> path = new ArrayList<>();
        Node currentNode = end;
        while (currentNode != null) {
            path.add(0, currentNode.position);
            currentNode = currentNode.parent;
        }

        if(path.isEmpty()) return path;
        List<BlockPos> smooth = new ArrayList<>();
        smooth.add(path.get(0));
        int currPoint = 0;
        int maxIterations = 2000;
        while (currPoint + 1 < path.size() && maxIterations-- > 0) {
            int nextPos = currPoint + 1;
            for (int i = path.size() - 1; i >= nextPos; i--) {
                if (BlockUtil.blocksBetweenValid(path.get(currPoint), path.get(i))) {
                    nextPos = i;
                    break;
                }
            }
            smooth.add(path.get(nextPos));
            currPoint = nextPos;
        }
        return smooth;
    }

    public static final class Node {
        private final BlockPos position;
        private final Node parent;
        private float gCost = Float.MAX_VALUE;
        private float hCost = Float.MAX_VALUE;
        private float yaw = 0;

        public Node(BlockPos position, Node parent) {
            this.position = position;
            this.parent = parent;
        }

        private float angleCost(BlockPos from, BlockPos to) {
            int deltaX = to.getX() - from.getX();
            int deltaZ = to.getZ() - from.getZ();
            float yaw = (float) -Math.toDegrees(Math.atan2(deltaX, deltaZ));
            return AngleUtil.yawTo360(yaw);
        }

        private void calculateCost(Node endNode) {
            float cost = 0;
            if (this.parent != null) {
                this.yaw = angleCost(this.parent.position, this.position);
                if (this.parent.parent != null) cost += AngleUtil.yawTo360(Math.abs(this.yaw - this.parent.yaw)) / 360;
                if (this.parent.position.getY() < this.position.getY() && !BlockUtil.walkableWithoutJump(this.position))
                    cost += 1.5f;
                if (BlockUtil.walkableWithoutJump(this.position)) cost -= 1f;
            }
            for (BlockPos pos : BlockUtil.neighbourGenerator(this.position.up().up().up(), 1)) {
                if (Ref.world().isBlockFullCube(pos)) cost += 1.5f;
            }
            if (this.parent != null) gCost = (float) Math.sqrt(this.parent.position.distanceSq(this.position));
            else gCost = 0;
            this.gCost += cost;
            this.hCost = (float) Math.sqrt(endNode.position.distanceSq(this.position));
        }

        private float getFCost() {
            return gCost + hCost;
        }

        private List<Node> getNeighbours() {
            List<Node> neighbours = new ArrayList<>();
            for (BlockPos pos : BlockUtil.neighbourGenerator(this.position, -1, 1, -4, 3, -1, 1)) {
                Node newNode = new Node(pos, this);
                if (newNode.isWalkable()) neighbours.add(newNode);
            }
            return neighbours;
        }

        private boolean isNotIn(List<Node> nodes) {
            return nodes.stream().noneMatch(node -> position == node.position);
        }

        public boolean isWalkable() {
            // head hit
            if (this.parent != null && this.parent.position.getY() < this.position.getY()) {
                if (!passableBlocks.contains(Ref.world().getBlockState(this.parent.position.add(0, 3, 0)).getBlock())) {
                    return false;
                }
            }
            // left right front back collision
            if (parent != null && parent.position.getX() != position.getX() && parent.position.getZ() != position.getZ()) {
                if(!passableBlocks.contains(Ref.world().getBlockState(position.add(1, 1, 0)).getBlock())
                        || !passableBlocks.contains(Ref.world().getBlockState(position.add(-1, 1, 0)).getBlock())
                        || !passableBlocks.contains(Ref.world().getBlockState(position.add(0, 1, 1)).getBlock())
                        || !passableBlocks.contains(Ref.world().getBlockState(position.add(0, 1, -1)).getBlock()))
                    return false;
            }
            return passableBlocks.contains(Ref.world().getBlockState(position.up()).getBlock())
                    && passableBlocks.contains(Ref.world().getBlockState(position.up().up()).getBlock())
                    && Ref.world().getBlockState(position).getBlock().getMaterial().isSolid();
        }

        private final List<Block> passableBlocks = Arrays.stream(new Block[]{
                Blocks.air,
                Blocks.tallgrass,
                Blocks.double_plant,
                Blocks.yellow_flower,
                Blocks.red_flower,
                Blocks.vine,
                Blocks.redstone_wire,
                Blocks.snow_layer,
                Blocks.cocoa,
                Blocks.end_portal,
                Blocks.tripwire,
                Blocks.web,
                Blocks.flower_pot,
                Blocks.wooden_pressure_plate,
                Blocks.stone_pressure_plate,
                Blocks.redstone_torch,
                Blocks.lever,
                Blocks.stone_button,
                Blocks.wooden_button,
                Blocks.carpet,
                Blocks.standing_sign,
                Blocks.wall_sign,
                Blocks.rail,
                Blocks.detector_rail,
                Blocks.activator_rail,
                Blocks.golden_rail,
        }).collect(Collectors.toList());
    }
}