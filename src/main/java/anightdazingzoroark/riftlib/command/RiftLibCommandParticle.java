package anightdazingzoroark.riftlib.command;

import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
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
        if (args.length == 0) throw new WrongUsageException("command.riftlib.particle_usage", new Object[0]);
        //position only
        else {
            String particleName = args[0];
            if (!RiftLibParticleHelper.isRiftLibParticle(particleName)) throw new WrongUsageException("command.riftlib.particle_usage", new Object[0]);
            Vec3d senderPos = sender.getPositionVector();

            double xPos = parseDouble(senderPos.x, args[1], true);
            double yPos = parseDouble(senderPos.y, args[2], true);
            double zPos = parseDouble(senderPos.z, args[3], true);

            //position only, rotation is the player's rotation
            if (args.length <= 4) {
                //get sender rotation
                double rotationX = 0;
                double rotationY = 180;

                if (sender instanceof Entity) {
                    Entity entity = (Entity) sender;

                    float rotationXDeg = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * Minecraft.getMinecraft().getRenderPartialTicks();
                    float rotationYDeg = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * Minecraft.getMinecraft().getRenderPartialTicks();

                    rotationX = Math.toRadians(-rotationXDeg);
                    rotationY = Math.toRadians(180 - rotationYDeg);
                }

                RiftLibParticleHelper.createParticle(particleName, xPos, yPos, zPos, rotationX, rotationY);
            }
            //position and custom rotation
            else {
                double rotationX = Math.toRadians(parseDegrees(args[5]));
                double rotationY = Math.toRadians(parseDegrees(args[4]));
                RiftLibParticleHelper.createParticle(particleName, xPos, yPos, zPos, rotationX, rotationY);
            }
        }
    }

    private static double parseDegrees(String value) throws NumberInvalidException {
        try {
            return Double.parseDouble(value);
        }
        catch (Exception e) {
            throw new NumberInvalidException("commands.generic.num.invalid", value);
        }
    }
}
