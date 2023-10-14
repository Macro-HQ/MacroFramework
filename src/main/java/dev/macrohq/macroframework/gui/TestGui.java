package dev.macrohq.macroframework.gui;

import dev.macrohq.macroframework.util.Ref;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class TestGui extends GuiScreen {
    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        System.out.println("Drawing screen");
        super.drawScreen(mouseX, mouseY, partialTicks);
        }
}
