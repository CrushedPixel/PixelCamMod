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
package com.replaymod.pixelcam.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class CameraTiltCT implements IClassTransformer {

    private static final String TILT_HANDLER = "com/replaymod/pixelcam/renderer/TiltHandler";
    private static final String CLASS_NAME = "net.minecraft.client.renderer.EntityRenderer";

    private static final String ORIENT_CAMERA = "orientCamera";
    private static final String ORIENT_CAMERA_OBFUSCATED = "f";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (CLASS_NAME.equals(transformedName)) {
            return transform(bytes, name.equals(transformedName) ? ORIENT_CAMERA : ORIENT_CAMERA_OBFUSCATED);
        }
        return bytes;
    }

    private byte[] transform(byte[] bytes, String name_orientCamera) {
        ClassReader classReader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        boolean success = false;
        for (MethodNode m : classNode.methods) {
            if ("(F)V".equals(m.desc) && name_orientCamera.equals(m.name)) {
                inject(m.instructions.iterator());
                success = true;
            }
        }
        if (!success) {
            throw new NoSuchMethodError();
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void inject(ListIterator<AbstractInsnNode> iter) {
        iter.add(new MethodInsnNode(INVOKESTATIC, TILT_HANDLER, "getTilt", "()F", false));
        iter.add(new LdcInsnNode(0f));
        iter.add(new LdcInsnNode(0f));
        iter.add(new LdcInsnNode(1f));
        iter.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/opengl/GL11", "glRotatef", "(FFFF)V", false));
        System.out.println("PIXELCAM CORE PATCHER: Patched EntityRenderer.orientCamera(F) method");
    }

}
