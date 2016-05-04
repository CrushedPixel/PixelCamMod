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

        double diffX = interpolate(mc.thePlayer.prevPosX, mc.thePlayer.posX) - focusPoint.getX();
        double diffY = interpolate(mc.thePlayer.prevPosY, mc.thePlayer.posY) + mc.thePlayer.eyeHeight - focusPoint.getY();
        double diffZ = interpolate(mc.thePlayer.prevPosZ, mc.thePlayer.posZ) - focusPoint.getZ();

        double distance = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2) + Math.pow(diffZ, 2));

        if(distance == 0) return;

        diffX /= distance;
        diffY /= distance;
        diffZ /= distance;

        double pitch = Math.asin(diffY);
        double yaw = Math.atan2(diffZ, diffX);

        yaw = (90 + yaw * 180f / (float)Math.PI);
        pitch = (pitch * 180f / (float)Math.PI);

        mc.thePlayer.prevRotationYaw = mc.thePlayer.rotationYaw = (float)yaw;
        mc.thePlayer.prevRotationPitch = mc.thePlayer.rotationPitch = (float)pitch;
    }

    private double interpolate(double prevPos, double pos) {
        return prevPos + ((pos - prevPos) * mc.getRenderPartialTicks());
    }

}
