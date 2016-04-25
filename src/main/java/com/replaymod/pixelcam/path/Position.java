package com.replaymod.pixelcam.path;

import com.replaymod.pixelcam.TiltHandler;
import com.replaymod.pixelcam.interpolation.Interpolate;
import net.minecraft.entity.Entity;

public class Position {

    @Interpolate
    public double x;

    @Interpolate
    public double y;

    @Interpolate
    public double z;

    @Interpolate
    public double yaw;

    @Interpolate
    public double pitch;

    @Interpolate
    public double tilt;

    public Position(double x, double y, double z, float yaw, float pitch, float tilt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.tilt = tilt;
    }

    public Position(Entity entity) {
        this(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch, TiltHandler.getTilt());
    }

    public Position() {

    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return (float)yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return (float)pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public float getTilt() {
        return (float)tilt;
    }

    public void setTilt(double tilt) {
        this.tilt = tilt;
    }
}