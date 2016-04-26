package com.replaymod.pixelcam.path;

import com.google.common.base.Preconditions;
import com.replaymod.pixelcam.renderer.TiltHandler;
import com.replaymod.pixelcam.command.CamCommand;
import com.replaymod.pixelcam.interpolation.Interpolation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TravellingProcess {

    private Minecraft mc = Minecraft.getMinecraft();

    private final Interpolation<Position> interpolation;
    private final long duration;

    private long startTime;

    private boolean active;

    public boolean isActive() {
        return active;
    }

    public TravellingProcess(Interpolation<Position> interpolation, long duration) {
        this.interpolation = interpolation;
        this.duration = duration;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void start() {
        Preconditions.checkState(!active);

        startTime = System.currentTimeMillis();
        active = true;
    }

    public void stop() {
        Preconditions.checkState(active);

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
        TiltHandler.setTilt(pos.getTilt());

        if(progress >= 1) {
            CamCommand.sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.start.finished"));
            active = false;
        }
    }

}
