package com.replaymod.pixelcam.command;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;

public class WrongUsageException extends CommandException {

    public WrongUsageException(String message, Object... params) {
        super("pixelcam.commands.cam.error.usage", I18n.format(message, params));
    }

}
