package dev.macrohq.macroframework;

import dev.macrohq.macroframework.util.Ref;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = "macroframework", name = "MacroFramework", version = "1.0")
public class Main {
    public static GuiScreen gui = null;

    @Mod.EventHandler
    public void oninit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(gui == null) return;
        Ref.mc().displayGuiScreen(gui);
        gui = null;
    }
}
