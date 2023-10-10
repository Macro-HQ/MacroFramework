package dev.macrohq.macroframework.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;

public final class Ref {
    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP player() {
        return mc().thePlayer;
    }

    public static WorldClient world() {
        return mc().theWorld;
    }

    public static GameSettings gameSettings() {
        return mc().gameSettings;
    }
}
