package io.github.treesoid.mongoauthkeys.util;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AddServerButton extends ButtonWidget {
    public boolean state = false;
    private final Text trueMessage;

    public AddServerButton(int x, int y, int width, int height, Text trueMessage, Text falseMessage, PressAction onPress) {
        super(x, y, width, height, falseMessage, onPress);
        this.trueMessage = trueMessage;
    }

    @Override
    public Text getMessage() {
        return state ? trueMessage : super.getMessage();
    }

    public void toggle() {
        state = !state;
    }
}
