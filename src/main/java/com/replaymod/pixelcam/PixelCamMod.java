/*
 * This file is part of PixelCam Mod, licensed under the Apache License, Version 2.0.
 *
 * Copyright (c) 2016 CrushedPixel <http://crushedpixel.eu>
 * Copyright (c) contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.replaymod.pixelcam;

import com.replaymod.pixelcam.command.CamCommand;
import com.replaymod.pixelcam.input.CustomKeyBindings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = PixelCamMod.MODID, clientSideOnly = true, useMetadata = true, acceptedMinecraftVersions = "1.9")
public class PixelCamMod {
    public static final String MODID = "pixelcam";

    @Mod.Instance(value = MODID)
    public static PixelCamMod instance;

    public CamCommand camCommand;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new CustomKeyBindings().register();
        camCommand = new CamCommand(new PathSaveHandler());
        camCommand.register();
    }

}
