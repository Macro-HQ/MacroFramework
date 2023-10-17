package dev.macrohq.macroframework.gui;

import dev.macrohq.macroframework.command.annotation.Command;
import dev.macrohq.macroframework.command.annotation.Main;
import dev.macrohq.macroframework.util.Ref;

@Command(value = "openguicus")
public class OpenCmd {
    @Main
    private void main() {
        dev.macrohq.macroframework.Main.gui = new TestGui();
    }
}
