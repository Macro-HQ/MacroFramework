package dev.macrohq.macroframework;

import dev.macrohq.macroframework.command.CommandManager;
import dev.macrohq.macroframework.gui.OpenCmd;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;

public class MFLoader implements IFMLLoadingPlugin {
    public static final int PRIMARY_800 = new Color(204, 102, 0, 255).getRGB();
    public static final int PRIMARY_700 = new Color(255, 136, 51, 255).getRGB();
    public static final int PRIMARY_700_80 = new Color(255, 136, 51, 204).getRGB();
    public static final int PRIMARY_600 = new Color(255, 153, 51, 255).getRGB();
    public static final int PRIMARY_500 = new Color(255, 187, 51, 255).getRGB();

    private void changeField(Class<?> clazz, String name, int value) throws IllegalAccessException, NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    static {
        Launch.blackboard.put("Tweakers", Launch.blackboard.get("TweakClasses"));
        MixinBootstrap.init();
        Mixins.addConfigurations("mixins.macroframework.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
        CommandManager.register(new OpenCmd());
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
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