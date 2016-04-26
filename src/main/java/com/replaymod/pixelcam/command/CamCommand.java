package com.replaymod.pixelcam.command;

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
        return 1;
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

    private void help(String[] args) throws CommandException {
        if(args.length != 0) throw new WrongUsageException("pixelcam.commands.cam.help.usage");
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.help.main"));
        writeHelpMessage("p");
        writeHelpMessage("goto");
        writeHelpMessage("clear");
        writeHelpMessage("start");
        writeHelpMessage("stop");
    }

    private void writeHelpMessage(String subcommand) {
        sendSuccessMessage(new TextComponentTranslation("pixelcam.commands.cam.help.scheme",
                I18n.format("pixelcam.commands.cam." + subcommand + ".usage"), I18n.format("pixelcam.commands.cam.help." + subcommand)));
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
            return getListOfStringsMatchingLastWord(args, "p", "goto", "clear", "start", "stop", "help");
        }

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
