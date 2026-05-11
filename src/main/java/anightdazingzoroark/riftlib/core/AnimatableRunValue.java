package anightdazingzoroark.riftlib.core;

import net.minecraftforge.fml.relauncher.Side;

/**
 * This class is basically for animationMessageEffects.
 *
 * @param runValue The runnable to execute upon receiving a message.
 * @param sideOrder The order in sides (server or client) in which it is to be executed on.
 *                  If left blank, it will default to Side.SERVER.
 * */
public record AnimatableRunValue(Runnable runValue, Side... sideOrder) {}
