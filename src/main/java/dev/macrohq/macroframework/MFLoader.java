package dev.macrohq.macroframework;

import dev.macrohq.macroframework.command.CommandManager;
import dev.macrohq.macroframework.gui.OpenCmd;
import dev.macrohq.macroframework.gui.TestGui;
import dev.macrohq.macroframework.util.Ref;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@IFMLLoadingPlugin.MCVersion("1.8.9")
public class MFLoader implements IFMLLoadingPlugin {

    static {
        MixinBootstrap.init();
        Mixins.addConfigurations("mixins.macroframework.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    public MFLoader() {
        CommandManager.register(new OpenCmd());
        try {
            Field f_exceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            f_exceptions.setAccessible(true);
            Set<String> exceptions = (Set<String>) f_exceptions.get(Launch.classLoader);
            exceptions.remove("org.lwjgl.");
        } catch (Exception e) {
            throw new RuntimeException("Unable to load lwjgl3, things will break.");
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"dev.macrohq.macroframework.MFTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}