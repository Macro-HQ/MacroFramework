package dev.macrohq.macroframework.gui;

import dev.macrohq.macroframework.command.annotations.Command;
import dev.macrohq.macroframework.command.annotations.Main;
import dev.macrohq.macroframework.util.Ref;

@Command(value = "openguicus")
public class OpenCmd {
    @Main
    private void main() {
        Ref.mc().displayGuiScreen(new TestGui());
    }
}
