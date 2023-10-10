package dev.macrohq.macroframework.macro;

public interface Task {

    /**
     * This method will be called every tick.
     * It will stop being called and proceed to the next task once nextTask returns a Task.
     */
    void tick();

    /**
     * This method will be called each tick, before the tick method.
     * @return If null is returned the task will continue to execute. If a task is
     * returned the current task will be stopped and the returned task will be executed.
     */
    Task nextTask();
}