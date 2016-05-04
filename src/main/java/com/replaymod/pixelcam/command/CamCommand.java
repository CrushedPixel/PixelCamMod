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
package com.replaymod.pixelcam.command;

import com.replaymod.pixelcam.path.FocusPointHandler;
import com.replaymod.pixelcam.renderer.PathVisualizer;
import com.replaymod.pixelcam.renderer.TiltHandler;
import com.replaymod.pixelcam.interpolation.Interpolation;
import com.replaymod.pixelcam.interpolation.InterpolationType;
import com.replaymod.pixelcam.path.CameraPath;
import com.replaymod.pixelcam.path.Position;
import com.replaymod.pixelcam.path.TravellingProcess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DecimalFormat;
import java.util.List;

public class CamCommand extends CommandBase {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final String COMMAND_NAME = "cam";

    private static final PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("min")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    private final CameraPath cameraPath = new CameraPath();

    private TravellingProcess travellingProcess;

    private final PathVisualizer pathVisualizer = new PathVisualizer(cameraPath);

    private final FocusPointHandler focusPointHandler = new FocusPointHandler();

    public boolean isTravelling() {
        return travellingProcess != null && travellingProcess.isActive();
    }

    public PathVisualizer getPathVisualizer() {
        return pathVisualizer;
    }

    public void register() {
        ClientCommandHandler.instance.registerCommand(this);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "pixelcam.commands.cam.base.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(sender != mc.thePlayer) throw new CommandException("commands.generic.permission");
        if(!mc.thePlayer.isCreative() && !mc.thePlayer.isSpectator()) throw new CommandException("pixelcam.commands.error.gamemode");

        if(args.length <= 0) throw new CommandException(getCommandUsage(sender));

        String base = args[0];

        args = ArrayUtils.remove(args, 0);

        if(base.equalsIgnoreCase("clear")) clear(args);
        else if(base.equalsIgnoreCase("goto")) goTo(args);
        else if(base.equalsIgnoreCase("p")) p(args);
        else if(base.equalsIgnoreCase("start")) start(args);
        else if(base.equalsIgnoreCase("stop")) stop(args);
        else if(base.equalsIgnoreCase("focus")) focus(args);
        else if(base.equalsIgnoreCase("help")) help(args);
        else throw new CommandNotFoundException("pixelcam.commands.cam.notFound");
    }

    private void clear(String[] args) throws CommandException {
        if(args.length > 1) throw new CommandException("pixelcam.commands.cam.clear.usage");

        int index = -1;

        if(args.length == 1) {
            Integer i = parsePathIndex(args[0]);
            if(i == null) throw new CommandException("pixelcam.commands.cam.error.index", args[0]);
            index = i;
        }

        if(index == -1) {
            cameraPath.clear();
            sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.clear.success.all"));
        } else {
            cameraPath.removePoint(index);
            sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.clear.success.one", index+1));
        }

    }

