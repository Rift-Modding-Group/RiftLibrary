package anightdazingzoroark.riftlib.message;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftLibMessageWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleNetworkWrapper {
    @SafeVarargs
    public RiftLibMessageWrapper(String channelName, Class<? extends RiftLibMessage<?>>... messageClasses) {
        super(channelName);

        int id = 0;
        for (Class<? extends RiftLibMessage<?>> messageClass : messageClasses) {
            try {
                RiftLibMessage<?> message = messageClass.newInstance();

                // Cast to the proper types for registerMessage
                Class<? extends IMessageHandler<REQ, REPLY>> handlerClass = (Class<? extends IMessageHandler<REQ, REPLY>>) messageClass;
                Class<REQ> messageType = (Class<REQ>) messageClass;

                if (message.side() == RiftLibMessageSide.SERVER || message.side() == RiftLibMessageSide.BOTH) {
                    this.registerMessage(
                            handlerClass,
                            messageType,
                            id++,
                            Side.SERVER
                    );
                }
                if (message.side() == RiftLibMessageSide.CLIENT || message.side() == RiftLibMessageSide.BOTH) {
                    this.registerMessage(
                            handlerClass,
                            messageType,
                            id++,
                            Side.CLIENT
                    );
                }
            }
            catch (Exception e) {}
        }
    }
}
