package anightdazingzoroark.riftlib.command;

import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class RiftLibCommandParticle extends CommandBase {
    private static final String VARIABLES_ARGUMENT = "variables";

    @Override
    @NotNull
    public String getName() {
        return "riftlibparticle";
    }

    @Override
    @NotNull
    public String getUsage(@NotNull ICommandSender sender) {
        return "command.riftlib.particle_usage";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        if (args.length < 4) throw new WrongUsageException(this.getUsage(sender));

        String particleName = args[0];
        if (!RiftLibParticleHelper.isRiftLibParticle(particleName)) throw new WrongUsageException(this.getUsage(sender));

        Vec3d senderPosition = sender.getPositionVector();
        double x = parseDouble(senderPosition.x, args[1], true);
        double y = parseDouble(senderPosition.y, args[2], true);
        double z = parseDouble(senderPosition.z, args[3], true);
        double rotationX = 0D;
        double rotationY = Math.PI;

        if (sender instanceof Entity entity) {
            rotationX = Math.toRadians(-entity.rotationPitch);
            rotationY = Math.toRadians(180D - entity.rotationYaw);
        }

        int variableStart = args.length;
        boolean hasVariableMarker = false;
        if (args.length > 4) {
            if (VARIABLES_ARGUMENT.equalsIgnoreCase(args[4])) {
                variableStart = 5;
                hasVariableMarker = true;
            }
            else {
                if (args.length < 6) throw new WrongUsageException(this.getUsage(sender));

                rotationY = Math.toRadians(parseDegrees(args[4]));
                rotationX = Math.toRadians(parseDegrees(args[5]));
                if (args.length > 6) {
                    if (!VARIABLES_ARGUMENT.equalsIgnoreCase(args[6])) throw new WrongUsageException(this.getUsage(sender));
                    variableStart = 7;
                    hasVariableMarker = true;
                }
            }
        }

        String[] variables = Arrays.copyOfRange(args, variableStart, args.length);
        if ((hasVariableMarker && variables.length == 0) || variables.length % 2 != 0) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        RiftLibParticleHelper.createParticle(particleName, x, y, z, rotationX, rotationY, variables);
    }

    @Override
    @NotNull
    public List<String> getTabCompletions(
            @NotNull MinecraftServer server, @NotNull ICommandSender sender,
            String @NotNull [] args, @Nullable BlockPos targetPosition
    ) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, RiftLibParticleHelper.getParticleIdentifiers());
        }
        if (args.length >= 2 && args.length <= 4) {
            return getTabCompletionCoordinate(args, 1, targetPosition);
        }

        int variableMarkerIndex = -1;
        if (args.length > 4 && VARIABLES_ARGUMENT.equalsIgnoreCase(args[4])) variableMarkerIndex = 4;
        else if (args.length > 6 && VARIABLES_ARGUMENT.equalsIgnoreCase(args[6])) variableMarkerIndex = 6;

        if (variableMarkerIndex >= 0) {
            int currentArgument = args.length - 1;
            return getListOfStringsMatchingLastWord(args, (currentArgument - variableMarkerIndex) % 2 == 1 ? "variable." : "0");
        }
        if (args.length == 5) return getListOfStringsMatchingLastWord(args, "0", VARIABLES_ARGUMENT);
        if (args.length == 6) return getListOfStringsMatchingLastWord(args, "0");
        if (args.length == 7) return getListOfStringsMatchingLastWord(args, VARIABLES_ARGUMENT);
        return List.of();
    }

    private static double parseDegrees(@NotNull String value) throws NumberInvalidException {
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException exception) {
            throw new NumberInvalidException("commands.generic.num.invalid", value);
        }
    }
}
