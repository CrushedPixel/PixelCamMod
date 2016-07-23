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
package com.replaymod.pixelcam.path;

import com.google.common.base.Preconditions;
import com.replaymod.pixelcam.command.CamCommand;
import com.replaymod.pixelcam.interpolation.Interpolation;
import com.replaymod.pixelcam.renderer.TiltHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TravellingProcess {

    private Minecraft mc = Minecraft.getMinecraft();

    private final Interpolation<Position> interpolation;
    private final long duration;

    private boolean wasSpectator;

    private long startTime;

    private boolean active;

    private boolean repeat;

    public boolean isActive() {
        return active;
    }

    public TravellingProcess(Interpolation<Position> interpolation, long duration) {
        this.interpolation = interpolation;
        this.duration = duration;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void start(boolean repeat) {
        Preconditions.checkState(!active);
        this.repeat = repeat;

        wasSpectator = mc.thePlayer.isSpectator();
        mc.thePlayer.sendChatMessage("/gamemode 3");

        startTime = System.currentTimeMillis();
        active = true;
    }

    public void stop() {
        Preconditions.checkState(active);

        if(!wasSpectator) mc.thePlayer.sendChatMessage("/gamemode 1");

        active = false;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(!active) return;

        long current = System.currentTimeMillis();

        float progress = Math.min(1, (current - startTime) / (float)duration);

        Position pos = new Position();

        interpolation.applyPoint(progress, pos);

        mc.thePlayer.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());

        //this fixes camera jerking; setPositionAndRotation fights to keep prevRotationPitch and yaw within [-180, 180),
        // but keeps rotationPitch and rotationYaw within [0, 360)
        mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch;
        mc.thePlayer.prevRotationYaw = mc.thePlayer.rotationYaw;

        //this fixes camera jittering
        mc.thePlayer.lastTickPosX = pos.getX();
        mc.thePlayer.lastTickPosY = pos.getY();
        mc.thePlayer.lastTickPosZ = pos.getZ();

        TiltHandler.setTilt(pos.getTilt());
        mc.gameSettings.fovSetting = (float) pos.getFov();

        if(progress >= 1) {
            if(!repeat) {
                CamCommand.sendMessage(new TextComponentTranslation("pixelcam.commands.cam.start.finished"));
                stop();
            } else {
                startTime = System.currentTimeMillis();
            }
        }
    }

}
