package dev.macrohq.macroframework.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class RotationUtil {
    private static Rotation startRotation;
    private static Rotation endRotation;
    private static long startTime;
    private static long endTime;
    private static volatile boolean done;

    public static void ease(Rotation targetRotation, long millis) {
        done = false;
        startRotation = new Rotation(Ref.player().rotationYaw, Ref.player().rotationPitch);
        Rotation neededChange = getNeededChange(startRotation, targetRotation);
        endRotation = new Rotation(startRotation.getYaw() + neededChange.getYaw(), startRotation.getPitch() + neededChange.getPitch());
        startTime = System.currentTimeMillis();
        endTime = startTime + millis;
    }

    /**
     * Eases the player's rotation to the target rotation in a specified direction.
     *
     * @param targetRotation The target rotation.
     * @param millis         The duration that rotating should take in milliseconds.
     * @param direction      false for left and true for right.
     */
    public static void easeDirection(Rotation targetRotation, long millis, boolean direction) {
        done = false;
        startRotation = new Rotation(Ref.player().rotationYaw, Ref.player().rotationPitch);
        float endRotationYaw;
        if (!direction)
            endRotationYaw = startRotation.getYaw() - targetRotation.getYaw();
        else endRotationYaw = startRotation.getYaw() + targetRotation.getYaw();
        endRotation = new Rotation(endRotationYaw, targetRotation.getPitch());
        startTime = System.currentTimeMillis();
        endTime = startTime + millis;
    }

    public static void lock(Entity entity, double y) {
        done = false;
        new Thread(() -> {
           while(!done) {
               Rotation rotation = AngleUtil.getRotation(entity, y);
               Ref.player().rotationYaw = rotation.getYaw();
               Ref.player().rotationPitch = rotation.getPitch();
           }
        });
    }

    public static void stop() {
        startRotation = null;
        endRotation = null;
        startTime = 0;
        endTime = 0;
        done = true;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (done) return;
        if (System.currentTimeMillis() <= endTime) {
            Ref.player().rotationYaw = interpolate(startRotation.getYaw(), endRotation.getYaw());
            Ref.player().rotationPitch = interpolate(startRotation.getPitch(), endRotation.getPitch());
            return;
        }
        Ref.player().rotationYaw = endRotation.getYaw();
        Ref.player().rotationPitch = endRotation.getPitch();
        done = true;
    }

    private static Rotation getNeededChange(Rotation startRot, Rotation endRot) {
        float yawChange = MathHelper.wrapAngleTo180_float(endRot.getYaw()) - MathHelper.wrapAngleTo180_float(startRot.getYaw());
        if (yawChange <= -180.0f)
            yawChange += 360.0f;
        else if (yawChange > 180.0f)
            yawChange += -360.0f;
        return new Rotation(yawChange, endRot.getPitch() - startRot.getPitch());
    }

    private float interpolate(float start, float end) {
        float spentMillis = (System.currentTimeMillis() - startTime);
        float relativeProgress = spentMillis / (endTime - startTime);
        return (end - start) * easeOutCubic(relativeProgress) + start;
    }

    private float easeOutCubic(float number) {
        return (float) (1.0 - Math.pow((1.0 - number), 3.0));
    }

    public static final class Rotation {
        private final float yaw;
        private final float pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
