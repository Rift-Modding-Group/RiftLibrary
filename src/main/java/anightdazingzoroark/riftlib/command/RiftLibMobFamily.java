package anightdazingzoroark.riftlib.command;

import anightdazingzoroark.riftlib.mobFamily.MobFamily;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class RiftLibMobFamily extends CommandBase {
    @Override
    public String getName() {
        return "riftlibmobfamily";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.riftlib.mob_family_usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            CommandBase.notifyCommandListener(sender, this, "command.riftlib.mob_family_all", new Object[] {MobFamilyHelper.getAllMobFamilyNames()});
        }
        else if (args.length == 1) {
            String mobFamilyName = args[0];
            MobFamily mobFamily = MobFamilyHelper.getMobFamily(mobFamilyName);
            if (mobFamily != null) {
                List<String> mobFamilymembers = mobFamily.getFamilyMembers();
                CommandBase.notifyCommandListener(sender, this, "command.riftlib.mob_family_all_members", new Object[] {mobFamilyName, mobFamilymembers});
            }
            else throw new WrongUsageException("command.riftlib.mob_family_invalid_member", new Object[]{mobFamilyName});
        }
    }
}
