package dev.macrohq.macroframework.util;

public final class SleepUtil {
    public static void sleep(long millis) {
        try {Thread.sleep(millis);} catch (InterruptedException ignored) {}
    }
}
