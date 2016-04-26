package com.replaymod.pixelcam.input;

import com.replaymod.pixelcam.PixelCamMod;
import com.replaymod.pixelcam.renderer.TiltHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomKeyBindings {

    private static Minecraft mc = Minecraft.getMinecraft();

    private static final float TILT_SPEED = 1;
    private static final float TILT_SLOW = 0.1f;

    private final List<CustomKeyBinding> customKeyBindings = new ArrayList<>();

    private final CustomKeyBinding addPoint = new CustomKeyBinding("pixelcam.input.addPoint", Keyboard.KEY_P, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/cam p");
        }
    };

    private final CustomKeyBinding togglePathVisualization = new CustomKeyBinding("pixelcam.input.toggleVisualization", Keyboard.KEY_O, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            PixelCamMod.instance.camCommand.getPathVisualizer().togglePathVisibility();
        }
    };

    private final CustomKeyBinding tiltLeft = new CustomKeyBinding("pixelcam.input.tiltLeft", Keyboard.KEY_J, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.changeTilt(GuiScreen.isCtrlKeyDown() ? -TILT_SLOW : -TILT_SPEED);
        }

        @Override
        public boolean checkPressed(boolean guiScreen) {
            return Keyboard.isKeyDown(getKeyCode()) && mc.currentScreen == null;
        }
    };

    private final CustomKeyBinding tiltRight = new CustomKeyBinding("pixelcam.input.tiltRight", Keyboard.KEY_L, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.changeTilt(GuiScreen.isCtrlKeyDown() ? TILT_SLOW : TILT_SPEED);
        }

        @Override
        public boolean checkPressed(boolean guiScreen) {
            return Keyboard.isKeyDown(getKeyCode()) && mc.currentScreen == null;
        }
    };

    private final CustomKeyBinding tiltReset = new CustomKeyBinding("pixelcam.input.tiltReset", Keyboard.KEY_K, "pixelcam.title", false) {
        @Override
        public void onPressed() {
            TiltHandler.resetTilt();
        }
    };

    public CustomKeyBindings() {
        customKeyBindings.add(addPoint);
        customKeyBindings.add(togglePathVisualization);
        customKeyBindings.add(tiltLeft);
        customKeyBindings.add(tiltRight);
        customKeyBindings.add(tiltReset);
    }

    public void register() {
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

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        onKeyInput(mc.currentScreen != null);
    }

    public void onKeyInput(boolean guiScreen) {
        for(CustomKeyBinding binding : customKeyBindings) {
            binding.press(guiScreen);
        }
    }

}
