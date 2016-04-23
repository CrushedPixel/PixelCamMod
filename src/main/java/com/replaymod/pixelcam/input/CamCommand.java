package com.replaymod.pixelcam.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;

public class CamCommand extends CommandBase {

    private Minecraft mc = Minecraft.getMinecraft();
    private static final String COMMAND_NAME = "cam";

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

        if(args.length <= 0) throw new CommandException(getCommandUsage(sender));

        String base = args[0];

        args = ArrayUtils.remove(args, 0);

        if(base.equalsIgnoreCase("clear")) clear(args);
        else if(base.equalsIgnoreCase("goto")) goTo(args);
        else if(base.equalsIgnoreCase("p")) p(args);
        else if(base.equalsIgnoreCase("start")) start(args);
        else if(base.equalsIgnoreCase("stop")) stop(args);
        else if(base.equalsIgnoreCase("help")) help(args);
        else throw new CommandNotFoundException("pixelcam.commands.cam.notFound");
    }

    private void clear(String[] args) throws CommandException {
        if(args.length != 0) throw new CommandException("pixelcam.commands.cam.clear.usage");
        //TODO
    }

    private void goTo(String[] args) throws CommandException {
        if(args.length != 1) throw new CommandException("pixelcam.commands.cam.goto.usage");
        //TODO
    }

    private void p(String[] args) throws CommandException {
        if(args.length > 1) throw new CommandException("pixelcam.commands.cam.p.usage");
        //TODO
    }

    private void start(String[] args) throws CommandException {
        if(args.length != 1) throw new CommandException("pixelcam.commands.cam.start.usage");
        //TODO
    }

    private void stop(String[] args) throws CommandException {
        if(args.length != 0) throw new CommandException("pixelcam.commands.cam.stop.usage");
        //TODO
    }

    private void help(String[] args) throws CommandException {
        if(args.length != 0) throw new CommandException("pixelcam.commands.cam.help.usage");
        mc.thePlayer.addChatMessage(new TextComponentTranslation("pixelcam.commands.cam.help.main"));
        writeHelpMessage("p");
        writeHelpMessage("goto");
        writeHelpMessage("clear");
        writeHelpMessage("start");
        writeHelpMessage("stop");
    }

    private void writeHelpMessage(String subcommand) {
        mc.thePlayer.addChatMessage(new TextComponentTranslation("pixelcam.commands.cam.help.scheme",
                I18n.format("pixelcam.commands.cam."+subcommand+".usage"), I18n.format("pixelcam.commands.cam.help."+subcommand)));
    }

}
