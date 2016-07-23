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
package com.replaymod.pixelcam.renderer;

import com.replaymod.pixelcam.PixelCamMod;
import com.replaymod.pixelcam.interpolation.Interpolation;
import com.replaymod.pixelcam.interpolation.InterpolationType;
import com.replaymod.pixelcam.path.CameraPath;
import com.replaymod.pixelcam.path.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class PathVisualizer {

    private Minecraft mc = Minecraft.getMinecraft();

    private ResourceLocation pointTexture = new ResourceLocation("pixelcam", "point.png");

    private final CameraPath cameraPath;

    private PathVisibility pathVisibility = PathVisibility.SPLINE;

    public PathVisualizer(CameraPath cameraPath) {
        this.cameraPath = cameraPath;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void togglePathVisibility() {
        pathVisibility = pathVisibility.next();
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(pathVisibility == PathVisibility.NONE
                || cameraPath.getPointCount() == 0
                || PixelCamMod.instance.camCommand.isTravelling()) return;

        Entity entity = mc.getRenderViewEntity();

        float partial = event.getPartialTicks();

        double doubleX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial;
        double doubleY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial;
        double doubleZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial;

        Position prev = null;

        GlStateManager.pushAttrib();

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if(pathVisibility == PathVisibility.LINEAR || pathVisibility == PathVisibility.BOTH) {
            for(Position pos : cameraPath.getPoints()) {
                if(prev != null) drawConnection(doubleX, doubleY, doubleZ, prev, pos, Color.RED);
                prev = pos;
            }
        }

        if(pathVisibility == PathVisibility.SPLINE || pathVisibility == PathVisibility.BOTH) {
            Interpolation<Position> interpolation = cameraPath.getInterpolation(InterpolationType.SPLINE);

            prev = null;

            for(int i = 0; i < cameraPath.getPointCount(); i++) {
                //draw 100 connection lines between the two points
                for(int j = 0; j < 100; j++) {
                    float k = (i / (float) cameraPath.getPointCount()) + (j / 100f * 1f / cameraPath.getPointCount());
                    Position pos = new Position();

                    interpolation.applyPoint(k, pos);

                    if(prev != null) drawConnection(doubleX, doubleY, doubleZ, prev, pos, Color.GREEN);

                    prev = pos;
                }
            }
        }

        GlStateManager.blendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);

        int index = 1;
        for(Position pos : cameraPath.getPoints()) {
            drawPoint(doubleX, doubleY, doubleZ, pos, index);
            index++;
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableBlend();

        GlStateManager.popAttrib();
    }

    private void drawPoint(double playerX, double playerY, double playerZ, Position pos, int index) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableBlend();

        GlStateManager.color(1, 1, 1, 0.5f);

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        mc.renderEngine.bindTexture(pointTexture);

        double x = pos.getX() - playerX;
        double y = pos.getY() - playerY;
        double z = pos.getZ() - playerZ;

        GlStateManager.translate(x, y + mc.thePlayer.eyeHeight, z);

        float pitch = mc.getRenderManager().playerViewX;
        float yaw = mc.getRenderManager().playerViewY;

        GL11.glNormal3f(0, 1, 0);

        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.rotate(pitch, 1, 0, 0);

        float minX = -0.5f;
        float minY = -0.5f;
        float maxX = 0.5f;
        float maxY = 0.5f;

        float size = 10/16f;

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        vb.pos(minX, minY, 0).tex(size, size).endVertex();
        vb.pos(minX, maxY, 0).tex(size, 0).endVertex();
        vb.pos(maxX, maxY, 0).tex(0, 0).endVertex();
        vb.pos(maxX, minY, 0).tex(0, size).endVertex();

        Tessellator.getInstance().draw();
        vb.setTranslation(0, 0, 0);

        GlStateManager.disableBlend();

        String string = String.valueOf(index);
        float scale = 0.05f/(string.length());

        GlStateManager.rotate(180, 0, 0, 1);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-mc.fontRendererObj.getStringWidth(string)/2+0.5, -3.5, -0.1);
        mc.fontRendererObj.drawString(string, 0, 0, Color.BLACK.getRGB());

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void drawConnection(double playerX, double playerY, double playerZ, Position pos1, Position pos2, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        double x = pos1.getX() - playerX;
        double y = pos1.getY() - playerY;
        double z = pos1.getZ() - playerZ;

        GL11.glLineWidth(3);

        GlStateManager.disableTexture2D();

        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), 0.7f);

        VertexBuffer vb = Tessellator.getInstance().getBuffer();

        vb.setTranslation(x, y + mc.thePlayer.eyeHeight, z);

        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        vb.pos(pos2.getX() - pos1.getX(), pos2.getY() - pos1.getY(), pos2.getZ() - pos1.getZ()).endVertex();
        vb.pos(0, 0, 0).endVertex();

        Tessellator.getInstance().draw();

        vb.setTranslation(0, 0, 0);

        GlStateManager.enableTexture2D();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

}
