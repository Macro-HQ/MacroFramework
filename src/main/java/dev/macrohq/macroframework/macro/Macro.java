package dev.macrohq.macroframework.macro;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public abstract class Macro {
    private boolean enabled;
    private Task currentTask;

    /**
     * Will be called when the user tries to enable the macro, before the enable method.
     * @return if true is returned the enable method is called, if not it won't be called.
     */
    public abstract boolean canEnable();
    public final void enable() {
        if(enabled || !canEnable()) return;
        enabled = true;
        onEnable();
    }
    protected abstract void onEnable();
    public final void disable() {
        if(!enabled) return;
        enabled = false;
        onDisable();
    }
    protected abstract void onDisable();
    public abstract List<Task> getTasks();
    public final boolean isEnabled() { return enabled; }
    public final Task getCurrentTask() { return currentTask; }

    @SubscribeEvent
    public final void onTick(TickEvent.ClientTickEvent event) {
        if(!enabled) return;
        if(currentTask == null) {
            currentTask = getTasks().get(0);
        }
        if(currentTask.nextTask() != null) {
            currentTask = currentTask.nextTask();
            return;
        }
        currentTask.tick();
    }
}