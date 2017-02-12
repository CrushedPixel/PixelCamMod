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

import com.replaymod.pixelcam.PixelCamMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FocusPointHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    private Position focusPoint;

    private boolean enabled;

    public FocusPointHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public Position getFocusPoint() {
        return focusPoint;
    }

    public void setFocusPoint(Position focusPoint) {
        this.focusPoint = focusPoint;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(!enabled || focusPoint == null || PixelCamMod.instance.camCommand.isTravelling()) return;

        double diffX = interpolate(mc.player.prevPosX, mc.player.posX) - focusPoint.getX();
        double diffY = interpolate(mc.player.prevPosY, mc.player.posY) + mc.player.eyeHeight - focusPoint.getY();
        double diffZ = interpolate(mc.player.prevPosZ, mc.player.posZ) - focusPoint.getZ();

        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2) + Math.pow(diffZ, 2));

        if(distance == 0) return;

        diffX /= distance;
        diffY /= distance;
        diffZ /= distance;

        double pitch = Math.asin(diffY);
        double yaw = Math.atan2(diffZ, diffX);

        yaw = (90 + yaw * 180f / (float)Math.PI);
        pitch = (pitch * 180f / (float)Math.PI);

        mc.player.prevRotationYaw = mc.player.rotationYaw = (float)yaw;
        mc.player.prevRotationPitch = mc.player.rotationPitch = (float)pitch;
    }

    private double interpolate(double prevPos, double pos) {
        return prevPos + ((pos - prevPos) * mc.getRenderPartialTicks());
    }

}
