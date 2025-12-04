package anightdazingzoroark.riftlib.command;

import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

public class RiftLibCommandParticle extends CommandBase {
    @Override
    public String getName() {
        return "riftlibparticle";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.riftlib.particle_usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) throw new WrongUsageException("command.riftlib.particle_usage", new Object[0]);
        else {
            String particleName = args[0];

            if (RiftLibParticleHelper.isRiftLibParticle(particleName)) {
                Vec3d vec3d = sender.getPositionVector();
                double xPos = parseDouble(vec3d.x, args[1], true);
                double yPos = parseDouble(vec3d.y, args[2], true);
                double zPos = parseDouble(vec3d.z, args[3], true);

                RiftLibParticleHelper.createParticle(particleName, xPos, yPos, zPos);
            }
        }
    }
}
