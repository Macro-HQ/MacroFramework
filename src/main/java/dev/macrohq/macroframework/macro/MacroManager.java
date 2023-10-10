package dev.macrohq.macroframework.macro;

import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MacroManager {
    private static final List<Macro> macros = new ArrayList<>();
    private static Macro activeMacro;

    public static void startMacro(Class<Macro> macro) {
        if(activeMacro != null) activeMacro.disable();
        Optional<Macro> macroOptional = macros.stream().filter(macroElement -> macroElement.getClass() == macro).findFirst();
        if(!macroOptional.isPresent()) return;
        activeMacro = macroOptional.get();
        activeMacro.enable();
    }

    public static void stopMacro() {
        if(activeMacro == null) return;
        activeMacro.disable();
        activeMacro = null;
    }

    public static void toggleMacro(Class<Macro> macro) {
        if(activeMacro == null) startMacro(macro); else stopMacro();
    }

    public static void register(Macro macro) {
        macros.add(macro);
        MinecraftForge.EVENT_BUS.register(macro);
    }
}
