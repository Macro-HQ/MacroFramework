package dev.macrohq.macroframework.util;

import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class MouseGrabUtil {
    private static boolean isUnGrabbed = false;
    private static MouseHelper oldMouseHelper;
    private static boolean doesGameWantUnGrabbed = false;

    public static void unGrabMouse() {
        if (isUnGrabbed) return;
        Ref.mc().gameSettings.pauseOnLostFocus = false;
        if (oldMouseHelper == null) oldMouseHelper = Ref.mc().mouseHelper;
        doesGameWantUnGrabbed = !Mouse.isGrabbed();
        oldMouseHelper.ungrabMouseCursor();
        Ref.mc().inGameHasFocus = true;
        Ref.mc().mouseHelper = new MouseHelper() {
            @Override
            public void mouseXYChange() {}
            @Override
            public void grabMouseCursor() {
                doesGameWantUnGrabbed = false;
            }
            @Override
            public void ungrabMouseCursor() {
                doesGameWantUnGrabbed = true;
            }
        };
        isUnGrabbed = true;
    }

    public void reGrabMouse() {
        if (!isUnGrabbed) return;
        Ref.mc().mouseHelper = oldMouseHelper;
        if (!doesGameWantUnGrabbed) Ref.mc().mouseHelper.grabMouseCursor();
        oldMouseHelper = null;
        isUnGrabbed = false;
    }

    public boolean isGrabbed() {
        return !isUnGrabbed;
    }
}
