package dev.macrohq.macroframework.util;

import dev.macrohq.macroframework.mixin.MinecraftInvoker;
import net.minecraft.client.settings.KeyBinding;

public class KeyBindUtil {
    public static void leftClick() {
        ((MinecraftInvoker) Ref.mc()).invokeClickMouse();
    }

    public static void middleClickMouse() {
        ((MinecraftInvoker) Ref.mc()).invokeMiddleClickMouse();
    }

    public static void rightClickMouse() {
        ((MinecraftInvoker) Ref.mc()).invokeRightClickMouse();
    }

    public static void toggleKey(KeyBinding key, boolean pressed) {
        KeyBinding.setKeyBindState(key.getKeyCode(), pressed);
    }

    public static void clickKey(KeyBinding key) {
        clickKey(key, 100);
    }

    public static void clickKey(KeyBinding key, long millis) {
        new Thread(() -> {
            KeyBinding.setKeyBindState(key.getKeyCode(), true);
            SleepUtil.sleep(millis);
            KeyBinding.setKeyBindState(key.getKeyCode(), false);
        }).start();
    }
}