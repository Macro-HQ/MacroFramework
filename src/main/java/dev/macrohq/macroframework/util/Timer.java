package dev.macrohq.macroframework.util;

public class Timer {

    private final long endTime;

    public Timer(long millis) {
        this.endTime = System.currentTimeMillis() + millis;
    }

    public Timer(long millis, Runnable callback) {
        this.endTime = System.currentTimeMillis() + millis;
        new Thread(() -> {
            while(!isDone()) {
                SleepUtil.sleep(1);
            }
            callback.run();
        }).start();
    }

    public boolean isDone() {
        return System.currentTimeMillis() >= endTime;
    }
}
