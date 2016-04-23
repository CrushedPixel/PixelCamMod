package com.replaymod.pixelcam.input;


import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public abstract class CustomKeyBinding extends KeyBinding {

    private final boolean guiScreensEnabled;

    public CustomKeyBinding(String description, int keyCode, String category, boolean guiScreensEnabled) {
        super(description, keyCode, category);
        this.guiScreensEnabled = guiScreensEnabled;
    }

    public abstract void onPressed();

    public boolean checkPressed(boolean guiScreen) {
        return isPressed() || (guiScreensEnabled && guiScreen && Keyboard.isKeyDown(getKeyCode()));
    }

    public void press(boolean guiScreen) {
        if(checkPressed(guiScreen)) onPressed();
    }

}