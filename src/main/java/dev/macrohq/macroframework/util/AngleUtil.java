package dev.macrohq.macroframework.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class AngleUtil {
    public static float yawTo360(float yaw) {
        return (((yaw % 360) + 360) % 360);
    }

    public static RotationUtil.Rotation getRotation(BlockPos pos) {
        return getRotation(new Vec3(pos));
    }

    public static RotationUtil.Rotation getRotation(Entity entity, double y) {
        return getRotation(new Vec3(entity.posX, y, entity.posZ));
    }

    public static RotationUtil.Rotation getRotation(Vec3 pos) {
        double deltaX = pos.xCoord - Ref.player().posX;
        double deltaY = pos.yCoord - Ref.player().posY - Ref.player().getEyeHeight();
        double deltaZ = pos.zCoord - Ref.player().posZ;
        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float pitch = (float) -Math.atan2(dist, deltaY);
        float yaw = (float) Math.atan2(deltaZ, deltaX);
        pitch = (float) MathHelper.wrapAngleTo180_double((pitch * 180f / Math.PI + 90) * -1);
        yaw = (float) MathHelper.wrapAngleTo180_double(yaw * 180 / Math.PI - 90);
        return new RotationUtil.Rotation(yaw, pitch);
    }
}
