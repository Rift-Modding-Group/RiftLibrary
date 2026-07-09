package anightdazingzoroark.riftlib.command;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.internalMessage.RiftLibShowBoundingBoxMessage;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * A helper command to show bounding boxes
 * */
public class RiftLibShowBoundingBox extends CommandBase {
    @Override
    public String getName() {
        return "riftlibshowboundingbox";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.riftlib.show_bounding_box_usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) throw new WrongUsageException("command.riftlib.show_bounding_box_usage", new Object[0]);
        else {
            Entity entity = getEntity(server, sender, args[0]);
            if (!(entity instanceof IAnimatable<?> animatable && animatable.getAnimationData() instanceof AnimationDataEntity animData)) {
                throw new WrongUsageException("command.riftlib.not_animatable", new Object[]{entity.getName()});
            }
            String action = args[1];
            if (!action.equals("show") && !action.equals("hide")) {
                throw new WrongUsageException("command.riftlib.show_bounding_box_usage_invalid_action", new Object[]{action});
            }

            String boundingBoxName = args[2];

            //perform on server then client for each action
            if (action.equals("show")) animData.defineWorldSpaceAABB(boundingBoxName, true);
            else if (action.equals("hide")) animData.removeWorldSpaceAABB(boundingBoxName);
        }
    }
}
