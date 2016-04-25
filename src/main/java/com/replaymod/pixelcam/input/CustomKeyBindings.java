package com.replaymod.pixelcam.input;

import com.replaymod.pixelcam.TiltHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomKeyBindings {

    private static Minecraft mc = Minecraft.getMinecraft();

    private final List<CustomKeyBinding> customKeyBindings = new ArrayList<>();

    private final CustomKeyBinding tiltLeft = new CustomKeyBinding("pixelcam.input.tiltLeft", Keyboard.KEY_J, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.changeTilt(-1);
        }

        @Override
        public boolean checkPressed(boolean guiScreen) {
            return Keyboard.isKeyDown(getKeyCode());
        }
    };

    private final CustomKeyBinding tiltRight = new CustomKeyBinding("pixelcam.input.tiltRight", Keyboard.KEY_L, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.changeTilt(1);
        }

        @Override
        public boolean checkPressed(boolean guiScreen) {
            return Keyboard.isKeyDown(getKeyCode());
        }
    };


    private final CustomKeyBinding tiltReset = new CustomKeyBinding("pixelcam.input.tiltReset", Keyboard.KEY_K, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.resetTilt();
        }
    };

    public CustomKeyBindings() {
        customKeyBindings.add(tiltLeft);
        customKeyBindings.add(tiltRight);
        customKeyBindings.add(tiltReset);

        List<KeyBinding> bindings = new ArrayList<KeyBinding>(Arrays.asList(mc.gameSettings.keyBindings));
        bindings.addAll(customKeyBindings);

        mc.gameSettings.keyBindings = bindings.toArray(new KeyBinding[bindings.size()]);

        mc.gameSettings.loadOptions();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        onKeyInput(true);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        onKeyInput(false);
    }

    public void onKeyInput(boolean guiScreen) {
        for(CustomKeyBinding binding : customKeyBindings) {
            binding.press(guiScreen);
        }
    }

}
