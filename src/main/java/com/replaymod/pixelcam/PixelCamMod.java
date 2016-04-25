package com.replaymod.pixelcam;

import com.replaymod.pixelcam.command.CamCommand;
import com.replaymod.pixelcam.input.CustomKeyBindings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = PixelCamMod.MODID, useMetadata = true)
public class PixelCamMod {
    public static final String MODID = "pixelcam";

    @Mod.Instance(value = MODID)
    public static PixelCamMod instance;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new CustomKeyBindings();

        CamCommand command = new CamCommand();
        ClientCommandHandler.instance.registerCommand(command);
    }


}