    private void goTo(String[] args) throws CommandException {
        if(args.length != 1) throw new WrongUsageException("pixelcam.commands.cam.goto.usage");

        Integer index = parsePathIndex(args[0]);
        if(index == null) throw new CommandException("pixelcam.commands.cam.error.index", args[0]);

        Position pos = cameraPath.getPoint(index);

        mc.thePlayer.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        TiltHandler.setTilt(pos.getTilt());

        //success message
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.goto.success", index+1,
                round2(pos.getX()), round2(pos.getY()), round2(pos.getZ()), round2(pos.getYaw()), round2(pos.getPitch()), pos.getTilt()));
    }

    private void p(String[] args) throws CommandException {
        if(args.length > 1) throw new WrongUsageException("pixelcam.commands.cam.p.usage");

        int index = -1;

        if(args.length == 1) {
            Integer i = parsePathIndex(args[0]);
            if(i == null) throw new CommandException("pixelcam.commands.cam.error.index", args[0]);
            index = i;
        }

        Position pos = new Position(mc.getRenderViewEntity());

        index = cameraPath.addPoint(pos, index);

        //success message
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.p.success", index+1,
                round2(pos.getX()), round2(pos.getY()), round2(pos.getZ()), round2(pos.getYaw()), round2(pos.getPitch()), pos.getTilt()));
    }

    private Integer parsePathIndex(String index) {
        Integer i;

        try {
            i = Integer.valueOf(index);
        } catch(NumberFormatException e) {
            return null;
        }

        if(i-1 < 0 || i-1 >= cameraPath.getPointCount()) return null;

        return i-1;
    }

    private void start(String[] args) throws CommandException {
        if(args.length < 1 || args.length > 2) throw new WrongUsageException("pixelcam.commands.cam.start.usage");
        long duration = periodFormatter.parsePeriod(args[0]).toStandardDuration().getMillis();

        InterpolationType type = InterpolationType.SPLINE;

        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("linear")) {
                type = InterpolationType.LINEAR;
            } else if(args[1].equalsIgnoreCase("spline")) {
                type = InterpolationType.SPLINE;
            } else {
                throw new CommandException("pixelcam.commands.cam.error.interpolation", args[1]);
            }
        }

        if(cameraPath.getPointCount() < 2) {
            throw new CommandException("pixelcam.commands.cam.start.tooFewPoints");
        }

        Interpolation<Position> interpolation = cameraPath.getInterpolation(type);

        if(travellingProcess != null && travellingProcess.isActive()) {
            travellingProcess.stop();
        }

        travellingProcess = new TravellingProcess(interpolation, duration);
        travellingProcess.start();

        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.start.started"));
    }

    private void stop(String[] args) throws CommandException {
        if(args.length != 0) throw new WrongUsageException("pixelcam.commands.cam.stop.usage");

        if(travellingProcess != null && travellingProcess.isActive()) {
            travellingProcess.stop();
            CamCommand.sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.stop.success"));
        }
    }

    private void focus(String[] args) throws CommandException {
        if(args.length == 0) throw new WrongUsageException("pixelcam.commands.cam.focus.usage");

        if(args[0].equalsIgnoreCase("disable")) {
            if(args.length > 1) throw new WrongUsageException("pixelcam.commands.cam.focus.usage");
            focusPointHandler.setEnabled(false);
            sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.focus.disabled"));

        } else if(args[0].equalsIgnoreCase("enable")) {
            if(args.length != 1 && args.length != 4) throw new WrongUsageException("pixelcam.commands.cam.focus.usage");
            focusPointHandler.setEnabled(true);

            if(args.length == 1 && focusPointHandler.getFocusPoint() == null) {
                Position pos = new Position(mc.thePlayer);
                focusPointHandler.setFocusPoint(pos);
                sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.focus.set",
                        round2(pos.getX()), round2(pos.getY()), round2(pos.getZ())));
            }

            if(args.length == 4) {
                Position pos = parseXYZ(args[1], args[2], args[3]);
                focusPointHandler.setFocusPoint(pos);
                sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.focus.set",
                        round2(pos.getX()), round2(pos.getY()), round2(pos.getZ())));
            }

            sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.focus.enabled"));

        } else if(args.length == 3) {
            Position pos = parseXYZ(args[0], args[1], args[2]);
            focusPointHandler.setFocusPoint(pos);
            sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.focus.set",
                    round2(pos.getX()), round2(pos.getY()), round2(pos.getZ())));

        } else {
            throw new WrongUsageException("pixelcam.commands.cam.focus.usage");
        }
    }

    private void help(String[] args) throws CommandException {
        if(args.length != 0) throw new WrongUsageException("pixelcam.commands.cam.help.usage");
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.help.main"));
        writeHelpMessage("p");
        writeHelpMessage("goto");
        writeHelpMessage("clear");
        writeHelpMessage("start");
        writeHelpMessage("stop");
        writeHelpMessage("focus");
    }

    private void writeHelpMessage(String subcommand) {
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.help.scheme",
                I18n.format("pixelcam.commands.cam." + subcommand + ".usage"), I18n.format("pixelcam.commands.cam.help." + subcommand)));
    }

    private Position parseXYZ(String xIn, String yIn, String zIn) throws CommandException {
       double x = parseCoordinate(mc.thePlayer.posX, xIn, true).func_179628_a();
        double y = parseCoordinate(mc.thePlayer.posY, yIn, true).func_179628_a();
        double z = parseCoordinate(mc.thePlayer.posZ, zIn, true).func_179628_a();

        return new Position(x, y, z, 0, 0, 0);
    }

    private String round2(double value) {
        return new DecimalFormat("#.00").format(value);
    }
    
    public static void sendSuccessMessage(ITextComponent message) {
        mc.thePlayer.addChatMessage(message.setChatStyle(new Style().setColor(TextFormatting.DARK_GREEN)));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if(args.length < 2) {
            return getListOfStringsMatchingLastWord(args, "p", "goto", "clear", "start", "stop", "focus", "help");
        }

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
