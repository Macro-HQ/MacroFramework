package dev.macrohq.macroframework.util;

import net.minecraft.util.ChatComponentText;

public final class Logger {
    public static void log(String message) {
        Ref.player().addChatMessage(new ChatComponentText(message));
    }
}