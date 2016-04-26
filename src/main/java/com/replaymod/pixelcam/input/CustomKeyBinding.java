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
package com.replaymod.pixelcam.input;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public abstract class CustomKeyBinding extends KeyBinding {

    private final boolean guiScreensEnabled;

    public CustomKeyBinding(String description, int keyCode, String category, boolean guiScreensEnabled) {
        super(description, keyCode, category);
        this.guiScreensEnabled = guiScreensEnabled;
    }

    public abstract void onPressed();

    public boolean checkPressed(boolean guiScreen) {
        return isPressed() || (guiScreensEnabled && guiScreen && Keyboard.isKeyDown(getKeyCode()));
    }

    public void press(boolean guiScreen) {
        if(checkPressed(guiScreen)) onPressed();
    }

}