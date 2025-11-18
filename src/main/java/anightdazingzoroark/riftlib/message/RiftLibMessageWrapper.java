package anightdazingzoroark.riftlib.message;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftLibMessageWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleNetworkWrapper {
    private int id;

    public RiftLibMessageWrapper(String channelName) {
        super(channelName);
    }

    public void registerMessage(Class<? extends RiftLibMessage<?>> messageClass, RiftLibMessageSide side) {
        //Cast to the proper types for registerMessage
        Class<? extends IMessageHandler<REQ, REPLY>> handlerClass = (Class<? extends IMessageHandler<REQ, REPLY>>) messageClass;
        Class<REQ> messageType = (Class<REQ>) messageClass;

        if (side == RiftLibMessageSide.SERVER || side == RiftLibMessageSide.BOTH) {
            this.registerMessage(
                    handlerClass,
                    messageType,
                    this.id++,
                    Side.SERVER
            );
        }
        if (side == RiftLibMessageSide.CLIENT || side == RiftLibMessageSide.BOTH) {
            this.registerMessage(
                    handlerClass,
                    messageType,
                    this.id++,
                    Side.CLIENT
            );
        }
    }
}
