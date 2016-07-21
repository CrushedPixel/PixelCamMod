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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.replaymod.pixelcam.path.CameraPath;
import com.replaymod.pixelcam.path.Position;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by misson20000 on 7/20/16.
 */
public class PathSaveHandler {
    private File saveDir;
    private Gson gson;
    private Type listType = new TypeToken<List<Position>>() {}.getType();

    public PathSaveHandler() {
        saveDir = new File(Minecraft.getMinecraft().mcDataDir, "paths");
        if(!saveDir.exists()) {
            saveDir.mkdir();
        } // might be a good idea to do a bit more sanity checking here, but I don't care enough to code that

        gson = new Gson();
    }

    public String[] listSaveNames() {
        String[] src = saveDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".path");
            }
        });

        // strip .path extension
        String[] dst = new String[src.length];
        for(int i = 0; i < src.length; i++) {
            dst[i] = src[i].substring(0, src[i].length()-5);
        }

        return dst;
    }

    public void loadPath(CameraPath cameraPath, String name) throws FileNotFoundException {
        File f = new File(saveDir, name + ".path");
        cameraPath.setPoints(gson.<List<Position>>fromJson(new FileReader(f), listType));
    }

    public void savePath(CameraPath path, String name) throws IOException {
        File f = new File(saveDir, name + ".path");
        FileWriter writer = new FileWriter(f);
        gson.toJson(path.getPoints(), listType, writer);
        writer.close();
    }
}
